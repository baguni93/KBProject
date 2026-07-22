export default {
  title: '2팀', // 메인 타이틀
  subtitle: '최종 프로젝트', // 서브 타이틀
  menus: [
    // 메인 메뉴 구성 정보
    {
      title: '로그인',
      url: '/auth/login',
      icon: 'fa-solid fa-paste',
    },
    {
      title: '로그인',
      url: '/auth/login',
      icon: 'fa-solid fa-plane-departure',
    },
    {
      title: '로그인',
      url: '/auth/login',
      icon: 'fa-regular fa-images',
    },
  ],
  accoutMenus: {
    // 인증 관련 메뉴 정보
    login: {
      url: '/auth/login',
      title: '로그인',
      icon: 'fa-solid fa-right-to-bracket',
    },
    join: {
      url: '/auth/join',
      title: '회원가입',
      icon: 'fa-solid fa-user-plus',
    },
  },
};
