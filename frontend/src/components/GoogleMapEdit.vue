<script setup lang="ts">
import { useAlbumStore } from "@/stores/album";
import { ref, onUnmounted, nextTick } from "vue";
import config from "@/Config";

const albumStore = useAlbumStore();

const props = defineProps<{
  imageId: number;
  externalId: string;
  markerLatitude?: number;
  markerLongitude?: number;
  mapCenterLatitude?: number;
  mapCenterLongitude?: number;
  zoomLevel?: number;
  description: string;
  albumId: number;
}>();

const description = ref(props.description);
const showOverlay = ref(false);
const googleMapInitialized = ref(false);
const mapLoadError = ref(false);

const mapContainer = ref<HTMLElement | null>(null);

let _map: google.maps.Map | null = null;
let _marker: google.maps.Marker | null = null;

const userStore = useAlbumStore();

const descriptionChanged = (): void => {
  userStore.setAlbumDescription(props.imageId, description.value);
};

const addSection = (): void => {
  userStore.addSection(props.imageId);
};

const deleteThisElement = (): void => {
  userStore.deleteElement(props.albumId, props.imageId);
};

const createMap = async (): Promise<void> => {
  if (!mapContainer.value) return;

  if (!window.google?.maps) {
    await loadGoogleMapsAPI();
    if (!window.google?.maps) {
      mapLoadError.value = true;
      return;
    }
  }

  console.log("Initializing Google Maps...");
  console.log(props);
  try {
    const initialLat = props.markerLatitude ?? 0;
    const initialLng = props.markerLongitude ?? 0;
    const mapCenterLat = props.mapCenterLatitude ?? initialLat;
    const mapCenterLng = props.mapCenterLongitude ?? initialLng;
    const initialZoom = props.zoomLevel ?? 10;

    _map = new google.maps.Map(mapContainer.value, {
      center: { lat: mapCenterLat, lng: mapCenterLng },
      zoom: initialZoom,
      mapTypeId: google.maps.MapTypeId.ROADMAP,
      mapTypeControl: true,
      zoomControl: true,
      streetViewControl: false,
      fullscreenControl: false,
      gestureHandling: "greedy",
    });

    _marker = new google.maps.Marker({
      position: { lat: initialLat, lng: initialLng },
      map: _map,
      draggable: true,
      title: "Location",
      animation: google.maps.Animation.DROP,
    });

    // _marker.addListener("dragend", saveMapChanges);
    // _map.addListener("idle", saveMapChanges);

    googleMapInitialized.value = true;
  } catch (error) {
    console.error("Error initializing Google Maps:", error);
    mapLoadError.value = true;
  }
};

const destroyMap = (): void => {
  if (_marker) {
    google.maps.event.clearInstanceListeners(_marker);
    _marker.setMap(null);
    _marker = null;
  }

  if (_map) {
    google.maps.event.clearInstanceListeners(_map);
    _map = null;
  }

  googleMapInitialized.value = false;
};

const toggleOverlay = async (event: Event): Promise<void> => {
  event.stopPropagation();
  showOverlay.value = !showOverlay.value;

  if (showOverlay.value) {
    mapLoadError.value = false;
    await nextTick();
    createMap();
  } else {
    destroyMap();
  }
};

const closeOverlay = (): void => {
  showOverlay.value = false;
  destroyMap();
};

const saveMapChanges = async (): Promise<void> => {
  if (!_map || !_marker) return;

  const markerPosition = _marker.getPosition();
  const mapCenter = _map.getCenter();
  const mapZoom = _map.getZoom();

  if (markerPosition && mapCenter && mapZoom !== undefined) {
    await albumStore.updateMap(props.imageId, {
      markerLatitude: markerPosition.lat(),
      markerLongitude: markerPosition.lng(),
      mapCenterLatitude: mapCenter.lat(),
      mapCenterLongitude: mapCenter.lng(),
      zoomLevel: mapZoom,
    });
    closeOverlay();
  }
};

const loadGoogleMapsAPI = (): Promise<boolean> => {
  return new Promise((resolve) => {
    if (window.google?.maps) {
      resolve(true);
      return;
    }

    if (document.getElementById("google-maps-api")) {
      resolve(false);
      return;
    }

    const API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;
    const script = document.createElement("script");
    script.id = "google-maps-api";
    script.src = `https://maps.googleapis.com/maps/api/js?key=${API_KEY}`;
    script.defer = true;
    script.async = true;

    script.onload = () => resolve(true);
    script.onerror = () => {
      console.error("Failed to load Google Maps API");
      mapLoadError.value = true;
      resolve(false);
    };

    document.head.appendChild(script);
  });
};

onUnmounted(() => {
  destroyMap();
});

declare global {
  interface Window {
    google?: typeof google;
  }
}
</script>

<template>
  <div class="map-edit-container">
    <div class="action-buttons">
      <button
        class="edit-btn section-btn"
        @click="addSection"
        title="Add Section"
      >
        <font-awesome-icon :icon="['fas', 'heading']" />
      </button>
      <button
        class="edit-btn delete-btn"
        @click="deleteThisElement"
        title="Delete"
      >
        <font-awesome-icon :icon="['fas', 'trash']" />
      </button>
    </div>
    <div class="content-area">
      <div class="content-layout">
        <div class="map-wrapper">
          <img
            :src="`${config.apiServer}/api/public/v1/image/${externalId}?small=true`"
            class="map-preview"
            @click="toggleOverlay"
            title="Click to enlarge and edit map"
          />
        </div>
        <div class="text-content">
          <div class="description-area">
            <textarea
              v-model="description"
              @blur="descriptionChanged"
              placeholder="Add a description..."
              class="description-input"
            ></textarea>
          </div>
        </div>
      </div>
    </div>

    <!-- Map Overlay -->
    <teleport to="body" v-if="showOverlay">
      <div class="map-overlay" @click="closeOverlay">
        <div class="overlay-content" @click.stop>
          <!-- Control buttons -->
          <div class="map-controls">
            <button
              class="control-btn save"
              @click="saveMapChanges"
              title="Save Changes"
            >
              <font-awesome-icon :icon="['fas', 'save']" />
            </button>
          </div>

          <!-- Google Maps container -->
          <div ref="mapContainer" class="google-map-container"></div>

          <!-- Loading display -->
          <div v-if="!googleMapInitialized" class="map-loading">
            <img
              :src="`${config.apiServer}/api/public/v1/image/${externalId}`"
              class="loading-image"
            />
            <div class="loading-text">
              <font-awesome-icon :icon="['fas', 'spinner']" spin />
              Loading Map...
            </div>
          </div>

          <!-- Error display -->
          <div v-if="mapLoadError" class="map-error">
            <font-awesome-icon :icon="['fas', 'exclamation-triangle']" />
            Error loading map
            <button @click="createMap" class="retry-btn">Retry</button>
          </div>

          <!-- Close button -->
          <button class="close-btn" @click="closeOverlay" title="Close">
            <font-awesome-icon :icon="['fas', 'times']" />
          </button>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.map-edit-container {
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

.map-edit-container:hover {
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

.map-wrapper {
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

.map-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 0;
  transition: transform 0.2s ease;
  cursor: pointer;
}

.map-preview:hover {
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

.edit-btn:hover {
  background-color: #f0f0f0;
  transform: translateX(1px);
}

.rotate-btn {
  color: #198754;
}

.section-btn {
  color: #0d6efd;
}

.delete-btn {
  color: #dc3545;
}

.map-overlay {
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
}

.overlay-content {
  position: relative;
  width: 90vw;
  height: 90vw;
  max-width: 640px;
  max-height: 640px;
  background-color: #f8f9fa;
  border-radius: 8px;
  overflow: hidden;
}

.google-map-container {
  width: 100%;
  height: 100%;
  background-color: #e9ecef;
}

.map-controls {
  position: absolute;
  top: 20px;
  right: 60px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 100;
}

.control-btn {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background-color: white;
  border: none;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 1rem;
  color: #333;
  transition: all 0.2s ease;
}

.control-btn:hover:not(:disabled) {
  transform: scale(1.1);
  background-color: #f8f9fa;
}

.control-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.save {
  color: #007bff;
}

.map-loading {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: #e9ecef;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.loading-image {
  max-width: 60%;
  max-height: 60%;
  object-fit: contain;
  opacity: 0.5;
}

.loading-text {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #6c757d;
  font-size: 1rem;
}

.map-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 2rem;
  background-color: #fff0f0;
  color: #dc3545;
  border-radius: 8px;
  text-align: center;
}

.retry-btn {
  margin-top: 1rem;
  padding: 0.5rem 1.5rem;
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.retry-btn:hover {
  background-color: #c82333;
}

.close-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  background-color: rgba(0, 0, 0, 0.6);
  border: none;
  color: white;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 10px;
  border-radius: 50%;
  transition: background-color 0.2s ease;
  z-index: 10;
}

.close-btn:hover {
  background-color: rgba(0, 0, 0, 0.8);
}

@media (max-width: 768px) {
  .map-edit-container {
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

  .map-wrapper {
    width: 100%;
    height: 180px;
    margin-bottom: 10px;
  }

  .overlay-content {
    width: 95vw;
    height: 95vh;
  }
}
</style>
