<template>
  <div class="page-layout random-box-page">
    <PageHeader title="랜덤박스" />

    <main class="page-content">
      <section class="box-stage kb-card">
      <div
        :class="[
          'gift-visual',
          {
            opened: lastOpenResult && !opening,
            opening,
            'opening-all': opening && openingMode === 'all',
          },
        ]"
      >
        <i :class="lastOpenResult && !opening ? 'fa-solid fa-box-open' : 'fa-solid fa-gift'"></i>

        <span v-if="!lastOpenResult && !opening" class="spark spark-1">✦</span>
        <span v-if="!lastOpenResult && !opening" class="spark spark-2">✦</span>

        <template v-if="opening">
          <span class="burst burst-1">✦</span>
          <span class="burst burst-2">✦</span>
          <span class="burst burst-3">✦</span>
          <span class="burst burst-4">✦</span>
          <span class="burst burst-5">✦</span>
          <span v-if="openingMode === 'all'" class="burst burst-6">✦</span>
          <span v-if="openingMode === 'all'" class="burst burst-7">✦</span>
        </template>
      </div>

      <template v-if="lastOpenResult && !opening">
        <div class="eyebrow text-13-bold">보유 랜덤박스 {{ randomBoxes.length }}개</div>
        <h2 class="text-20-bold">축하해요!</h2>
        <div class="reward-value text-28-bold">{{ formatNumber(lastOpenResult.totalRewardPoint ?? lastOpenResult.rewardPoint) }}P</div>
        <p class="text-13">포인트가 바로 적립되었어요.</p>
      </template>
      <template v-else>
        <div class="eyebrow text-13-bold">보유 랜덤박스 {{ randomBoxes.length }}개</div>
        <h2 class="text-20-bold">랜덤박스를 열어보세요</h2>
        <p class="text-13">두근두근, 어떤 포인트가 나올까요?</p>
      </template>
      </section>

      <div class="box-actions">
      <button
        type="button"
        class="content-btn primary"
        :disabled="loading || randomBoxes.length === 0"
        @click="openOne"
      >
        {{ loading ? '개봉 중...' : (randomBoxes.length === 0 ? '모두 열었어요' : '1개 열기') }}
      </button>
      <button type="button" class="content-btn secondary" :disabled="loading || randomBoxes.length === 0" @click="openAll">
        모두 열기
      </button>
      </div>

      <section class="kb-section">
      <div class="kb-section-title-row">
        <h2 class="kb-section-title text-18-bold">랜덤박스 안내</h2>
        <span class="box-count text-13-bold">{{ randomBoxes.length }}개 보유</span>
      </div>
      <div class="guide-card kb-card">
        <div class="guide-row">
          <div class="guide-icon"><i class="fa-solid fa-clock-rotate-left"></i></div>
          <div>
            <strong class="text-15-bold">오래 받은 박스부터</strong>
            <span class="text-13">1개 열기 시 가장 오래된 박스가 열려요.</span>
          </div>
        </div>
        <div class="guide-row">
          <div class="guide-icon"><i class="fa-solid fa-coins"></i></div>
          <div>
            <strong class="text-15-bold">포인트 즉시 적립</strong>
            <span class="text-13">당첨 포인트는 포인트 지갑에 바로 반영돼요.</span>
          </div>
        </div>

        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import PageHeader from '@/components/common/PageHeader.vue';
import pointWalletApi from '@/api/pointWalletApi';
import { formatNumber, getApiErrorMessage } from '@/util/pointWallet';

const randomBoxes = ref([]);
const lastOpenResult = ref(null);
const loading = ref(false);
const opening = ref(false);
const openingMode = ref('one');
const message = ref('');
const messageType = ref('success');
const issueReasonLabels = { PAYMENT: '결제', ATTENDANCE: '출석 체크', EVENT: '이벤트', FEED_SHARE: '피드 공유', TRANSFER: '송금' };
const getIssueReasonLabel = (reason) => issueReasonLabels[reason] ?? reason ?? '-';
const formatDate = (value) => value ? String(value).slice(0, 10) : '-';
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
    const [result] = await Promise.all([
      pointWalletApi.openRandomBox(oldestBox.userRandomBoxId),
      waitForOpenAnimation(),
    ]);

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
  if (randomBoxes.value.length === 0) {
    messageType.value = 'error';
    message.value = '열 수 있는 랜덤박스가 없습니다.';
    return;
  }

  loading.value = true;
  opening.value = true;
  openingMode.value = 'all';
  message.value = '';

  try {
    const [result] = await Promise.all([
      pointWalletApi.openAllRandomBoxes(),
      waitForOpenAnimation(1250),
    ]);

    lastOpenResult.value = result;
    messageType.value = 'success';
    message.value = result.openedCount > 0
      ? `${result.openedCount}개를 열어 총 ${formatNumber(result.totalRewardPoint)}P를 받았습니다.`
      : '열 수 있는 랜덤박스가 없습니다.';
    await loadRandomBoxes();
  } catch (error) {
    messageType.value = 'error';
    message.value = getApiErrorMessage(error, '랜덤박스 전체 개봉에 실패했습니다.');
  } finally {
    opening.value = false;
    loading.value = false;
  }
};

const initialize = async () => { loading.value = true; try { await loadRandomBoxes(); } catch (error) { messageType.value = 'error'; message.value = getApiErrorMessage(error, '랜덤박스 정보를 불러오지 못했습니다.'); } finally { loading.value = false; } };
onMounted(initialize);
</script>

<style scoped>



.random-box-page { background: var(--color-bg-screen); }
.box-stage { margin-top: 14px; min-height: 365px; padding: 34px 24px; display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center; background: linear-gradient(180deg, var(--color-bg-page) 0%, #fffaf0 100%); box-shadow: none; border: 1px solid var(--color-divider); }
.gift-visual { position: relative; width: 126px; height: 126px; margin-bottom: 18px; display: flex; align-items: center; justify-content: center; border-radius: 40px; background: #fff4c9; color: #f4aa00; font-size: 66px; transform: rotate(-2deg); }
.gift-visual.opened { background: #fff1b9; transform: none; }
.gift-visual.opening {
  z-index: 1;
  animation: box-shake .58s ease-in-out, box-open-pop .47s ease-out .58s;
}

.gift-visual.opening::before,
.gift-visual.opening::after {
  position: absolute;
  content: '';
  pointer-events: none;
  border-radius: 50%;
}

.gift-visual.opening::before {
  inset: -18px;
  border: 3px solid rgba(255, 188, 46, .34);
  animation: open-ring .72s ease-out .45s both;
}

.gift-visual.opening::after {
  inset: -34px;
  background: radial-gradient(circle, rgba(255, 215, 92, .26) 0%, rgba(255, 215, 92, 0) 68%);
  animation: open-glow .72s ease-out .5s both;
  z-index: -1;
}

.gift-visual.opening-all {
  animation: box-shake-all .68s ease-in-out, box-open-pop-all .57s ease-out .68s;
}

.burst {
  position: absolute;
  color: var(--color-primary);
  opacity: 0;
  font-size: 18px;
  pointer-events: none;
  animation: burst-out .62s ease-out .52s both;
}

.opening-all .burst {
  animation-delay: .66s;
}

.burst-1 { top: -8px; left: 50%; --burst-x: -4px; --burst-y: -34px; }
.burst-2 { top: 18%; right: -4px; --burst-x: 30px; --burst-y: -20px; }
.burst-3 { right: 2px; bottom: 12%; --burst-x: 32px; --burst-y: 22px; }
.burst-4 { bottom: -5px; left: 24%; --burst-x: -14px; --burst-y: 34px; }
.burst-5 { top: 25%; left: -6px; --burst-x: -32px; --burst-y: -18px; }
.burst-6 { top: -2px; right: 10%; --burst-x: 24px; --burst-y: -34px; }
.burst-7 { bottom: 4px; left: 8%; --burst-x: -28px; --burst-y: 28px; }

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
  72% { transform: scale(.96) rotate(0); }
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
  70% { transform: scale(.94) rotate(0); }
  100% { transform: scale(1) rotate(0); }
}

@keyframes open-ring {
  0% { opacity: 0; transform: scale(.65); }
  45% { opacity: 1; }
  100% { opacity: 0; transform: scale(1.35); }
}

@keyframes open-glow {
  0% { opacity: 0; transform: scale(.6); }
  45% { opacity: 1; transform: scale(1); }
  100% { opacity: 0; transform: scale(1.35); }
}

@keyframes burst-out {
  0% { opacity: 0; transform: translate(0, 0) scale(.5) rotate(0deg); }
  35% { opacity: 1; }
  100% { opacity: 0; transform: translate(var(--burst-x), var(--burst-y)) scale(1.35) rotate(30deg); }
}

@media (prefers-reduced-motion: reduce) {
  .gift-visual.opening,
  .gift-visual.opening-all,
  .gift-visual.opening::before,
  .gift-visual.opening::after,
  .burst {
    animation-duration: .01ms !important;
    animation-delay: 0ms !important;
  }
}
.spark { position: absolute; color: var(--color-primary); font-size: 18px; animation: twinkle 1.4s infinite alternate; }
.spark-1 { top: 9px; right: 8px; }.spark-2 { left: 0; bottom: 18px; animation-delay: .4s; }
@keyframes twinkle { to { transform: scale(1.4); opacity: .45; } }
.eyebrow { color: var(--color-primary-border); }
.box-stage h2 { margin: 7px 0 6px; letter-spacing: -.7px; }
.box-stage p { margin: 0; color: var(--color-text-sub); }
.reward-value { margin: 2px 0 5px; }
.box-actions { margin-top: 14px; display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.box-count { color: var(--color-primary-border); }
.guide-card { padding: 6px 16px 14px; box-shadow: none; border: 1px solid var(--color-divider); }
.guide-row { padding: 13px 0; display: flex; align-items: center; gap: 12px; border-bottom: 1px solid var(--color-divider); }
.guide-icon { width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; border-radius: 12px; background: color-mix(in srgb, var(--color-primary) 18%, var(--color-bg-page)); color: var(--color-primary-border); }
.guide-row strong,.guide-row span { display: block; }.guide-row span { margin-top: 2px; color: var(--color-text-sub); }





</style>
