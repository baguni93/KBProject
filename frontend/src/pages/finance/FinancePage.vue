<template>
  <div class="finance-page">
    <!-- 포인트 조회 -->
    <PointView :point="userPoint" />

    <!-- 소비분석 등 ..  -->
    <div class="finance-card">
      <h3>금융생활+</h3>
      <p class="subtitle">나의 소비·혜택·이벤트를 한눈에 확인하세요</p>

      <div class="menu-grid"></div>
    </div>

    <!-- 이벤트 페이지 이동 버튼 -->
    <button class="bottom-banner-btn" @click="goToEventMain">
      이벤트 참여하러 가기 <i class="fa-solid fa-arrow-right"></i>
    </button>
  </div>
</template>
<script setup>
import { ref, onMounted, watch, isProxy } from 'vue';
import { useRouter } from 'vue-router';

import pointWalletApi from '@/api/pointWalletApi';
import PointView from '@/components/finance/PointView.vue';

const router = useRouter();
const userPoint = ref(0);

onMounted(async () => {
  try {
    const data = await pointWalletApi.getWallet();

    userPoint.value = data.getWallet;
  } catch (err) {
    console.error('데이터 로드 실패', err);
  }
});

const goToEventMain = () => {
  router.push('/event/main');
};

const goToPointWallet = () => {
  router.push('/point-wallet');
};
</script>
<style scoped>
.finance-page {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 500px;
  margin: 0 auto;
  box-sizing: border-box;
}

/* 중간 메뉴 배너 영역 */
.finance-card {
  background-color: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 40px 24px;
  box-sizing: border-box;
  width: 100%;
  min-height: 250px;
}

h3 {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 8px 0;
  color: #333333;
}

.subtitle {
  font-size: 14px;
  color: #666666;
  margin: 0 0 16px 0;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

/* 이벤트 페이지 이동 버튼 */
.bottom-banner-btn {
  width: 100%;
  padding: 16px;
  background-color: #ffbc00;
  color: #222222;
  font-weight: bold;
  font-size: 16px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  box-sizing: border-box;
  transition: background-color 0.2s;
}

.bottom-banner-btn:hover {
  background-color: #e5a900;
}
</style>
