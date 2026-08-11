<template>
  <div class="complete-page">
    <!-- 메인 컨텐츠 영역 (폭죽 파티클 및 안내문구) -->
    <div class="content-body">
      <div class="confetti-area">
        <div class="congrats-icon">🎉</div>
        <div class="main-title">카드 신청이 완료되었습니다!</div>
        <div class="sub-title">
          카드 자랑하기를 통해 나만의 카드를<br />친구들에게 알려보세요
        </div>
      </div>

      <!-- 발급된 카드 미리보기 (정중앙 정렬) -->
      <div class="card-preview-box">
        <CardCanvasPreview ref="childRef" />
      </div>
    </div>

    <!-- 하단 버튼 영역 -->
    <div class="button-area">
      <button class="share-btn" @click="openModal">
        <i class="fa-solid fa-share-nodes"></i> 카드 자랑하기
      </button>
      <button class="share-btn" @click="handleAddCard">
        간편 결제 연동하기
      </button>
      <!-- 부모 화면에 있는 버튼에서 자식 함수를 호출 -->
      <button @click="handleParentClick">
        부모가 자식에게 캡처 명령 내리기
      </button>
      <button class="confirm-btn" @click="handleConfirm">확인</button>
    </div>

    <!-- 카드 자랑하기 모달 (바텀 시트) -->
    <div v-if="isModalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="bottom-sheet">
        <div class="sheet-handle"></div>

        <div class="sheet-header">
          <div class="sheet-title">카드 자랑하기</div>
          <div class="sheet-desc">피드에 공유할 공개 범위를 선택해 주세요</div>
        </div>

        <!-- 공개 범위 선택 옵션 -->
        <div class="options-container">
          <!-- 전체 공개 -->
          <div
            class="option-card"
            :class="{ active: selectedScope === 'public' }"
            @click="selectedScope = 'public'"
          >
            <div class="option-icon-text">
              <div class="icon-circle">🌍</div>
              <div class="text-group">
                <div class="option-title">전체 공개</div>
                <div class="option-desc">
                  모든 사용자가 피드에서 볼 수 있어요
                </div>
              </div>
            </div>
            <div class="radio-icon">
              <i
                v-if="selectedScope === 'public'"
                class="fa-solid fa-circle-check"
              ></i>
              <div v-else class="radio-empty"></div>
            </div>
          </div>

          <!-- 친구 공개 -->
          <div
            class="option-card"
            :class="{ active: selectedScope === 'friends' }"
            @click="selectedScope = 'friends'"
          >
            <div class="option-icon-text">
              <div class="icon-circle">👥</div>
              <div class="text-group">
                <div class="option-title">친구 공개</div>
                <div class="option-desc">
                  친구로 등록된 사용자만 볼 수 있어요
                </div>
              </div>
            </div>
            <div class="radio-icon">
              <i
                v-if="selectedScope === 'friends'"
                class="fa-solid fa-circle-check"
              ></i>
              <div v-else class="radio-empty"></div>
            </div>
          </div>
        </div>

        <!-- 모달 내부 액션 버튼 -->
        <div class="sheet-action-area">
          <button class="action-share-btn" @click="handleShareSubmit">
            자랑하기
          </button>

          <button class="action-cancel-btn" @click="closeModal">취소</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useCardEditorStore } from '@/stores/cardEditorStore';
import CardCanvasPreview from '@/components/card-editor/CardCanvasPreview.vue';

const router = useRouter();
const cardStore = useCardEditorStore();

const isModalOpen = ref(false);
const selectedScope = ref('public'); // 'public' 또는 'friends'

const openModal = () => {
  isModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
};

// 확인 버튼 (메인으로 이동 또는 초기화)
const handleConfirm = () => {
  cardStore.reset();
  cardStore.history = [];
  router.push('/'); // 이동할 메인 페이지 경로로 수정하세요
};

const handleAddCard = () => {
  cardStore.reset();
  cardStore.history = [];
  router.push('/wallet/card/add'); // 이동할 메인 페이지 경로로 수정하세요
};

// 자랑하기 최종 제출
const handleShareSubmit = () => {
  console.log('공개 범위:', selectedScope.value);
  console.log('공유될 카드 데이터:', {
    name: cardStore.cardName,
    number: cardStore.cardNumber,
    englishName: cardStore.cardEnglishName,
  });

  alert('피드에 성공적으로 공유되었습니다!');
  closeModal();
  router.push('/'); // 공유 후 이동할 페이지
};

const childRef = ref(null);

// 2. 부모 버튼을 눌렀을 때 실행될 함수
const handleParentClick = async () => {
  if (childRef.value?.testDownloadCard) {
    // 자식 안에 있는 캡처 함수 실행!
    await childRef.value.testDownloadCard();
    console.log('부모가 자식의 캡처 기능을 성공적으로 호출했습니다.');
  } else {
    console.log('자식 컴포넌트가 아직 준비되지 않았거나 함수가 없습니다.');
  }
};
</script>

<style scoped>
.complete-page {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  box-sizing: border-box;
  padding: 16px;
  position: relative;
  overflow: hidden;
}

.page-header {
  font-size: 16px;
  font-weight: bold;
  color: #111;
  text-align: left;
  margin-bottom: 24px;
}

.content-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 24px;
}

.confetti-area {
  text-align: center;
}

.congrats-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.main-title {
  font-size: 20px;
  font-weight: bold;
  color: #111;
  margin-bottom: 6px;
}

.sub-title {
  font-size: 13px;
  color: #777;
  line-height: 1.4;
}

/* 🚀 카드 미리보기 박스 (정중앙 정렬 및 크기 고정) */
.card-preview-box {
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

/* 하단 버튼 영역 */
.button-area {
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
  color: #111;
  font-size: 15px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
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
}

/* 바텀 시트 모달 스타일 */
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
  background-color: #ffffff;
  border-top-left-radius: 24px;
  border-top-right-radius: 24px;
  padding: 16px 20px 24px 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 16px;
  animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.sheet-handle {
  width: 40px;
  height: 4px;
  background-color: #ddd;
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
  color: #111;
  margin-bottom: 4px;
}

.sheet-desc {
  font-size: 12px;
  color: #888;
}

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
  font-size: 14px;
  font-weight: bold;
  color: #222;
}

.option-desc {
  font-size: 11px;
  color: #888;
  margin-top: 2px;
}

.radio-icon {
  font-size: 18px;
  color: #ffc107;
}

.radio-empty {
  width: 18px;
  height: 18px;
  border: 2px solid #ccc;
  border-radius: 50%;
  box-sizing: border-box;
}

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
  color: #111;
  font-size: 15px;
  font-weight: bold;
  cursor: pointer;
}

.action-cancel-btn {
  width: 100%;
  height: 40px;
  background: transparent;
  border: none;
  color: #777;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
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
</style>
