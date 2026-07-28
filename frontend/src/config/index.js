export default {
  title: 'KB Pay',
  subtitle: '',
  menus: [
    {
      title: 'KB Pay 지갑',
      url: '/wallet',
      icon: 'fa-solid fa-wallet',
    },
    {
      title: '송금하기',
      url: '/remittance',
      icon: 'fa-solid fa-paper-plane',
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
