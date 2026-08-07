import 'bootstrap/dist/css/bootstrap.css';
import 'vue-awesome-paginate/dist/style.css';
import './assets/main.css';
import './assets/kb-mobile.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';

import '@/components/common/common/common.css';

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
