<template>
  <div class="search-page">
    <PageHeader title="사용자 검색" />

    <SearchBar v-model="keyword" editable @search="searchMember" />

    <SearchResultList :search-result="result" />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import searchApi from '@/api/searchApi';
import PageHeader from '@/components/common/PageHeader.vue';
import SearchBar from '@/components/search/SearchBar.vue';
import SearchResultList from '@/components/search/SearchResultList.vue';
import { useAuthStore } from '@/stores/auth.js';
const authStore = useAuthStore();
const userId = authStore.userId;

const searchUserTemplate = {
  userId: 0,
  userName: '',
  nickname: '',
  profileImageName: '',
  friend: false,
};

const result = ref([]);
const keyword = ref('');

const searchMember = async () => {
  console.log('검색:', keyword.value);
  const data = await searchApi.searchMember({
    keyword: keyword.value,
    userId: userId,
  });

  result.value = data;
  console.log('search:', result.value);
};
</script>

<style scoped>
.search-page {
  width: 100%;
  min-height: 100%;
  padding: 0 20px 30px;
  background: #ffffff;
}
</style>
