<script setup lang="ts">
import draggable from "vuedraggable";
import { useAlbumStore } from "@/stores/album";
import { storeToRefs } from "pinia";
import ImageEdit from "@/components/ImageEdit.vue";
import SectionEdit from "@/components/SectionEdit.vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";
import { ref, computed } from "vue";
import config from "@/Config";
import { useLoginStore } from "@/stores/login";
import { useImageProcessingStore } from "@/stores/imageProcessing";
import GoogleMapEdit from "@/components/GoogleMapEdit.vue";

const route = useRoute();
const router = useRouter();
const albumId = route.params.albumId as string;

const userStore = useAlbumStore();
const loginStore = useLoginStore();
const imageProcessingStore = useImageProcessingStore();

const { album, loading } = storeToRefs(userStore);

const uploadErrors = ref([] as Array<unknown>);
const inputfiles = ref<HTMLFormElement>();
const editingTitle = ref(false);
const newAlbumTitle = ref("");
const isDragging = ref(false);
const isUploading = ref(false);
const dragCounter = ref(0);
const uploadProgress = ref(0);
const selectedFilesCount = ref(0);

userStore.albumLoad({ albumId: albumId });

// Computes whether the album is empty
const isAlbumEmpty = computed(() => {
  return album.value.albumElements.length === 0;
});

// Handle drag and drop functionality
const onDragEnter = (e: DragEvent) => {
  e.preventDefault();
  e.stopPropagation();
  dragCounter.value++;
  if (dragCounter.value === 1) {
    isDragging.value = true;
  }
};

const onDragLeave = (e: DragEvent) => {
  e.preventDefault();
  e.stopPropagation();
  dragCounter.value--;
  if (dragCounter.value === 0) {
    isDragging.value = false;
  }
};

const onDragOver = (e: DragEvent) => {
  e.preventDefault();
  e.stopPropagation();
};

const onDrop = (e: DragEvent) => {
  e.preventDefault();
  e.stopPropagation();
  isDragging.value = false;
  dragCounter.value = 0;

  const files = e.dataTransfer?.files;
  if (files && files.length > 0) {
    processFiles(files);
  }
};

const onUpdate = (event: { oldIndex: number; newIndex: number }) => {
  userStore.changeOrder(albumId, event.oldIndex, event.newIndex);
};

const startEditingTitle = () => {
  newAlbumTitle.value = album.value.description;
  editingTitle.value = true;
  setTimeout(() => {
    document.getElementById("album-title-input")?.focus();
  }, 50);
};

const saveAlbumTitle = async () => {
  if (newAlbumTitle.value.trim() !== "") {
    await userStore.updateAlbumTitle(albumId, newAlbumTitle.value.trim());
    editingTitle.value = false;
  }
};

const uploadFiles = () => {
  const filesToUpload = inputfiles.value?.files;
  if (filesToUpload && filesToUpload.length > 0) {
    processFiles(filesToUpload);
  }
};

const processFiles = (files: FileList) => {
  uploadErrors.value = [];
  isUploading.value = true;
  uploadProgress.value = 0;
  selectedFilesCount.value = 0;

  const totalFiles = files.length;
  let processedFiles = 0;

  for (const file of files) {
    const reader = new FileReader();
    reader.readAsArrayBuffer(file);

    reader.onload = function () {
      const form = new FormData();
      form.append("image", file);
      form.append("albumId", albumId);

      axios
        .post(`${config.apiServer}/api/v1/image`, form, {
          headers: {
            "Content-Type": file.type,
            Authorization: `Bearer ${loginStore.accessToken}`,
          },
        })
        .then((response) => {
          processedFiles++;
          uploadProgress.value = Math.round(
            (processedFiles / totalFiles) * 100
          );

          if (response.status !== 200) {
            uploadErrors.value.push(response);
          }

          if (processedFiles === totalFiles) {
            isUploading.value = false;
            // Trigger immediate stats fetch to start tracking processing
            imageProcessingStore.fetchStats();
          }
        })
        .catch((error) => {
          processedFiles++;
          uploadProgress.value = Math.round(
            (processedFiles / totalFiles) * 100
          );
          uploadErrors.value.push(error);

          if (processedFiles === totalFiles) {
            isUploading.value = false;
            // Trigger immediate stats fetch to start tracking processing
            imageProcessingStore.fetchStats();
          }
        });
    };

    reader.onerror = function () {
      processedFiles++;
      uploadProgress.value = Math.round((processedFiles / totalFiles) * 100);
      uploadErrors.value.push({
        error: `Failed to load ${file.name} with error ${reader.error}`,
      });

      if (processedFiles === totalFiles) {
        isUploading.value = false;
        // Trigger immediate stats fetch to start tracking processing
        imageProcessingStore.fetchStats();
      }
    };
  }

  if (inputfiles.value) {
    inputfiles.value.value = "";
  }
};

const copyShareLink = () => {
  const shareUrl = `${window.location.origin}/album/${album.value.secretId}/0`;
  navigator.clipboard.writeText(shareUrl);

  const shareBtn = document.getElementById("share-btn");
  if (shareBtn) {
    shareBtn.innerText = "Copied!";
    setTimeout(() => {
      shareBtn.innerText = "Copy Link";
    }, 2000);
  }
};

const navigateToAlbumsList = () => {
  router.push("/secured/album");
};

const viewPublicAlbum = () => {
  window.open(`/album/${album.value.secretId}/0`, "_blank");
};

const onFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement;
  selectedFilesCount.value = input.files?.length || 0;
};
</script>

<template>
  <div
    class="album-editor"
    @dragenter="onDragEnter"
    @dragleave="onDragLeave"
    @dragover="onDragOver"
    @drop="onDrop"
  >
    <!-- App header -->
    <header class="app-header">
      <div class="app-logo" @click="navigateToAlbumsList">
        <font-awesome-icon :icon="['fas', 'camera']" class="logo-icon" />
        <h1>PicZ</h1>
      </div>

      <div class="header-controls">
        <router-link to="/secured/album" class="back-link">
          <font-awesome-icon :icon="['fas', 'arrow-left']" />
          <span>Back to Albums</span>
        </router-link>

        <a class="account-link" :href="`${config.idpServer}/account/`">
          <font-awesome-icon :icon="['fas', 'user-circle']" />
          <span>Account</span>
        </a>
      </div>
    </header>

    <!-- Main content -->
    <main class="editor-content" :class="{ 'drop-active': isDragging }">
      <div v-if="isDragging" class="drop-overlay">
        <div class="drop-content">
          <font-awesome-icon
            :icon="['fas', 'cloud-upload-alt']"
            class="drop-icon"
          />
          <h3>Drop images to upload</h3>
        </div>
      </div>

      <div class="editor-container">
        <!-- Album title section -->
        <div class="album-header">
          <div class="album-title-section">
            <div v-if="!editingTitle" class="title-display">
              <h1>{{ album.description || "Untitled Album" }}</h1>
              <button @click="startEditingTitle" class="edit-title-btn">
                <font-awesome-icon :icon="['fas', 'edit']" />
              </button>
            </div>
            <div v-else class="title-edit">
              <input
                id="album-title-input"
                type="text"
                v-model="newAlbumTitle"
                placeholder="Enter album title"
                @keyup.enter="saveAlbumTitle"
              />
              <div class="title-actions">
                <button
                  @click="saveAlbumTitle"
                  class="save-btn"
                  :disabled="loading.updatingTitle"
                >
                  <font-awesome-icon
                    v-if="loading.updatingTitle"
                    :icon="['fas', 'spinner']"
                    class="fa-spin"
                  />
                  <font-awesome-icon v-else :icon="['fas', 'check']" />
                </button>
                <button @click="editingTitle = false" class="cancel-btn">
                  <font-awesome-icon :icon="['fas', 'times']" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Share section -->
        <div v-if="!isAlbumEmpty" class="action-panel share-panel">
          <div class="panel-content">
            <h2>
              <font-awesome-icon :icon="['fas', 'share-alt']" /> Share Your
              Album
            </h2>
            <p>Share your album with others using this link:</p>

            <div class="share-actions">
              <button class="view-btn" @click="viewPublicAlbum">
                <font-awesome-icon :icon="['fas', 'eye']" />
                <span>View Album</span>
              </button>

              <button id="share-btn" class="share-btn" @click="copyShareLink">
                <font-awesome-icon :icon="['fas', 'copy']" />
                <span>Copy Link</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Upload section -->
        <div class="action-panel upload-panel">
          <div class="panel-content">
            <h2>
              <font-awesome-icon :icon="['fas', 'cloud-upload-alt']" /> Add
              Images
            </h2>
            <p>
              Upload images to your album by clicking the button below or drag
              and drop images anywhere on this page.
            </p>

            <div
              v-if="
                !isUploading &&
                !imageProcessingStore.getAlbumProcessingCount(parseInt(albumId))
              "
              class="upload-zone"
            >
              <input
                type="file"
                id="file-input"
                ref="inputfiles"
                multiple
                accept="image/*"
                class="file-input"
                @change="onFileChange"
              />
              <label for="file-input" class="file-label">
                <font-awesome-icon :icon="['fas', 'plus']" />
                <span>Select Files</span>
              </label>

              <button @click="uploadFiles" class="upload-btn">
                <font-awesome-icon :icon="['fas', 'upload']" />
                <span>Upload</span>
                <span v-if="selectedFilesCount > 0" class="selected-file-count"
                  >{{ selectedFilesCount }} file{{
                    selectedFilesCount !== 1 ? "s" : ""
                  }}</span
                >
              </button>
            </div>

            <div v-else-if="isUploading" class="upload-progress-container">
              <div class="progress-bar-wrapper">
                <div
                  class="progress-bar"
                  :style="{ width: `${uploadProgress}%` }"
                ></div>
              </div>
              <span class="progress-text"
                >Uploading images... {{ uploadProgress }}%</span
              >
            </div>

            <div v-else class="processing-container">
              <div class="processing-indicator">
                <font-awesome-icon
                  :icon="['fas', 'cog']"
                  class="fa-spin processing-icon"
                />
                <span class="processing-text"
                  >Processing
                  {{
                    imageProcessingStore.getAlbumProcessingCount(
                      parseInt(albumId)
                    )
                  }}
                  image{{
                    imageProcessingStore.getAlbumProcessingCount(
                      parseInt(albumId)
                    ) !== 1
                      ? "s"
                      : ""
                  }}...</span
                >
              </div>
            </div>
          </div>
        </div>

        <!-- Loading state -->
        <div
          v-if="loading.album && album.albumElements.length === 0"
          class="loading-album"
        >
          <font-awesome-icon
            :icon="['fas', 'spinner']"
            class="fa-spin loading-icon"
          />
          <h3>Loading album...</h3>
          <p>Please wait while we load your album content.</p>
        </div>

        <!-- Empty state -->
        <div v-else-if="isAlbumEmpty" class="empty-album">
          <font-awesome-icon :icon="['fas', 'images']" class="empty-icon" />
          <h3>Your album is empty</h3>
          <p>Start by uploading some images to create your album.</p>
        </div>

        <!-- Album elements list -->
        <div v-else class="album-elements">
          <h2 class="elements-title">
            <font-awesome-icon :icon="['fas', 'layer-group']" />
            <span>Album Content</span>
            <small
              >{{ album.albumElements.length }} item{{
                album.albumElements.length !== 1 ? "s" : ""
              }}</small
            >
          </h2>

          <draggable
            v-model="album.albumElements"
            tag="ul"
            class="elements-list"
            handle=".drag-handle"
            item-key="id"
            @end="onUpdate"
            :animation="200"
          >
            <template #item="{ element }">
              <li class="element-item">
                <div class="drag-handle">
                  <font-awesome-icon :icon="['fas', 'grip-lines']" />
                </div>

                <div class="element-content">
                  <ImageEdit
                    v-if="element.elementType == 'IMAGE'"
                    :internal-id="element.id"
                    :external-id="element.secretId"
                    :creation-date="element.creationDate"
                    :description="element.description"
                    :albumId="albumId"
                  />

                  <GoogleMapEdit
                    v-else-if="element.elementType == 'MAP'"
                    :image-id="element.id"
                    :external-id="element.secretId"
                    :description="element.description"
                    :albumId="parseInt(albumId, 10)"
                    :marker-longitude="element.longitude"
                    :marker-latitude="element.latitude"
                    :mapCenterLongitude="element.mapCenterLongitude"
                    :mapCenterLatitude="element.mapCenterLatitude"
                    :zoom-level="element.zoomLevel"
                  />

                  <SectionEdit
                    v-else-if="element.elementType == 'SECTION'"
                    :internal-id="element.id"
                    :description="element.description"
                    :albumId="albumId"
                  />
                </div>
              </li>
            </template>
          </draggable>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.album-editor {
  min-height: 100vh;
  background-color: #f9fafb;
  color: #334155;
  font-family: "SF Pro Display", -apple-system, BlinkMacSystemFont, sans-serif;
  position: relative;
}

/* Header Styles */
.app-header {
  background-color: white;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.app-logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
}

.logo-icon {
  font-size: 1.5rem;
  color: #6366f1;
}

.app-logo h1 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
  background: linear-gradient(45deg, #6366f1, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.5px;
}

.header-controls {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.back-link,
.account-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #64748b;
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  transition: color 0.2s ease;
}

.back-link:hover,
.account-link:hover {
  color: #4f46e5;
}

/* Main Content Styles */
.editor-content {
  position: relative;
  min-height: calc(100vh - 64px);
}

.editor-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 1.5rem;
}

/* Album Header */
.album-header {
  margin-bottom: 1.5rem;
}

.album-title-section {
  margin-bottom: 1rem;
}

.title-display {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.title-display h1 {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
  color: #1e293b;
}

.edit-title-btn {
  background: none;
  border: none;
  color: #94a3b8;
  font-size: 1rem;
  padding: 0.5rem;
  cursor: pointer;
  border-radius: 9999px;
  transition: all 0.2s ease;
}

.edit-title-btn:hover {
  background-color: #f1f5f9;
  color: #4f46e5;
}

.title-edit {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.title-edit input {
  flex: 1;
  font-size: 1.5rem;
  padding: 0.75rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  background-color: white;
  color: #1e293b;
  transition: all 0.2s ease;
}

.title-edit input:focus {
  outline: none;
  border-color: #a5b4fc;
  box-shadow: 0 0 0 3px rgba(165, 180, 252, 0.2);
}

.title-actions {
  display: flex;
  gap: 0.5rem;
}

.save-btn,
.cancel-btn {
  background: none;
  border: none;
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.save-btn {
  background-color: #6366f1;
  color: white;
}

.save-btn:hover {
  background-color: #4f46e5;
}

.cancel-btn {
  background-color: #e2e8f0;
  color: #64748b;
}

.cancel-btn:hover {
  background-color: #cbd5e1;
  color: #475569;
}

/* Action Panels */
.action-panel {
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  margin-bottom: 1.5rem;
  overflow: hidden;
}

.panel-content {
  padding: 1.25rem;
}

.action-panel h2 {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0 0 0.75rem 0;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.action-panel p {
  color: #64748b;
  margin: 0 0 1.25rem 0;
  font-size: 0.95rem;
}

/* Share Panel */
.share-panel {
  border-top: 4px solid #6366f1;
}

.share-actions {
  display: flex;
  gap: 1rem;
}

.view-btn,
.share-btn {
  padding: 0.75rem 1.25rem;
  border-radius: 0.5rem;
  font-weight: 600;
  font-size: 0.875rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
}

.view-btn {
  background-color: #f1f5f9;
  color: #475569;
}

.view-btn:hover {
  background-color: #e2e8f0;
  color: #334155;
}

.share-btn {
  background-color: #6366f1;
  color: white;
}

.share-btn:hover {
  background-color: #4f46e5;
}

/* Upload Panel */
.upload-panel {
  border-top: 4px solid #6366f1;
}

.upload-zone {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.file-input {
  display: none;
}

.file-label {
  flex: 1;
  padding: 1rem;
  background-color: #f8fafc;
  border: 2px dashed #cbd5e1;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  color: #64748b;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.file-label:hover {
  background-color: #f1f5f9;
  border-color: #94a3b8;
  color: #475569;
}

.upload-btn {
  padding: 0.75rem 1.25rem;
  border-radius: 0.5rem;
  font-weight: 600;
  font-size: 0.875rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  background-color: #6366f1;
  color: white;
}

.upload-btn:hover {
  background-color: #4f46e5;
}

.selected-file-count {
  margin-left: 0.5rem;
  padding: 0.1rem 0.5rem;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 9999px;
  font-size: 0.75rem;
}

.upload-progress-container {
  margin-top: 1rem;
}

.progress-bar-wrapper {
  width: 100%;
  height: 0.5rem;
  background-color: #e2e8f0;
  border-radius: 9999px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 9999px;
  transition: width 0.3s ease;
}

.progress-text {
  color: #64748b;
  font-size: 0.875rem;
  font-weight: 500;
}

/* Processing State */
.processing-container {
  margin-top: 1rem;
}

.processing-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 1rem;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
}

.processing-icon {
  font-size: 1.25rem;
  color: #6366f1;
}

.processing-text {
  color: #64748b;
  font-size: 0.875rem;
  font-weight: 500;
}

/* Loading State */
.loading-album {
  padding: 4rem 2rem;
  text-align: center;
  background-color: white;
  border-radius: 0.75rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 1px 2px rgba(0, 0, 0, 0.06);
}

.loading-icon {
  font-size: 3rem;
  color: #6366f1;
  margin-bottom: 1.5rem;
}

.loading-album h3 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  color: #1e293b;
}

.loading-album p {
  color: #64748b;
  margin: 0;
}

/* Empty State */
.empty-album {
  padding: 4rem 2rem;
  text-align: center;
  background-color: white;
  border-radius: 0.75rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 1px 2px rgba(0, 0, 0, 0.06);
}

.empty-icon {
  font-size: 4rem;
  color: #cbd5e1;
  margin-bottom: 1.5rem;
}

.empty-album h3 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  color: #1e293b;
}

.empty-album p {
  color: #64748b;
  margin: 0;
}

/* Album Elements List */
.album-elements {
  margin-top: 1.5rem;
}

.elements-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0 0 1rem 0;
  color: #1e293b;
}

.elements-title small {
  margin-left: auto;
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 500;
}

.elements-list {
  padding: 0;
  margin: 0;
}

.element-item {
  list-style-type: none;
  margin-bottom: 0.75rem;
  display: flex;
  background-color: white;
  border-radius: 0.5rem;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
  border: 1px solid #e2e8f0;
}

.element-item:hover {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.drag-handle {
  padding: 0.75rem;
  display: flex;
  align-items: center;
  color: #94a3b8;
  cursor: move;
  background-color: #f8fafc;
  border-right: 1px solid #e2e8f0;
}

.element-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* Drag and Drop */
.drop-active {
  position: relative;
}

.drop-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(30, 41, 59, 0.7);
  z-index: 90;
  display: flex;
  align-items: center;
  justify-content: center;
}

.drop-content {
  background-color: white;
  padding: 3rem;
  border-radius: 1rem;
  text-align: center;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1),
    0 10px 10px -5px rgba(0, 0, 0, 0.04);
  animation: pulse 2s ease-in-out infinite;
}

.drop-icon {
  font-size: 4rem;
  color: #6366f1;
  margin-bottom: 1.5rem;
}

.drop-content h3 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
  color: #1e293b;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

/* Responsive Design */
@media (max-width: 768px) {
  .app-header {
    padding: 1rem;
  }

  .editor-container {
    padding: 1rem;
  }

  .title-display h1 {
    font-size: 1.5rem;
  }

  .title-edit {
    flex-direction: column;
    align-items: stretch;
    gap: 0.5rem;
  }

  .title-edit input {
    font-size: 1.25rem;
  }

  .title-actions {
    justify-content: flex-end;
  }

  .share-actions {
    flex-direction: column;
    gap: 0.75rem;
  }

  .view-btn,
  .share-btn {
    width: 100%;
    justify-content: center;
  }

  .upload-zone {
    flex-direction: column;
    gap: 0.75rem;
  }

  .file-label,
  .upload-btn {
    width: 100%;
    justify-content: center;
  }

  .selected-file-count {
    margin-left: 0.5rem;
  }

  .drag-handle {
    padding: 0.5rem;
  }
}
</style>
