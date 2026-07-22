import 'bootstrap/dist/css/bootstrap.css';
import 'vue-awesome-paginate/dist/style.css';
import './assets/main.css';
import { useKakao } from 'vue3-kakao-maps/@utils';
const rest_api_key = 'f39ef1e5e8e1c3df672337a13333f372';
useKakao(rest_api_key, ['services']);

import { createApp } from 'vue';
import { createPinia } from 'pinia';
import VueAwesomePaginate from 'vue-awesome-paginate';

import App from './App.vue';
import router from './router';

const app = createApp(App);

app.use(VueAwesomePaginate);
app.use(createPinia());
app.use(router);

app.mount('#app');
