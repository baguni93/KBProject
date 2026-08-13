<template>
  <div class="signup-page page-layout">
    <!-- 공통 상단바 -->
    <PageHeader
        :show-back="false"
        :show-refresh="false"
    />

    <!-- 상단 영역 -->
    <section class="signup-header">
      <h1 class="text-28-bold">약관 동의</h1>
      <p class="text-15">가입을 위해 약관에 동의해주세요.</p>
    </section>

    <!-- 중앙 내용 -->
    <section class="agreement-section page-content">
      <label class="all-agreement">
        <input
            :checked="signupStore.isAllChecked"
            type="checkbox"
            @change="changeAll"
        />

        <span class="check-box"></span>
        <strong class="text-18-bold">전체 동의</strong>
      </label>

      <div class="divider"></div>

      <AgreementCheckItem
          v-for="(agreement, index) in signupStore.agreements"
          :key="agreement.agreementId"
          :model-value="agreement.agreed"
          :title="agreement.agreementName"
          :required="agreement.requiredYn === 'Y'"
          :last="index === signupStore.agreements.length - 1"
          detail-mode="navigate"
          @update:model-value="(agreed) => changeAgreement({ agreementType: agreement.agreementType, agreed })"
          @open-detail="showAgreementDetail(agreement.agreementType)"
      />

      <div class="divider bottom-divider"></div>

      <p v-if="errorMessage" class="error-message text-13">
        {{ errorMessage }}
      </p>
    </section>

    <!-- 하단 버튼 -->
    <div class="bottom-btn-area single">
      <button
          class="bottom-btn"
          :disabled="!signupStore.isRequiredChecked"
          type="button"
          @click="next"
      >
        다음
      </button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import agreementApi from '@/api/agreementApi';
import AgreementCheckItem from '@/components/common/AgreementCheckItem.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import { useSignupStore } from '@/stores/signup';

const router = useRouter();
const signupStore = useSignupStore();
const errorMessage = ref('');

// 약관 조회
const loadAgreements = async () => {
  try {
    const agreements = await agreementApi.getAgreements();
    signupStore.setAgreements(agreements);
  } catch (error) {
    console.error(error);
    errorMessage.value = '약관을 불러오지 못했습니다.';
  }
};

// 전체 동의
const changeAll = (event) => {
  signupStore.setAllAgreements(event.target.checked);
};

// 개별 동의
const changeAgreement = ({ agreementType, agreed }) => {
  signupStore.setAgreementChecked(agreementType, agreed);
};

// 약관 상세 이동
const showAgreementDetail = (agreementType) => {
  router.push(`/signup/agreement/${agreementType}`);
};

// 다음 화면
const next = () => {
  router.push('/signup/check');
};

onMounted(loadAgreements);
</script>

<style scoped>
@import "@/components/common/common/common.css";
@import "@/components/common/common/layout.css";

.signup-page {
  background: var(--color-bg-page);
}

.signup-header {
  flex-shrink: 0;
  margin-top: 24px;
}

.signup-header h1 {
  margin: 0 0 16px;
  color: var(--color-text-main);
}

.signup-header p {
  margin: 0;
  color: var(--color-text-sub);
  line-height: 1.4;
}

.agreement-section {
  margin-top: 28px;
  overflow-y: auto;
  box-sizing: border-box;
  padding-right: 2px;
}

.all-agreement {
  display: flex;
  align-items: center;
  min-height: 48px;
  cursor: pointer;
}

.all-agreement input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
}

.check-box {
  flex-shrink: 0;
  width: 26px;
  height: 26px;
  margin-right: 14px;
  border: 1px solid #999999;
  border-radius: 6px;
  background: var(--color-bg-page);
  box-sizing: border-box;
}

.all-agreement input:checked + .check-box {
  border-color: var(--color-primary);
  background: var(--color-primary);
}

.all-agreement input:checked + .check-box::after {
  display: block;
  width: 7px;
  height: 13px;
  margin: 4px 0 0 8px;
  border: solid #ffffff;
  border-width: 0 2px 2px 0;
  content: '';
  transform: rotate(45deg);
}

.all-agreement strong {
  color: var(--color-text-main);
}

.divider {
  height: 1px;
  margin: 12px 0 8px;
  background: var(--color-divider);
}

.bottom-divider {
  margin-top: 8px;
}

.error-message {
  margin: 16px 0 0;
  color: var(--color-error);
}
</style>