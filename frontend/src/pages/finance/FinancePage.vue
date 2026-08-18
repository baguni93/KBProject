<template>
  <div class="finance-shell">
    <PageHeader title="금융" :showBack="false" />

    <div class="page-layout finance-page">
      <main class="page-content">
        <button type="button" class="balance-card kb-card" aria-label="포인트 지갑으로 이동" @click="goTo('/point-wallet')">
          <div class="balance-main">
            <div class="balance-content">
              <div class="balance-label text-13-bold">내 포인트</div>

              <div class="balance-value text-28-bold">
                <template v-if="loading">-</template>
                <template v-else>{{ formatNumber(pointBalance) }}</template>
                <span>P</span>
              </div>

              <div class="balance-sub text-13">현금처럼 전환해서 사용할 수 있어요</div>
            </div>

            <div class="point-symbol" aria-hidden="true">P</div>
          </div>

          <div class="balance-link-row">
            <span class="text-13-bold">포인트 지갑 바로가기</span>
            <i class="fa-solid fa-chevron-right"></i>
          </div>
        </button>

        <section class="event-section">
          <button type="button" class="event-banner" @click="goTo('/event')">
            <div class="event-banner__content">
              <span class="event-chip text-13-bold">EVENT</span>
              <strong class="event-title text-13-bold">이벤트 참여하고 혜택 받기</strong>
            </div>

            <div class="event-banner__action">
              <div class="event-banner__icon" aria-hidden="true"><i class="fa-solid fa-gift"></i></div>
              <i class="fa-solid fa-chevron-right event-arrow" aria-hidden="true"></i>
            </div>
          </button>
        </section>

        <section class="kb-section finance-service-section">
          <div class="section-heading">
            <h2 class="text-18-bold">금융 서비스</h2>
            <p class="text-13">자주 사용하는 금융 기능을 이용해보세요.</p>
          </div>

          <div class="service-grid">
            <button v-for="service in services" :key="service.title" type="button" class="service-card kb-card" @click="goTo(service.path)">
              <div class="service-card__top">
                <div :class="['service-icon', service.iconClass]"><i :class="service.icon"></i></div>
                <span v-if="service.badge" class="service-badge text-13-bold">{{ service.badge }}</span>
              </div>

              <div class="service-card__body">
                <strong class="text-15-bold">{{ service.title }}</strong>
                <span class="service-description text-13">{{ service.description }}</span>
              </div>

              <i class="fa-solid fa-chevron-right service-arrow"></i>
            </button>
          </div>
        </section>

        <p v-if="errorMessage" class="finance-message text-13">{{ errorMessage }}</p>
      </main>
    </div>
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
  { title: '포인트 지갑', description: '출석체크로 포인트와 랜덤박스 받기', path: '/point-wallet', icon: 'fa-solid fa-coins', iconClass: 'wallet' },
  { title: '소비 분석', description: '내 소비패턴 분석으로 AI 칭호와 추천 상품 확인', path: '/analysis', icon: 'fa-solid fa-chart-pie', iconClass: 'analysis' },
  { title: '랜덤박스', description: '보유 중인 랜덤박스를 열고 포인트 받기', path: '/point-wallet/random-box', icon: 'fa-solid fa-gift', iconClass: 'random-box', badge: randomBoxCount.value > 0 ? `${randomBoxCount.value}개` : '' },
  { title: '커스텀 카드', description: '나만의 디자인과 혜택으로 카드 만들기', path: '/card/create/intro', icon: 'fa-solid fa-wand-magic-sparkles', iconClass: 'custom-card' },
]);

const loadFinanceSummary = async () => {
  loading.value = true;
  errorMessage.value = '';

  const [walletResult, randomBoxResult] = await Promise.allSettled([pointWalletApi.getWallet(), pointWalletApi.getUnopenedRandomBoxCount()]);

  if (walletResult.status === 'fulfilled') pointBalance.value = Number(walletResult.value?.pointBalance ?? 0);
  if (randomBoxResult.status === 'fulfilled') randomBoxCount.value = Number(randomBoxResult.value?.unopenedCount ?? 0);
  if (walletResult.status === 'rejected' || randomBoxResult.status === 'rejected') errorMessage.value = '일부 금융 정보를 불러오지 못했어요. 잠시 후 다시 확인해 주세요.';

  loading.value = false;
};

const goTo = (path) => router.push(path);

onMounted(loadFinanceSummary);
</script>

<style scoped>
.finance-shell {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--color-bg-screen);
}

.finance-shell > :first-child {
  flex-shrink: 0;
}

.finance-page {
  height: auto;
  flex: 1;
  min-height: 0;
  overflow: hidden;
  color: var(--color-text-main);
}

.page-content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-top: 12px;
  scrollbar-width: none;
}

.page-content::-webkit-scrollbar {
  display: none;
}

/* 포인트 잔액 카드 */
.balance-card {
  width: 100%;
  padding: 0;
  overflow: hidden;
  display: block;
  border: 0;
  background: linear-gradient(135deg, #fff4c6 0%, #fffaf0 100%);
  color: var(--color-text-main);
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.balance-main {
  min-height: 132px;
  padding: 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.balance-content {
  min-width: 0;
}

.balance-label {
  color: var(--color-text-sub);
}

.balance-value {
  margin-top: 6px;
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

.balance-link-row {
  min-height: 50px;
  padding: 0 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid rgba(221, 221, 221, 0.6);
}

.balance-link-row i {
  color: var(--color-text-muted);
  font-size: 12px;
}

/* 이벤트 배너 */
.event-section {
  margin-top: 14px;
}

.event-banner {
  width: 100%;
  min-height: 70px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid #f4d57e;
  border-radius: 18px;
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
  align-items: center;
  gap: 10px;
}

.event-chip {
  flex-shrink: 0;
  padding: 5px 8px;
  border-radius: 999px;
  background: var(--color-primary);
  color: var(--color-text-main);
  font-size: 10px;
  line-height: 1;
}

.event-title {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.event-banner__action {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.event-banner__icon {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: var(--color-primary);
  color: var(--color-text-white);
  font-size: 17px;
  animation: event-wiggle 4s ease-in-out infinite;
}

.event-arrow {
  color: var(--color-text-muted);
  font-size: 12px;
}

/* 금융 서비스 */
.finance-service-section {
  margin-top: 22px;
}

.section-heading {
  margin: 0 3px 12px;
}

.section-heading h2 {
  margin: 0;
}

.section-heading p {
  margin: 5px 0 0;
  color: var(--color-text-sub);
  line-height: 1.45;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.service-card {
  position: relative;
  min-width: 0;
  min-height: 142px;
  padding: 14px 14px 32px;
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
.service-icon.random-box {
  background: #fff2c0;
  color: #dda000;
}

.service-icon.analysis {
  background: #eef2ff;
  color: #5f74c8;
}

.service-icon.custom-card {
  background: #f2edff;
  color: #7657c8;
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
  gap: 5px;
}

.service-card__body strong,
.service-description {
  display: block;
  max-width: 100%;
  word-break: keep-all;
}

.service-description {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
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

@keyframes point-float {
  0%, 18%, 100% { transform: translateY(0) scale(1); }
  7% { transform: translateY(-4px) scale(1.03); }
  12% { transform: translateY(1px) scale(0.99); }
}

@keyframes event-wiggle {
  0%, 16%, 100% { transform: rotate(0deg); }
  5% { transform: rotate(-5deg); }
  8% { transform: rotate(5deg); }
  11% { transform: rotate(-3deg); }
  14% { transform: rotate(0deg); }
}

@media (prefers-reduced-motion: reduce) {
  .point-symbol,
  .event-banner__icon {
    animation: none;
  }
}

@media (max-width: 360px) {
  .balance-main {
    min-height: 122px;
    padding: 18px;
  }

  .balance-link-row {
    padding: 0 18px;
  }

  .point-symbol {
    width: 52px;
    height: 52px;
    flex-basis: 52px;
    font-size: 26px;
  }

  .service-card {
    min-height: 148px;
    padding: 13px 13px 32px;
  }
}
</style>
