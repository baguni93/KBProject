<template>
  <div class="card-page">
    <main class="card-container" @click="closeMenu">
      <header class="page-header">
        <button class="back-button" type="button" aria-label="이전 화면" @click="goBack">
          &lt;
        </button>

        <h1>연결 카드 관리</h1>
        <div class="header-empty"></div>
      </header>

      <section class="title-section">
        <h2>
          연결된 카드를<br />
          확인하고 관리해 보세요
        </h2>

        <p>
          연결 카드는 송금, 결제, 혜택 분석 등에<br />
          사용할 수 있어요.
        </p>
      </section>

      <section class="card-section">
        <div class="card-summary">
          <div class="card-count">
            <span>연결 카드</span>
            <strong>{{ cardStore.cardCount }}</strong>
          </div>

          <button class="refresh-button" :disabled="loading" type="button" aria-label="카드 목록 새로고침" @click.stop="loadCards">
            <span :class="{ rotating: loading }">↻</span>
          </button>
        </div>

        <div class="card-list-area">
          <p v-if="loading" class="state-message">
            카드 목록을 불러오고 있어요.
          </p>

          <div v-else-if="errorMessage" class="error-area">
            <p>{{ errorMessage }}</p>

            <button type="button" @click="loadCards">
              다시 불러오기
            </button>
          </div>

          <div v-else-if="cardStore.cards.length === 0" class="empty-area">
            <div class="empty-icon">💳</div>

            <strong>연결된 카드가 없어요</strong>

            <p>
              카드를 연결하면 결제와 혜택 서비스를<br />
              편리하게 이용할 수 있어요.
            </p>
          </div>

          <div v-else class="card-list">
            <article v-for="card in cardStore.cards" :key="card.linkedCardId" class="card-item">
              <div class="card-image-area">
                <img v-if="card.cardImageUrl" :alt="card.cardName" :src="card.cardImageUrl" class="card-image" />

                <div v-else class="card-image-fallback">
                  💳
                </div>
              </div>

              <div class="card-info">
                <div class="card-title">
                  <strong>{{ card.cardName }}</strong>

                  <span v-if="card.primaryYn === 'Y'" class="primary-badge">
                    대표카드
                  </span>
                </div>

                <p v-if="card.cardCompanyName">
                  {{ card.cardCompanyName }}
                </p>

                <small>
                  {{ maskCardNumber(card.cardNumber) }}
                </small>
              </div>

              <button
                  v-if="cardStore.cards.length > 1"
                  class="menu-button"
                  type="button"
                  aria-label="카드 관리 메뉴"
                  @click.stop="toggleMenu(card.linkedCardId)"
              >
                ⋮
              </button>

              <div
                  v-if="cardStore.cards.length > 1 && openedCardId === card.linkedCardId"
                  class="card-menu"
                  @click.stop
              >
                <button v-if="card.primaryYn !== 'Y'" type="button" @click="changePrimary(card)">
                  <span class="menu-icon">☆</span>
                  대표카드 설정
                </button>

                <button class="delete-button" type="button" @click="openDisconnectModal(card)">
                  <span class="menu-icon">♲</span>
                  카드 연결 해제
                </button>
              </div>
            </article>
          </div>
        </div>
      </section>

      <p v-if="toastMessage" class="toast-message">
        <span class="toast-icon">✓</span>
        {{ toastMessage }}
      </p>

      <button class="connect-button" type="button" @click="goConnect">
        <span>＋</span>
        카드 연결하기
      </button>

      <div v-if="disconnectTarget" class="modal-overlay" @click.self="closeDisconnectModal">
        <section class="disconnect-modal">
          <button class="modal-close-button" type="button" aria-label="닫기" @click="closeDisconnectModal">
            ×
          </button>

          <div class="warning-icon">!</div>

          <h3>카드 연결을 해제할까요?</h3>

          <article class="selected-card">
            <div class="selected-card-image">
              <img v-if="disconnectTarget.cardImageUrl" :alt="disconnectTarget.cardName" :src="disconnectTarget.cardImageUrl" />

              <span v-else>💳</span>
            </div>

            <div>
              <strong>{{ disconnectTarget.cardName }}</strong>
              <p>{{ maskCardNumber(disconnectTarget.cardNumber) }}</p>
            </div>
          </article>

          <div class="modal-guide">
            <strong>안내사항</strong>

            <p>
              카드를 해제하면 해당 카드의 결제 및<br />
              혜택 서비스를 이용할 수 없어요.
            </p>
          </div>

          <div class="modal-button-area">
            <button class="modal-cancel-button" :disabled="disconnecting" type="button" @click="closeDisconnectModal">
              취소
            </button>

            <button class="modal-delete-button" :disabled="disconnecting" type="button" @click="removeCard">
              {{ disconnecting ? '해제 중...' : '연결 해제' }}
            </button>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { disconnectCard, getCards, setPrimaryCard } from '@/api/cardApi';
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

    const data = await getCards(userId);

    // 백엔드가 배열을 직접 반환하는 경우와 cards 필드에 배열을 담아 반환하는 경우를 모두 처리
    const cards = Array.isArray(data) ? data : data.cards || [];

    cardStore.setCards(cards);
  } catch (error) {
    console.error(error);
    errorMessage.value = error.response?.data?.message || '카드 목록을 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

// 관리 메뉴 열기
const toggleMenu = (linkedCardId) => {
  openedCardId.value = openedCardId.value === linkedCardId ? null : linkedCardId;
};

// 관리 메뉴 닫기
const closeMenu = () => {
  openedCardId.value = null;
};

// 대표카드 변경
const changePrimary = async (card) => {
  try {
    errorMessage.value = '';

    await setPrimaryCard(cardStore.userId, card.linkedCardId);

    openedCardId.value = null;

    await loadCards();

    showToast('대표카드가 변경되었습니다.');
  } catch (error) {
    console.error(error);
    errorMessage.value = error.response?.data?.message || '대표카드 변경에 실패했습니다.';
  }
};

// 카드 연결 해제 모달 열기
const openDisconnectModal = (card) => {
  if (cardStore.cards.length <= 1) return;

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
  if (!disconnectTarget.value || cardStore.cards.length <= 1) return;

  try {
    disconnecting.value = true;
    errorMessage.value = '';

    await disconnectCard(cardStore.userId, disconnectTarget.value.linkedCardId);

    disconnectTarget.value = null;

    await loadCards();

    showToast('카드 연결이 해제되었습니다.');
  } catch (error) {
    console.error(error);
    errorMessage.value = error.response?.data?.message || '카드 연결 해제에 실패했습니다.';
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

// 설정 메인 화면
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
.card-page {
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.card-container {
  position: relative;
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  padding: 10px 28px 140px;
  background: #ffffff;
  box-sizing: border-box;
  overflow: visible;
}

.page-header {
  display: grid;
  grid-template-columns: 38px 1fr 38px;
  min-height: 44px;
  flex-shrink: 0;
  align-items: center;
}

.page-header h1 {
  margin: 0;
  color: #222222;
  font-size: 17px;
  font-weight: 700;
  text-align: center;
}

.back-button {
  justify-self: start;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 27px;
  line-height: 1;
  cursor: pointer;
}

.header-empty {
  width: 38px;
}

.title-section {
  flex-shrink: 0;
  margin-top: 38px;
}

.title-section h2 {
  margin: 0;
  color: #111111;
  font-size: 25px;
  font-weight: 800;
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.title-section p {
  margin: 14px 0 0;
  color: #999999;
  font-size: 12px;
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
  border: 1px solid #e5e5e5;
  border-bottom: 0;
  border-radius: 14px 14px 0 0;
  background: #ffffff;
  box-sizing: border-box;
}

.card-count {
  display: flex;
  align-items: center;
  gap: 7px;
}

.card-count span {
  color: #333333;
  font-size: 13px;
  font-weight: 700;
}

.card-count strong {
  color: #e99b00;
  font-size: 14px;
  font-weight: 800;
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
  color: #777777;
  font-size: 23px;
  cursor: pointer;
}

.refresh-button:active {
  background: #f5f5f5;
}

.refresh-button:disabled {
  cursor: default;
}

.rotating {
  display: inline-block;
  animation: rotate 0.8s linear infinite;
}

.card-list-area {
  position: relative;
  border: 1px solid #e5e5e5;
  border-radius: 0 0 14px 14px;
  background: #ffffff;
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
  border-bottom: 1px solid #eeeeee;
  background: #ffffff;
  box-sizing: border-box;
  overflow: visible;
}

.card-item:last-child {
  border-bottom: 0;
}

.card-image-area {
  display: flex;
  flex: none;
  width: 58px;
  height: 38px;
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
  border-radius: 6px;
  background: #f4f4f4;
  font-size: 22px;
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
  color: #222222;
  font-size: 14px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.primary-badge {
  flex: none;
  padding: 3px 7px;
  border: 1px solid #ffbc2e;
  border-radius: 8px;
  background: #fff9e9;
  color: #d78d00;
  font-size: 9px;
  font-weight: 800;
}

.card-info p {
  margin: 5px 0 0;
  color: #777777;
  font-size: 11px;
}

.card-info small {
  display: block;
  margin-top: 3px;
  color: #999999;
  font-size: 10px;
  letter-spacing: 0.3px;
}

.menu-button {
  flex: none;
  width: 34px;
  height: 42px;
  padding: 0;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #333333;
  font-size: 23px;
  line-height: 1;
  cursor: pointer;
}

.menu-button:active {
  background: #f5f5f5;
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
  border: 1px solid #eeeeee;
  border-radius: 11px;
  background: #ffffff;
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
  color: #333333;
  font-size: 12px;
  font-weight: 600;
  text-align: left;
  cursor: pointer;
}

.card-menu button:hover {
  background: #f7f7f7;
}

.card-menu .delete-button {
  color: #e53935;
}

.menu-icon {
  display: inline-flex;
  width: 18px;
  justify-content: center;
  font-size: 15px;
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
  font-size: 31px;
}

.empty-area strong {
  display: block;
  color: #222222;
  font-size: 17px;
  font-weight: 800;
}

.empty-area p {
  margin: 11px 0 0;
  color: #999999;
  font-size: 12px;
  line-height: 1.55;
}

.state-message {
  margin: 0;
  padding: 70px 20px;
  color: #777777;
  font-size: 13px;
  text-align: center;
}

.error-area {
  padding: 54px 20px;
  text-align: center;
}

.error-area p {
  margin: 0;
  color: #e53935;
  font-size: 13px;
  line-height: 1.5;
}

.error-area button {
  margin-top: 15px;
  padding: 8px 14px;
  border: 1px solid #dddddd;
  border-radius: 9px;
  background: #ffffff;
  color: #555555;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.connect-button {
  position: absolute;
  right: 28px;
  bottom: 58px;
  left: 28px;
  display: flex;
  width: auto;
  height: 58px;
  align-items: center;
  justify-content: center;
  gap: 5px;
  margin: 0;
  border: 1px dashed #cccccc;
  border-radius: 10px;
  background: #ffffff;
  color: #222222;
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
}

.connect-button span {
  font-size: 21px;
  font-weight: 500;
}

.connect-button:active {
  border-color: #ffbc2e;
  background: #fffaf0;
}

.toast-message {
  position: absolute;
  z-index: 20;
  right: 28px;
  bottom: 128px;
  left: 28px;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  padding: 14px 15px;
  border: 1px solid #dfeee2;
  border-radius: 10px;
  background: #f7fff8;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  color: #3c7350;
  font-size: 12px;
  font-weight: 600;
  box-sizing: border-box;
}

.toast-icon {
  display: inline-flex;
  width: 18px;
  height: 18px;
  flex: none;
  align-items: center;
  justify-content: center;
  border: 1px solid #42a866;
  border-radius: 50%;
  color: #42a866;
  font-size: 11px;
  font-weight: 800;
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
  position: relative;
  width: 100%;
  padding: 28px 20px 20px;
  border-radius: 18px;
  background: #ffffff;
  box-sizing: border-box;
}

.modal-close-button {
  position: absolute;
  top: 13px;
  right: 15px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #999999;
  font-size: 23px;
  cursor: pointer;
}

.warning-icon {
  display: flex;
  width: 48px;
  height: 48px;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  border-radius: 50%;
  background: #fff1d4;
  color: #ffae00;
  font-size: 25px;
  font-weight: 800;
}

.disconnect-modal h3 {
  margin: 18px 0 0;
  color: #222222;
  font-size: 18px;
  font-weight: 800;
  text-align: center;
}

.selected-card {
  display: flex;
  align-items: center;
  gap: 13px;
  margin-top: 22px;
  padding: 14px;
  border: 1px solid #eeeeee;
  border-radius: 12px;
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
  font-size: 23px;
}

.selected-card strong {
  display: block;
  color: #222222;
  font-size: 13px;
  font-weight: 800;
}

.selected-card p {
  margin: 5px 0 0;
  color: #999999;
  font-size: 10px;
}

.modal-guide {
  margin-top: 15px;
  padding: 14px;
  border-radius: 12px;
  background: #f7f7f7;
}

.modal-guide strong {
  color: #555555;
  font-size: 11px;
}

.modal-guide p {
  margin: 7px 0 0;
  color: #888888;
  font-size: 10px;
  line-height: 1.5;
}

.modal-button-area {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 9px;
  margin-top: 20px;
}

.modal-cancel-button,
.modal-delete-button {
  height: 48px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}

.modal-cancel-button {
  border: 1px solid #dddddd;
  background: #ffffff;
  color: #444444;
}

.modal-delete-button {
  border: 1px solid #ffc8c8;
  background: #ffeaea;
  color: #e53935;
}

.modal-cancel-button:disabled,
.modal-delete-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

@media (max-width: 360px) {
  .card-container {
    padding-right: 20px;
    padding-left: 20px;
  }

  .connect-button,
  .toast-message {
    right: 20px;
    left: 20px;
  }
}

@keyframes rotate {
  to {
    transform: rotate(360deg);
  }
}
</style>