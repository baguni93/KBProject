<template>
  <div class="page">
    <PageHeaderStep
      class="page-title"
      :title="pageTitle"
      v-on:go-back="handleGoBack"
    />

    <CardEditor class="card-editor" :tabs="editorTabs" :tab-height="tapHeight">
      <template #default="{ tab }">
        <component
          :is="currentComponent"
          :tab="tab"
          @update:isValid="(valid) => (isRegisterValid = valid)"
        />
      </template>
    </CardEditor>

    <div class="button-area">
      <button
        class="next-btn"
        :disabled="isNextButtonDisabled"
        @click="handleButtonClick"
      >
        {{ btnText }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

import PageHeaderStep from '@/components/common/PageHeaderStep.vue';
import CardEditor from '@/components/card-editor/CardEditor.vue';
import CardBackgroundPanel from '@/components/card-editor/CardBackgroundPanel.vue';
import CardDecorationPanel from '@/components/card-editor/CardDecorationPanel.vue';
import CardRegisterPanel from '@/components/card-editor/CardRegisterPanel.vue';
import { useRouter } from 'vue-router';
import { useCardEditorStore } from '@/stores/cardEditorStore';

const cardStore = useCardEditorStore();

cardStore.cardName = '나만의 카드';
cardStore.createCardNumber();

const router = useRouter();

//인덱스 초기값 잡음
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
    title: '카드 꾸미기',
    buttonText: '다음',
    component: CardDecorationPanel,
    tabHeight: '60px',
    tabs: [
      {
        key: 'pattern',
        label: '패턴',
        icon: 'fa-solid fa-shapes',
      },
      {
        key: 'text',
        label: '텍스트',
        icon: 'fa-solid fa-font',
      },
      {
        key: 'emoji',
        label: '이모지',
        icon: 'fa-regular fa-face-smile',
      },
      {
        key: 'drawing',
        label: '그리기',
        icon: 'fa-solid fa-pencil',
      },
    ],
  },
  {
    title: '신청하기',
    buttonText: '신청하기',
    component: CardRegisterPanel,
    tabHeight: '',
    tabs: [],
  },
];

// 💡 마지막 단계일 때 폼이 미입력 상태면 버튼 비활성화
const isNextButtonDisabled = computed(() => {
  const isLastStep = stepIndex.value === steps.length - 1;
  if (isLastStep) {
    return !isRegisterValid.value;
  }
  return false;
});

const handleGoBack = () => {
  if (stepIndex.value > 0) {
    stepIndex.value--;

    return;
  } else {
    cardStore.reset();
    router.back();
  }
};

// 💡 버튼 클릭 시 마지막 단계면 최종 신청 로직, 아니면 다음 단계로 이동
const handleButtonClick = () => {
  const isLastStep = stepIndex.value === steps.length - 1;

  if (isLastStep) {
    cardStore.saveStep();
    cardStore.reset();
    router.push('/card/complete');
    console.log('최종 제출 데이터:', cardStore.history);
  } else {
    if (stepIndex.value < steps.length - 1) {
      stepIndex.value++;
      cardStore.saveStep();
    }
  }
};
</script>

<style scoped>
.page {
  width: 100%;

  height: 100%;

  display: flex;

  flex-direction: column;

  min-height: 0;

  box-sizing: border-box;

  overflow: hidden;

  padding: 12px;
}

.page-title {
  flex-shrink: 0;

  margin-top: 12px;
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
}

.next-btn {
  width: 85%;

  height: 46px;

  border: none;

  border-radius: 14px;

  background: #ffc400;

  font-size: 15px;

  font-weight: 700;
}
</style>
