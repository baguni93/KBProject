<template>
  <main class="point-wallet-page">
    <PageHeader title="포인트 지갑" :showBack="true" :customBack="true" @back="goToFinance" />

    <div class="point-wallet-content">
      <section class="balance-card kb-card">
        <div class="balance-info">
          <div class="balance-label text-13-bold">내 포인트</div>

          <div class="balance-value text-28-bold">
            {{formatNumber(wallet?.pointBalance)}}<span class="text-18-bold">P</span>
          </div>

          <div class="balance-sub text-13">현금처럼 전환해서 사용할 수 있어요</div>
        </div>

        <div class="point-symbol">P</div>
      </section>

      <section class="quick-card kb-card">
        <router-link to="/point-wallet/random-box" class="quick-item">
          <div class="quick-icon gift">
            <i class="fa-solid fa-gift"></i>
          </div>

          <div class="quick-text">
            <strong class="text-15-bold">랜덤박스</strong>
            <span class="text-13-bold">{{ randomBoxCount }}개</span>
          </div>

          <i class="fa-solid fa-chevron-right quick-arrow"></i>
        </router-link>

        <div class="quick-divider"></div>

        <router-link to="/point-wallet/conversion" class="quick-item">
          <div class="quick-icon exchange">
            <i class="fa-solid fa-wallet"></i>
          </div>

          <div class="quick-text">
            <strong class="text-15-bold">포인트 전환</strong>
          </div>

          <i class="fa-solid fa-chevron-right quick-arrow"></i>
        </router-link>
      </section>

      <section class="kb-section attendance-section">
        <div class="kb-section-title-row attendance-title-row">
          <h2 class="kb-section-title text-20-bold">출석 체크</h2>

          <button type="button" class="calendar-month-button text-13-bold" @click="isCalendarExpanded = !isCalendarExpanded">
            {{ currentYear }}.{{ String(currentMonth).padStart(2, '0') }}
            <i :class="['fa-solid', isCalendarExpanded ? 'fa-chevron-up' : 'fa-chevron-down']"></i>
          </button>
        </div>

        <div class="attendance-card kb-card">
          <div class="calendar-grid calendar-head">
            <span v-for="dayName in dayNames" :key="dayName">{{ dayName }}</span>
          </div>

          <div class="calendar-grid calendar-body">
            <template v-for="(week, weekIndex) in visibleCalendarWeeks" :key="weekIndex">
              <div v-for="(day, dayIndex) in week" :key="`${weekIndex}-${dayIndex}`" :class="['calendar-day', getCalendarDayClass(day)]">
                <template v-if="day">
                  <span class="calendar-date-number">{{ day }}</span>
                  <span v-if="day === todayDate" class="calendar-today-label">오늘</span>
                </template>
              </div>
            </template>
          </div>

          <div :class="['attendance-info', { completed: attendanceStatus?.attendedToday }]">
            <div class="attendance-copy">
              <div class="attendance-copy-icon">
                <i :class="['fa-solid', attendanceStatus?.attendedToday ? 'fa-circle-check' : 'fa-calendar-check']"></i>
              </div>

              <div>
                <strong class="text-15-bold">{{ attendanceStatus?.attendedToday ? '오늘 출석 완료' : '오늘 출석하고 보상 받기' }}</strong>
                <span class="text-13">
                    {{ attendanceStatus?.attendedToday ? `${formatNumber(attendanceStatus?.rewardPoint)}P + 랜덤박스 ${attendanceStatus?.randomBoxCount ?? 0}개를 받았어요` : `${formatNumber(attendanceStatus?.rewardPoint)}P + 랜덤박스 ${attendanceStatus?.randomBoxCount ?? 0}개` }}
                  </span>
              </div>
            </div>

            <div v-if="attendanceStatus?.attendedToday" class="attendance-complete text-13-bold">
              <i class="fa-solid fa-check"></i>
              완료
            </div>

            <button v-else type="button" class="attendance-button text-13-bold" :disabled="attendanceLoading" @click="submitAttendance">
              출석하기
            </button>
          </div>
        </div>
      </section>

      <section class="kb-section recent-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title text-20-bold">최근 이용내역</h2>

          <router-link class="kb-section-link text-13-bold" to="/point-wallet/transactions">
            전체 보기
            <i class="fa-solid fa-chevron-right"></i>
          </router-link>
        </div>

        <PointTransactionList :transactions="recentTransactions" :loading="loading" loading-text="포인트 정보를 불러오는 중이에요." empty-text="최근 포인트 이용내역이 없어요." />
      </section>
    </div>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import PointTransactionList from '@/components/common/PointTransactionList.vue';
import pointWalletApi from '@/api/pointWalletApi';
import { formatNumber, getApiErrorMessage } from '@/util/pointWallet';

const router = useRouter();
const wallet = ref(null);
const attendanceStatus = ref(null);
const randomBoxCount = ref(0);
const transactions = ref([]);
const attendedDateKeys = ref(new Set());
const loading = ref(false);
const attendanceLoading = ref(false);
const message = ref('');
const messageType = ref('success');
const isCalendarExpanded = ref(false);

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
  for (let index = 0; index < cells.length; index += 7) weeks.push(cells.slice(index, index + 7));
  return weeks;
});

const currentWeekIndex = computed(() => calendarWeeks.value.findIndex((week) => week.includes(todayDate)));
const visibleCalendarWeeks = computed(() => isCalendarExpanded.value ? calendarWeeks.value : [calendarWeeks.value[currentWeekIndex.value] ?? calendarWeeks.value[0]]);

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

const recentTransactions = computed(() => transactions.value.filter((transaction) => ['EARN', 'USE'].includes(transaction.transactionType)).slice(0, 5).map((transaction) => ({ ...transaction, createdAt: toDateKey(transaction.createdAt) })));

const getCalendarDayClass = (day) => {
  if (!day) return {};
  const dateKey = `${currentYear}-${String(currentMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  const isToday = day === todayDate;
  const isAttended = attendedDateKeys.value.has(dateKey) || (isToday && attendanceStatus.value?.attendedToday);
  return { today: isToday, attended: isAttended, 'today-attended': isToday && isAttended };
};

const goToFinance = () => router.push('/finance');

const loadPage = async () => {
  loading.value = true;
  message.value = '';
  try {
    const [walletData, attendanceData, countData, recentData, earnedTransactions] = await Promise.all([pointWalletApi.getWallet(), pointWalletApi.getTodayAttendanceStatus(), pointWalletApi.getUnopenedRandomBoxCount(), pointWalletApi.getRecentTransactions(), pointWalletApi.getTransactions('EARN')]);
    wallet.value = walletData;
    attendanceStatus.value = attendanceData;
    randomBoxCount.value = countData.unopenedCount ?? 0;
    transactions.value = recentData ?? [];
    attendedDateKeys.value = new Set((earnedTransactions ?? []).filter((transaction) => transaction.reasonType === 'ATTENDANCE').map((transaction) => toDateKey(transaction.createdAt)).filter(Boolean));
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

<style scoped>
.point-wallet-page {
  min-height: 100%;
  background: var(--color-bg-screen);
}

.point-wallet-page :deep(.page-header) {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  padding: 0 24px;
  background: var(--color-bg-page);
}

.point-wallet-content {
  padding: 12px 24px 40px;
}

.balance-card {
  min-height: 132px;
  padding: 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border: 1px solid rgba(255, 188, 46, 0.22);
  background: linear-gradient(135deg, #fff4c6 0%, #fffaf0 100%);
  box-shadow: 0 5px 18px rgba(30, 30, 30, 0.05);
}

.balance-info {
  min-width: 0;
}

.balance-label {
  color: #6c654f;
}

.balance-value {
  margin-top: 6px;
  color: var(--color-text-main);
  line-height: 1.15;
  letter-spacing: -1px;
}

.balance-value span {
  margin-left: 3px;
  font-size: 17px;
  font-weight: 600;
}

.balance-sub {
  margin-top: 8px;
  color: var(--color-text-sub);
  line-height: 1.45;
  word-break: keep-all;
}

.point-symbol {
  width: 60px;
  height: 60px;
  flex: 0 0 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-text-white);
  font-size: 30px;
  font-weight: 600;
  box-shadow: inset 0 -3px 0 rgba(0, 0, 0, 0.08), 0 6px 14px rgba(242, 170, 16, 0.18);
  animation: point-float 3.2s ease-in-out infinite;
}

.quick-card {
  margin-top: 12px;
  display: grid;
  grid-template-columns: 1fr 1px 1fr;
  align-items: center;
  padding: 12px 10px;
  box-shadow: 0 5px 18px rgba(30, 30, 30, 0.05);
}

.quick-item {
  min-width: 0;
  padding: 3px 8px;
  display: grid;
  grid-template-columns: 36px 1fr 10px;
  align-items: center;
  gap: 8px;
  color: var(--kb-ink);
  text-decoration: none;
}

.quick-text {
  min-width: 0;
  display: flex;
  align-items: baseline;
  gap: 5px;
  white-space: nowrap;
}

.quick-text strong,
.quick-text span {
  display: inline;
}

.quick-text span {
  color: var(--kb-subtext);
}

.quick-icon {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 11px;
  font-size: 15px;
}

.quick-icon.gift,
.quick-icon.exchange {
  background: #fff0c2;
  color: #ef9c00;
}

.quick-divider {
  width: 1px;
  height: 32px;
  background: var(--kb-line);
}

.quick-arrow {
  color: #bbb;
  font-size: 10px;
}

.attendance-section {
  margin-top: 22px;
}

.attendance-title-row {
  margin-bottom: 10px;
}

.calendar-month-button {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--color-text-sub);
  cursor: pointer;
}

.calendar-month-button i {
  font-size: 10px;
}

.attendance-card {
  padding: 16px 14px 14px;
  box-shadow: 0 5px 18px rgba(30, 30, 30, 0.05);
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.calendar-head {
  margin-bottom: 8px;
  color: var(--color-text-muted);
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
  height: 44px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  font-size: 12px;
}

.calendar-date-number {
  width: 29px;
  height: 29px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: var(--color-text-main);
  line-height: 1;
}

.calendar-day.attended .calendar-date-number {
  position: relative;
  background: #fff0b8;
  color: #b97800;
  font-weight: 600;
}

.calendar-day.attended:not(.today) .calendar-date-number::after {
  content: '✓';
  position: absolute;
  right: -2px;
  bottom: -2px;
  width: 13px;
  height: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--kb-yellow);
  color: var(--color-text-main);
  font-size: 8px;
  font-weight: 600;
}

.calendar-day.today .calendar-date-number {
  background: var(--kb-yellow);
  color: var(--color-text-main);
  font-weight: 600;
}

.calendar-today-label {
  margin-top: 2px;
  color: #b97800;
  font-size: 10px;
  font-weight: 600;
  line-height: 1;
  letter-spacing: -0.2px;
}


.attendance-info {
  margin-top: 10px;
  padding-top: 13px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-top: 1px solid var(--kb-line);
}

.attendance-copy {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.attendance-copy-icon {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 11px;
  background: #fff6d8;
  color: #ef9c00;
}

.attendance-copy strong,
.attendance-copy span {
  display: block;
}

.attendance-copy span {
  margin-top: 3px;
  color: var(--kb-subtext);
}

.attendance-button {
  min-width: 78px;
  height: 38px;
  flex-shrink: 0;
  border: 0;
  border-radius: 11px;
  background: var(--kb-yellow);
  color: var(--color-text-main);
  cursor: pointer;
}

.attendance-button:disabled {
  background: var(--color-bg-disabled);
  color: var(--color-text-disabled);
  cursor: not-allowed;
}

.attendance-complete {
  min-width: 70px;
  height: 36px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  border-radius: 11px;
  background: #fff6d8;
  color: #b97800;
}

.recent-section {
  margin-top: 22px;
  padding-bottom: 8px;
}

.kb-section-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}
@keyframes point-float {
  0%, 18%, 100% { transform: translateY(0) scale(1); }
  7% { transform: translateY(-4px) scale(1.03); }
  12% { transform: translateY(1px) scale(0.99); }
}

@media (prefers-reduced-motion: reduce) {
  .point-symbol {
    animation: none;
  }
}

@media (max-width: 360px) {
  .balance-card {
    min-height: 122px;
    padding: 18px;
  }

  .point-symbol {
    width: 52px;
    height: 52px;
    flex-basis: 52px;
    font-size: 26px;
  }
}

</style>