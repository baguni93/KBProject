<template>
  <div class="point-card" @click="goToPointWallet">
    <div class="point-info">
      <span class="point-label">내 포인트</span>
      <div class="point-value">
        <b>{{ localPoint }}</b> <span class="unit">P</span>
      </div>
    </div>

    <!-- 포인트 새로고침 -->
    <button class="reload-btn" @click="reloadPoint">
      <i class="fa-solid fa-rotate-right"></i>
    </button>
  </div>
</template>
<script setup>
import { ref, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import pointWalletApi from '@/api/pointWalletApi';

const props = defineProps({
  point: {
    type: Number,
    default: 0,
  },
});

const router = useRouter();

const localPoint = ref(props.point);

watch(
  () => props.point,
  (newVal) => {
    localPoint.value = newVal;
  },
);

onMounted(() => {
  reloadPoint();
});

const reloadPoint = async () => {
  try {
    const resData = await pointWalletApi.getWallet();
    localPoint.value = resData.point;
  } catch (error) {
    // 예외 처리
  }
};

// 포인트 지갑 페이지로 이동
const goToPointWallet = () => {
  router.push('/point-wallet');
};
</script>

<style scoped>
.point-card {
  background-color: #ffb703;
  border-radius: 20px;
  padding: 16px 20px;
  min-height: 104px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 12px rgba(255, 183, 3, 0.2);
  margin-bottom: 0;
  position: relative;
}

.point-card.clickable {
  cursor: pointer;
}

.point-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.point-label {
  font-size: 13px;
  color: #7a5c00;
  font-weight: 600;
}

.point-value {
  font-size: 30px;
  font-weight: 800;
  color: #000;
  margin-top: 4px;
  line-height: 1;
}

.point-value b {
  font-family: sans-serif;
}

.point-value .unit {
  font-size: 20px;
  font-weight: 700;
  margin-left: 2px;
}

.reload-btn {
  background-color: rgba(255, 255, 255, 0.25);
  border: none;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  color: #ffffff;
  font-size: 13px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  padding: 0;
  transition:
    transform 0.2s,
    background-color 0.2s;
}

.reload-btn:hover {
  background-color: rgba(255, 255, 255, 0.4);
}

.reload-btn:active {
  transform: rotate(180deg);
}
</style>
