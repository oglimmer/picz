<script setup lang="ts">
import axios from "axios";
import type {
  AlbumElementResponse,
  GetAlbumResponseImage,
} from "@/stores/album";
import { useRoute, useRouter } from "vue-router";
import { ref } from "vue";
import config from "@/Config";
import logo from "@/assets/logo-full.jpg";

const route = useRoute();
const router = useRouter();

const albumId = route.params.albumId; //secretId
const imageId = route.params.imageId; //secretId

const showHelp = ref(false);
const isLoading = ref(true);
const loadingError = ref(false);

let data: GetAlbumResponseImage = {
  id: -1,
  albumElements: [],
  description: "",
  secretId: "",
};

try {
  const resp = await axios.get<GetAlbumResponseImage>(
    `${config.apiServer}/api/public/v1/album?secretId=${albumId}`
  );
  data = resp.data;
  document.title = data.description;
  isLoading.value = false;
} catch {
  loadingError.value = true;
  isLoading.value = false;
  setTimeout(() => {
    router.push({
      name: "landing",
    });
  }, 3000);
}

const imageIdxForParams = data.albumElements.findIndex(
  (e) => e.secretId === imageId
);

const imageIdx = ref(imageIdxForParams); // -1 if not found, is the title page

const pageMode = ref(imageId === "_" ? 0 : 1); // 0: all images, 1: single image

const backButton = () => {
  if (imageIdx.value > -1) {
    imageIdx.value = imageIdx.value - 1;
    router.push({
      name: "album",
      params: {
        albumId: albumId,
        imageId:
          imageIdx.value === -1
            ? 0
            : data.albumElements[imageIdx.value].secretId,
      },
    });
  }
};
const backButtonTouch = (e: any) => {
  e.preventDefault();
  backButton();
};

const nextButton = () => {
  if (imageIdx.value < data.albumElements.length - 1) {
    imageIdx.value = imageIdx.value + 1;
    router.push({
      name: "album",
      params: {
        albumId: albumId,
        imageId: data.albumElements[imageIdx.value].secretId,
      },
    });
  }
};

const nextButtonTouch = (e: any) => {
  e.preventDefault();
  nextButton();
};

const help = (e: any) => {
  e.preventDefault();
  showHelp.value = true;
};

const hideHelp = (e: any) => {
  e.preventDefault();
  showHelp.value = false;
};

const showAllImages = () => {
  pageMode.value = 0;
  router.push({
    name: "album",
    params: {
      albumId: albumId,
      imageId: "_",
    },
  });
};
const showSingleImages = (albumElement: AlbumElementResponse) => {
  imageIdx.value = data.albumElements.findIndex((e) => e === albumElement);
  pageMode.value = 1;
  router.push({
    name: "album",
    params: {
      albumId: albumId,
      imageId: albumElement.secretId,
    },
  });
};
</script>

<template>
  <!-- Loading state -->
  <div v-if="isLoading" class="loading-container">
    <div class="loading-content">
      <font-awesome-icon
        :icon="['fas', 'spinner']"
        class="fa-spin loading-spinner"
      />
      <h3>Loading album...</h3>
      <p>Please wait while we load your photos.</p>
    </div>
  </div>

  <!-- Error state -->
  <div v-else-if="loadingError" class="error-container">
    <div class="error-content">
      <font-awesome-icon
        :icon="['fas', 'exclamation-triangle']"
        class="error-icon"
      />
      <h3>Album not found</h3>
      <p>This album doesn't exist or is no longer available.</p>
      <p class="redirect-notice">Redirecting to homepage in a few seconds...</p>
    </div>
  </div>

  <!-- Album content -->
  <div v-else-if="pageMode === 0" class="image-container">
    <img
      v-for="ae in data.albumElements.filter(
        (e) => e.elementType !== 'SECTION'
      )"
      :key="ae.id"
      @click="showSingleImages(ae)"
      :src="`${config.apiServer}/api/public/v1/image/${ae.secretId}?small=true`"
    />
  </div>
  <div v-else-if="pageMode === 1">
    <GlobalEvents
      @keyup.left="backButton"
      @keyup.right="nextButton"
      @keyup.up="showAllImages"
    />
    <div
      class="background"
      v-touch:swipe.left="nextButton"
      v-touch:swipe.right="backButton"
    >
      <div v-if="showHelp" class="helpBox">
        <h4>You can flip through an album</h4>
        <ul>
          <li>by clicking the left or right half of the screen</li>
          <li>by clicking "←", "→" at the top left corner</li>
          <li>by using the arrow keys on your keyboard</li>
          <li>or swiping left - right with your finger</li>
        </ul>
        <h4>You can get to an overview of all images</h4>
        <ul>
          <li>by clicking "↑" at the top left corner</li>
          <li>by using the up arrow on your keyboard</li>
        </ul>
        <button v-touch="hideHelp">Close</button>
      </div>
      <div v-if="imageIdx !== -1">
        <div v-if="data.albumElements[imageIdx].elementType == 'IMAGE'">
          <img
            :src="`${config.apiServer}/api/public/v1/image/${data.albumElements[imageIdx].secretId}`"
            class="userpic"
          />
        </div>
        <div v-if="data.albumElements[imageIdx].elementType == 'MAP'">
          <img
            :src="`${config.apiServer}/api/public/v1/image/${data.albumElements[imageIdx].secretId}`"
            class="userpic"
          />
        </div>
        <div v-if="data.albumElements[imageIdx].elementType == 'SECTION'">
          <div class="sectionDescriptionDiv">
            <h1 class="description">
              {{ data.albumElements[imageIdx].description }}
            </h1>
          </div>
          <img
            :src="`${config.apiServer}/api/public/v1/image/${
              data.albumElements[imageIdx + 1].secretId
            }?small=true`"
            class="sectionpic"
          />
        </div>
      </div>
      <div v-if="imageIdx === -1">
        <div class="sectionDescriptionDiv">
          <h1 class="description">{{ data.description }} <br /></h1>
          <h1 class="description">
            {{
              data.albumElements.filter((e) => e.elementType == "IMAGE").length
            }}
            images <br />
          </h1>
          <h1 class="description" v-if="data.albumElements.length > 0">
            {{ new Date(data.albumElements[0].orderNo).toLocaleDateString() }}
            to
            {{
              new Date(
                data.albumElements[data.albumElements.length - 1].orderNo
              ).toLocaleDateString()
            }}
          </h1>
        </div>
        <img :src="logo" class="sectionpic" />
      </div>
      <div class="right-half" v-on:click="nextButton"></div>
      <div class="left-half" v-on:click="backButton"></div>
      <div
        class="preload"
        v-if="
          data.albumElements[
            imageIdx + 1 < data.albumElements.length ? imageIdx + 1 : 0
          ].elementType == 'IMAGE' ||
          data.albumElements[
            imageIdx + 1 < data.albumElements.length ? imageIdx + 1 : 0
          ].elementType == 'MAP'
        "
      >
        <img
          :src="`${config.apiServer}/api/public/v1/image/${
            data.albumElements[
              imageIdx + 1 < data.albumElements.length ? imageIdx + 1 : 0
            ].secretId
          }`"
        />
      </div>
      <div class="topbox">
        <button
          v-if="imageIdx > -1"
          style="margin-right: 12px"
          v-touch="backButtonTouch"
        >
          ←
        </button>
        <button v-if="imageIdx <= -1" style="margin-right: 12px" disabled>
          &nbsp;
        </button>
        <button
          v-if="imageIdx < data.albumElements.length - 1"
          v-touch="nextButtonTouch"
        >
          →
        </button>
        <button v-if="imageIdx >= data.albumElements.length - 1" disabled>
          &nbsp;
        </button>
        <button style="margin-left: 12px" v-touch="help">?</button>
        <button style="margin-left: 12px" v-touch="showAllImages">↑</button>
      </div>
      <div v-if="imageIdx !== -1">
        <div
          v-if="
            (data.albumElements[imageIdx].elementType == 'IMAGE' ||
              data.albumElements[imageIdx].elementType == 'MAP') &&
            data.albumElements[imageIdx].description
          "
          class="imageDesc"
        >
          {{ data.albumElements[imageIdx].description }}
        </div>
      </div>
    </div>
    <div style="height: 430px"></div>
  </div>
</template>

<style scoped>
.image-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(416px, 1fr));
}

.image-container img {
  width: 100%;
}

.preload {
  position: absolute;
  top: -10px;
  left: -10px;
  width: 1px;
  height: 1px;
  overflow: hidden;
}

.helpBox {
  padding: 8px;
  border: 2px solid black;
  background-color: #ecedee;
  width: 400px;
  border-radius: 10px;
  z-index: 1200;
  box-shadow: 5px 5px 10px 0px rgba(0, 0, 0, 0.61);
}

.right-half {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 50%;
  z-index: 500;
}

.left-half {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 50%;
  z-index: 500;
}

.background {
  background-color: white;
  height: 100%;
  width: 100%;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
  display: flex;
  justify-content: center;
  align-items: center;
}

.imageDesc {
  border: 1px solid #555555;
  background-color: rgba(255, 255, 255, 0.8);
  font-weight: bolder;
  position: fixed;
  left: 20px;
  right: 20px;
  bottom: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 200;
  padding: 20px;
  border-radius: 15px;
}

.sectionDescriptionDiv {
  position: absolute;
  left: 20%;
  top: 10%;
  right: 45px;
  z-index: 200;
  background-color: rgba(255, 255, 255, 0.3);
}

.description {
  position: relative;
  padding: 25px;
  color: black;
}

.topbox {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 900;
}

button,
button:focus-within {
  background-color: #c2c2e3;
  border: 3px solid #333333;
  outline: 3px solid white;
  padding: 5px;
  border-radius: 5px;
  min-width: 50px;
}

.userpic {
  width: 100%; /* ?? */
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  margin: auto;
  /* display: block; */
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
}

.sectionpic {
  filter: blur(1.5rem); /* ?? */
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: fixed;
  top: 0;
  left: 0;
}

/* Loading and error states */
.loading-container,
.error-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-content,
.error-content {
  text-align: center;
  padding: 3rem;
  background-color: white;
  border-radius: 1rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1),
    0 2px 4px -1px rgba(0, 0, 0, 0.06);
  max-width: 400px;
}

.loading-spinner {
  font-size: 3rem;
  color: #6366f1;
  margin-bottom: 1.5rem;
}

.error-icon {
  font-size: 3rem;
  color: #ef4444;
  margin-bottom: 1.5rem;
}

.loading-content h3,
.error-content h3 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  color: #1e293b;
}

.loading-content p,
.error-content p {
  color: #64748b;
  margin: 0 0 0.5rem 0;
}

.redirect-notice {
  font-size: 0.875rem !important;
  color: #94a3b8 !important;
}
</style>
