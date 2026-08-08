<template>
  <div class="signup-page">
    <!-- 1. 상단 영역 (Header) -->
    <section class="signup-header">
      <h1>약관 동의</h1>
      <p>가입을 위해 약관에 동의해주세요.</p>
    </section>

    <!-- 2. 중앙 내용 영역 -->
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
        @update:model-value="
          (agreed) =>
            changeAgreement({ agreementType: agreement.agreementType, agreed })
        "
        @open-detail="showAgreementDetail(agreement.agreementType)"
      />

      <div class="divider bottom-divider"></div>

      <p v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </p>
    </section>

    <!-- 3. 하단 버튼 영역 -->
    <div class="bottom-btn-area.single">
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
.signup-page {
  width: 100%;
  height: 100dvh;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  /* 💡 하단 패딩을 20px -> 40px로 늘려 버튼 위치를 위로 올립니다 */
  padding: 36px 24px 70px;
  background: #ffffff;
}

/* 1. 상단 헤더 영역 */
.signup-header {
  flex-shrink: 0;
}

.signup-header h1 {
  margin: 0 0 16px;
  color: #111111;
  font-size: 28px;
  font-weight: 700;
}

.signup-header p {
  margin: 0;
  color: #777777;
  font-size: 15px;
  font-weight: 500;
  line-height: 1.4;
}

/* 2. 중앙 내용 영역 */
.agreement-section {
  flex: 1;
  min-height: 0;
  /* 💡 헤더와의 간격을 적당히 조절합니다 */
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
  background: #ffffff;
  box-sizing: border-box;
}

.all-agreement input:checked + .check-box {
  border-color: #ffbc2e;
  background: #ffbc2e;
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
  font-size: 17px;
  font-weight: 700;
  color: #111111;
}

.divider {
  height: 1px;
  margin: 12px 0 8px;
  background: #eeeeee;
}

.bottom-divider {
  margin-top: 8px;
}

.error-message {
  margin: 16px 0 0;
  color: #d32f2f;
  font-size: 14px;
}
</style>
