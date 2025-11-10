<script setup lang="ts">
import { type ListAlbumResponse, useAlbumStore } from "@/stores/album";
import { storeToRefs } from "pinia";
import { useImageProcessingStore } from "@/stores/imageProcessing";
import { useLoginStore } from "@/stores/login";
import config from "@/Config";
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import TosModal from "@/components/TosModal.vue";

const userStore = useAlbumStore();
const imageProcessingStore = useImageProcessingStore();
const loginStore = useLoginStore();
const router = useRouter();
const { albumList, loading } = storeToRefs(userStore);
const { isProcessing } = storeToRefs(imageProcessingStore);
const { userCapacity } = storeToRefs(loginStore);
const searchQuery = ref("");

userStore.albumListLoad();

// Filtered albums based on search query
const filteredAlbums = computed(() => {
  if (!searchQuery.value) return albumList.value;
  const lowerQuery = searchQuery.value.toLowerCase();
  return albumList.value.filter((album) =>
    album.description.toLowerCase().includes(lowerQuery)
  );
});

// Format bytes to human readable format
const formatBytes = (bytes: number) => {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
};

// Calculate usage percentage
const usagePercentage = computed(() => {
  if (!userCapacity.value) return 0;
  return Math.round(
    (userCapacity.value.usedCapacity / userCapacity.value.capacity) * 100
  );
});

const deleteAlbum = async (id: number, event: Event) => {
  event.stopPropagation();
  if (confirm("Are you sure you want to delete this album?")) {
    await userStore.albumDelete(`${id}`);
  }
};

const goToEditAlbum = (id: number, event: Event) => {
  event.stopPropagation();
  router.push(`/secured/album/${id}`);
};

const goToAlbum = (id: number) => {
  router.push(`/secured/album/${id}`);
};

const createNewAlbum = async () => {
  const newAlbumId = await userStore.albumCreate();
  router.push(`/secured/album/${newAlbumId}`);
};

// Random pastel color for default album covers
const getRandomColor = (id: number) => {
  const colors = [
    "linear-gradient(135deg, #B8E1FC, #90C1F5)",
    "linear-gradient(135deg, #FDCBF1, #E6DEE9)",
    "linear-gradient(135deg, #A1FFC9, #70E1B7)",
    "linear-gradient(135deg, #FFD3B6, #FFAAA5)",
    "linear-gradient(135deg, #D4A5E5, #CABDFF)",
    "linear-gradient(135deg, #FFEBB7, #FED98E)",
    "linear-gradient(135deg, #C5FAD5, #9AE5BD)",
    "linear-gradient(135deg, #FFDBDB, #FFB1B1)",
  ];
  return colors[id % colors.length];
};

const getAlbumCoverStyle = (album: ListAlbumResponse) => {
  if (album.titleSecretId) {
    return {
      backgroundImage: `url(${config.apiServer}/api/public/v1/image/${album.titleSecretId}?small=true)`,
      backgroundSize: "cover",
      backgroundPosition: "center",
    };
  } else {
    // Fallback to random color when no image is available
    return { background: getRandomColor(album.id) };
  }
};
</script>

<template>
  <div class="album-view">
    <!-- App header -->
    <header class="app-header">
      <div class="app-logo">
        <font-awesome-icon :icon="['fas', 'camera']" class="logo-icon" />
        <h1>PicZ</h1>
      </div>

      <div class="header-controls">
        <div class="search-container">
          <font-awesome-icon :icon="['fas', 'search']" class="search-icon" />
          <input
            type="text"
            v-model="searchQuery"
            placeholder="Search albums..."
            class="search-input"
          />
        </div>

        <div v-if="isProcessing" class="global-processing-indicator">
          <font-awesome-icon :icon="['fas', 'cog']" class="fa-spin" />
          <span>Processing...</span>
        </div>

        <a class="account-link" :href="`${config.idpServer}/account/`">
          <font-awesome-icon :icon="['fas', 'user-circle']" />
          <span>Account</span>
        </a>
      </div>
    </header>

    <!-- Page content -->
    <main class="page-content">
      <div class="page-header">
        <div class="title-section">
          <h2>My Albums</h2>
          <div class="header-info">
            <p class="album-count">
              {{ filteredAlbums.length }} album{{
                filteredAlbums.length !== 1 ? "s" : ""
              }}
            </p>
            <div v-if="userCapacity" class="capacity-info">
              <div class="capacity-text">
                <font-awesome-icon :icon="['fas', 'hdd']" />
                <span
                  >{{ formatBytes(userCapacity.usedCapacity) }} of
                  {{ formatBytes(userCapacity.capacity) }} used</span
                >
              </div>
              <div class="capacity-bar">
                <div
                  class="capacity-fill"
                  :style="{ width: usagePercentage + '%' }"
                  :class="{
                    'capacity-warning': usagePercentage > 80,
                    'capacity-critical': usagePercentage > 95,
                  }"
                ></div>
              </div>
              <span class="capacity-percentage">{{ usagePercentage }}%</span>
            </div>
          </div>
        </div>

        <button
          class="create-album-btn"
          @click="createNewAlbum"
          :disabled="loading.creatingAlbum"
        >
          <font-awesome-icon
            v-if="loading.creatingAlbum"
            :icon="['fas', 'spinner']"
            class="fa-spin"
          />
          <font-awesome-icon v-else :icon="['fas', 'plus']" />
          <span>{{
            loading.creatingAlbum ? "Creating..." : "Create Album"
          }}</span>
        </button>
      </div>

      <!-- Album grid -->
      <div v-if="loading.albumList" class="loading-state">
        <div class="loading-content">
          <font-awesome-icon
            :icon="['fas', 'spinner']"
            class="fa-spin loading-icon"
          />
          <p>Loading albums...</p>
        </div>
      </div>

      <div class="empty-state" v-else-if="albumList.length === 0">
        <div class="empty-state-content">
          <font-awesome-icon :icon="['fas', 'photo-film']" class="empty-icon" />
          <h3>No Albums Yet</h3>
          <p>Create your first album to start organizing your photos.</p>
          <button
            class="create-first-album-btn"
            @click="createNewAlbum"
            :disabled="loading.creatingAlbum"
          >
            <font-awesome-icon
              v-if="loading.creatingAlbum"
              :icon="['fas', 'spinner']"
              class="fa-spin"
            />
            <font-awesome-icon v-else :icon="['fas', 'plus']" />
            <span>{{
              loading.creatingAlbum ? "Creating..." : "Create Your First Album"
            }}</span>
          </button>
        </div>
      </div>

      <div v-else-if="filteredAlbums.length === 0" class="no-results">
        <font-awesome-icon :icon="['fas', 'search']" class="no-results-icon" />
        <p>No albums match your search</p>
      </div>

      <div class="album-grid" v-else>
        <div
          v-for="album in filteredAlbums"
          :key="album.id"
          class="album-card"
          @click="goToAlbum(album.id)"
        >
          <div class="album-cover" :style="getAlbumCoverStyle(album)">
            <div class="image-count">
              <font-awesome-icon :icon="['fas', 'image']" />
              <span>{{ album.imageCount }}</span>
            </div>
            <div
              v-if="imageProcessingStore.isAlbumProcessing(album.id)"
              class="processing-badge"
            >
              <font-awesome-icon :icon="['fas', 'cog']" class="fa-spin" />
              <span>{{
                imageProcessingStore.getAlbumProcessingCount(album.id)
              }}</span>
            </div>
          </div>

          <div class="album-details">
            <h3 class="album-title">
              {{ album.description || "Untitled Album" }}
            </h3>

            <div class="album-meta">
              <span v-if="album.owner" class="owner">
                <font-awesome-icon :icon="['fas', 'user']" />
                {{ album.owner }}
              </span>
            </div>

            <div class="album-actions">
              <button
                class="action-btn edit-btn"
                @click="goToEditAlbum(album.id, $event)"
                title="Edit Album"
              >
                <font-awesome-icon :icon="['fas', 'pen']" />
              </button>

              <button
                class="action-btn delete-btn"
                @click="deleteAlbum(album.id, $event)"
                title="Delete Album"
                :disabled="loading.deletingAlbum[album.id]"
              >
                <font-awesome-icon
                  v-if="loading.deletingAlbum[album.id]"
                  :icon="['fas', 'spinner']"
                  class="fa-spin"
                />
                <font-awesome-icon v-else :icon="['fas', 'trash']" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- TOS Modal -->
    <TosModal />
  </div>
</template>

<style scoped>
/* Main Layout */
.album-view {
  min-height: 100vh;
  background-color: #f9fafb;
  color: #334155;
  font-family: "SF Pro Display", -apple-system, BlinkMacSystemFont, sans-serif;
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
  z-index: 10;
}

.app-logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
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

.search-container {
  position: relative;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}

.search-input {
  padding: 0.625rem 1rem 0.625rem 2.5rem;
  border-radius: 9999px;
  border: 1px solid #e2e8f0;
  background-color: #f8fafc;
  width: 240px;
  font-size: 0.875rem;
  transition: all 0.2s ease;
}

.search-input:focus {
  outline: none;
  border-color: #a5b4fc;
  box-shadow: 0 0 0 3px rgba(165, 180, 252, 0.2);
  background-color: white;
}

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

.account-link:hover {
  color: #4f46e5;
}

.global-processing-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6366f1;
  font-size: 0.875rem;
  font-weight: 500;
}

.processing-badge {
  position: absolute;
  top: 0.75rem;
  left: 0.75rem;
  background-color: rgba(99, 102, 241, 0.9);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  backdrop-filter: blur(4px);
}

/* Page Content */
.page-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.title-section h2 {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 0.25rem 0;
  color: #1e293b;
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.album-count {
  color: #64748b;
  font-size: 0.875rem;
  margin: 0;
}

.capacity-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.875rem;
}

.capacity-text {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  color: #64748b;
  min-width: 140px;
}

.capacity-bar {
  flex: 1;
  max-width: 200px;
  height: 8px;
  background-color: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.capacity-fill {
  height: 100%;
  background-color: #22c55e;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.capacity-fill.capacity-warning {
  background-color: #f59e0b;
}

.capacity-fill.capacity-critical {
  background-color: #ef4444;
}

.capacity-percentage {
  font-weight: 600;
  color: #475569;
  min-width: 35px;
  text-align: right;
}

.capacity-loading {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6366f1;
  font-size: 0.875rem;
}

.create-album-btn {
  background-color: #6366f1;
  color: white;
  border: none;
  border-radius: 0.5rem;
  padding: 0.75rem 1.25rem;
  font-weight: 600;
  font-size: 0.875rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(99, 102, 241, 0.2);
}

.create-album-btn:hover {
  background-color: #4f46e5;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(79, 70, 229, 0.25);
}

/* Album Grid */
.album-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.album-card {
  background-color: white;
  border-radius: 0.75rem;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 1px 2px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  cursor: pointer;
  position: relative;
}

.album-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1),
    0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

.album-cover {
  height: 180px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-count {
  background-color: rgba(255, 255, 255, 0.85);
  color: #475569;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.375rem;
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  backdrop-filter: blur(4px);
}

.album-details {
  padding: 1.25rem;
  position: relative;
  border-top: 1px solid #f1f5f9;
}

.album-title {
  font-size: 1rem;
  font-weight: 600;
  margin: 0 0 0.5rem 0;
  color: #1e293b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-meta {
  display: flex;
  color: #64748b;
  font-size: 0.75rem;
  gap: 1rem;
}

.owner {
  display: flex;
  align-items: center;
  gap: 0.375rem;
}

.album-actions {
  position: absolute;
  right: 1.25rem;
  bottom: 1.25rem;
  display: flex;
  gap: 0.5rem;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.album-card:hover .album-actions {
  opacity: 1;
}

.action-btn {
  border: none;
  background: none;
  cursor: pointer;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  border-radius: 9999px;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background-color: #f1f5f9;
}

.edit-btn:hover {
  color: #3b82f6;
}

.delete-btn:hover {
  color: #ef4444;
}

/* Empty state */
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
}

.empty-state-content {
  max-width: 400px;
  text-align: center;
  padding: 3rem;
  background-color: white;
  border-radius: 1rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1),
    0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.empty-icon {
  font-size: 4rem;
  color: #cbd5e1;
  margin-bottom: 1.5rem;
}

.empty-state-content h3 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  color: #1e293b;
}

.empty-state-content p {
  color: #64748b;
  margin-bottom: 1.5rem;
}

.create-first-album-btn {
  background-color: #6366f1;
  color: white;
  border: none;
  border-radius: 0.5rem;
  padding: 0.75rem 1.5rem;
  font-weight: 600;
  font-size: 0.875rem;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(99, 102, 241, 0.2);
}

.create-first-album-btn:hover {
  background-color: #4f46e5;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(79, 70, 229, 0.25);
}

/* No results */
.no-results {
  text-align: center;
  padding: 4rem 0;
  color: #94a3b8;
}

.no-results-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

/* Loading state */
.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
}

.loading-content {
  text-align: center;
  padding: 3rem;
  background-color: white;
  border-radius: 1rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1),
    0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.loading-icon {
  font-size: 3rem;
  color: #6366f1;
  margin-bottom: 1.5rem;
}

.loading-content p {
  color: #64748b;
  margin: 0;
  font-size: 1.125rem;
}

/* Button loading styles */
.create-album-btn:disabled,
.create-first-album-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Responsive design */
@media (max-width: 768px) {
  .app-header {
    padding: 1rem;
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .app-logo {
    justify-content: center;
  }

  .header-controls {
    flex-direction: column;
    gap: 1rem;
  }

  .search-container {
    width: 100%;
  }

  .search-input {
    width: 100%;
  }

  .account-link {
    align-self: flex-end;
  }

  .page-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .create-album-btn {
    width: 100%;
    justify-content: center;
  }

  .capacity-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .capacity-text {
    min-width: auto;
  }

  .capacity-bar {
    width: 100%;
    max-width: none;
  }

  .album-grid {
    grid-template-columns: 1fr;
  }

  .album-card:hover {
    transform: none;
  }

  .album-actions {
    opacity: 1;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .album-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
}
</style>
