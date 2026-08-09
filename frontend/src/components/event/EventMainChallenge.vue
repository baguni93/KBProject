<template>
  <div class="challenge-card" v-if="challenge">
    <!-- 이벤트 챌린지 배경 추가 예정-->
    <div class="challenge-background">
      <p>이벤트 챌린지</p>
    </div>
    <!-- 이벤트 챌린지 대시보드 -->
    <div class="challenge-status">
      <div class="status-row-top">
        <div class="progress-bar-bg">
          <div
            class="progress-bar-fill"
            :style="{ width: progressPercent + '%' }"
          ></div>
        </div>
        <div class="percentage-info">
          <span class="percent-num">{{ progressPercent }}%</span>
          <span class="lv-text"
            >/ Lv.{{ challenge.userChallengeLevel || 1 }}</span
          >
        </div>
      </div>

      <div class="status-row-bottom">
        <span class="xp-counter">
          {{ challenge.userChallengeExe || 0 }} /
          {{ challenge.userChallengeMaxExe || 1000 }} XP
        </span>

        <span v-if="progressPercent >= 100" class="success-tag">
          🎉 목표 달성!
        </span>
      </div>

      <!-- 보상 받기 버튼 -->
      <button
        v-if="progressPercent >= 100"
        class="level-reward-btn"
        @click="claimReward"
      >
        <i class="fa-solid fa-gift gift-icon"></i> 보상 받기
      </button>
    </div>
  </div>
</template>

<script setup>
import { defineProps, computed } from 'vue';

const props = defineProps({
  challenge: {
    type: Object,
    required: true,
    default: () => ({
      userChallengeLevel: 1,
      userChallengeExe: 0,
      userChallengeMaxExe: 1000,
      status: 'PROCESS',
    }),
  },
});

const progressPercent = computed(() => {
  if (
    !props.challenge ||
    !props.challenge.userChallengeMaxExe ||
    props.challenge.userChallengeMaxExe === 0
  ) {
    return 0;
  }

  const percent =
    (props.challenge.userChallengeExe / props.challenge.userChallengeMaxExe) *
    100;

  return Math.min(Math.floor(percent), 100);
});

const claimReward = () => {
  alert('보상 수령');
};
</script>
<style scoped>
.challenge-background {
}

/* 챌린지 데이터 대시보드 */
.challenge-status {
  background-color: #1e222b;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-sizing: border-box;
  border-radius: 12px;
}

.status-row-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.progress-bar-bg {
  flex: 1;
  height: 8px;
  background-color: #2c313d;
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background-color: #00cc99;
  border-radius: 4px;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.percentage-info {
  font-size: 14px;
  font-weight: bold;
  white-space: nowrap;
}

.percent-num {
  color: #ffffff;
}

.lv-text {
  color: #6c757d;
  margin-left: 2px;
}

.status-row-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
  margin-bottom: 4px;
}

.xp-counter {
  font-size: 12px;
  color: #8a94a0;
  font-weight: 500;
}

.success-tag {
  font-size: 12px;
  color: #00cc99;
  font-weight: bold;
}

/* 보상받기 버튼 */
.level-reward-btn {
  width: 100%;
  background-color: #ffb703;
  color: #222222;
  font-size: 15px;
  font-weight: 800;
  border: none;
  border-radius: 16px;
  padding: 10px;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  box-sizing: border-box;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(255, 183, 3, 0.15);
}

.level-reward-btn:hover:not(:disabled) {
  background-color: #ca9203;
  transform: translateY(-1px);
}

.gift-icon {
  font-size: 16px;
  color: #d35400;
}

.level-reward-btn:disabled {
  background-color: #2c313d;
  color: #5c6370;
  cursor: not-allowed;
  box-shadow: none;
}

.level-reward-btn:disabled .gift-icon {
  color: #5c6370;
}
</style>
