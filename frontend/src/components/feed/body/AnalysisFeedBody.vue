<template>
  <div class="analysis-card">
    <div class="analysis-badge">AI 소비 분석</div>

    <h3 class="analysis-title">
      {{ feed.analysis.aiTitle }}
    </h3>

    <p class="analysis-summary">
      {{ feed.analysis.aiAnalysisSummary }}
    </p>

    <div class="analysis-guide">내 소비패턴이 궁금하다면?</div>

    <button class="analysis-btn" @click="goAnalysis">
      <span class="icon">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
          <path
            d="M8 6L16 12L8 18"
            stroke="white"
            stroke-width="2.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </span>

      <span class="text">소비패턴 하러가기</span>
    </button>
  </div>
</template>

<script setup>
defineProps({
  feed: {
    type: Object,
    required: true,
  },
});

const goAnalysis = () => {
  console.log('소비패턴 페이지 이동');
  // router.push('/analysis');
};
</script>

<style scoped>
.analysis-card {
  margin-top: 12px;
  padding: 18px;

  background: linear-gradient(135deg, #fff8f4 0%, #ffffff 100%);
  border: 1px solid #ffe4d6;
  border-radius: 18px;

  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.analysis-badge {
  display: inline-block;

  padding: 6px 12px;
  margin-bottom: 12px;

  border-radius: 999px;

  background: #fff0e8;
  color: #ff7a45;

  font-size: 12px;
  font-weight: 700;
}

.analysis-title {
  margin: 0 0 10px;

  font-size: 20px;
  font-weight: 700;
  color: #222;
}

.analysis-summary {
  margin: 0;

  color: #666;
  font-size: 15px;
  line-height: 1.7;

  word-break: keep-all;
}

.analysis-guide {
  margin-top: 20px;
  margin-bottom: 10px;

  text-align: center;

  font-size: 13px;
  color: #777;
}

/* 버튼 */
.analysis-btn {
  position: relative;

  width: 85%;
  max-width: 420px;

  height: clamp(48px, 8vw, 58px);

  margin: 0 auto;

  border: none;
  border-radius: 18px;

  display: flex;
  align-items: center;
  justify-content: center;

  gap: clamp(6px, 2vw, 12px);

  padding: 0 clamp(12px, 4vw, 24px);

  background: linear-gradient(90deg, #ff8a3d, #ffb347);

  color: white;

  cursor: pointer;

  box-shadow: 0 8px 20px rgba(255, 138, 61, 0.25);

  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease,
    opacity 0.15s ease;

  -webkit-tap-highlight-color: transparent;
}

/* 클릭 */
.analysis-btn:active {
  transform: scale(0.96);

  opacity: 0.85;

  box-shadow: 0 4px 10px rgba(255, 138, 61, 0.2);
}

/* hover */
.analysis-btn:hover {
  filter: brightness(1.05);
}

.icon {
  display: flex;
  align-items: center;

  flex-shrink: 0;

  width: clamp(18px, 5vw, 24px);
}

.icon svg {
  width: 100%;
  height: auto;
}

.text {
  font-size: clamp(12px, 3.8vw, 16px);

  font-weight: 600;

  white-space: nowrap;
}

@media (max-width: 360px) {
  .analysis-btn {
    width: 90%;
  }

  .text {
    font-size: 12px;
  }
}
</style>
