<template>
  <main class="random-box-page">
    <PageHeader title="랜덤박스" :showBack="true" :customBack="true" @back="goToPointWallet" />

    <div class="random-box-content">
      <section class="box-stage kb-card">
        <div class="gift-visual" :class="{ opened: lastOpenResult && !opening }">
          <i :class="lastOpenResult && !opening ? 'fa-solid fa-box-open' : 'fa-solid fa-gift'"></i>
          <span v-if="!lastOpenResult" class="spark spark-1">✦</span>
          <span v-if="!lastOpenResult" class="spark spark-2">✦</span>
        </div>

        <template v-if="lastOpenResult && !opening">
          <div class="box-count-chip text-13-bold"><i class="fa-solid fa-gift"></i> 보유 랜덤박스 {{ randomBoxes.length }}개</div>
          <h2 class="text-20-bold">축하해요!</h2>
          <div class="reward-value text-28-bold">{{ formatNumber(lastOpenResult.totalRewardPoint ?? lastOpenResult.rewardPoint) }}P</div>
          <p class="text-13">포인트가 바로 적립되었어요.</p>
        </template>

        <template v-else>
          <div class="box-count-chip text-13-bold"><i class="fa-solid fa-gift"></i> 보유 랜덤박스 {{ randomBoxes.length }}개</div>
          <h2 class="text-20-bold">랜덤박스를 열어보세요</h2>
          <p class="text-13">두근두근, 어떤 포인트가 나올까요?</p>
        </template>

        <div class="box-actions">
          <button type="button" class="content-btn secondary open-one-button" :disabled="loading || randomBoxes.length === 0" @click="openOne">
            <i class="fa-solid fa-gift"></i>
            {{ opening && openingMode === 'one' ? '개봉 중...' : '1개 열기' }}
          </button>

          <button type="button" class="content-btn primary open-all-button" :disabled="loading || randomBoxes.length === 0" @click="openAll">
            <i class="fa-solid fa-gift"></i>
            {{ opening && openingMode === 'all' ? `모두 여는 중 (${randomBoxes.length}개)` : `모두 열기 (${randomBoxes.length}개)` }}
          </button>
        </div>
      </section>

      <section class="kb-section guide-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title text-20-bold">랜덤박스 안내</h2>
        </div>

        <div class="guide-card kb-card">
          <div class="guide-row">
            <div class="guide-icon"><i class="fa-solid fa-clock-rotate-left"></i></div>
            <div class="guide-copy">
              <strong class="text-14-bold">먼저 받은 박스부터 열려요</strong>
              <span class="text-12">1개 열기 시 가장 오래 보유한 박스가 먼저 열려요.</span>
            </div>
          </div>

          <div class="guide-row">
            <div class="guide-icon"><i class="fa-solid fa-coins"></i></div>
            <div class="guide-copy">
              <strong class="text-14-bold">포인트 즉시 적립</strong>
              <span class="text-12">획득한 포인트는 바로 포인트 지갑에 반영돼요.</span>
            </div>
          </div>

          <button
              type="button"
              class="guide-row policy-row"
              :aria-expanded="showIssuePolicy"
              @click="showIssuePolicy = !showIssuePolicy"
          >
            <span class="guide-icon"><i class="fa-solid fa-gift"></i></span>
            <span class="guide-copy">
              <strong class="text-14-bold">랜덤박스는 언제 받을 수 있나요?</strong>
              <span class="text-12">현재 적용된 랜덤박스 지급 조건을 확인해보세요.</span>
            </span>
            <i :class="['fa-solid', showIssuePolicy ? 'fa-chevron-up' : 'fa-chevron-down', 'policy-arrow']"></i>
          </button>

          <transition name="policy-slide">
            <div v-if="showIssuePolicy" class="issue-policy-panel">
              <div class="issue-policy-list">
                <div v-for="condition in issueConditions" :key="condition.title" class="issue-policy-item">
                  <span class="issue-policy-icon"><i :class="['fa-solid', condition.icon]"></i></span>
                  <div class="issue-policy-copy">
                    <strong class="text-13-bold">{{ condition.title }}</strong>
                    <span class="text-12">{{ condition.description }}</span>
                  </div>
                </div>
              </div>

            </div>
          </transition>

          <button
              type="button"
              class="guide-row policy-row"
              :aria-expanded="showProbabilityPolicy"
              @click="showProbabilityPolicy = !showProbabilityPolicy"
          >
            <span class="guide-icon"><i class="fa-solid fa-circle-info"></i></span>
            <span class="guide-copy">
              <strong class="text-14-bold">보상 확률 정보</strong>
              <span class="text-12">어떤 포인트가 얼마나 자주 나오는지 확인해보세요.</span>
            </span>
            <i :class="['fa-solid', showProbabilityPolicy ? 'fa-chevron-up' : 'fa-chevron-down', 'policy-arrow']"></i>
          </button>

          <transition name="policy-slide">
            <div v-if="showProbabilityPolicy" class="probability-panel">
              <div class="probability-head">
                <strong class="text-13-bold">랜덤박스 보상 확률</strong>
                <span class="text-12-bold">총 확률 100%</span>
              </div>

              <div class="probability-list">
                <div v-for="reward in rewardProbabilities" :key="reward.range" class="probability-item">
                  <span class="probability-range text-12-bold">{{ reward.range }}</span>
                  <strong class="probability-rate text-12-bold">{{ reward.rate }}</strong>
                </div>
              </div>

              <div class="probability-note text-12-bold">
                <i class="fa-solid fa-star"></i>
                <span>랜덤박스 1개의 평균 기대 포인트는 약 <strong>18.6P</strong>예요.</span>
              </div>
            </div>
          </transition>
        </div>
      </section>
    </div>

    <transition name="opening-overlay">
      <div v-if="opening" class="box-opening-overlay" aria-live="polite">
        <div :class="['opening-gift', { 'opening-all': openingMode === 'all' }]">
          <i class="fa-solid fa-gift"></i>
          <span class="burst burst-1">✦</span>
          <span class="burst burst-2">✦</span>
          <span class="burst burst-3">✦</span>
          <span class="burst burst-4">✦</span>
          <span class="burst burst-5">✦</span>
          <span v-if="openingMode === 'all'" class="burst burst-6">✦</span>
          <span v-if="openingMode === 'all'" class="burst burst-7">✦</span>
        </div>
        <strong class="opening-label text-18-bold">{{ openingMode === 'all' ? '랜덤박스를 열고 있어요' : '랜덤박스를 열고 있어요' }}</strong>
      </div>
    </transition>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import pointWalletApi from '@/api/pointWalletApi';
import { formatNumber, getApiErrorMessage } from '@/util/pointWallet';

const router = useRouter();
/**
 * @typedef {Object} RandomBox
 * @property {number|string} userRandomBoxId
 */

/**
 * @typedef {Object} OpenResult
 * @property {number} [rewardPoint]
 * @property {number} [totalRewardPoint]
 * @property {number} [openedCount]
 */

/** @type {import('vue').Ref<RandomBox[]>} */
const randomBoxes = ref([]);

/** @type {import('vue').Ref<OpenResult|null>} */
const lastOpenResult = ref(null);
const loading = ref(false);
const opening = ref(false);
const openingMode = ref('one');
const message = ref('');
const messageType = ref('success');
const showIssuePolicy = ref(false);
const showProbabilityPolicy = ref(false);
const issueConditions = [
  {
    icon: 'fa-calendar-check',
    title: '출석 체크',
    description: '출석 체크를 완료하면 랜덤박스 1개가 발급돼요.',
  },
  {
    icon: 'fa-share-nodes',
    title: '피드 작성·공유',
    description: '피드가 정상 등록되면 1개가 발급되며 하루 최대 10개까지 받을 수 있어요.',
  },
  {
    icon: 'fa-paper-plane',
    title: '일반 송금',
    description: '정산이 아닌 일반 송금 성공 시 같은 수취 대상 기준 1회 랜덤박스가 발급돼요.',
  },
];
const rewardProbabilities = [
  { range: '1 ~ 10P', rate: '90%' },
  { range: '11 ~ 100P', rate: '9%' },
  { range: '101 ~ 1,000P', rate: '0.9%' },
  { range: '1,001 ~ 5,000P', rate: '0.09%' },
  { range: '10,000P', rate: '0.01%' },
];
const goToPointWallet = () => router.push('/point-wallet');
const loadRandomBoxes = async () => { randomBoxes.value = await pointWalletApi.getUnopenedRandomBoxes(); };
const waitForOpenAnimation = (duration = 1050) => new Promise((resolve) => window.setTimeout(resolve, duration));

const openOne = async () => {
  const oldestBox = randomBoxes.value[0];
  if (!oldestBox) { messageType.value = 'error'; message.value = '열 수 있는 랜덤박스가 없습니다.'; return; }

  loading.value = true;
  opening.value = true;
  openingMode.value = 'one';
  message.value = '';

  try {
    const [rawResult] = await Promise.all([
      pointWalletApi.openRandomBox(oldestBox.userRandomBoxId),
      waitForOpenAnimation(),
    ]);
    const result = /** @type {OpenResult} */ (rawResult);
    lastOpenResult.value = result;
    messageType.value = 'success';
    message.value = `${formatNumber(result.rewardPoint)}P가 적립되었습니다.`;
    await loadRandomBoxes();
  } catch (error) {
    messageType.value = 'error';
    message.value = getApiErrorMessage(error, '랜덤박스 개봉에 실패했습니다.');
  } finally {
    opening.value = false;
    loading.value = false;
  }
};

const openAll = async () => {
  if (randomBoxes.value.length === 0) { messageType.value = 'error'; message.value = '열 수 있는 랜덤박스가 없습니다.'; return; }

  loading.value = true;
  opening.value = true;
  openingMode.value = 'all';
  message.value = '';

  try {
    const [rawResult] = await Promise.all([
      pointWalletApi.openAllRandomBoxes(),
      waitForOpenAnimation(1250),
    ]);
    const result = /** @type {OpenResult} */ (rawResult);
    lastOpenResult.value = result;
    messageType.value = 'success';
    message.value = result.openedCount > 0 ? `${result.openedCount}개를 열어 총 ${formatNumber(result.totalRewardPoint)}P를 받았습니다.` : '열 수 있는 랜덤박스가 없습니다.';
    await loadRandomBoxes();
  } catch (error) {
    messageType.value = 'error';
    message.value = getApiErrorMessage(error, '랜덤박스 전체 개봉에 실패했습니다.');
  } finally {
    opening.value = false;
    loading.value = false;
  }
};

const initialize = async () => {
  loading.value = true;
  try {
    await loadRandomBoxes();
  } catch (error) {
    messageType.value = 'error';
    message.value = getApiErrorMessage(error, '랜덤박스 정보를 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
};

onMounted(initialize);
</script>

<style scoped>
.random-box-page {
  min-height: 100%;
  background: var(--color-bg-screen);
}

/*noinspection CssUnusedSymbol*/
.random-box-page :deep(.page-header) {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  padding: 0 24px;
  background: var(--color-bg-page);
}

.random-box-content {
  padding: 12px 24px 40px;
}

.box-stage {
  padding: 24px 20px 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 1px solid rgba(255, 188, 46, 0.22);
  background: linear-gradient(180deg, #fffaf0 0%, #fffdf7 100%);
  box-shadow: 0 5px 18px rgba(30, 30, 30, 0.05);
  text-align: center;
}

.gift-visual {
  position: relative;
  width: 104px;
  height: 92px;
  margin: 4px 0 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  color: #f0a000;
  font-size: 64px;
  filter: drop-shadow(0 10px 10px rgba(242, 170, 16, 0.16));
  transform: rotate(-2deg);
}

.gift-visual::after {
  position: absolute;
  right: 16px;
  bottom: 4px;
  left: 16px;
  height: 14px;
  border-radius: 50%;
  background: rgba(242, 170, 16, 0.12);
  filter: blur(6px);
  content: '';
  z-index: -1;
}

.gift-visual.opened {
  transform: none;
}

.spark {
  position: absolute;
  color: var(--color-primary);
  font-size: 18px;
  animation: twinkle 1.4s infinite alternate;
}

.spark-1 {
  top: 2px;
  right: 2px;
}

.spark-2 {
  bottom: 8px;
  left: 2px;
  animation-delay: 0.4s;
}

.box-count-chip {
  padding: 6px 11px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid rgba(255, 188, 46, 0.35);
  border-radius: 999px;
  background: var(--color-bg-page);
  color: #c48200;
  font-size: 13px;
}

.box-stage h2 {
  margin: 12px 0 5px;
  color: var(--color-text-main);
  font-size: 18px;
  font-weight: 600;
  letter-spacing: -0.5px;
}

.box-stage p {
  margin: 0;
  color: var(--color-text-sub);
  font-size: 13px;
  line-height: 1.45;
}

.reward-value {
  margin: 2px 0 5px;
  color: #ef9c00;
}

.box-actions {
  width: 100%;
  margin-top: 18px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.box-actions .content-btn {
  height: 46px;
  gap: 7px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
}

.open-one-button {
  border: 1px solid var(--color-border-main);
  background: var(--color-bg-page);
  color: var(--color-text-main);
}

.open-all-button {
  border: 1px solid var(--color-primary-border);
  background: var(--color-primary);
  color: var(--color-text-main);
}

.box-actions .content-btn:disabled {
  border-color: var(--color-border-main);
  background: var(--color-bg-disabled);
  color: var(--color-text-disabled);
  box-shadow: none;
  cursor: not-allowed;
}

.box-actions .content-btn:disabled i {
  color: var(--color-text-disabled);
}

.guide-section {
  margin-top: 20px;
}

.guide-section .kb-section-title {
  font-size: 18px;
}

.guide-card {
  padding: 4px 16px;
  overflow: hidden;
}

.guide-row {
  width: 100%;
  min-height: 72px;
  padding: 13px 0;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 0;
  border-bottom: 1px solid var(--color-divider);
  background: transparent;
  color: var(--color-text-main);
  font: inherit;
  text-align: left;
}

.guide-icon {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: #fff5d3;
  color: #d89400;
  font-size: 17px;
}

.guide-copy {
  min-width: 0;
  flex: 1;
}

.guide-copy strong,
.guide-copy span {
  display: block;
}

.guide-copy span {
  margin-top: 4px;
  color: var(--color-text-sub);
  line-height: 1.45;
  word-break: keep-all;
}

.policy-row {
  cursor: pointer;
  border-bottom: 0;
}

.policy-arrow {
  flex-shrink: 0;
  color: var(--color-text-muted);
  font-size: 11px;
}

.issue-policy-panel,
.probability-panel {
  margin: 0 -4px 12px;
  padding: 16px;
  border-radius: 14px;
  background: var(--color-bg-screen);
}

.issue-policy-list {
  display: flex;
  flex-direction: column;
}

.issue-policy-item {
  min-height: 58px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-divider);
}

.issue-policy-item:first-child {
  padding-top: 0;
}

.issue-policy-icon {
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  background: #fff5d3;
  color: #d89400;
  font-size: 13px;
}

.issue-policy-copy {
  min-width: 0;
  flex: 1;
}

.issue-policy-copy strong,
.issue-policy-copy span {
  display: block;
}

.issue-policy-copy span {
  margin-top: 4px;
  color: var(--color-text-sub);
  line-height: 1.5;
  word-break: keep-all;
}

.issue-policy-note {
  margin-top: 12px;
  padding: 11px 12px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  border-radius: 10px;
  background: #fff8e6;
  color: var(--color-text-sub);
  line-height: 1.5;
  word-break: keep-all;
}

.issue-policy-note i {
  margin-top: 2px;
  color: #ef9c00;
}

.probability-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.probability-head span {
  color: var(--color-text-sub);
}

.probability-list {
  margin-top: 12px;
  border-top: 1px solid var(--color-divider);
}

.probability-item {
  min-height: 38px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid var(--color-divider);
}

.probability-range {
  color: var(--color-text-main);
}

.probability-rate {
  color: #ef9c00;
}

.probability-note {
  margin-top: 12px;
  padding: 11px 12px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  border-radius: 10px;
  background: #fff8e6;
  color: var(--color-text-sub);
  line-height: 1.45;
}

.probability-note i {
  margin-top: 2px;
  color: #ef9c00;
}

.probability-note strong {
  color: #ef9c00;
}

.box-opening-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 26px;
  background: rgba(17, 17, 17, 0.62);
  backdrop-filter: blur(2px);
}

.opening-gift {
  position: relative;
  width: 180px;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 52px;
  background: #fff1bd;
  color: #f0a000;
  font-size: 96px;
  box-shadow: 0 20px 55px rgba(0, 0, 0, 0.28), inset 0 -5px 0 rgba(242, 170, 16, 0.1);
  animation: box-shake 0.58s ease-in-out, box-open-pop 0.47s ease-out 0.58s;
}

.opening-gift::before,
.opening-gift::after {
  position: absolute;
  border-radius: 50%;
  content: '';
  pointer-events: none;
}

.opening-gift::before {
  inset: -24px;
  border: 3px solid rgba(255, 188, 46, 0.42);
  animation: open-ring 0.72s ease-out 0.45s both;
}

.opening-gift::after {
  inset: -46px;
  z-index: -1;
  background: radial-gradient(circle, rgba(255, 215, 92, 0.32) 0%, rgba(255, 215, 92, 0) 68%);
  animation: open-glow 0.72s ease-out 0.5s both;
}

/*noinspection CssUnusedSymbol*/
.opening-gift.opening-all {
  animation: box-shake-all 0.68s ease-in-out, box-open-pop-all 0.57s ease-out 0.68s;
}

.opening-label {
  color: var(--color-text-white);
}

.burst {
  position: absolute;
  color: var(--color-primary);
  opacity: 0;
  font-size: 22px;
  pointer-events: none;
  animation: burst-out 0.62s ease-out 0.52s both;
}

/*noinspection CssUnusedSymbol*/
.opening-all .burst {
  animation-delay: 0.66s;
}

.burst-1 { top: -8px; left: 50%; --burst-x: -4px; --burst-y: -42px; }
.burst-2 { top: 18%; right: -4px; --burst-x: 38px; --burst-y: -26px; }
.burst-3 { right: 2px; bottom: 12%; --burst-x: 40px; --burst-y: 28px; }
.burst-4 { bottom: -5px; left: 24%; --burst-x: -18px; --burst-y: 42px; }
.burst-5 { top: 25%; left: -6px; --burst-x: -40px; --burst-y: -24px; }
.burst-6 { top: -2px; right: 10%; --burst-x: 30px; --burst-y: -42px; }
.burst-7 { bottom: 4px; left: 8%; --burst-x: -34px; --burst-y: 34px; }

/*noinspection CssUnusedSymbol*/
.policy-slide-enter-active,
.policy-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

/*noinspection CssUnusedSymbol*/
.policy-slide-enter-from,
.policy-slide-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/*noinspection CssUnusedSymbol*/
.opening-overlay-enter-active,
.opening-overlay-leave-active {
  transition: opacity 0.2s ease;
}

/*noinspection CssUnusedSymbol*/
.opening-overlay-enter-from,
.opening-overlay-leave-to {
  opacity: 0;
}

@keyframes twinkle {
  to { transform: scale(1.4); opacity: 0.45; }
}

@keyframes box-shake {
  0%, 100% { transform: rotate(-2deg) translateX(0) scale(1); }
  18% { transform: rotate(-9deg) translateX(-3px) scale(1.02); }
  36% { transform: rotate(9deg) translateX(3px) scale(1.04); }
  54% { transform: rotate(-7deg) translateX(-2px) scale(1.05); }
  72% { transform: rotate(6deg) translateX(2px) scale(1.04); }
  88% { transform: rotate(-3deg) translateX(-1px) scale(1.02); }
}

@keyframes box-open-pop {
  0% { transform: scale(1) rotate(-2deg); }
  45% { transform: scale(1.24) rotate(0); }
  72% { transform: scale(0.96) rotate(0); }
  100% { transform: scale(1) rotate(0); }
}

@keyframes box-shake-all {
  0%, 100% { transform: rotate(-2deg) translateX(0) scale(1); }
  12% { transform: rotate(-12deg) translateX(-4px) scale(1.03); }
  24% { transform: rotate(12deg) translateX(4px) scale(1.05); }
  36% { transform: rotate(-10deg) translateX(-4px) scale(1.07); }
  48% { transform: rotate(10deg) translateX(4px) scale(1.08); }
  60% { transform: rotate(-7deg) translateX(-3px) scale(1.08); }
  72% { transform: rotate(7deg) translateX(3px) scale(1.07); }
  86% { transform: rotate(-3deg) translateX(-1px) scale(1.03); }
}

@keyframes box-open-pop-all {
  0% { transform: scale(1) rotate(-2deg); }
  42% { transform: scale(1.32) rotate(0); }
  70% { transform: scale(0.94) rotate(0); }
  100% { transform: scale(1) rotate(0); }
}

@keyframes open-ring {
  0% { opacity: 0; transform: scale(0.65); }
  45% { opacity: 1; }
  100% { opacity: 0; transform: scale(1.35); }
}

@keyframes open-glow {
  0% { opacity: 0; transform: scale(0.6); }
  45% { opacity: 1; transform: scale(1); }
  100% { opacity: 0; transform: scale(1.35); }
}

@keyframes burst-out {
  0% { opacity: 0; transform: translate(0, 0) scale(0.5) rotate(0deg); }
  35% { opacity: 1; }
  100% { opacity: 0; transform: translate(var(--burst-x), var(--burst-y)) scale(1.35) rotate(30deg); }
}

@media (prefers-reduced-motion: reduce) {
  .spark,
  .opening-gift,
  .opening-gift.opening-all,
  .opening-gift::before,
  .opening-gift::after,
  .burst {
    animation-duration: 0.01ms !important;
    animation-delay: 0ms !important;
  }
}

@media (max-width: 360px) {
  .random-box-content {
    padding-right: 18px;
    padding-left: 18px;
  }

  .box-stage {
    padding: 24px 16px 18px;
  }

  .gift-visual {
    width: 92px;
    height: 82px;
    font-size: 56px;
  }

  .box-actions .content-btn {
    font-size: 14px;
  }
}
</style>