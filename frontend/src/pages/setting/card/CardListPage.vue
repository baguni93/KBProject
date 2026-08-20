<template>
  <main class="page-layout card-page" @click="closeMenu">
    <PageHeader title="연결 카드 관리" custom-back @back="goBack" />

    <div class="page-content">
      <section class="title-section">
        <h2 class="text-26-bold">연결된 카드를 관리해 보세요</h2>

        <p class="text-15">
          연결 카드는 송금, 결제, 혜택 분석 등에<br />
          사용할 수 있어요.
        </p>
      </section>

      <section class="card-section">
        <div class="card-summary">
          <div class="card-count">
            <span class="text-13-bold">연결 카드</span>
            <strong class="text-15-bold">{{ cardStore.cardCount }}</strong>
          </div>

          <button
            class="refresh-button"
            :disabled="loading"
            type="button"
            aria-label="카드 목록 새로고침"
            @click.stop="loadCards"
          >
            <i
              class="fa-solid fa-rotate-right"
              :class="{ rotating: loading }"
            ></i>
          </button>
        </div>

        <div class="card-list-area">
          <p v-if="loading" class="state-message text-13">
            카드 목록을 불러오고 있어요.
          </p>

          <div v-else-if="errorMessage" class="error-area">
            <p class="text-13">{{ errorMessage }}</p>

            <button
              class="retry-button text-13-bold"
              type="button"
              @click="loadCards"
            >
              다시 불러오기
            </button>
          </div>

          <div v-else-if="cardStore.cards.length === 0" class="empty-area">
            <div class="empty-icon">
              <i class="fa-regular fa-credit-card"></i>
            </div>

            <strong class="text-18-bold">연결된 카드가 없어요</strong>

            <p class="text-13">
              카드를 연결하면 결제와 혜택 서비스를<br />
              편리하게 이용할 수 있어요.
            </p>
          </div>

          <div v-else class="card-list">
            <article
              v-for="card in cardStore.cards"
              :key="card.linkedCardId"
              class="card-item"
            >
              <div class="card-image-area">
                <img
                  v-if="card.cardImageUrl"
                  :alt="card.cardName"
                  :src="card.cardImageUrl"
                  class="card-image"
                  @error="card.cardImageUrl = null"
                />

                <div v-else class="card-image-fallback">
                  <i class="fa-regular fa-credit-card"></i>
                </div>
              </div>

              <div class="card-info">
                <div class="card-title">
                  <strong class="text-15-bold">{{ card.cardName }}</strong>

                  <span v-if="card.representYn === 'Y'" class="primary-badge">
                    대표카드
                  </span>
                </div>

                <p v-if="card.cardCompanyName" class="text-13">
                  {{ card.cardCompanyName }}
                </p>

                <small>
                  {{ card.maskedCardNumber }}
                </small>
              </div>

              <button
                class="menu-button"
                type="button"
                aria-label="카드 관리 메뉴"
                @click.stop="toggleMenu(card.linkedCardId)"
              >
                <i class="fa-solid fa-ellipsis-vertical"></i>
              </button>

              <div
                v-if="openedCardId === card.linkedCardId"
                class="card-menu"
                @click.stop
              >
                <button
                  v-if="card.representYn !== 'Y'"
                  class="text-13"
                  type="button"
                  @click="changePrimary(card)"
                >
                  <span class="menu-icon">
                    <i class="fa-regular fa-star"></i>
                  </span>
                  대표카드 설정
                </button>

                <button
                  class="delete-button text-13"
                  type="button"
                  @click="openDisconnectModal(card)"
                >
                  <span class="menu-icon">
                    <i class="fa-solid fa-link-slash"></i>
                  </span>
                  카드 연결 해제
                </button>
              </div>
            </article>
          </div>
        </div>
      </section>

      <p v-if="toastMessage" class="toast-message text-13-bold">
        <span class="toast-icon">
          <i class="fa-solid fa-check"></i>
        </span>
        {{ toastMessage }}
      </p>

      <button
        class="content-add-btn connect-button"
        type="button"
        @click="goConnect"
      >
        <i class="fa-solid fa-plus"></i>
        카드 연결하기
      </button>

      <div
        v-if="disconnectTarget"
        class="modal-overlay"
        @click.self="closeDisconnectModal"
      >
        <section class="disconnect-modal">
          <div class="warning-icon">
            <i class="fa-solid fa-exclamation"></i>
          </div>

          <h3 class="text-20-bold">카드 연결을 해제할까요?</h3>

          <article class="selected-card">
            <div class="selected-card-image">
              <img
                v-if="disconnectTarget.cardImageUrl"
                :alt="disconnectTarget.cardName"
                :src="disconnectTarget.cardImageUrl"
                @error="disconnectTarget.cardImageUrl = null"
              />

              <span v-else>
                <i class="fa-regular fa-credit-card"></i>
              </span>
            </div>

            <div class="selected-card-info">
              <strong class="text-15-bold">{{
                disconnectTarget.cardName
              }}</strong>
              <p class="text-13">{{ disconnectTarget.maskedCardNumber }}</p>
            </div>
          </article>

          <p class="modal-guide text-13">
            카드를 해제하면 해당 카드의 결제 및<br />
            혜택 서비스를 이용할 수 없어요.
          </p>

          <div class="modal-button-area">
            <button
              class="modal-cancel-button text-15-bold"
              :disabled="disconnecting"
              type="button"
              @click="closeDisconnectModal"
            >
              취소
            </button>

            <button
              class="modal-delete-button text-15-bold"
              :disabled="disconnecting"
              type="button"
              @click="removeCard"
            >
              {{ disconnecting ? '해제 중...' : '연결 해제' }}
            </button>
          </div>
        </section>
      </div>
    </div>
  </main>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { disconnectCard, getCards, setPrimaryCard } from '@/api/cardApi';
import PageHeader from '@/components/common/PageHeader.vue';
import { useCardStore } from '@/stores/card';

const router = useRouter();
const cardStore = useCardStore();

const loading = ref(false);
const disconnecting = ref(false);
const errorMessage = ref('');
const toastMessage = ref('');
const openedCardId = ref(null);
const disconnectTarget = ref(null);

let toastTimer = null;

// 카드번호 마스킹
const maskCardNumber = (cardNumber) => {
  if (!cardNumber) return '';

  const onlyNumber = String(cardNumber).replace(/[^0-9]/g, '');

  if (onlyNumber.length < 8) return onlyNumber;

  return `**** **** **** ${onlyNumber.slice(-4)}`;
};

// 카드 목록 조회
const loadCards = async () => {
  const userId = cardStore.userId;

  if (!userId) {
    await router.replace('/intro');
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';
    openedCardId.value = null;

    const data = await getCards();

    // 백엔드가 배열을 직접 반환하는 경우와 cards 필드에 배열을 담아 반환하는 경우를 모두 처리
    const cards = Array.isArray(data) ? data : data.cards || [];

    cardStore.setCards(cards);
  } catch (error) {
    console.error(error);
    errorMessage.value =
      error.response?.data?.message || '카드 목록을 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

// 관리 메뉴 열기
const toggleMenu = (linkedCardId) => {
  openedCardId.value =
    openedCardId.value === linkedCardId ? null : linkedCardId;
};

// 관리 메뉴 닫기
const closeMenu = () => {
  openedCardId.value = null;
};

// 대표카드 변경
const changePrimary = async (card) => {
  try {
    errorMessage.value = '';

    await setPrimaryCard(card.linkedCardId);

    openedCardId.value = null;

    await loadCards();

    showToast('대표카드가 변경되었습니다.');
  } catch (error) {
    console.error(error);
    errorMessage.value =
      error.response?.data?.message || '대표카드 변경에 실패했습니다.';
  }
};

// 카드 연결 해제 모달 열기
const openDisconnectModal = (card) => {
  openedCardId.value = null;
  disconnectTarget.value = card;
};

// 카드 연결 해제 모달 닫기
const closeDisconnectModal = () => {
  if (disconnecting.value) return;

  disconnectTarget.value = null;
};

// 카드 연결 해제
const removeCard = async () => {
  if (!disconnectTarget.value) return;

  try {
    disconnecting.value = true;
    errorMessage.value = '';

    await disconnectCard(disconnectTarget.value.linkedCardId);

    disconnectTarget.value = null;

    await loadCards();

    showToast('카드 연결이 해제되었습니다.');
  } catch (error) {
    console.error(error);
    errorMessage.value =
      error.response?.data?.message || '카드 연결 해제에 실패했습니다.';
  } finally {
    disconnecting.value = false;
  }
};

// 토스트 메시지 표시
const showToast = (message) => {
  toastMessage.value = message;

  if (toastTimer) window.clearTimeout(toastTimer);

  toastTimer = window.setTimeout(() => {
    toastMessage.value = '';
  }, 2000);
};

// 카드 연결 화면 이동
const goConnect = async () => {
  await router.push('/wallet/card/add');
};

// 이전 화면
const goBack = () => {
  router.push('/setting');
};

onMounted(() => {
  loadCards();
});

onBeforeUnmount(() => {
  if (toastTimer) window.clearTimeout(toastTimer);
});
</script>

<style scoped>
@import '@/components/common/common/common.css';

.card-page {
  width: 100%;
  /* 모바일 브라우저 주소창 이슈를 해결하기 위해 dvh 사용 */
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  background: var(--color-bg-page);
}

.title-section {
  flex-shrink: 0;
  margin-top: 38px;
}

.title-section h2 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.title-section p {
  margin: 14px 0 0;
  color: var(--color-text-muted);
  line-height: 1.55;
}

.card-section {
  position: relative;
  min-height: 0;
  margin-top: 32px;
  overflow: visible;
}

.card-summary {
  display: flex;
  height: 52px;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  border: 1px solid var(--color-divider);
  border-bottom: 0;
  border-radius: 14px 14px 0 0;
  background: var(--color-bg-page);
  box-sizing: border-box;
}

.card-count {
  display: flex;
  align-items: center;
  gap: 7px;
}

.card-count span {
  color: var(--color-text-main);
}

.card-count strong {
  color: var(--color-primary-active);
}

.refresh-button {
  display: flex;
  width: 32px;
  height: 32px;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: var(--color-text-sub);
  font-size: 16px;
  cursor: pointer;
}

.refresh-button:active:not(:disabled) {
  background: var(--color-bg-screen);
}

.refresh-button:disabled {
  cursor: default;
}

.rotating {
  animation: rotate 0.8s linear infinite;
}

.card-list-area {
  position: relative;
  border: 1px solid var(--color-divider);
  border-radius: 0 0 14px 14px;
  background: var(--color-bg-page);
  overflow: visible;
}

.card-list {
  display: flex;
  flex-direction: column;
  overflow: visible;
}

.card-item {
  position: relative;
  display: flex;
  min-height: 80px;
  align-items: center;
  gap: 13px;
  padding: 13px;
  border-bottom: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-sizing: border-box;
  overflow: visible;
}

.card-item:last-child {
  border-bottom: 0;
}

.card-image-area {
  display: flex;
  width: 58px;
  height: 38px;
  flex: none;
  align-items: center;
  justify-content: center;
}

.card-image {
  display: block;
  width: 58px;
  height: 38px;
  border-radius: 6px;
  object-fit: cover;
}

.card-image-fallback {
  display: flex;
  width: 58px;
  height: 38px;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #fff4d6;
  color: var(--color-primary-active);
  font-size: 20px;
}

.card-info {
  min-width: 0;
  flex: 1;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 7px;
}

.card-title strong {
  overflow: hidden;
  color: var(--color-text-main);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.primary-badge {
  flex: none;
  padding: 3px 7px;
  border: 1px solid var(--color-primary);
  border-radius: 8px;
  background: #fff9e9;
  color: var(--color-primary-active);
  font-size: 9px;
  font-weight: 600;
}

.card-info p {
  margin: 5px 0 0;
  color: var(--color-text-muted);
}

.card-info small {
  display: block;
  margin-top: 3px;
  color: var(--color-text-muted);
  font-size: 11px;
  font-weight: 400;
  letter-spacing: 0.3px;
}

.menu-button {
  display: flex;
  width: 34px;
  height: 42px;
  flex: none;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--color-text-main);
  font-size: 18px;
  cursor: pointer;
}

.menu-button:active {
  background: var(--color-bg-screen);
}

.card-menu {
  position: absolute;
  z-index: 30;
  top: 58px;
  right: 13px;
  display: flex;
  width: 150px;
  flex-direction: column;
  padding: 6px;
  border: 1px solid var(--color-divider);
  border-radius: 11px;
  background: var(--color-bg-page);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.15);
  box-sizing: border-box;
}

.card-menu button {
  display: flex;
  min-height: 42px;
  align-items: center;
  gap: 8px;
  padding: 0 10px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--color-text-main);
  text-align: left;
  cursor: pointer;
}

.card-menu button:hover {
  background: var(--color-bg-screen);
}

.card-menu .delete-button {
  color: var(--color-error);
}

.menu-icon {
  display: inline-flex;
  width: 18px;
  justify-content: center;
  font-size: 14px;
}

.empty-area {
  padding: 52px 20px 48px;
  text-align: center;
}

.empty-icon {
  display: flex;
  width: 68px;
  height: 68px;
  align-items: center;
  justify-content: center;
  margin: 0 auto 21px;
  border-radius: 22px;
  background: #fff4d6;
  color: var(--color-primary-active);
  font-size: 28px;
}

.empty-area strong {
  display: block;
  color: var(--color-text-main);
}

.empty-area p {
  margin: 11px 0 0;
  color: var(--color-text-muted);
  line-height: 1.55;
}

.state-message {
  margin: 0;
  padding: 70px 20px;
  color: var(--color-text-sub);
  text-align: center;
}

.error-area {
  padding: 54px 20px;
  text-align: center;
}

.error-area p {
  margin: 0;
  color: var(--color-error);
  line-height: 1.5;
}

.retry-button {
  margin-top: 15px;
  padding: 8px 14px;
  border: 1px solid var(--color-border-main);
  border-radius: 9px;
  background: var(--color-bg-page);
  color: var(--color-text-main);
  cursor: pointer;
}

.retry-button:active {
  background: var(--color-bg-screen);
}

.connect-button {
  position: absolute;
  right: 24px;
  bottom: 32px;
  left: 24px;
  display: flex;
  width: auto;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.connect-button i {
  font-size: 16px;
}

.toast-message {
  position: absolute;
  z-index: 50;
  right: 24px;
  bottom: 106px;
  left: 24px;
  display: flex;
  align-items: center;
  gap: 9px;
  margin: 0;
  padding: 14px 16px;
  border: 1px solid rgba(34, 197, 94, 0.18);
  border-radius: 12px;
  background: rgba(34, 197, 94, 0.12);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: #15803d;
  box-sizing: border-box;
  box-shadow: 0 4px 14px rgba(34, 197, 94, 0.08);
  animation: toast-in 0.25s ease-out;
}

.toast-icon {
  display: inline-flex;
  width: 20px;
  height: 20px;
  flex: none;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(34, 197, 94, 0.16);
  color: #16a34a;
  font-size: 10px;
}

.modal-overlay {
  position: absolute;
  z-index: 100;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(0, 0, 0, 0.45);
  box-sizing: border-box;
}

.disconnect-modal {
  width: 100%;
  max-width: 330px;
  padding: 28px 20px 20px;
  border-radius: 18px;
  background: var(--color-bg-page);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.18);
  box-sizing: border-box;
}

.warning-icon {
  display: flex;
  width: 52px;
  height: 52px;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  border-radius: 50%;
  background: #fff1d4;
  color: var(--color-primary-active);
  font-size: 22px;
}

.disconnect-modal h3 {
  margin: 20px 0 0;
  color: var(--color-text-main);
  text-align: center;
}

.selected-card {
  display: flex;
  align-items: center;
  gap: 13px;
  margin-top: 24px;
  padding: 14px;
  border: 1px solid var(--color-border-main);
  border-radius: 12px;
  box-sizing: border-box;
}

.selected-card-image {
  display: flex;
  width: 58px;
  height: 38px;
  flex: none;
  align-items: center;
  justify-content: center;
}

.selected-card-image img {
  width: 58px;
  height: 38px;
  border-radius: 6px;
  object-fit: cover;
}

.selected-card-image span {
  display: flex;
  width: 58px;
  height: 38px;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #fff4d6;
  color: var(--color-primary-active);
  font-size: 20px;
}

.selected-card-info {
  min-width: 0;
}

.selected-card-info strong {
  display: block;
  color: var(--color-text-main);
}

.selected-card-info p {
  margin: 5px 0 0;
  color: var(--color-text-muted);
}

.modal-guide {
  margin: 20px 0 0;
  color: var(--color-text-sub);
  line-height: 1.6;
  text-align: center;
}

.modal-button-area {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 24px;
}

.modal-cancel-button,
.modal-delete-button {
  height: 50px;
  border-radius: 10px;
  cursor: pointer;
}

.modal-cancel-button {
  border: 1px solid var(--color-border-main);
  background: var(--color-bg-page);
  color: var(--color-text-main);
}

.modal-delete-button {
  border: 1px solid #ffc7c7;
  background: #ffe7e7;
  color: var(--color-error);
}

.modal-cancel-button:active {
  background: var(--color-bg-screen);
}

.modal-cancel-button:disabled,
.modal-delete-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

@keyframes toast-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes rotate {
  to {
    transform: rotate(360deg);
  }
}
</style>
