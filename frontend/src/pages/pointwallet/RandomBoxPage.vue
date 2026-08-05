<template>
  <div class="kb-mobile-page random-box-page">
    <header class="kb-app-header">
      <router-link class="kb-icon-button" to="/point-wallet" aria-label="뒤로가기"><i class="fa-solid fa-chevron-left"></i></router-link>
      <h1 class="kb-app-header__title">랜덤박스</h1>
      <span></span>
    </header>

    <div v-if="message" :class="['kb-toast', messageType === 'success' ? 'kb-toast--success' : 'kb-toast--error']">{{ message }}</div>

    <section class="box-stage kb-card">
      <div :class="['gift-visual', { opened: lastOpenResult }]">
        <i :class="lastOpenResult ? 'fa-solid fa-box-open' : 'fa-solid fa-gift'"></i>
        <span v-if="!lastOpenResult" class="spark spark-1">✦</span>
        <span v-if="!lastOpenResult" class="spark spark-2">✦</span>
      </div>

      <template v-if="lastOpenResult">
        <div class="eyebrow">랜덤박스 결과</div>
        <h2>축하해요!</h2>
        <div class="reward-value">{{ formatNumber(lastOpenResult.totalRewardPoint ?? lastOpenResult.rewardPoint) }}P</div>
        <p>포인트가 바로 적립되었어요.</p>
      </template>
      <template v-else>
        <div class="eyebrow">보유 랜덤박스 {{ randomBoxes.length }}개</div>
        <h2>랜덤박스를 열어보세요</h2>
        <p>두근두근, 어떤 포인트가 나올까요?</p>
      </template>
    </section>

    <div class="box-actions">
      <button type="button" class="kb-primary-button" :disabled="loading || randomBoxes.length === 0" @click="openOne">
        {{ loading ? '개봉 중...' : '1개 열기' }}
      </button>
      <button type="button" class="kb-secondary-button" :disabled="loading || randomBoxes.length === 0" @click="openAll">
        모두 열기
      </button>
    </div>

    <section class="kb-section">
      <div class="kb-section-title-row"><h2 class="kb-section-title">랜덤박스 안내</h2><span class="box-count">{{ randomBoxes.length }}개 보유</span></div>
      <div class="guide-card kb-card">
        <div class="guide-row"><div class="guide-icon"><i class="fa-solid fa-clock-rotate-left"></i></div><div><strong>오래 받은 박스부터</strong><span>1개 열기 시 가장 오래된 박스가 열려요.</span></div></div>
        <div class="guide-row"><div class="guide-icon"><i class="fa-solid fa-coins"></i></div><div><strong>포인트 즉시 적립</strong><span>당첨 포인트는 포인트 지갑에 바로 반영돼요.</span></div></div>
        <div v-if="randomBoxes.length" class="next-box">다음 박스 · {{ getIssueReasonLabel(randomBoxes[0].issueReason) }} / {{ formatDate(randomBoxes[0].issuedAt) }}</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import pointWalletApi from '@/api/pointWalletApi';
import { formatNumber, getApiErrorMessage } from '@/util/pointWallet';

const randomBoxes = ref([]);
const lastOpenResult = ref(null);
const loading = ref(false);
const message = ref('');
const messageType = ref('success');
const issueReasonLabels = { ATTENDANCE: '출석 체크', EVENT: '이벤트', FEED_SHARE: '피드 공유', TRANSFER: '송금' };
const getIssueReasonLabel = (reason) => issueReasonLabels[reason] ?? reason ?? '-';
const formatDate = (value) => value ? String(value).slice(0, 10) : '-';
const loadRandomBoxes = async () => { randomBoxes.value = await pointWalletApi.getUnopenedRandomBoxes(); };

const openOne = async () => {
  const oldestBox = randomBoxes.value[0];
  if (!oldestBox) { messageType.value = 'error'; message.value = '열 수 있는 랜덤박스가 없습니다.'; return; }
  loading.value = true; message.value = '';
  try {
    const result = await pointWalletApi.openRandomBox(oldestBox.userRandomBoxId);
    lastOpenResult.value = result; messageType.value = 'success'; message.value = `${formatNumber(result.rewardPoint)}P가 적립되었습니다.`;
    await loadRandomBoxes();
  } catch (error) { messageType.value = 'error'; message.value = getApiErrorMessage(error, '랜덤박스 개봉에 실패했습니다.'); }
  finally { loading.value = false; }
};

const openAll = async () => {
  loading.value = true; message.value = '';
  try {
    const result = await pointWalletApi.openAllRandomBoxes();
    lastOpenResult.value = result; messageType.value = 'success';
    message.value = result.openedCount > 0 ? `${result.openedCount}개를 열어 총 ${formatNumber(result.totalRewardPoint)}P를 받았습니다.` : '열 수 있는 랜덤박스가 없습니다.';
    await loadRandomBoxes();
  } catch (error) { messageType.value = 'error'; message.value = getApiErrorMessage(error, '랜덤박스 전체 개봉에 실패했습니다.'); }
  finally { loading.value = false; }
};

const initialize = async () => { loading.value = true; try { await loadRandomBoxes(); } catch (error) { messageType.value = 'error'; message.value = getApiErrorMessage(error, '랜덤박스 정보를 불러오지 못했습니다.'); } finally { loading.value = false; } };
onMounted(initialize);
</script>

<style scoped>
.random-box-page { background: #fff; }
.box-stage { min-height: 365px; padding: 34px 24px; display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center; background: linear-gradient(180deg, #fff 0%, #fffaf0 100%); box-shadow: none; border: 1px solid #f2f2f2; }
.gift-visual { position: relative; width: 126px; height: 126px; margin-bottom: 18px; display: flex; align-items: center; justify-content: center; border-radius: 40px; background: #fff4c9; color: #f4aa00; font-size: 66px; transform: rotate(-2deg); }
.gift-visual.opened { background: #fff1b9; transform: none; }
.spark { position: absolute; color: var(--kb-yellow); font-size: 18px; animation: twinkle 1.4s infinite alternate; }
.spark-1 { top: 9px; right: 8px; }.spark-2 { left: 0; bottom: 18px; animation-delay: .4s; }
@keyframes twinkle { to { transform: scale(1.4); opacity: .45; } }
.eyebrow { color: #a87900; font-size: 12px; font-weight: 800; }
.box-stage h2 { margin: 7px 0 6px; font-size: 22px; font-weight: 900; letter-spacing: -.7px; }
.box-stage p { margin: 0; color: var(--kb-subtext); font-size: 12px; }
.reward-value { margin: 2px 0 5px; font-size: 34px; font-weight: 900; }
.box-actions { margin-top: 14px; display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.box-count { color: #a87900; font-size: 12px; font-weight: 700; }
.guide-card { padding: 6px 16px 14px; box-shadow: none; border: 1px solid #f0f0f0; }
.guide-row { padding: 13px 0; display: flex; align-items: center; gap: 12px; border-bottom: 1px solid #f2f2f2; }
.guide-icon { width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; border-radius: 12px; background: var(--kb-yellow-soft); color: #d89400; }
.guide-row strong,.guide-row span { display: block; }.guide-row strong { font-size: 13px; }.guide-row span { margin-top: 2px; color: var(--kb-subtext); font-size: 10px; }
.next-box { margin-top: 12px; padding: 10px 12px; border-radius: 10px; background: #f7f7f7; color: #777; font-size: 10px; }
</style>
