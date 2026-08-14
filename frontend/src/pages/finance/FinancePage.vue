<template>
  <div class="page-layout finance-page">
    <PageHeader title="금융" :showBack="false" />

    <main class="page-content">
    <section class="finance-intro">
      <p class="intro-eyebrow text-13-bold">나의 맞춤형 금융 생활</p>
      <h1 class="intro-title text-20-bold">
        이벤트부터 소비분석까지 <br />
        한눈에 확인해요
      </h1>
    </section>

    <!-- 이벤트 -->
    <section class="event-section">
      <button type="button" class="event-banner" @click="goTo('/event')">
        <div class="event-banner__content">
          <span class="event-chip text-13-bold">EVENT</span>
          <strong class="text-15-bold">이벤트 참여하고 혜택 받아보세요</strong>
          <span class="text-13">
            진행 중인 이벤트와 참여 혜택을 확인할 수 있어요.
          </span>
        </div>

        <div class="event-banner__icon" aria-hidden="true">
          <i class="fa-solid fa-gift"></i>
        </div>
      </button>
    </section>

    <!-- PointWalletPage의 잔액 카드와 동일한 UI -->
    <button
      type="button"
      class="balance-card kb-card"
      aria-label="포인트 지갑으로 이동"
      @click="goTo('/point-wallet')"
    >
      <div class="balance-content">
        <div class="balance-label text-13-bold">내 포인트</div>

        <div class="balance-value text-28-bold">
          <template v-if="loading">-</template>
          <template v-else>{{ formatNumber(pointBalance) }}</template>
          <span>P</span>
        </div>

        <div class="balance-sub text-13">
          현금처럼 전환해서 사용할 수 있어요
        </div>
      </div>

      <div class="point-symbol" aria-hidden="true">P</div>
    </button>

    <!-- 금융생활 바로가기 -->
    <section class="kb-section finance-service-section">
      <div class="section-heading">
        <h2 class="text-18-bold">금융생활+</h2>
        <p class="text-13">자주 사용하는 금융 기능을 바로 이용해보세요.</p>
      </div>

      <div class="service-grid">
        <button
          v-for="service in services"
          :key="service.title"
          type="button"
          class="service-card kb-card"
          @click="goTo(service.path)"
        >
          <div class="service-card__top">
            <div :class="['service-icon', service.iconClass]">
              <i :class="service.icon"></i>
            </div>

            <span v-if="service.badge" class="service-badge text-13-bold">
              {{ service.badge }}
            </span>
          </div>

          <div class="service-card__body">
            <strong class="text-15-bold">{{ service.title }}</strong>
            <span class="service-description text-13">
              {{ service.description }}
            </span>
          </div>

          <i class="fa-solid fa-chevron-right service-arrow"></i>
        </button>
      </div>
    </section>

    <p v-if="errorMessage" class="finance-message text-13">
      {{ errorMessage }}
    </p>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import pointWalletApi from '@/api/pointWalletApi';
import { formatNumber } from '@/util/pointWallet';

const router = useRouter();

const pointBalance = ref(0);
const randomBoxCount = ref(0);
const loading = ref(false);
const errorMessage = ref('');

const services = computed(() => [
  {
    title: '포인트 지갑',
    description: '출석체크로 포인트와 랜덤박스 받기',
    path: '/point-wallet',
    icon: 'fa-solid fa-coins',
    iconClass: 'wallet',
  },
  {
    title: '소비 분석',
    description: '내 소비패턴 분석으로 AI 칭호와 추천 상품 확인',
    path: '/analysis',
    icon: 'fa-solid fa-chart-pie',
    iconClass: 'analysis',
  },
  {
    title: '랜덤박스',
    description: '보유 중인 랜덤박스를 열고 포인트 받기',
    path: '/point-wallet/random-box',
    icon: 'fa-solid fa-gift',
    iconClass: 'random-box',
    badge: randomBoxCount.value > 0 ? `${randomBoxCount.value}개` : '',
  },
  {
    title: '포인트 전환',
    description: '포인트를 전자 지갑으로 간편하게 전환',
    path: '/point-wallet/conversion',
    icon: 'fa-solid fa-wallet',
    iconClass: 'conversion',
  },
]);

const loadFinanceSummary = async () => {
  loading.value = true;
  errorMessage.value = '';

  const [walletResult, randomBoxResult] = await Promise.allSettled([
    pointWalletApi.getWallet(),
    pointWalletApi.getUnopenedRandomBoxCount(),
  ]);

  if (walletResult.status === 'fulfilled') {
    pointBalance.value = Number(walletResult.value?.pointBalance ?? 0);
  }

  if (randomBoxResult.status === 'fulfilled') {
    randomBoxCount.value = Number(randomBoxResult.value?.unopenedCount ?? 0);
  }

  if (
    walletResult.status === 'rejected' ||
    randomBoxResult.status === 'rejected'
  ) {
    errorMessage.value =
      '일부 금융 정보를 불러오지 못했어요. 잠시 후 다시 확인해 주세요.';
  }

  loading.value = false;
};

const goTo = (path) => {
  router.push(path);
};

onMounted(loadFinanceSummary);
</script>

<style scoped>
.finance-page {
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.finance-intro {
  padding: 8px 4px 2px;
}

.intro-eyebrow {
  display: block;
  margin: 0 0 6px;
  color: var(--color-text-muted);
}

.intro-title {
  max-width: 280px;
  margin: 0;
  line-height: 1.5;
  letter-spacing: -0.5px;
  word-break: keep-all;
  overflow-wrap: break-word;
}

/* =========================
   이벤트 배너
========================= */
.event-section {
  margin-top: 12px;
}

.event-banner {
  width: 100%;
  min-height: 94px;
  padding: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid #f4d57e;
  border-radius: var(--kb-radius-lg);
  background: #fffaf0;
  color: var(--color-text-main);
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.event-banner__content {
  min-width: 0;
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: flex-start;
}

.event-chip {
  margin-bottom: 6px;
  padding: 4px 7px;
  border-radius: 999px;
  background: var(--color-primary);
  color: var(--color-text-main);
  font-size: 10px;
  line-height: 1;
}

.event-banner__content strong,
.event-banner__content span:last-child {
  max-width: 100%;
  word-break: keep-all;
  overflow-wrap: break-word;
}

.event-banner__content span:last-child {
  margin-top: 4px;
  color: var(--color-text-sub);
  line-height: 1.45;
}

.event-banner__icon {
  width: 48px;
  height: 48px;
  flex: 0 0 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 15px;
  background: var(--color-primary);
  color: var(--color-text-white);
  font-size: 21px;
}

/* =========================
   포인트 잔액 카드
   PointWalletPage와 동일한 기준
========================= */
.balance-card {
  width: 100%;
  min-height: 126px;
  margin-top: 10px;
  padding: 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border: 0;
  background: linear-gradient(135deg, #fff4c6 0%, #fffaf0 100%);
  color: var(--color-text-main);
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.balance-content {
  min-width: 0;
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
  line-height: 1.45;
  word-break: keep-all;
  overflow-wrap: break-word;
}

.point-symbol {
  width: 54px;
  height: 54px;
  flex: 0 0 54px;
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
   금융생활 바로가기
========================= */
.finance-service-section {
  margin-top: 16px;
}

.section-heading {
  margin: 0 3px 10px;
}

.section-heading h2 {
  margin: 0;
}

.section-heading p {
  margin: 4px 0 0;
  color: var(--color-text-sub);
  line-height: 1.45;
  word-break: keep-all;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.service-card {
  position: relative;
  min-width: 0;
  min-height: 148px;
  padding: 14px 14px 34px;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
  color: var(--color-text-main);
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.service-card:active,
.balance-card:active,
.event-banner:active {
  transform: scale(0.99);
}

.service-card__top {
  min-height: 38px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}

.service-icon {
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 16px;
}

.service-icon.wallet,
.service-icon.random-box,
.service-icon.conversion {
  background: #fff2c0;
  color: #dda000;
}

.service-icon.analysis {
  background: #eef2ff;
  color: #5f74c8;
}

.service-badge {
  min-width: 34px;
  padding: 4px 7px;
  border-radius: 999px;
  background: #fff3c5;
  color: #a87600;
  line-height: 1;
  text-align: center;
  white-space: nowrap;
}

.service-card__body {
  min-width: 0;
  margin-top: 10px;
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 4px;
}

.service-card__body strong,
.service-description {
  display: block;
  max-width: 100%;
  word-break: keep-all;
  overflow-wrap: break-word;
}

.service-description {
  color: var(--color-text-sub);
  line-height: 1.45;
}

.service-arrow {
  position: absolute;
  right: 14px;
  bottom: 14px;
  color: #b9b9b9;
  font-size: 11px;
}

.finance-message {
  margin: 12px 4px 0;
  color: var(--color-error);
  line-height: 1.45;
  word-break: keep-all;
}

@media (max-width: 360px) {
  .service-card {
    min-height: 158px;
    padding: 13px 13px 34px;
  }

  .balance-card {
    padding: 18px;
  }

  .point-symbol {
    width: 48px;
    height: 48px;
    flex-basis: 48px;
    font-size: 25px;
  }
}
</style>
