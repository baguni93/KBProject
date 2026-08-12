<template>
  <div class="page page-layout">
    <!-- 상단 헤더 -->
    <header class="header-area">
      <PageHeader
          :title="loading ? '불러오는 중...' : agreement.agreementName || '약관 상세'"
          :custom-back="true"
          @back="goBack"
      />

      <div v-if="!loading && !errorMessage" class="badge-wrapper">
        <span
            :class="[
              'agreement-type',
              'text-13-bold',
              agreement.requiredYn === 'Y' ? 'required' : 'optional',
            ]"
        >
          {{ agreement.requiredYn === 'Y' ? '필수 약관' : '선택 약관' }}
        </span>
      </div>
    </header>

    <!-- 중앙 내용 영역 -->
    <main class="content-area page-content">
      <section v-if="loading" class="status-message text-15">
        약관을 불러오는 중입니다.
      </section>

      <section v-else-if="errorMessage" class="status-message error-message text-15">
        {{ errorMessage }}
      </section>

      <template v-else>
        <div class="agreement-scroll text-15">
          {{ agreement.agreementContent }}
        </div>

        <label class="consent-label">
          <input
              :checked="isAgreed"
              type="checkbox"
              @change="changeAgreement"
          />

          <span class="check-box"></span>

          <span class="text-15-bold">
            위 약관에 동의합니다.
          </span>
        </label>
      </template>
    </main>

    <!-- 하단 버튼 -->
    <div class="bottom-btn-area single">
      <button class="bottom-btn" type="button" @click="goBack">
        확인
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import agreementApi from '@/api/agreementApi';
import { useSignupStore } from '@/stores/signup';
import PageHeader from '@/components/common/PageHeader.vue';

const route = useRoute();
const router = useRouter();
const signupStore = useSignupStore();

const agreement = ref({});
const loading = ref(false);
const errorMessage = ref('');

const isAgreed = computed(() => {
  const item = signupStore.agreements.find((item) => item.agreementType === route.params.agreementType);
  return item?.agreed ?? false;
});

// 약관 상세 조회
const loadAgreement = async () => {
  try {
    loading.value = true;
    agreement.value = await agreementApi.getAgreementDetail(route.params.agreementType);
  } catch (error) {
    console.error(error);
    errorMessage.value = '약관 내용을 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

// 약관 동의
const changeAgreement = (event) => {
  signupStore.setAgreementChecked(route.params.agreementType, event.target.checked);
};

// 이전 화면
const goBack = () => {
  router.back();
};

onMounted(loadAgreement);
</script>

<style scoped>
@import "@/components/common/common/common.css";
@import "@/components/common/common/layout.css";

.page {
  background: var(--color-bg-page);
}

.header-area {
  flex-shrink: 0;
}

.badge-wrapper {
  margin-top: 8px;
  padding: 0 4px;
}

.agreement-type {
  display: inline-block;
}

.required {
  color: #ef3d3d;
}

.optional {
  color: var(--color-text-sub);
}

.content-area {
  margin-top: 12px;
  overflow: hidden;
}

.agreement-scroll {
  flex: 1;
  min-height: 0;
  padding: 16px;
  border: 1px solid var(--color-border-main);
  border-radius: 12px;
  color: var(--color-text-main);
  font-weight: 350;
  line-height: 1.6;
  white-space: pre-wrap;
  overflow-y: auto;
  box-sizing: border-box;
}

.consent-label {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  min-height: 52px;
  margin-top: 12px;
  padding: 0 4px;
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
  border-radius: 6px;
  background: var(--color-bg-page);
  box-sizing: border-box;
}

.consent-label input:checked + .check-box {
  border-color: var(--color-primary);
  background: var(--color-primary);
}

.consent-label input:checked + .check-box::after {
  display: block;
  width: 6px;
  height: 12px;
  margin: 3px 0 0 8px;
  border: solid #ffffff;
  border-width: 0 2px 2px 0;
  content: '';
  transform: rotate(45deg);
}

.status-message {
  padding-top: 100px;
  color: var(--color-text-sub);
  text-align: center;
}

.error-message {
  color: var(--color-error);
}
</style>