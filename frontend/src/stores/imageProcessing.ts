import { computed } from "vue";
import { defineStore } from "pinia";
import { useLoginStore } from "./login";
import { useAlbumStore } from "./album";

export const useImageProcessingStore = defineStore("imageProcessing", () => {
  const loginStore = useLoginStore();
  const albumStore = useAlbumStore();

  // Computed property to check if any processing is happening
  const isProcessing = computed(() => {
    if (!loginStore.userCapacity) return false;
    return (
      Object.values(loginStore.userCapacity.processingCounterByAlbum).reduce(
        (sum, count) => sum + count,
        0
      ) > 0
    );
  });

  // Check if specific album is processing
  const isAlbumProcessing = (albumId: number) => {
    if (!loginStore.userCapacity) return false;
    return loginStore.userCapacity.processingAlbumIds.includes(albumId);
  };

  // Get processing count for specific album
  const getAlbumProcessingCount = (albumId: number) => {
    if (!loginStore.userCapacity) return 0;
    return (
      loginStore.userCapacity.processingCounterByAlbum[albumId.toString()] || 0
    );
  };

  // Fetch processing statistics from API
  const fetchStats = async () => {
    const previousCounts =
      loginStore.userCapacity?.processingCounterByAlbum || {};

    // Fetch updated user data (which now includes stats)
    await loginStore.fetchUserInfo();

    if (loginStore.userCapacity) {
      const newCounts = loginStore.userCapacity.processingCounterByAlbum;

      // Check for changes in processing counts to trigger album reloads
      for (const albumId in previousCounts) {
        const prevCount = previousCounts[albumId];
        const newCount = newCounts[albumId] || 0;

        // If count decreased (processing finished), reload the album
        if (prevCount > newCount) {
          // Reload album if it's currently loaded
          if (albumStore.album.id === parseInt(albumId)) {
            await albumStore.albumLoad({ albumId });
          }
        }
      }

      // Update album list image counts
      albumStore.albumList.forEach((album) => {
        album.imageCount =
          loginStore.userCapacity!.numberOfImagesByAlbum[album.id] || 0;
      });
    }
  };

  // Start polling for processing statistics
  const startPolling = () => {
    // Initial fetch
    fetchStats();

    // Set up 5-second polling using loginStore's fetch method
    // Store the interval for cleanup
    return setInterval(fetchStats, 5000);
  };

  // Stop polling
  const stopPolling = (interval?: NodeJS.Timeout) => {
    if (interval) {
      clearInterval(interval);
    }
  };

  // Reset store state
  const reset = () => {
    // User capacity will be reset by login store
    // No local state to reset anymore
  };

  // Handle logout cleanup
  const handleLogout = () => {
    reset();
    loginStore.clearUserData();
  };

  return {
    isProcessing,
    isAlbumProcessing,
    getAlbumProcessingCount,
    fetchStats,
    startPolling,
    stopPolling,
    reset,
    handleLogout,
  };
});
