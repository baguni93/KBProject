<template>
  <div class="page">
    <PageHeaderStep
      :title="pageTitle"
      @back="handleGoBack"
      :custom-back="true"
    />

    <CardEditor
      class="card-editor"
      :tabs="editorTabs"
      :tab-height="tapHeight"
      ref="editorRef"
      :selected-benefit="selectedBenefitData"
    >
      <template #default="{ tab }">
        <!-- 단계를 오갈 때 입력 상태가 유지되도록 KeepAlive 추가 -->
        <KeepAlive>
          <component
            :is="currentComponent"
            :tab="tab"
            :action-trigger="actionTrigger"
            @update:isValid="
              (valid) => {
                // 단계별로 유효성 변수를 다르게 분기 처리
                if (currentIndex === 5) isRegisterValid = valid;
                else if (currentIndex === 3) isAccountValid = valid;
                else if (currentIndex === 0) isBenefitValid = valid;
              }
            "
            @update:loading="(val) => (isAccountLoading = val)"
            @next="handleStepNext"
            @failed="handleStepFailed"
            @select-benefit="handleBenefitSelected"
          />
        </KeepAlive>
      </template>
    </CardEditor>

    <div class="bottom-btn-area single">
      <button
        class="bottom-btn"
        :disabled="isNextButtonDisabled"
        @click="handleButtonClick"
      >
        {{ isAccountLoading ? '확인 중...' : btnText }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { useModalStore } from '@/stores/userModalStore';
const modalStore = useModalStore();
import { ref, computed } from 'vue';
import PageHeaderStep from '@/components/common/PageHeaderStep.vue';
import CardEditor from '@/components/card-editor/CardEditor.vue';
import CardBackgroundPanel from '@/components/card-editor/CardBackgroundPanel.vue';
import CardDecorationPanel from '@/components/card-editor/CardDecorationPanel.vue';
import CardRegisterPanel from '@/components/card-editor/CardRegisterPanel.vue';
import CardBenifitPanel from '@/components/card-editor/CardBenifitPanel.vue';
import CardCheckCanIssuePage from './CardCheckCanIssuePage.vue';
import CardIssueVerificationPage from './CardIssueVerification.page.vue';
import { useRouter } from 'vue-router';
import { useCardEditorStore } from '@/stores/cardEditorStore';
import customCardApi from '@/api/customCard.Api.js';
import { useAuthStore } from '@/stores/auth.js';
const authStore = useAuthStore();
const userId = authStore.userId;

const cardStore = useCardEditorStore();

cardStore.cardName = '나만의 카드';
cardStore.createCardNumber();

const router = useRouter();

// 💡 1. 최종 선택된 혜택 데이터를 담을 상태 추가
const selectedBenefitData = ref(null); // 선택된 혜택 데이터
const isBenefitValid = ref(false); // 0단계 유효성 상태

// 💡 2. 자식/에디터에서 혜택이 선택되었을 때 호출될 함수 추가
const handleBenefitSelected = (benefitPack) => {
  selectedBenefitData.value = benefitPack; // 이 값이 바뀌면 에디터 뱃지가 뜸
  console.log('최종 선택된 혜택:', benefitPack);
};

// 인덱스 초기값
const stepIndex = ref(0);

const currentIndex = computed(() => steps[stepIndex.value].index);
const editorTabs = computed(() => steps[stepIndex.value].tabs);
const pageTitle = computed(() => steps[stepIndex.value].title);
const tapHeight = computed(() => steps[stepIndex.value].tabHeight);
const currentStep = computed(() => steps[stepIndex.value]);
const currentComponent = computed(() => currentStep.value.component);
const btnText = computed(() => steps[stepIndex.value].buttonText);

// 상태 변수들
const isRegisterValid = ref(false);
const isAccountValid = ref(false); // 3단계 유효성 상태
const isAccountLoading = ref(false); // 3단계 로딩 상태
const actionTrigger = ref(0); // 자식에게 실행 신호를 주는 트리거
const editorRef = ref(null);

const steps = [
  {
    index: 0,
    title: '나만의 혜택 선택',
    buttonText: '다음',
    component: CardBenifitPanel,
    tabHeight: '42px',
    tabs: [
      { key: 'lifestyle', label: '디지털/구독' },
      { key: 'shopping', label: '쇼핑/뷰티' },
      { key: 'daily', label: '생활/교통' },
    ],
  },
  {
    index: 1,
    title: '카드 배경 선택',
    buttonText: '다음',
    component: CardBackgroundPanel,
    tabHeight: '42px',
    tabs: [
      { key: 'basic', label: '기본' },
      { key: 'photo', label: '내 사진' },
    ],
  },
  {
    index: 2,
    title: '카드 꾸미기',
    buttonText: '다음',
    component: CardDecorationPanel,
    tabHeight: '60px',
    tabs: [
      { key: 'pattern', label: '패턴', icon: 'fa-solid fa-shapes' },
      { key: 'text', label: '텍스트', icon: 'fa-solid fa-font' },
      { key: 'emoji', label: '이모지', icon: 'fa-regular fa-face-smile' },
      { key: 'drawing', label: '그리기', icon: 'fa-solid fa-pencil' },
    ],
  },
  {
    index: 3,
    title: '계좌 확인',
    buttonText: '1원 송금',
    component: CardCheckCanIssuePage,
    tabHeight: '',
    tabs: [],
  },
  {
    index: 4,
    title: '계좌 확인',
    buttonText: '다음',
    component: CardIssueVerificationPage,
    tabHeight: '',
    tabs: [],
  },
  {
    index: 5,
    title: '카드 정보 입력',
    buttonText: '신청하기',
    component: CardRegisterPanel,
    tabHeight: '',
    tabs: [],
  },
];

// 버튼 비활성화 제어
const isNextButtonDisabled = computed(() => {
  const isLastStep = stepIndex.value === steps.length - 1;

  if (isLastStep) {
    return !isRegisterValid.value;
  }

  // 3단계(계좌 확인)일 때: 자식이 알려준 유효성값과 로딩 상태 반영
  if (currentIndex.value === 3) {
    return !isAccountValid.value || isAccountLoading.value;
  } else if (currentIndex.value === 0) {
    return !isBenefitValid.value; // 혜택이 선택 안 되었으면 비활성화!
  }

  return false;
});

const handleGoBack = async () => {
  if (stepIndex.value > 0) {
    actionTrigger.value = 0;
    stepIndex.value--;
    return;
  } else {
    const isConfirmed = await modalStore.showConfirm(
      '카드 신청을 취소하시겠습니까?',
      '카드 신청',
    );
    if (!isConfirmed) {
      return;
    }
    cardStore.reset();
    router.push('/card/create/intro');
  }
};

// 자식이 API 성공 후 @next 이벤트를 쏘았을 때 다음 단계로 이동
const handleStepNext = () => {
  if (stepIndex.value < steps.length - 1) {
    actionTrigger.value = 0;
    stepIndex.value++;
    cardStore.saveStep();
  }
};

const handleStepFailed = () => {
  actionTrigger.value = 0;
  console.log('test');
};

//카드 데이터 저장
const handleSave = async () => {
  // 1. 서버에 보낼 데이터 준비
  try {
    // 2. API 호출
    console.log(editorRef.value.childRef);
    const cardImageFormData = await editorRef.value.childRef.uploadCardImage();
    const response = await cardStore.createCardPayload(
      userId,
      cardImageFormData,
    );
    console.log('저장 성공:', response.data);
  } catch (error) {
    console.error('저장 실패:', error);
  }
};

//가상 실물 카드 테이블에 추가

// 버튼 클릭 핸들러
const handleButtonClick = async () => {
  const isLastStep = stepIndex.value === steps.length - 1;

  if (isLastStep) {
    cardStore.saveStep();

    await handleSave();

    cardStore.reset();
    router.push('/card/complete');

    console.log('최종 제출 데이터:', cardStore.history);
    return;
  }

  // 3단계(계좌 확인)일 때는 ref 대신 트리거 값을 올려서 자식에게 실행 신호 전달
  if (currentIndex.value === 3 || currentIndex.value === 4) {
    actionTrigger.value = 1;
    return;
  }

  // 그 외 일반 단계 이동 (0, 1, 2 단계)
  if (stepIndex.value < steps.length - 1) {
    stepIndex.value++;
    cardStore.saveStep();
  }
};

const handleOuterClick = async () => {
  // editorRef.value.childRef를 통해 손자(CardCanvas)의 함수 바로 호출!
  if (editorRef.value?.childRef?.testDownloadCard) {
    await editorRef.value.childRef.testDownloadCard();
    console.log('부모가 손자의 캡처 기능을 직접 실행했습니다!');
  }
};
</script>

<style scoped>
.page {
  width: 100%;
  /* 모바일 브라우저 주소창 이슈를 해결하기 위해 dvh 사용 */
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  padding: 0 20px 30px;
}

.card-editor {
  flex: 1;
  min-height: 0;
  margin-top: 12px;
  overflow: hidden;
}

.button-area {
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px 0 8px;
  /* ⚠️ 기존에 잘못 들어갔던 :active 효과 제거 완료 */
}

.next-btn {
  width: 85%;
  height: 46px;
  border: none;
  border-radius: 14px;
  background: #ffc400;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

/* 💡 클릭 효과(active)는 컨테이너가 아닌 버튼 자체에 적용 */
.next-btn:active:not(:disabled) {
  background: #f3aa0b;
}

/* 비활성화 버튼 스타일 (선택 사항이지만 추가해두면 직관적입니다) */
.next-btn:disabled {
  background: #e0e0e0;
  color: #9e9e9e;
  cursor: not-allowed;
}
</style>
