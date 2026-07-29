import 'bootstrap/dist/css/bootstrap.css';
import 'vue-awesome-paginate/dist/style.css';
import './assets/main.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';

import { createApp } from 'vue';
import { createPinia } from 'pinia';
import VueAwesomePaginate from 'vue-awesome-paginate';

import App from './App.vue';
import router from './router';

import { useKakao } from 'vue3-kakao-maps/@utils';

const app = createApp(App);

app.use(VueAwesomePaginate);
app.use(createPinia());
app.use(router);

try {
  const rest_api_key = 'f39ef1e5e8e1c3df672337a13333f372';
  useKakao(rest_api_key, ['services']);
} catch (e) {
  console.warn('Kakao maps load warning:', e);
}

app.mount('#app');
