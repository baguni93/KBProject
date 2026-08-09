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
      return profile.value;
    } catch (e) {
      console.log('Profile fetch fallback');
      profile.value = {
        userId: userId,
        nickname: '김국민',
        introduction: 'KB Pay와 함께하는 즐거운 일상',
        imageName: null,
      };
      return profile.value;
    }
  };

  return {
    profile,
    getProfile,
  };
});
