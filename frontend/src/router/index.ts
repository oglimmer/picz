import { createRouter, createWebHistory } from "vue-router";
import LandingView from "../views/open/LandingView.vue";
import { useLoginStore } from "@/stores/login";
import { useImageProcessingStore } from "@/stores/imageProcessing";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "landing",
      component: LandingView,
    },
    {
      path: "/login",
      name: "home",
      component: () => import("../views/secured/LoginView.vue"),
    },
    {
      path: "/secured/album",
      name: "album-list",
      component: () => import("../views/secured/AlbumListView.vue"),
    },
    {
      path: "/secured/album/:albumId",
      name: "album-edit",
      component: () => import("../views/secured/EditAlbumView.vue"),
    },
    {
      path: "/album/:albumId",
      name: "album-direct",
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import("../views/open/AlbumShowView.vue"),
    },
    {
      path: "/album/:albumId/:imageId",
      name: "album",
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import("../views/open/AlbumShowView.vue"),
    },
    {
      path: "/tos",
      name: "tos",
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import("../views/open/TosView.vue"),
    },
    {
      path: "/imprint",
      name: "imprint",
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import("../views/open/ImprintView.vue"),
    },
    {
      path: "/support",
      name: "support",
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import("../views/open/SupportView.vue"),
    },
  ],
});

router.beforeEach(async (to) => {
  const loginStore = useLoginStore();
  const imageProcessingStore = useImageProcessingStore();

  if (
    !loginStore.accessToken &&
    to.name !== "home" &&
    to.name != "album-direct" &&
    to.name != "album" &&
    to.name != "landing" &&
    to.name != "tos" &&
    to.name != "imprint" &&
    to.name != "support"
  ) {
    sessionStorage.setItem("rememberPath", to.fullPath);
    return { name: "home" };
  }

  // Start polling when entering secured pages with valid token
  if (loginStore.accessToken && to.path.startsWith("/secured")) {
    imageProcessingStore.startPolling();
  }
});

router.afterEach((to, from) => {
  const imageProcessingStore = useImageProcessingStore();

  // Stop polling when leaving secured pages
  if (from.path.startsWith("/secured") && !to.path.startsWith("/secured")) {
    imageProcessingStore.stopPolling();
  }
});

export default router;
