export default {
  title: 'KB Pay',
  subtitle: '',
  menus: [
    {
      title: '피드',
      url: '/feed',
      icon: 'fa-solid fa-paste',
    },
    {
      title: '송금',
      url: '/auth/login',
      icon: 'fa-solid fa-plane-departure',
    },
    {
      title: '결제',
      url: '/auth/login',
      icon: 'fa-regular fa-images',
    },
    {
      title: '금융',
      url: '/auth/login',
      icon: 'fa-regular fa-images',
    },
    {
      title: '마이',
      url: '/auth/login',
      icon: 'fa-regular fa-images',
    },
  ],
  accoutMenus: {
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
