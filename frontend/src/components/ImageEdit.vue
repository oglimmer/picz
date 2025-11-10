<script setup lang="ts">
import { useAlbumStore } from "@/stores/album";
import { computed, ref, onMounted, onUnmounted } from "vue";
import { storeToRefs } from "pinia";
import config from "@/Config";

const props = defineProps([
  "internalId",
  "externalId",
  "description",
  "creationDate",
  "albumId",
]);

const description = ref(props.description);
const showOverlay = ref(false);

const userStore = useAlbumStore();
const { loading, album } = storeToRefs(userStore);

const rotate = (id: number) => {
  userStore.albumRotate(id);
};

// interface KeyDownEvent {
//   key: string;
//   charCode: number;
//   keyCode: number;
// }

const descriptionChanged = () => {
  userStore.setAlbumDescription(props.internalId, description.value);
};

const addSection = () => {
  userStore.addSection(props.internalId);
};

const addMap = () => {
  userStore.addMap(props.internalId);
};

const deleteThisElement = () => {
  userStore.deleteElement(props.albumId, props.internalId);
};

const toggleOverlay = (event: Event) => {
  // Prevent the event from bubbling to parent elements
  event.stopPropagation();
  if (!showOverlay.value) {
    // Reset to current image when opening overlay
    currentImageData.value.externalId = props.externalId;
  }
  showOverlay.value = !showOverlay.value;
};

const closeOverlay = () => {
  showOverlay.value = false;
};

const formattedDate = computed(() => {
  if (!props.creationDate) return "";

  const date = new Date(Number(props.creationDate));
  return date.toLocaleString();
});

// Get all images from the album
const imageElements = computed(() => {
  return album.value.albumElements.filter(
    (element) => element.elementType === "IMAGE"
  );
});

// Current image data for overlay
const currentImageData = ref({ externalId: props.externalId });

// Find current image index (based on what's shown in overlay)
const currentImageIndex = computed(() => {
  return imageElements.value.findIndex(
    (element) => element.secretId === currentImageData.value.externalId
  );
});

const navigateToImage = (direction: "next" | "prev") => {
  const images = imageElements.value;
  const currentIndex = currentImageIndex.value;

  let newIndex;
  if (direction === "next") {
    newIndex = currentIndex + 1 >= images.length ? 0 : currentIndex + 1;
  } else {
    newIndex = currentIndex - 1 < 0 ? images.length - 1 : currentIndex - 1;
  }

  if (images[newIndex]) {
    currentImageData.value.externalId = images[newIndex].secretId;
  }
};

const handleKeyDown = (event: KeyboardEvent) => {
  if (!showOverlay.value) return;

  if (event.key === "Escape") {
    closeOverlay();
  } else if (event.key === "ArrowLeft") {
    event.preventDefault();
    navigateToImage("prev");
  } else if (event.key === "ArrowRight") {
    event.preventDefault();
    navigateToImage("next");
  }
};

onMounted(() => {
  document.addEventListener("keydown", handleKeyDown);
});

onUnmounted(() => {
  document.removeEventListener("keydown", handleKeyDown);
});
</script>

<template>
  <div class="image-edit-container">
    <div class="action-buttons">
      <button
        class="edit-btn rotate-btn"
        @click="rotate(internalId)"
        title="Rotate"
        :disabled="loading.rotating[internalId]"
      >
        <font-awesome-icon
          v-if="loading.rotating[internalId]"
          :icon="['fas', 'spinner']"
          class="fa-spin"
        />
        <font-awesome-icon v-else :icon="['fas', 'rotate']" />
      </button>
      <button
        class="edit-btn section-btn"
        @click="addSection"
        title="Add Section"
        :disabled="loading.addingSection"
      >
        <font-awesome-icon
          v-if="loading.addingSection"
          :icon="['fas', 'spinner']"
          class="fa-spin"
        />
        <font-awesome-icon v-else :icon="['fas', 'heading']" />
      </button>
      <button
        class="edit-btn map-btn"
        @click="addMap"
        title="Add Map"
        :disabled="loading.addingMap"
      >
        <font-awesome-icon
          v-if="loading.addingMap"
          :icon="['fas', 'spinner']"
          class="fa-spin"
        />
        <font-awesome-icon v-else :icon="['fas', 'map']" />
      </button>
      <button
        class="edit-btn delete-btn"
        @click="deleteThisElement"
        title="Delete"
        :disabled="loading.deletingElement[internalId]"
      >
        <font-awesome-icon
          v-if="loading.deletingElement[internalId]"
          :icon="['fas', 'spinner']"
          class="fa-spin"
        />
        <font-awesome-icon v-else :icon="['fas', 'trash']" />
      </button>
    </div>
    <div class="content-area">
      <div class="content-layout">
        <div class="image-wrapper">
          <img
            :src="`${config.apiServer}/api/public/v1/image/${externalId}?small=true`"
            class="image-preview"
            @click="toggleOverlay"
            title="Click to enlarge"
          />
        </div>
        <div class="text-content">
          <div class="description-area">
            <textarea
              v-model="description"
              @blur="descriptionChanged"
              placeholder="Add a description..."
              class="description-input"
              :disabled="loading.updatingDescription[internalId]"
            ></textarea>
            <div
              v-if="loading.updatingDescription[internalId]"
              class="description-loading"
            >
              <font-awesome-icon :icon="['fas', 'spinner']" class="fa-spin" />
              <span>Saving description...</span>
            </div>
          </div>
          <div class="meta-info">
            <span class="creation-date"
              ><font-awesome-icon :icon="['fas', 'calendar']" />
              {{ formattedDate }}</span
            >
          </div>
        </div>
      </div>
    </div>

    <!-- Image Overlay -->
    <div v-if="showOverlay" class="image-overlay" @click="closeOverlay">
      <div class="overlay-content">
        <img
          :src="`${config.apiServer}/api/public/v1/image/${currentImageData.externalId}`"
          class="overlay-image"
          @click.stop
        />
        <button class="close-overlay" @click="closeOverlay">
          <font-awesome-icon :icon="['fas', 'times']" />
        </button>

        <!-- Navigation arrows (only show if more than one image) -->
        <div v-if="imageElements.length > 1" class="navigation-arrows">
          <button
            class="nav-arrow nav-arrow-left"
            @click.stop="navigateToImage('prev')"
          >
            <font-awesome-icon :icon="['fas', 'chevron-left']" />
          </button>
          <button
            class="nav-arrow nav-arrow-right"
            @click.stop="navigateToImage('next')"
          >
            <font-awesome-icon :icon="['fas', 'chevron-right']" />
          </button>
        </div>

        <!-- Image counter -->
        <div v-if="imageElements.length > 1" class="image-counter">
          {{ currentImageIndex + 1 }} / {{ imageElements.length }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.image-edit-container {
  display: flex;
  width: 100%;
  background-color: #fafafa;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eaeaea;
  transition: box-shadow 0.2s ease;
  position: relative;
  margin-bottom: 10px;
}

.image-edit-container:hover {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.content-area {
  flex: 1;
  padding: 0;
}

.content-layout {
  display: flex;
  gap: 0;
}

.image-wrapper {
  flex: 1;
  height: 250px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
  border-radius: 0;
  padding: 0;
  overflow: hidden;
}

.text-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 10px;
}

.image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 0;
  transition: transform 0.2s ease;
  cursor: pointer;
}

.image-preview:hover {
  transform: scale(1.05);
}

.description-area {
  margin-bottom: 10px;
}

.description-input {
  width: 100%;
  min-height: 120px;
  max-height: 120px;
  padding: 8px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-family: inherit;
  font-size: 0.9rem;
  resize: vertical;
  transition: border-color 0.2s ease;
  background-color: white;
}

.description-input:focus {
  border-color: #a8a8d8;
  outline: none;
}

.description-input:disabled {
  background-color: #f8f9fa;
  opacity: 0.7;
}

.description-loading {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: #6366f1;
}

.meta-info {
  display: flex;
  justify-content: flex-start;
  font-size: 0.8rem;
  color: #888;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  padding: 12px 0;
  gap: 8px;
  background-color: #f5f5f5;
  border-right: 1px solid #eaeaea;
}

.edit-btn {
  background-color: white;
  border: 1px solid #dedede;
  border-radius: 4px;
  padding: 4px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  transition: all 0.2s ease;
  color: #555;
  margin: 0 8px;
}

.edit-btn:hover:not(:disabled) {
  background-color: #f0f0f0;
  transform: translateX(1px);
}

.edit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.rotate-btn {
  color: #198754;
}

.section-btn {
  color: #0d6efd;
}

.map-btn {
  color: #ffc107;
}

.delete-btn {
  color: #dc3545;
}

.image-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  cursor: pointer;
}

.overlay-content {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.overlay-image {
  max-width: 100%;
  max-height: 90vh;
  object-fit: contain;
  border-radius: 4px;
  cursor: default;
}

.close-overlay {
  position: absolute;
  top: -40px;
  right: 0;
  background-color: transparent;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 8px;
  transition: all 0.2s ease;
}

.close-overlay:hover {
  color: #f8f9fa;
  transform: scale(1.1);
}

.navigation-arrows {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  transform: translateY(-50%);
  pointer-events: none;
}

.nav-arrow {
  position: absolute;
  background-color: rgba(0, 0, 0, 0.5);
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 12px;
  border-radius: 50%;
  transition: all 0.2s ease;
  pointer-events: auto;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
}

.nav-arrow:hover {
  background-color: rgba(0, 0, 0, 0.7);
  transform: scale(1.1);
}

.nav-arrow-left {
  left: 20px;
}

.nav-arrow-right {
  right: 20px;
}

.image-counter {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

@media (max-width: 768px) {
  .image-edit-container {
    flex-direction: column-reverse;
  }

  .action-buttons {
    flex-direction: row;
    justify-content: flex-end;
    padding: 8px;
    border-right: none;
    border-top: 1px solid #eaeaea;
  }

  .content-layout {
    flex-direction: column;
  }

  .image-wrapper {
    margin-bottom: 10px;
  }
}
</style>
