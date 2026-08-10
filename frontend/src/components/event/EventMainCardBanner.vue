<template>
  <div class="banner-card" @click="goToCustomCard">
    <div class="card-icon-wrapper">
      <div class="mini-card"></div>
    </div>

    <div class="banner-content">
      <div class="banner-title">
        <span class="sparkle">✨</span> 나만의 커스텀 카드 만들기
        <span class="sparkle">✨</span>
      </div>
      <p class="banner-desc">원하는 이미지로 카드를 제작해 보세요!</p>
    </div>

    <div class="banner-arrow">-></div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

// 나만의 커스텀 카드 메인 페이지로 이동
const goToCustomCard = () => {
  router.push('/card');
};
</script>

<style scoped>
.banner-card {
  background-color: #14171a;
  border-radius: 16px;
  padding: 14px 20px;
  min-height: 90px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  margin-bottom: 0;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
}

.banner-card:hover {
  background-color: #22252a;
}

.card-icon-wrapper {
  margin-right: 14px;
  display: flex;
  align-items: center;
}

.mini-card {
  width: 38px;
  height: 24px;
  background-color: #ffb703;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(255, 183, 3, 0.3);
}

.banner-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.banner-title {
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: -0.3px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.sparkle {
  color: #ffb703;
  font-size: 11px;
}

.banner-desc {
  color: #a1a1a1;
  font-size: 11px;
  font-weight: 500;
  margin: 0;
}

.banner-arrow {
  display: flex;
  align-items: center;
  margin-left: 8px;
}

.arrow-icon {
  width: 14px;
  height: 14px;
  color: #a1a1a1;
  transition: transform 0.2s ease;
}

.banner-card:hover .arrow-icon {
  transform: translateX(3px);
  color: #ffffff;
}
</style>
