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
import { useUserStore } from '@/stores/user';
import PageHeader from '@/components/common/PageHeader.vue';
import SearchBar from '@/components/search/SearchBar.vue';
import SearchResultList from '@/components/search/SearchResultList.vue';

const searchUserTemplate = {
  userId: 0,
  userName: '',
  nickname: '',
  profileImageName: '',
  friend: false,
};

const result = ref([]);
const keyword = ref('');
const userStore = useUserStore();

const searchMember = async () => {
  console.log('검색:', keyword.value);
  const data = await searchApi.searchMember({
    keyword: keyword.value,
    userId: userStore.userId,
  });

  result.value = data;
  console.log('search:', result.value);
};
</script>

<style scoped>
.search-page {
  padding: 20px;
}
</style>
