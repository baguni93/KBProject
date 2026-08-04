<template>
  <div class="detail-page">
    <main class="detail-container">
      <button class="back-button" type="button" @click="goBack">
        &lt;
      </button>

      <section v-if="loading" class="status-message">
        약관을 불러오는 중입니다.
      </section>

      <section v-else-if="errorMessage" class="status-message error-message">
        {{ errorMessage }}
      </section>

      <section v-else class="detail-content">
        <header class="detail-header">
          <h1>{{ agreement.agreementName }}</h1>

          <span
              :class="[
              'agreement-type',
              agreement.requiredYn === 'Y' ? 'required' : 'optional',
            ]"
          >
            {{ agreement.requiredYn === 'Y' ? '필수 약관' : '선택 약관' }}
          </span>
        </header>

        <div class="agreement-scroll">
          {{ agreement.agreementContent }}
        </div>

        <label class="consent-label">
          <input
              :checked="isAgreed"
              type="checkbox"
              @change="changeAgreement"
          />

          <span class="check-box"></span>
          <span>위 약관에 동의합니다.</span>
        </label>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import agreementApi from '@/api/agreementApi';
import { useSignupStore } from '@/stores/signup';

const route = useRoute();
const router = useRouter();
const signupStore = useSignupStore();

const agreement = ref({});
const loading = ref(false);
const errorMessage = ref('');

const isAgreed = computed(() => {
  const item = signupStore.agreements.find(
      (agreement) => agreement.agreementType === route.params.agreementType,
  );

  return item?.agreed ?? false;
});

// 약관 상세 조회
const loadAgreement = async () => {
  try {
    loading.value = true;
    agreement.value = await agreementApi.getAgreementDetail(
        route.params.agreementType,
    );
  } catch (error) {
    console.error(error);
    errorMessage.value = '약관 내용을 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

// 약관 동의
const changeAgreement = (event) => {
  signupStore.setAgreementChecked(
      route.params.agreementType,
      event.target.checked,
  );
};

// 이전 화면
const goBack = () => {
  router.back();
};

onMounted(loadAgreement);
</script>

<style scoped>
.detail-page {
  display: flex;
  justify-content: center;
  min-height: 100vh;
  padding: 24px 0;
  background: #f4f4f4;
  overflow: auto;
}

.detail-container {
  position: relative;
  display: flex;
  flex: none;
  flex-direction: column;
  width: 390px;
  height: 844px;
  min-height: 844px;
  padding: 24px 24px 28px;
  background: #ffffff;
  overflow: hidden;
}

.back-button {
  align-self: flex-start;
  margin-bottom: 22px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 28px;
  line-height: 1;
}

.detail-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.detail-header {
  flex-shrink: 0;
}

.detail-header h1 {
  margin: 0 0 20px;
  color: #111111;
  font-size: 25px;
  font-weight: 700;
  line-height: 1.35;
}

.agreement-type {
  display: inline-block;
  margin-bottom: 20px;
  font-size: 15px;
  font-weight: 600;
}

.required {
  color: #ef3d3d;
}

.optional {
  color: #777777;
}

.agreement-scroll {
  flex: 1;
  min-height: 0;
  padding: 18px;
  border: 1px solid #dddddd;
  border-radius: 8px;
  color: #333333;
  font-size: 15px;
  line-height: 1.8;
  white-space: pre-wrap;
  overflow-y: auto;
}

.consent-label {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  min-height: 64px;
  margin-top: 16px;
  border-top: 1px solid #dddddd;
  cursor: pointer;
}

.consent-label input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
}

.check-box {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  margin-right: 12px;
  border: 1px solid #999999;
  border-radius: 5px;
  background: #ffffff;
}

.consent-label input:checked + .check-box {
  border-color: #ffbc2e;
  background: #ffbc2e;
}

.consent-label input:checked + .check-box::after {
  display: block;
  width: 7px;
  height: 12px;
  margin: 4px 0 0 8px;
  border: solid #ffffff;
  border-width: 0 2px 2px 0;
  content: '';
  transform: rotate(45deg);
}

.consent-label span:last-child {
  color: #222222;
  font-size: 16px;
}

.status-message {
  padding-top: 100px;
  color: #777777;
  text-align: center;
}

.error-message {
  color: #d32f2f;
}
</style>