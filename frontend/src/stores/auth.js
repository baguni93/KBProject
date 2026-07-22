import { ref, computed, reactive } from 'vue';
import { defineStore } from 'pinia';
import axios from 'axios';

const initState = {
  token: '',
  user: {
    username: '',
    email: '',
    roles: [],
  },
};

export const useAuthStore = defineStore('auth', () => {
  const state = ref({ ...initState });
  const isLogin = computed(() => !!state.value.user.username);
  const username = computed(() => state.value.user.username); // 로그인 사용자 ID
  const email = computed(() => state.value.user.email); // 로그인 사용자 email

  const login = async (member) => {
    // state.value.token = 'test token';
    // state.value.user = {
    //   username: member.username,
    //   email: member.username + '@test.com',
    // };

    const { data } = await axios.post('/api/auth/login', member);
    state.value = { ...data };
    console.log('suuuuuuuuuuuuu' + data);
    localStorage.setItem('auth', JSON.stringify(state.value));
  };

  const logout = () => {
    localStorage.clear();
    state.value = { ...initState };
  };

  const getToken = () => state.value.token;

  const load = () => {
    const auth = localStorage.getItem('auth');

    if (auth != null) {
      state.value = JSON.parse(auth);

      console.log(state.value);
    }
  };

  // 추가
  //   avatarUpdated 값은 이미지 URL을 변경하여 브라우저 캐시를 무효화하고,
  // 프로필 이미지가 즉시 다시 로드되도록 하기 위해 사용한다.
  // changeProfile : 프로필 수정 후 스토어 상태를 업데이트하는 함수

  const changeProfile = (member) => {
    // state.value.user.email = member.email;
    // 기존 user 정보를 유지하면서 email과 avatarUpdated만 덮어쓴다

    state.value.user = {
      ...state.value.user, // 기존 username, roles 등 나머지 정보 유지
      email: member.email, // 수정된 이메일로 교체
      // Date.now()는 현재 시각을 숫자로 반환한다
      // 이 값을 이미지 URL 뒤에 붙이면 브라우저가 캐시를 무시하고 새 이미지를 불러온다
      avatarUpdated: Date.now(),
    };

    console.log(member);
    // 변경된 상태를 localStorage에 저장해서 새로고침 후에도 유지되도록 한다
    localStorage.setItem('auth', JSON.stringify(state.value));
  };

  load();
  return {
    state,
    username,
    email,
    isLogin,
    changeProfile,
    login,
    logout,
    getToken,
  };
});
