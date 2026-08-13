<template>
  <div class="page-layout point-wallet-page">
    <PageHeader title="포인트 지갑" :showBack="false" />

    <main class="page-content">
    <section class="balance-card kb-card">
      <div>
        <div class="balance-label text-13-bold">내 포인트</div>

        <div class="balance-value text-28-bold">
          {{ formatNumber(wallet?.pointBalance) }}
          <span>P</span>
        </div>

        <div class="balance-sub text-13">
          현금처럼 전환해서 사용할 수 있어요
        </div>
      </div>

      <div class="point-symbol">P</div>
    </section>

    <!-- 랜덤박스 / 포인트 전환 바로가기 -->
    <section class="quick-card kb-card">
      <router-link to="/point-wallet/random-box" class="quick-item">
        <div class="quick-icon gift">
          <i class="fa-solid fa-gift"></i>
        </div>

        <div>
          <strong class="text-15-bold">랜덤박스</strong>
          <span class="text-13">{{ randomBoxCount }}개</span>
        </div>

        <i class="fa-solid fa-chevron-right quick-arrow"></i>
      </router-link>

      <div class="quick-divider"></div>

      <router-link to="/point-wallet/conversion" class="quick-item">
        <div class="quick-icon exchange">
          <i class="fa-solid fa-wallet"></i>
        </div>

        <div>
          <strong class="text-15-bold">포인트 전환</strong>
        </div>

        <i class="fa-solid fa-chevron-right quick-arrow"></i>
      </router-link>
    </section>

    <section class="kb-section">
      <div class="kb-section-title-row">
        <h2 class="kb-section-title text-18-bold">출석 체크</h2>

        <span class="calendar-month text-13">
          {{ currentYear }}.{{ String(currentMonth).padStart(2, '0') }}
        </span>
      </div>

      <div class="attendance-card kb-card">
        <!-- 요일 -->
        <div class="calendar-grid calendar-head">
          <span v-for="dayName in dayNames" :key="dayName">
            {{ dayName }}
          </span>
        </div>

        <!-- 날짜 -->
        <div class="calendar-grid calendar-body">
          <template v-for="(week, weekIndex) in calendarWeeks" :key="weekIndex">
            <div
              v-for="(day, dayIndex) in week"
              :key="`${weekIndex}-${dayIndex}`"
              :class="['calendar-day', getCalendarDayClass(day)]"
            >
              <template v-if="day">
                <span class="calendar-date-number">
                  {{ day }}
                </span>

                <span v-if="day === todayDate" class="calendar-today-label">
                  오늘
                </span>
              </template>
            </div>
          </template>
        </div>

        <!-- 출석 상태 -->
        <div class="attendance-info">
          <div>
            <strong class="text-15-bold">
              {{
                attendanceStatus?.attendedToday
                  ? '오늘 출석 완료!'
                  : '오늘도 출석하고 보상 받기'
              }}
            </strong>

            <span class="text-13">
              {{ formatNumber(attendanceStatus?.rewardPoint) }}P + 랜덤박스
              {{ attendanceStatus?.randomBoxCount ?? 0 }}개
            </span>
          </div>

          <button
            type="button"
            class="attendance-button text-13-bold"
            :disabled="attendanceLoading || attendanceStatus?.attendedToday"
            @click="submitAttendance"
          >
            {{ attendanceStatus?.attendedToday ? '완료' : '출석' }}
          </button>
        </div>
      </div>
    </section>

    <section class="kb-section">
      <div class="kb-section-title-row">
        <h2 class="kb-section-title text-18-bold">최근 이용내역</h2>

        <router-link
          class="kb-section-link text-13"
          to="/point-wallet/transactions"
        >
          전체 보기
          <i class="fa-solid fa-chevron-right"></i>
        </router-link>
      </div>

      <div class="transaction-card kb-card">
        <!-- 로딩 -->
        <div v-if="loading" class="kb-loading">
          <div class="spinner-border kb-spinner" role="status"></div>

          <div class="text-13">포인트 정보를 불러오는 중이에요.</div>
        </div>

        <!-- 최근 이용내역 -->
        <div v-else-if="recentTransactions.length" class="transaction-list">
          <div
            v-for="transaction in recentTransactions"
            :key="transaction.pointTransactionId"
            class="transaction-row"
          >
            <!-- 아이콘 -->
            <div
              :class="[
                'transaction-icon',
                transaction.transactionType === 'EARN' ? 'earn' : 'use',
              ]"
            >
              <i :class="getTransactionIcon(transaction.reasonType)"></i>
            </div>

            <!-- 거래 정보 -->
            <div class="transaction-content">
              <strong class="text-15-bold">
                {{ getReasonTypeLabel(transaction.reasonType) }}
              </strong>

              <span class="text-13">
                {{ formatDate(transaction.createdAt) }}
                ·
                {{ getTransactionTypeLabel(transaction.transactionType) }}
              </span>
            </div>

            <!-- 포인트 금액 -->
            <div
              :class="[
                'transaction-amount',
                'text-15-bold',
                transaction.transactionType === 'EARN'
                  ? 'kb-amount-positive'
                  : 'kb-amount-negative',
              ]"
            >
              {{ getPointSign(transaction.transactionType)
              }}{{ formatNumber(transaction.pointAmount) }}
            </div>
          </div>
        </div>

        <!-- 빈 상태 -->
        <div v-else class="kb-empty-state">
          <div class="kb-empty-state__icon">
            <i class="fa-solid fa-receipt"></i>
          </div>

          <strong class="text-15-bold"> 최근 포인트 이용내역이 없어요. </strong>
        </div>
      </div>
    </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import PageHeader from '@/components/common/PageHeader.vue';
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
const attendedDateKeys = ref(new Set());
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
  for (let day = 1; day <= lastDate; day += 1) cells.push(day);
  while (cells.length % 7 !== 0) cells.push(null);
  const weeks = [];
  for (let index = 0; index < cells.length; index += 7)
    weeks.push(cells.slice(index, index + 7));
  return weeks;
});

const recentTransactions = computed(() =>
  transactions.value
    .filter((transaction) =>
      ['EARN', 'USE'].includes(transaction.transactionType),
    )
    .slice(0, 5),
);

const toDateKey = (value) => {
  if (!value) return '';
  const normalized = String(value).replace(' ', 'T');
  const date = new Date(normalized);
  if (Number.isNaN(date.getTime())) return String(value).slice(0, 10);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const getCalendarDayClass = (day) => {
  if (!day) return {};

  const dateKey = `${currentYear}-${String(currentMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  const isToday = day === todayDate;
  const isAttended =
    attendedDateKeys.value.has(dateKey) ||
    (isToday && attendanceStatus.value?.attendedToday);

  return {
    today: isToday,
    attended: isAttended,
    'today-attended': isToday && isAttended,
  };
};

const formatDate = (value) => {
  if (!value) return '-';
  const date = new Date(String(value).replace(' ', 'T'));
  if (Number.isNaN(date.getTime())) return value;
  return `${date.getMonth() + 1}.${String(date.getDate()).padStart(2, '0')}`;
};

const getTransactionIcon = (reasonType) =>
  ({
    ATTENDANCE: 'fa-solid fa-calendar-check',
    RANDOM_BOX: 'fa-solid fa-gift',
    CONVERSION: 'fa-solid fa-arrow-right-arrow-left',
    EVENT: 'fa-solid fa-star',
  })[reasonType] ?? 'fa-solid fa-coins';

const loadPage = async () => {
  loading.value = true;
  message.value = '';
  try {
    const [
      walletData,
      attendanceData,
      countData,
      recentData,
      earnedTransactions,
    ] = await Promise.all([
      pointWalletApi.getWallet(),
      pointWalletApi.getTodayAttendanceStatus(),
      pointWalletApi.getUnopenedRandomBoxCount(),
      pointWalletApi.getRecentTransactions(),
      pointWalletApi.getTransactions('EARN'),
    ]);
    wallet.value = walletData;
    attendanceStatus.value = attendanceData;
    randomBoxCount.value = countData.unopenedCount ?? 0;
    transactions.value = recentData ?? [];
    attendedDateKeys.value = new Set(
      (earnedTransactions ?? [])
        .filter((transaction) => transaction.reasonType === 'ATTENDANCE')
        .map((transaction) => toDateKey(transaction.createdAt))
        .filter(Boolean),
    );
  } catch (error) {
    messageType.value = 'error';
    message.value = getApiErrorMessage(
      error,
      '포인트 지갑 정보를 불러오지 못했습니다.',
    );
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

<style scoped>
.point-wallet-page {
  background: var(--color-bg-screen);
}

/* =========================
   포인트 잔액 카드
========================= */

.balance-card {
  margin-top: 0;
  min-height: 126px;
  padding: 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #fff4c6 0%, #fffaf0 100%);
}

.balance-label {
  color: #6c654f;
}

.balance-value {
  margin-top: 2px;
  line-height: 1.2;
  letter-spacing: -1px;
}

.balance-value span {
  margin-left: 3px;
  font-size: 17px;
  font-weight: 800;
}

.balance-sub {
  margin-top: 6px;
  color: #827b68;
}

.point-symbol {
  width: 54px;
  height: 54px;

  display: flex;
  align-items: center;
  justify-content: center;

  border-radius: 50%;
  background: var(--kb-yellow);
  color: #fff;

  font-size: 28px;
  font-weight: 900;

  box-shadow: inset 0 -3px 0 rgba(0, 0, 0, 0.08);
}

/* =========================
   랜덤박스 / 포인트 전환
========================= */

.quick-card {
  margin-top: 12px;

  display: grid;
  grid-template-columns: 1fr 1px 1fr;
  align-items: center;

  padding: 13px 12px;

  box-shadow: 0 2px 12px rgba(30, 30, 30, 0.05);
}

.quick-item {
  min-width: 0;
  padding: 4px 8px;

  display: grid;
  grid-template-columns: 38px 1fr 12px;
  align-items: center;
  gap: 8px;

  color: var(--kb-ink);
  text-decoration: none;
}

.quick-item strong,
.quick-item span {
  display: block;
}

.quick-item span {
  margin-top: 2px;
  color: var(--kb-subtext);
}

.quick-icon {
  width: 36px;
  height: 36px;

  display: flex;
  align-items: center;
  justify-content: center;

  border-radius: 12px;
  font-size: 16px;
}

.quick-icon.gift,
.quick-icon.exchange {
  background: #fff0c2;
  color: #ef9c00;
}

.quick-divider {
  width: 1px;
  height: 34px;
  background: var(--kb-line);
}

.quick-arrow {
  color: #bbb;
  font-size: 10px;
}

/* =========================
   출석 체크
========================= */

.calendar-month {
  color: #777;
}

.attendance-card {
  padding: 16px 14px 14px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.calendar-head {
  margin-bottom: 7px;

  color: #999;
  font-size: 12px;
  text-align: center;
}

.calendar-head span:first-child {
  color: #ec6c6c;
}

.calendar-body {
  row-gap: 3px;
}

.calendar-day {
  position: relative;

  height: 40px;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;

  font-size: 12px;
}

.calendar-date-number {
  width: 27px;
  height: 27px;

  display: inline-flex;
  align-items: center;
  justify-content: center;

  border-radius: 50%;

  color: #222;
  line-height: 1;
}

.calendar-day.attended .calendar-date-number {
  background: var(--kb-yellow);
  color: #222;
  font-weight: 800;
}

.calendar-today-label {
  margin-top: 1px;

  color: #555;

  font-size: 10px;
  font-weight: 700;
  line-height: 1;

  letter-spacing: -0.2px;
}

.calendar-day.today .calendar-date-number {
  font-weight: 800;
}

.attendance-info {
  margin-top: 14px;
  padding-top: 13px;

  display: flex;
  align-items: center;
  justify-content: space-between;

  border-top: 1px solid var(--kb-line);
}

.attendance-info strong,
.attendance-info span {
  display: block;
}

.attendance-info span {
  margin-top: 3px;
  color: var(--kb-subtext);
}

.attendance-button {
  min-width: 62px;
  height: 34px;

  border: 0;
  border-radius: 10px;

  background: var(--kb-yellow);
  color: #222;
}

.attendance-button:disabled {
  background: #ececec;
  color: #999;
}

/* =========================
   최근 이용내역
========================= */

.transaction-card {
  overflow: hidden;
}

.transaction-row {
  min-height: 68px;
  padding: 12px 15px;

  display: flex;
  align-items: center;
  gap: 11px;

  border-bottom: 1px solid #f0f0f0;
}

.transaction-row:last-child {
  border-bottom: 0;
}

.transaction-icon {
  width: 38px;
  height: 38px;
  flex: 0 0 38px;

  display: flex;
  align-items: center;
  justify-content: center;

  border-radius: 12px;

  font-size: 15px;
}

.transaction-icon.earn {
  background: var(--kb-yellow-soft);
  color: #e39a00;
}

.transaction-icon.use {
  background: #f1f2f4;
  color: #555;
}

.transaction-content {
  min-width: 0;
  flex: 1;
}

.transaction-content strong,
.transaction-content span {
  display: block;
}

.transaction-content span {
  margin-top: 3px;
  color: var(--kb-subtext);
}

.transaction-amount {
  white-space: nowrap;
}
</style>
