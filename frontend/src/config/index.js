export default {
  title: 'KB Pay',
  subtitle: '',
  menus: [
    {
      title: '피드',
      url: '/feed',
      icon: 'fa-solid fa-house',
    },
    {
      title: '송금',
      url: '/remittance',
      icon: 'fa-regular fa-paper-plane',
    },
    {
      title: '결제',
      url: '/wallet',
      icon: 'fa-solid fa-qrcode',
    },
    {
      title: '금융',
      url: '/finance',
      icon: 'fa-regular fa-images',
    },
    {
      title: '마이',
      url: '/mypage',
      icon: 'fa-regular fa-user',
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
