<template>
  <div class="analysis-agreement-detail-page page-layout">
    <header class="header-area">
      <PageHeader
          :title="loading ? '불러오는 중...' : agreement.agreementName || '약관 상세'"
          :custom-back="true"
          @back="goBack"
      />

      <div v-if="!loading && !message" class="badge-wrapper">
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

    <main class="content-area page-content">
      <section v-if="loading" class="status-message text-15">
        약관을 불러오는 중입니다.
      </section>

      <section v-else-if="message" class="status-message error-message text-15">
        {{ message }}
      </section>

      <template v-else>
        <div class="agreement-scroll text-15">
          {{ agreement.agreementContent }}
        </div>

        <label class="consent-label">
          <input
              type="checkbox"
              :checked="isAgreed"
              @change="changeAgreement"
          />

          <span class="check-box"></span>

          <span class="text-15-bold">
            위 약관에 동의합니다.
          </span>
        </label>
      </template>
    </main>

    <div class="bottom-btn-area single">
      <button
          class="bottom-btn"
          type="button"
          @click="goBack"
      >
        확인
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import analysisAgreementApi from '@/api/analysisAgreementApi';
import PageHeader from '@/components/common/PageHeader.vue';
import { getAnalysisErrorMessage } from '@/util/analysis';

const route = useRoute();
const router = useRouter();

const agreement = ref({});
const loading = ref(false);
const message = ref('');

const DRAFT_KEY = 'analysisAgreementDraft';

const agreementId = computed(() => Number(route.params.agreementId));

const readDraft = () => {
  try {
    return JSON.parse(sessionStorage.getItem(DRAFT_KEY) || '{}');
  } catch {
    return {};
  }
};

const isAgreed = computed(() => {
  const draft = readDraft();
  return Boolean(draft[String(agreementId.value)]);
});

const loadAgreement = async () => {
  loading.value = true;
  message.value = '';

  try {
    const result = await analysisAgreementApi.getAgreements();
    const agreements = result.agreements ?? [];

    agreement.value =
        agreements.find(
            (item) => Number(item.agreementId) === agreementId.value,
        ) ?? {};

    if (!agreement.value.agreementId) {
      message.value = '약관 내용을 찾을 수 없습니다.';
    }
  } catch (error) {
    agreement.value = {};
    message.value = getAnalysisErrorMessage(
        error,
        '약관 내용을 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

const changeAgreement = (event) => {
  const draft = readDraft();
  draft[String(agreementId.value)] = event.target.checked;

  sessionStorage.setItem(DRAFT_KEY, JSON.stringify(draft));
};

const goBack = () => {
  router.back();
};

onMounted(loadAgreement);
</script>

<style scoped>
@import "@/components/common/common/common.css";
@import "@/components/common/common/layout.css";

.analysis-agreement-detail-page {
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