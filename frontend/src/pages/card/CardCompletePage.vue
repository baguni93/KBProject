<template>
  <div class="complete-page">
    <!-- 배경 장식 -->
    <div class="background-decoration decoration-left"></div>
    <div class="background-decoration decoration-right"></div>

    <!-- 메인 컨텐츠 -->
    <div class="content-body">
      <!-- 완료 애니메이션 -->
      <div class="success-visual">
        <span class="particle particle-1"></span>
        <span class="particle particle-2"></span>
        <span class="particle particle-3"></span>
        <span class="particle particle-4"></span>
        <span class="particle particle-5"></span>
        <span class="particle particle-6"></span>

        <span class="spark spark-1">
          <i class="fa-solid fa-star"></i>
        </span>

        <span class="spark spark-2">
          <i class="fa-solid fa-star"></i>
        </span>

        <div class="success-glow"></div>

        <div class="success-circle">
          <i class="fa-solid fa-check"></i>
        </div>
      </div>

      <!-- 완료 메시지 -->
      <div class="complete-message">
        <div class="main-title">
          카드 신청이<br />
          완료되었어요
        </div>

        <div class="sub-title">
          카드 자랑하기를 통해 나만의 카드를<br />
          친구들에게 알려보세요
        </div>
      </div>

      <!-- 발급된 카드 미리보기 -->
      <div class="card-preview-box">
        <CardCanvasPreview />
      </div>
    </div>

    <!-- 하단 버튼 영역 -->
    <div class="button-area">
      <button class="share-btn" @click="openModal">
        <i class="fa-solid fa-share-nodes"></i>
        카드 자랑하기
      </button>

      <button class="share-btn" @click="handleAddCard">
        간편 결제 연동하기
      </button>

      <button class="confirm-btn" @click="handleConfirm">확인</button>
    </div>
    <CardShareModal
      v-model="isModalOpen"
      @submit="handleShareSubmit"
      title="나만의 카드 자랑하기"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useCardEditorStore } from '@/stores/cardEditorStore';
import CardCanvasPreview from '@/components/card-editor/CardCanvasPreview.vue';
import CardShareModal from '../feed/components/CardShareModal.vue';
import { useFeedStore } from '@/stores/feed';
import { useModalStore } from '@/stores/userModalStore';
import { useWordFilterStore } from '@/stores/wordFilterStore.js';

const wordFilterStore = useWordFilterStore();
const useModal = useModalStore();

const feedStore = useFeedStore();

const router = useRouter();
const cardStore = useCardEditorStore();

const isModalOpen = ref(false);
const selectedScope = ref('PUBLIC');
const feedContent = ref('');

const openModal = () => {
  isModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
};

// 확인 버튼
const handleConfirm = () => {
  cardStore.reset();
  cardStore.history = [];
  router.push('/');
};

const handleAddCard = () => {
  cardStore.reset();
  cardStore.history = [];
  router.push('/wallet/card/add');
};

// 자랑하기 최종 제출
const handleShareSubmit = async (obj) => {
  console.log('내용:', obj.content);
  console.log('공개 범위:', obj.visibility);
  const trimmedText = obj.content.trim();
  if (!trimmedText) return;
  // 💡 공통 필터 스토어의 검증 함수 사용
  const validation = wordFilterStore.validateText(trimmedText);

  if (!validation.isValid) {
    await useModal.showAlert(validation.message);
    return; // 추가 중단
  }

  const fromData = feedStore.createRequestDTO({
    targetId: cardStore.customCardId,
    feedType: 'CARD',
    visibility: obj.visibility,
    content: obj.content,
  });

  await feedStore.createFeed(fromData);

  console.log('공유될 카드 데이터:', {
    name: cardStore.cardName,
    number: cardStore.cardNumber,
    englishName: cardStore.cardEnglishName,
  });

  closeModal();

  const result =
    await useModal.showSuccess('피드에 성공적으로 공유되었습니다!');

  if (result) {
    router.push('/feed');
  }
};
</script>

<style scoped>
/* =========================
   페이지
========================= */

.complete-page {
  width: 100%;
  height: 100%;

  display: flex;
  flex-direction: column;

  box-sizing: border-box;

  padding: 20px;

  position: relative;

  overflow: hidden;

  background: linear-gradient(180deg, #fffdf8 0%, #fffaf0 42%, #ffffff 100%);
}

/* =========================
   배경 장식
========================= */

.background-decoration {
  position: absolute;

  border-radius: 50%;

  pointer-events: none;
}

.decoration-left {
  top: -100px;
  left: -120px;

  width: 240px;
  height: 240px;

  background: rgba(255, 188, 46, 0.1);

  animation: backgroundFloatLeft 6s ease-in-out infinite;
}

.decoration-right {
  top: 280px;
  right: -110px;

  width: 210px;
  height: 210px;

  background: rgba(176, 164, 255, 0.05);

  animation: backgroundFloatRight 7s ease-in-out infinite;
}

/* =========================
   메인 컨텐츠
========================= */

.content-body {
  flex: 1;

  position: relative;
  z-index: 2;

  display: flex;
  flex-direction: column;

  align-items: center;
  justify-content: center;

  margin-top: -120px;

  gap: 8px;
}

/* =========================
   성공 애니메이션
========================= */

.success-visual {
  position: relative;

  width: 150px;
  height: 150px;

  margin-bottom: 2px;
}

/* 은은한 빛 */

.success-glow {
  position: absolute;

  top: 50%;
  left: 50%;

  width: 118px;
  height: 118px;

  border-radius: 50%;

  background: rgba(255, 188, 46, 0.16);

  transform: translate(-50%, -50%);

  animation: glow 2.2s ease-in-out 0.8s infinite;
}

/* 메인 체크 원 */

.success-circle {
  position: absolute;

  z-index: 2;

  top: 50%;
  left: 50%;

  display: flex;

  width: 92px;
  height: 92px;

  align-items: center;
  justify-content: center;

  border-radius: 50%;

  background: linear-gradient(145deg, #ffd15c, #ffc107);

  box-shadow:
    0 16px 34px rgba(255, 188, 46, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);

  color: #ffffff;

  font-size: 38px;

  transform: translate(-50%, -50%) scale(0);

  animation: successPop 0.55s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

.success-circle i {
  opacity: 0;

  transform: scale(0.5) rotate(-15deg);

  animation: checkAppear 0.35s ease 0.42s forwards;
}

/* =========================
   파티클
========================= */

.particle {
  position: absolute;

  z-index: 1;

  display: block;

  width: 8px;
  height: 8px;

  border-radius: 50%;

  background: #ffc107;

  opacity: 0;
}

.particle-1 {
  top: 24px;
  left: 20px;

  animation: particlePop 0.55s ease 0.35s forwards;
}

.particle-2 {
  top: 14px;
  right: 30px;

  width: 6px;
  height: 6px;

  animation: particlePop 0.55s ease 0.5s forwards;
}

.particle-3 {
  top: 70px;
  right: 2px;

  width: 10px;
  height: 10px;

  animation: particlePop 0.55s ease 0.4s forwards;
}

.particle-4 {
  right: 24px;
  bottom: 18px;

  width: 7px;
  height: 7px;

  animation: particlePop 0.55s ease 0.6s forwards;
}

.particle-5 {
  bottom: 20px;
  left: 22px;

  width: 6px;
  height: 6px;

  animation: particlePop 0.55s ease 0.48s forwards;
}

.particle-6 {
  top: 80px;
  left: 0;

  width: 9px;
  height: 9px;

  animation: particlePop 0.55s ease 0.58s forwards;
}

/* =========================
   별 장식
========================= */

.spark {
  position: absolute;

  z-index: 1;

  color: #ffd65c;

  opacity: 0;
}

.spark-1 {
  top: 12px;
  left: 58px;

  font-size: 11px;

  animation: sparkPop 0.55s ease 0.55s forwards;
}

.spark-2 {
  right: 36px;
  bottom: 12px;

  font-size: 9px;

  animation: sparkPop 0.55s ease 0.7s forwards;
}

/* =========================
   완료 메시지
========================= */

.complete-message {
  text-align: center;

  opacity: 0;

  transform: translateY(16px);

  animation: contentUp 0.5s ease 0.55s forwards;
}

.main-title {
  margin: 0;

  color: #111111;

  font-size: 24px;
  font-weight: 700;

  line-height: 1.4;

  letter-spacing: -0.8px;
}

.sub-title {
  margin-top: 12px;

  color: #777777;

  font-size: 14px;
  font-weight: 400;

  line-height: 1.65;
}

/* =========================
   카드 미리보기
========================= */

.card-preview-box {
  height: 180px;

  display: flex;

  align-items: center;
  justify-content: center;

  position: relative;

  margin-top: 4px;

  opacity: 0;

  transform: translateY(12px);

  animation: contentUp 0.5s ease 0.65s forwards;
}

/* =========================
   하단 버튼 영역
   기존 위치 유지
========================= */

.button-area {
  position: relative;

  z-index: 5;

  display: flex;
  flex-direction: column;

  gap: 10px;

  margin-top: auto;

  padding-bottom: 12px;
}

.share-btn {
  width: 100%;

  height: 48px;

  border-radius: 24px;

  background-color: #ffc107;

  border: none;

  color: #111111;

  font-size: 15px;
  font-weight: bold;

  cursor: pointer;

  display: flex;

  align-items: center;
  justify-content: center;

  gap: 6px;

  transition:
    transform 0.15s ease,
    opacity 0.15s ease;
}

.share-btn:active {
  transform: scale(0.98);
}

.confirm-btn {
  width: 100%;

  height: 48px;

  border-radius: 24px;

  background-color: #e9ecef;

  border: none;

  color: #495057;

  font-size: 15px;
  font-weight: bold;

  cursor: pointer;

  transition:
    transform 0.15s ease,
    opacity 0.15s ease;
}

.confirm-btn:active {
  transform: scale(0.98);
}

/* =========================
   모달
========================= */

.modal-backdrop {
  position: absolute;

  top: 0;
  left: 0;

  width: 100%;
  height: 100%;

  background-color: rgba(0, 0, 0, 0.5);

  display: flex;

  align-items: flex-end;

  z-index: 100;

  animation: fadeIn 0.2s ease-out;
}

.bottom-sheet {
  width: 100%;

  max-height: 88%;

  overflow-y: auto;

  background-color: #ffffff;

  border-top-left-radius: 24px;
  border-top-right-radius: 24px;

  padding: 16px 20px 24px;

  box-sizing: border-box;

  display: flex;

  flex-direction: column;

  gap: 16px;

  animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.sheet-handle {
  width: 40px;
  height: 4px;

  background-color: #dddddd;

  border-radius: 2px;

  align-self: center;

  margin-bottom: 4px;
}

.sheet-header {
  text-align: left;
}

.sheet-title {
  font-size: 16px;

  font-weight: bold;

  color: #111111;

  margin-bottom: 4px;
}

.sheet-desc {
  font-size: 13px;

  color: #888888;
}

/* =========================
   공개 범위
========================= */

.options-container {
  display: flex;

  flex-direction: column;

  gap: 10px;
}

.option-card {
  display: flex;

  align-items: center;
  justify-content: space-between;

  padding: 14px 16px;

  border-radius: 14px;

  border: 1.5px solid #eaeaea;

  background-color: #fafafa;

  cursor: pointer;

  transition: all 0.2s ease;
}

.option-card.active {
  border-color: #ffc107;

  background-color: #fffdf5;
}

.option-icon-text {
  display: flex;

  align-items: center;

  gap: 12px;
}

.icon-circle {
  font-size: 20px;

  width: 36px;
  height: 36px;

  background: #f0f0f0;

  border-radius: 50%;

  display: flex;

  align-items: center;
  justify-content: center;
}

.text-group {
  text-align: left;
}

.option-title {
  font-size: 15px;

  font-weight: bold;

  color: #222222;
}

.option-desc {
  font-size: 12px;

  color: #888888;

  margin-top: 2px;
}

.radio-icon {
  font-size: 18px;

  color: #ffc107;
}

.radio-empty {
  width: 18px;
  height: 18px;

  border: 2px solid #cccccc;

  border-radius: 50%;

  box-sizing: border-box;
}

/* =========================
   피드 내용 입력
========================= */

.feed-content-area {
  margin: 20px 0;

  display: flex;

  flex-direction: column;

  gap: 8px;
}

.feed-textarea {
  width: 100%;

  height: 80px;

  padding: 12px;

  border-radius: 12px;

  border: 1px solid #e5e7eb;

  background-color: #f9fafb;

  resize: none;

  font-size: 14px;

  box-sizing: border-box;

  outline: none;
}

.feed-textarea:focus {
  border-color: #ffc107;

  background-color: #fffdf5;
}

.char-count {
  text-align: right;

  font-size: 12px;

  color: #9ca3af;
}

/* =========================
   모달 버튼
========================= */

.sheet-action-area {
  display: flex;

  flex-direction: column;

  gap: 8px;

  margin-top: 8px;
}

.action-share-btn {
  width: 100%;

  height: 48px;

  border-radius: 24px;

  background-color: #ffc107;

  border: none;

  color: #111111;

  font-size: 16px;

  font-weight: bold;

  cursor: pointer;
}

.action-cancel-btn {
  width: 100%;

  height: 40px;

  background: transparent;

  border: none;

  color: #777777;

  font-size: 14px;

  font-weight: 600;

  cursor: pointer;
}

/* =========================
   애니메이션
========================= */

@keyframes successPop {
  0% {
    transform: translate(-50%, -50%) scale(0);
  }

  70% {
    transform: translate(-50%, -50%) scale(1.08);
  }

  100% {
    transform: translate(-50%, -50%) scale(1);
  }
}

@keyframes checkAppear {
  from {
    opacity: 0;

    transform: scale(0.5) rotate(-15deg);
  }

  to {
    opacity: 1;

    transform: scale(1) rotate(0);
  }
}

@keyframes particlePop {
  0% {
    opacity: 0;

    transform: scale(0);
  }

  60% {
    opacity: 1;

    transform: scale(1.4);
  }

  100% {
    opacity: 0.65;

    transform: scale(1);
  }
}

@keyframes sparkPop {
  0% {
    opacity: 0;

    transform: scale(0) rotate(-40deg);
  }

  60% {
    opacity: 1;

    transform: scale(1.4) rotate(12deg);
  }

  100% {
    opacity: 0.75;

    transform: scale(1) rotate(0);
  }
}

@keyframes glow {
  0%,
  100% {
    opacity: 0.55;

    transform: translate(-50%, -50%) scale(0.95);
  }

  50% {
    opacity: 1;

    transform: translate(-50%, -50%) scale(1.1);
  }
}

@keyframes contentUp {
  from {
    opacity: 0;

    transform: translateY(16px);
  }

  to {
    opacity: 1;

    transform: translateY(0);
  }
}

@keyframes backgroundFloatLeft {
  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(12px, 10px);
  }
}

@keyframes backgroundFloatRight {
  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(-10px, -8px);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }

  to {
    transform: translateY(0);
  }
}

/* =========================
   모션 최소화
========================= */

@media (prefers-reduced-motion: reduce) {
  .background-decoration,
  .success-circle,
  .success-circle i,
  .success-glow,
  .particle,
  .spark,
  .complete-message,
  .card-preview-box {
    opacity: 1;

    animation: none;

    transform: none;
  }

  .success-circle {
    transform: translate(-50%, -50%);
  }

  .success-glow {
    transform: translate(-50%, -50%);
  }
}

/* =========================
   작은 화면
========================= */

@media (max-width: 360px) {
  .complete-page {
    padding: 16px;
  }

  .content-body {
    margin-top: -80px;
  }

  .success-visual {
    width: 135px;
    height: 135px;
  }

  .success-circle {
    width: 84px;
    height: 84px;

    font-size: 34px;
  }

  .success-glow {
    width: 108px;
    height: 108px;
  }

  .main-title {
    font-size: 22px;
  }

  .sub-title {
    font-size: 13px;
  }

  .card-preview-box {
    height: 160px;
  }
}
</style>
