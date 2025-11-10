<script setup lang="ts">
import router from "@/router";
import { useLoginStore } from "@/stores/login";
import { useImageProcessingStore } from "@/stores/imageProcessing";

const loginStore = useLoginStore();
const imageProcessingStore = useImageProcessingStore();

async function signin() {
  const url = await loginStore.createSigninRequest();
  window.location.href = url;
}

async function processSigninResponse() {
  await loginStore.processSigninResponse();
  await loginStore.fetchUserInfo();
  // Start polling immediately after successful login
  imageProcessingStore.startPolling();
  const rememberPath = sessionStorage.getItem("rememberPath");
  if (rememberPath) {
    await router.push(rememberPath);
    sessionStorage.removeItem("rememberPath");
  } else {
    await router.push("/secured/album");
  }
}

if (!loginStore.accessToken) {
  if (window.location.href.indexOf("?") >= 0) {
    processSigninResponse();
  } else {
    signin();
  }
} else {
  // Start polling if already logged in
  imageProcessingStore.startPolling();
  router.push("/secured/album");
}
</script>

<template>
  <div
    id="appleid-signin"
    data-color="black"
    data-border="true"
    data-type="sign in"
  ></div>
</template>
