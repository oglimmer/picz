import "bootstrap/dist/css/bootstrap.min.css";
/* import the fontawesome core */
import { library } from "@fortawesome/fontawesome-svg-core";
/* import font awesome icon component */
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
/* import specific icons */
import { fas } from "@fortawesome/free-solid-svg-icons";

import { GlobalEvents } from "vue-global-events";

import Vue3TouchEvents, {
  type Vue3TouchEventsOptions,
} from "vue3-touch-events";

import { createApp } from "vue";
import { createPinia } from "pinia";

import config from "./Config";
import App from "./App.vue";
import router from "./router";

import "./assets/main.css";

console.log(
  `Using API @ ${config.apiServer}, ${config.idpServer}, ${config.idpClientId}`
);

const app = createApp(App);

/* add icons to the library */
library.add(fas);

app.component("font-awesome-icon", FontAwesomeIcon);
app.component("GlobalEvents", GlobalEvents);

app.use(createPinia());
app.use(router);
app.use<Vue3TouchEventsOptions>(Vue3TouchEvents, {
  disableClick: false,
  // any other global options...
});

app.mount("#app");

import "bootstrap/dist/js/bootstrap.js";
