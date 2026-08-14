<template>
  <div class="challenge-card" v-if="challenge">
    <!-- 이벤트 챌린지 상단 타이틀 -->
    <div class="challenge-background">
      <div class="level-label">
        <div class="level-title-group">
          <i class="fa-solid fa-bolt zap-icon"></i>
          <span class="label-text">이벤트 챌린지</span>
        </div>

        <!-- 우측 상단 포인트 배지 -->
        <span class="reward-point-badge">+{{ challenge.rewardPoint }}P</span>
      </div>
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
        </div>
      </div>

      <div class="status-row-bottom">
        <span class="lv-text">Lv.{{ challenge.currentLevel || 1 }}</span>
        <span class="xp-counter">
          {{ challenge.exp || 0 }} / {{ challenge.requiredExp || 1000 }} EXP
        </span>

        <!-- 목표 달성 (보상 미수령 및 완료 상태) -->
        <span
          v-if="isFinished || progressPercent >= 100"
          class="success-tag"
        >
          {{ isFinished ? '🎉 챌린지 완료!' : '🎉 목표 달성!' }}
        </span>
      </div>

      <!-- 보상받기 버튼(COMPLETE 상태) -->
      <button
        v-if="isFinished || progressPercent >= 100"
        class="level-reward-btn"
        :disabled="isFinished || isSubmitting"
        @click="handleclaimReward"
      >
        <i class="fa-solid fa-gift gift-icon"></i>
        {{ isFinished ? '챌린지 완료' : '보상받기' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { defineProps, computed, defineEmits, ref } from 'vue';

const props = defineProps({
  challenge: {
    type: Object,
    required: true,
    default: () => ({
      challengeId: 0,
      userChallengeId: 0,
      currentLevel: 1,
      currentTarget: 0,
      requiredExp: 1000,
      exp: 0,
      startDate: '',
      endDate: '',
      dDay: '',
    }),
  },
});

const emit = defineEmits(['claim-reward']);
const isFinished = computed(() => props.challenge.status === 'REWARDED');

// 진행률 (%) 계산
const progressPercent = computed(() => {
  if (
    !props.challenge ||
    !props.challenge.requiredExp ||
    props.challenge.requiredExp === 0
  ) {
    return isFinished.value ? 100 : 0;
  }

  if (isFinished.value) return 100;

  const percent = (props.challenge.exp / props.challenge.requiredExp) * 100;
  return Math.min(Math.floor(percent), 100);
});

const isSubmitting = ref(false);

const handleclaimReward = async () => {
  // 수령한 상태에서 클릭 방지
  if (isFinished.value || isSubmitting.value) return;

  if (props.challenge.status !== 'COMPLETE' && progressPercent.value < 100) {
    alert('아직 목표를 달성하지 못했습니다.');
    return;
  }

  try {
    isSubmitting.value = true;
    emit('claim-reward', props.challenge.challengeId);
  } finally {
    setTimeout(() => {
      isSubmitting.value = false;
    }, 500);
  }
};
</script>

<style scoped>
.challenge-card {
  width: 100%;
  background-color: #1e222b;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
}

.challenge-background {
  position: relative;
  padding: 16px 16px 10px 16px;
  box-sizing: border-box;
}

.level-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.level-title-group {
  display: flex;
  align-items: center;
  gap: 6px;
}

.reward-point-badge {
  font-size: 13px;
  font-weight: 800;
  color: #ffb703;
  background-color: rgba(255, 183, 3, 0.12);
  padding: 3px 8px;
  border-radius: 10px;
}

.zap-icon {
  font-size: 13px;
  color: #ffb703;
}

.label-text {
  font-size: 13px;
  font-weight: bold;
  color: #ffffff;
}

.challenge-status {
  padding: 0 16px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-sizing: border-box;
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

.lv-text {
  font-size: 12px;
  color: #a3abb6;
  font-weight: 800;
}

.success-tag {
  font-size: 12px;
  color: #00cc99;
  font-weight: bold;
}

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
