<template>
  <div class="container py-4">
    <div class="d-flex justify-content-between align-items-start mb-4">
      <div>
        <div class="small text-muted mb-1">화면 ID: point-001</div>
        <h2 class="mb-1">포인트 지갑</h2>
        <p class="text-muted mb-0">포인트 잔액, 출석 상태와 최근 이용내역을 확인합니다.</p>
      </div>
      <button type="button" class="btn btn-outline-secondary" :disabled="loading" @click="loadPage">
        새로고침
      </button>
    </div>

    <div v-if="message" :class="['alert', messageType === 'success' ? 'alert-success' : 'alert-danger']">
      {{ message }}
    </div>

    <section class="card mb-4">
      <div class="card-body">
        <div class="d-flex flex-wrap justify-content-between align-items-center gap-3">
          <div>
            <div class="text-muted small">내 포인트</div>
            <div class="display-6 fw-bold">{{ formatNumber(wallet?.pointBalance) }} P</div>
            <div class="small text-muted">최근 수정: {{ wallet?.updatedAt ?? '-' }}</div>
          </div>

          <div class="d-flex gap-2">
            <router-link class="btn btn-warning" to="/point-wallet/conversion">
              포인트 전환
            </router-link>
            <router-link class="btn btn-outline-dark" to="/point-wallet/transactions">
              전체 내역
            </router-link>
          </div>
        </div>

        <hr />

        <div class="d-flex justify-content-between align-items-center">
          <div>
            <div class="fw-semibold">미개봉 랜덤박스 {{ randomBoxCount }}개</div>
            <div class="small text-muted">오래 받은 랜덤박스부터 개봉됩니다.</div>
          </div>
          <router-link class="btn btn-outline-warning" to="/point-wallet/random-box">
            열기
          </router-link>
        </div>
      </div>
    </section>

    <section class="card mb-4">
      <div class="card-header d-flex justify-content-between align-items-center">
        <span class="fw-bold">출석 체크</span>
        <span>{{ currentYear }}년 {{ currentMonth }}월</span>
      </div>
      <div class="card-body">
        <div class="table-responsive mb-3">
          <table class="table table-bordered text-center align-middle mb-0">
            <thead>
              <tr>
                <th v-for="dayName in dayNames" :key="dayName">{{ dayName }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(week, weekIndex) in calendarWeeks" :key="weekIndex">
                <td
                  v-for="(day, dayIndex) in week"
                  :key="`${weekIndex}-${dayIndex}`"
                  :class="{ 'table-warning': day === todayDate }"
                >
                  <template v-if="day">
                    <div>{{ day }}</div>
                    <small v-if="day === todayDate && attendanceStatus?.attendedToday" class="fw-bold">출석 완료</small>
                  </template>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <div>
            <div>{{ attendanceStatus?.message ?? '출석 상태를 조회하고 있습니다.' }}</div>
            <div class="small text-muted">
              출석 보상: {{ formatNumber(attendanceStatus?.rewardPoint) }}P · 랜덤박스
              {{ attendanceStatus?.randomBoxCount ?? 0 }}개
            </div>
          </div>
          <button
            type="button"
            class="btn btn-warning"
            :disabled="attendanceLoading || attendanceStatus?.attendedToday"
            @click="submitAttendance"
          >
            {{ attendanceStatus?.attendedToday ? '출석 완료' : '출석 체크하기' }}
          </button>
        </div>
      </div>
    </section>

    <section class="card">
      <div class="card-header d-flex justify-content-between align-items-center">
        <span class="fw-bold">최근 이용내역</span>
        <router-link to="/point-wallet/transactions">전체 보기</router-link>
      </div>
      <div class="card-body p-0">
        <div v-if="recentTransactions.length" class="list-group list-group-flush">
          <div
            v-for="transaction in recentTransactions"
            :key="transaction.pointTransactionId"
            class="list-group-item d-flex justify-content-between align-items-center"
          >
            <div>
              <div class="fw-semibold">{{ getReasonTypeLabel(transaction.reasonType) }}</div>
              <div class="small text-muted">
                {{ getTransactionTypeLabel(transaction.transactionType) }} · {{ transaction.createdAt }}
              </div>
            </div>
            <div class="fw-bold">
              {{ getPointSign(transaction.transactionType) }}{{ formatNumber(transaction.pointAmount) }}P
            </div>
          </div>
        </div>
        <div v-else class="p-4 text-muted">최근 포인트 이용내역이 없습니다.</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import pointWalletApi from '@/api/pointWalletApi';
import {
  formatNumber,
  getApiErrorMessage,
  getPointSign,
  getReasonTypeLabel,
  getTransactionTypeLabel,
} from '@/util/pointWallet';

const wallet = ref(null);
const attendanceStatus = ref(null);
const randomBoxCount = ref(0);
const transactions = ref([]);
const loading = ref(false);
const attendanceLoading = ref(false);
const message = ref('');
const messageType = ref('success');

const now = new Date();
const currentYear = now.getFullYear();
const currentMonth = now.getMonth() + 1;
const todayDate = now.getDate();
const dayNames = ['일', '월', '화', '수', '목', '금', '토'];

const calendarWeeks = computed(() => {
  const firstDay = new Date(currentYear, currentMonth - 1, 1).getDay();
  const lastDate = new Date(currentYear, currentMonth, 0).getDate();
  const cells = Array(firstDay).fill(null);

  for (let day = 1; day <= lastDate; day += 1) {
    cells.push(day);
  }

  while (cells.length % 7 !== 0) {
    cells.push(null);
  }

  const weeks = [];
  for (let index = 0; index < cells.length; index += 7) {
    weeks.push(cells.slice(index, index + 7));
  }
  return weeks;
});

const recentTransactions = computed(() => transactions.value.slice(0, 3));

const loadPage = async () => {
  loading.value = true;
  message.value = '';

  try {
    const [walletData, attendanceData, countData, recentData] = await Promise.all([
      pointWalletApi.getWallet(),
      pointWalletApi.getTodayAttendanceStatus(),
      pointWalletApi.getUnopenedRandomBoxCount(),
      pointWalletApi.getRecentTransactions(),
    ]);

    wallet.value = walletData;
    attendanceStatus.value = attendanceData;
    randomBoxCount.value = countData.unopenedCount ?? 0;
    transactions.value = recentData;
  } catch (error) {
    messageType.value = 'error';
    message.value = getApiErrorMessage(error, '포인트 지갑 정보를 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
};

const submitAttendance = async () => {
  attendanceLoading.value = true;
  message.value = '';

  try {
    const result = await pointWalletApi.attend();
    await loadPage();
    messageType.value = 'success';
    message.value = result.message;
  } catch (error) {
    messageType.value = 'error';
    message.value = getApiErrorMessage(error, '출석 체크에 실패했습니다.');
  } finally {
    attendanceLoading.value = false;
  }
};

onMounted(loadPage);
</script>
