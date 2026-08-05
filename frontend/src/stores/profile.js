import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '@/api';

export const useProfileStore = defineStore('profile', () => {
  const profileTemplete = {
    profileId: 0,
    userId: 0,
    nickname: '',
    introduction: '',
    imageName: '',
    url: '',
  };

  const profile = ref({});

  // 프로필 조회
  const getProfile = async (userId) => {
    try {
      const res = await api.get(`/api/profile/${userId}`);

      profile.value = res.data;

      console.log(
        '프로필 조회 (비어있으면 테이블 채워주세요) :',
        profile.value,
      );

      return profile.value;
    } catch (e) {
      console.log(e);
    }
  };

  return {
    profile,
    getProfile,
  };
});
