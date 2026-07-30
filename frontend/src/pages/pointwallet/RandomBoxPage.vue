<template>
  <div class="container py-4">
    <div class="mb-4">
      <div class="small text-muted mb-1">화면 ID: point-002</div>
      <div class="d-flex justify-content-between align-items-center">
        <h2 class="mb-0">랜덤박스</h2>
        <router-link class="btn btn-outline-secondary" to="/point-wallet">돌아가기</router-link>
      </div>
    </div>

    <div v-if="message" :class="['alert', messageType === 'success' ? 'alert-success' : 'alert-danger']">
      {{ message }}
    </div>

    <section class="card text-center mb-4">
      <div class="card-body py-5">
        <div class="display-1 mb-3">🎁</div>
        <h3 v-if="lastOpenResult">축하합니다!</h3>
        <h3 v-else>랜덤박스를 열어보세요</h3>

        <template v-if="lastOpenResult">
          <p class="lead mb-1">총 {{ formatNumber(lastOpenResult.totalRewardPoint ?? lastOpenResult.rewardPoint) }}P를 받았습니다.</p>
          <p class="text-muted mb-0">현재 포인트: {{ formatNumber(lastOpenResult.pointBalance) }}P</p>
        </template>
        <template v-else>
          <p class="text-muted mb-0">미개봉 랜덤박스 {{ randomBoxes.length }}개</p>
        </template>
      </div>
    </section>

    <div class="row g-2 mb-4">
      <div class="col-6">
        <button
          type="button"
          class="btn btn-warning w-100"
          :disabled="loading || randomBoxes.length === 0"
          @click="openOne"
        >
          1개 열기
        </button>
      </div>
      <div class="col-6">
        <button
          type="button"
          class="btn btn-dark w-100"
          :disabled="loading || randomBoxes.length === 0"
          @click="openAll"
        >
          모두 열기
        </button>
      </div>
    </div>

    <section class="card">
      <div class="card-header d-flex justify-content-between">
        <span class="fw-bold">미개봉 랜덤박스</span>
        <span>{{ randomBoxes.length }}개</span>
      </div>
      <div class="card-body">
        <p class="mb-2">1개 열기는 가장 오래전에 받은 랜덤박스를 개봉합니다.</p>
        <div v-if="randomBoxes.length" class="small text-muted">
          다음 개봉 대상: {{ randomBoxes[0].issuedAt }} 지급 · {{ getIssueReasonLabel(randomBoxes[0].issueReason) }}
        </div>
        <div v-else class="text-muted">열 수 있는 랜덤박스가 없습니다.</div>
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

const issueReasonLabels = {
  ATTENDANCE: '출석 체크',
  EVENT: '이벤트',
  FEED_SHARE: '피드 공유',
  TRANSFER: '송금',
};

const getIssueReasonLabel = (reason) => issueReasonLabels[reason] ?? reason ?? '-';

const loadRandomBoxes = async () => {
  randomBoxes.value = await pointWalletApi.getUnopenedRandomBoxes();
};

const openOne = async () => {
  const oldestBox = randomBoxes.value[0];

  if (!oldestBox) {
    messageType.value = 'error';
    message.value = '열 수 있는 랜덤박스가 없습니다.';
    return;
  }

  loading.value = true;
  message.value = '';

  try {
    const result = await pointWalletApi.openRandomBox(oldestBox.userRandomBoxId);
    lastOpenResult.value = result;
    messageType.value = 'success';
    message.value = `랜덤박스 1개를 열어 ${formatNumber(result.rewardPoint)}P를 받았습니다.`;
    await loadRandomBoxes();
  } catch (error) {
    messageType.value = 'error';
    message.value = getApiErrorMessage(error, '랜덤박스 개봉에 실패했습니다.');
  } finally {
    loading.value = false;
  }
};

const openAll = async () => {
  loading.value = true;
  message.value = '';

  try {
    const result = await pointWalletApi.openAllRandomBoxes();
    lastOpenResult.value = result;
    messageType.value = 'success';
    message.value = result.openedCount > 0
      ? `${result.openedCount}개의 랜덤박스를 열어 총 ${formatNumber(result.totalRewardPoint)}P를 받았습니다.`
      : '열 수 있는 랜덤박스가 없습니다.';
    await loadRandomBoxes();
  } catch (error) {
    messageType.value = 'error';
    message.value = getApiErrorMessage(error, '랜덤박스 전체 개봉에 실패했습니다.');
  } finally {
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
