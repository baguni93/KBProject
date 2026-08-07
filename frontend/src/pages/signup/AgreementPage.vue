<template>
  <div class="signup-page">
    <main class="signup-container">
      <section class="signup-header">
        <h1>약관 동의</h1>

        <p>
          가입을 위해 약관에<br />
          동의해주세요.
        </p>
      </section>

      <section class="agreement-section">
        <label class="all-agreement">
          <input
              :checked="signupStore.isAllChecked"
              type="checkbox"
              @change="changeAll"
          />

          <span class="check-box"></span>
          <strong>전체 동의</strong>
        </label>

        <div class="divider"></div>

        <AgreementCheckItem
            v-for="agreement in signupStore.agreements"
            :key="agreement.agreementId"
            :model-value="agreement.agreed"
            :title="agreement.agreementName"
            :required="agreement.requiredYn === 'Y'"
            detail-mode="navigate"
            @update:model-value="(agreed) => changeAgreement({ agreementType: agreement.agreementType, agreed })"
            @open-detail="showAgreementDetail(agreement.agreementType)"
        />

        <div class="divider bottom-divider"></div>
      </section>

      <p
          v-if="errorMessage"
          class="error-message"
      >
        {{ errorMessage }}
      </p>

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
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import agreementApi from '@/api/agreementApi';
import AgreementCheckItem from '@/components/common/AgreementCheckItem.vue';
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
  signupStore.setAgreementChecked(
      agreementType,
      agreed,
  );
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
.signup-page {
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.signup-container {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-height: 0;
  padding: 82px 28px 140px;
  background: #ffffff;
  box-sizing: border-box;
}

.signup-header h1 {
  margin: 0 0 28px;
  color: #111111;
  font-size: 32px;
  font-weight: 700;
}

.signup-header p {
  margin: 0;
  color: #777777;
  font-size: 20px;
  font-weight: 600;
  line-height: 1.35;
}

.agreement-section {
  margin-top: 66px;
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
  width: 28px;
  height: 28px;
  margin-right: 14px;
  border: 1px solid #999999;
  border-radius: 6px;
  background: #ffffff;
  box-sizing: border-box;
}

.all-agreement input:checked + .check-box {
  border-color: #ffbc2e;
  background: #ffbc2e;
}

.all-agreement input:checked + .check-box::after {
  display: block;
  width: 8px;
  height: 14px;
  margin: 4px 0 0 9px;
  border: solid #ffffff;
  border-width: 0 2px 2px 0;
  content: '';
  transform: rotate(45deg);
}

.all-agreement strong {
  font-size: 18px;
  font-weight: 700;
}

.divider {
  height: 1px;
  margin: 20px 0 12px;
  background: #dddddd;
}

.bottom-divider {
  margin-top: 12px;
}

.error-message {
  margin: 16px 0 0;
  color: #d32f2f;
  font-size: 14px;
}
</style>