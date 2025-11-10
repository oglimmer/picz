import { ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import config from "@/Config";
import { useLoginStore } from "./login";

export interface GetAlbumResponseImage {
  id: number;
  albumElements: AlbumElementResponse[];
  description: string;
  secretId: string;
}

export interface AlbumElementResponse {
  id: number;
  elementType: string;
  description: string;
  secretId: string;
  creationDate: Date;
  orderNo: number;
  filename: string;
  longitude: number; // marker longitude
  latitude: number; // marker latitude
  mapCenterLongitude: number;
  mapCenterLatitude: number;
  mapSpanLongitude: number;
  mapSpanLatitude: number;
  zoomLevel: number;
}

export interface ListAlbumResponse {
  id: number;
  description: string;
  owner?: string;
  imageCount: number;
  titleSecretId: string;
}

export const useAlbumStore = defineStore("album", () => {
  const loginStore = useLoginStore();

  const album = ref({
    id: 0,
    albumElements: [],
    description: "",
    secretId: "",
  } as GetAlbumResponseImage);

  const albumList = ref([] as ListAlbumResponse[]);

  // Loading states
  const loading = ref({
    album: false,
    albumList: false,
    rotating: {} as Record<number, boolean>,
    updatingDescription: {} as Record<number, boolean>,
    addingSection: false,
    addingMap: false,
    creatingAlbum: false,
    deletingElement: {} as Record<number, boolean>,
    deletingAlbum: {} as Record<string, boolean>,
    changingOrder: false,
    updatingTitle: false,
    updatingMap: {} as Record<number, boolean>,
  });

  const albumLoad = async ({
    albumId,
    secretId,
  }: {
    albumId?: string;
    secretId?: string;
  }) => {
    loading.value.album = true;
    try {
      if (secretId) {
        const response = await axios.get<GetAlbumResponseImage>(
          `${config.apiServer}/api/v1/album?secretId=${secretId}`
        );
        album.value = response.data;
      } else {
        const response = await axios.get<GetAlbumResponseImage>(
          `${config.apiServer}/api/v1/album/${albumId}`,
          {
            headers: {
              Authorization: `Bearer ${loginStore.accessToken}`,
            },
          }
        );
        album.value = response.data;
      }
    } finally {
      loading.value.album = false;
    }
  };

  const albumListLoad = async () => {
    loading.value.albumList = true;
    try {
      const response = await axios.get<ListAlbumResponse[]>(
        `${config.apiServer}/api/v1/album`,
        {
          headers: {
            Authorization: `Bearer ${loginStore.accessToken}`,
          },
        }
      );
      albumList.value = response.data;
    } finally {
      loading.value.albumList = false;
    }
  };

  const albumRotate = async (id: number) => {
    loading.value.rotating[id] = true;
    try {
      const response = await axios.post<string>(
        `${config.apiServer}/api/v1/image/${id}/rotate`,
        {},
        {
          headers: {
            Authorization: `Bearer ${loginStore.accessToken}`,
          },
        }
      );
      album.value.albumElements.filter((e) => e.id === id)[0].secretId =
        response.data;
    } finally {
      delete loading.value.rotating[id];
    }
  };

  const setAlbumDescription = async (id: number, description: string) => {
    loading.value.updatingDescription[id] = true;
    try {
      await axios.patch(
        `${config.apiServer}/api/v1/image/${id}`,
        { description },
        {
          headers: {
            Authorization: `Bearer ${loginStore.accessToken}`,
          },
        }
      );
      album.value.albumElements.filter((e) => e.id === id)[0].description =
        description;
    } finally {
      delete loading.value.updatingDescription[id];
    }
  };

  const addSection = async (imageId: number) => {
    loading.value.addingSection = true;
    try {
      await axios.post(
        `${config.apiServer}/api/v1/album/${album.value.id}/section`,
        {
          imageId,
        },
        {
          headers: {
            Authorization: `Bearer ${loginStore.accessToken}`,
          },
        }
      );
      await albumLoad({ albumId: `${album.value.id}` });
    } finally {
      loading.value.addingSection = false;
    }
  };

  const addMap = async (imageId: number) => {
    loading.value.addingMap = true;
    try {
      await axios.post(
        `${config.apiServer}/api/v1/album/${album.value.id}/map`,
        {
          imageId,
        },
        {
          headers: {
            Authorization: `Bearer ${loginStore.accessToken}`,
          },
        }
      );
      await albumLoad({ albumId: `${album.value.id}` });
    } finally {
      loading.value.addingMap = false;
    }
  };

  const albumCreate = async () => {
    loading.value.creatingAlbum = true;
    try {
      const response = await axios.post(
        `${config.apiServer}/api/v1/album`,
        {},
        {
          headers: {
            Authorization: `Bearer ${loginStore.accessToken}`,
          },
        }
      );
      await albumListLoad();
      return response.data.id;
    } finally {
      loading.value.creatingAlbum = false;
    }
  };

  const deleteElement = async (albumId: number, id: number) => {
    loading.value.deletingElement[id] = true;
    try {
      await axios.delete(
        `${config.apiServer}/api/v1/album/${albumId}/element/${id}`,
        {
          headers: {
            Authorization: `Bearer ${loginStore.accessToken}`,
          },
        }
      );
      await albumLoad({ albumId: `${album.value.id}` });
    } finally {
      delete loading.value.deletingElement[id];
    }
  };

  const albumDelete = async (albumId: string) => {
    loading.value.deletingAlbum[albumId] = true;
    try {
      await axios.delete(`${config.apiServer}/api/v1/album/${albumId}`, {
        headers: {
          Authorization: `Bearer ${loginStore.accessToken}`,
        },
      });
      await albumListLoad();
    } finally {
      delete loading.value.deletingAlbum[albumId];
    }
  };

  const changeOrder = async (
    albumId: string,
    oldIndex: number,
    newIndex: number
  ) => {
    loading.value.changingOrder = true;
    try {
      await axios.post(
        `${config.apiServer}/api/v1/album/${albumId}/order`,
        {
          oldIndex,
          newIndex,
        },
        {
          headers: {
            Authorization: `Bearer ${loginStore.accessToken}`,
          },
        }
      );
      await albumLoad({ albumId: `${album.value.id}` });
    } finally {
      loading.value.changingOrder = false;
    }
  };

  const updateAlbumTitle = async (albumId: string, description: string) => {
    loading.value.updatingTitle = true;
    try {
      await axios.patch(
        `${config.apiServer}/api/v1/album/${albumId}`,
        { description },
        {
          headers: {
            Authorization: `Bearer ${loginStore.accessToken}`,
          },
        }
      );
      album.value.description = description;
    } finally {
      loading.value.updatingTitle = false;
    }
  };

  const updateMap = async (
    id: number,
    mapData: {
      markerLatitude?: number;
      markerLongitude?: number;
      mapCenterLatitude?: number;
      mapCenterLongitude?: number;
      zoomLevel?: number;
    }
  ) => {
    loading.value.updatingMap[id] = true;
    try {
      const updateResponse = await axios.put(
        `${config.apiServer}/api/v1/album/${album.value.id}/map-google`,
        {
          imageId: id,
          ...mapData,
        },
        {
          headers: {
            Authorization: `Bearer ${loginStore.accessToken}`,
          },
        }
      );

      // Update local state
      const element = album.value.albumElements.find((e) => e.id === id);
      if (element) {
        const data = updateResponse.data;
        element.longitude = data.longitude;
        element.latitude = data.latitude;
        element.mapCenterLongitude = data.mapCenterLongitude;
        element.mapCenterLatitude = data.mapCenterLatitude;
        element.zoomLevel = data.zoomLevel;
        element.secretId = data.secretId;
      }
    } finally {
      delete loading.value.updatingMap[id];
    }
  };

  return {
    album,
    albumLoad,
    albumRotate,
    setAlbumDescription,
    albumList,
    albumListLoad,
    albumCreate,
    addSection,
    addMap,
    deleteElement,
    changeOrder,
    albumDelete,
    updateAlbumTitle,
    updateMap,
    loading,
  };
});
