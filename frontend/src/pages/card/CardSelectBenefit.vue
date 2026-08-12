<template>
  <div class="page">
    <PageHeaderStep
      :title="pageTitle"
      @back="handleGoBack"
      :custom-back="true"
    />

    <CardInfoEditor
      class="card-editor"
      :tabs="editorTabs"
      :tab-height="tapHeight"
    >
      <template #default="{ tab }">
        <!-- 단계를 오갈 때 입력 상태가 유지되도록 KeepAlive 추가 -->
        <KeepAlive>
          <component
            :is="currentComponent"
            :tab="tab"
            @update:isValid="(valid) => (isRegisterValid = valid)"
          />
        </KeepAlive>
      </template>
    </CardInfoEditor>

    <div class="bottom-btn-area single">
      <button
        class="bottom-btn"
        :disabled="isNextButtonDisabled"
        @click="handleButtonClick"
      >
        {{ btnText }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { useModalStore } from '@/stores/userModalStore';
const modalStore = useModalStore();
import { ref, computed } from 'vue';
import PageHeaderStep from '@/components/common/PageHeaderStep.vue';
import CardBenifitPanel from '@/components/card-editor/CardBenifitPanel.vue';
import CardInfoEditor from '@/components/card-editor/CardInfoEditor.vue';
import { useRouter } from 'vue-router';
import { useCardEditorStore } from '@/stores/cardEditorStore';

const cardStore = useCardEditorStore();

cardStore.cardName = '나만의 카드';
cardStore.createCardNumber();

const router = useRouter();

// 인덱스 초기값
const stepIndex = ref(0);

const editorTabs = computed(() => steps[stepIndex.value].tabs);
const pageTitle = computed(() => steps[stepIndex.value].title);
const tapHeight = computed(() => steps[stepIndex.value].tabHeight);
const currentStep = computed(() => steps[stepIndex.value]);
const currentComponent = computed(() => currentStep.value.component);
const btnText = computed(() => steps[stepIndex.value].buttonText);
const isRegisterValid = ref(false);

const steps = [
  {
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
];

// 마지막 단계일 때 폼이 미입력 상태면 버튼 비활성화
const isNextButtonDisabled = computed(() => {
  return false;
});

const handleGoBack = () => {
  router.push('/card/create/intro');
};

// 버튼 클릭 시 마지막 단계면 최종 신청 로직, 아니면 다음 단계로 이동
const handleButtonClick = () => {
  router.push('/card/check/issue');
};
</script>

<style scoped>
.page {
  width: 100%;
  /* 모바일 브라우저 주소창 이슈를 해결하기 위해 dvh 사용 */
  height: 100dvh;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  padding: 12px;
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
