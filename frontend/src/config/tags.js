export default {
  FEEDTYPE_MAP: {
    PAYMENT: {
      values: [
        {
          value: '#결제',
          route: '/',
        },
      ],

      STORE: {
        icon: 'fa-solid fa-store',
      },
    },

    TRANSFER: {
      values: [
        {
          value: '#송금',
          route: '/transactions',
        },
      ],

      CATEGORY: {
        icon: 'fa-solid fa-tags',
      },
    },

    SETTLEMENT: {
      values: [
        {
          value: '#정산',
          route: '/transactions',
        },
      ],

      CATEGORY: {
        icon: 'fa-solid fa-tags',
      },
    },

    CARD: {
      values: [
        {
          value: '#KB 나만의 체크카드',
          route: '/card/create/intro',
        },
      ],

      icon: 'fa-solid fa-credit-card',
    },

    EVENT: {
      values: [
        {
          value: '#이벤트',
          route: '/event',
        },
        {
          value: '#포인트박스',
          route: '/point-wallet',
        },
      ],

      icon: 'fa-solid fa-gift',
    },

    ANALYSIS: {
      values: [
        {
          value: '#AI 소비 분석',
          route: '/analysis',
        },
      ],

      icon: 'fa-solid fa-chart-pie',
    },
  },
};
