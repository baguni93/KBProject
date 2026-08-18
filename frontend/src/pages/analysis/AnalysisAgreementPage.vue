<template>
  <div class="analysis-agreement-page page-layout">
    <PageHeader
        title=""
        :custom-back="true"
        :show-refresh="false"
        @back="goBack"
    />

    <section class="analysis-header">
      <h1 class="text-28-bold">약관 동의</h1>
      <p class="text-15">소비 분석을 위해 약관에 동의해주세요.</p>
    </section>

    <section class="agreement-section page-content">
      <div v-if="loading" class="status-message text-15">
        소비 분석 약관을 불러오는 중이에요.
      </div>

      <div v-else>
        <label class="all-agreement">
          <input
              type="checkbox"
              :checked="allChecked"
              @change="toggleAll"
          />
          <span class="check-box"></span>
          <strong class="text-18-bold">전체 동의</strong>
        </label>

        <div class="divider"></div>

        <AgreementCheckItem
            v-for="(agreement, index) in agreements"
            :key="agreement.agreementId"
            :model-value="Boolean(checkedMap[agreement.agreementId])"
            :title="agreement.agreementName"
            :required="agreement.requiredYn === 'Y'"
            :last="index === agreements.length - 1"
            detail-mode="navigate"
            @update:model-value="(checked) => changeAgreement(agreement.agreementId, checked)"
            @open-detail="showAgreementDetail(agreement.agreementId)"
        />

        <div class="divider bottom-divider"></div>

        <p v-if="message" class="error-message text-13">
          {{ message }}
        </p>
      </div>
    </section>

    <div class="bottom-btn-area single">
      <button
          type="button"
          class="bottom-btn"
          :disabled="!requiredChecked || saving || loading"
          @click="submitAgreement"
      >
        {{ saving ? '동의 처리 중...' : '소비 분석 서비스 시작' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import analysisAgreementApi from '@/api/analysisAgreementApi';
import AgreementCheckItem from '@/components/common/AgreementCheckItem.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import { getAnalysisErrorMessage } from '@/util/analysis';

const router = useRouter();

const agreements = ref([]);
const checkedMap = reactive({});
const loading = ref(false);
const saving = ref(false);
const message = ref('');

const DRAFT_KEY = 'analysisAgreementDraft';

const readDraft = () => {
  try {
    return JSON.parse(sessionStorage.getItem(DRAFT_KEY) || '{}');
  } catch {
    return {};
  }
};

const saveDraft = () => {
  sessionStorage.setItem(
      DRAFT_KEY,
      JSON.stringify({ ...checkedMap }),
  );
};

const requiredChecked = computed(() =>
    agreements.value
        .filter((agreement) => agreement.requiredYn === 'Y')
        .every((agreement) => Boolean(checkedMap[agreement.agreementId])),
);

const allChecked = computed(() =>
    agreements.value.length > 0 &&
    agreements.value.every(
        (agreement) => Boolean(checkedMap[agreement.agreementId]),
    ),
);

const loadAgreements = async () => {
  loading.value = true;
  message.value = '';

  try {
    const result = await analysisAgreementApi.getAgreements();
    agreements.value = result.agreements ?? [];

    const draft = readDraft();

    agreements.value.forEach((agreement) => {
      const id = String(agreement.agreementId);

      checkedMap[agreement.agreementId] =
          Object.prototype.hasOwnProperty.call(draft, id)
              ? Boolean(draft[id])
              : agreement.agreedYn === 'Y';
    });

    saveDraft();
  } catch (error) {
    agreements.value = [];
    message.value = getAnalysisErrorMessage(
        error,
        '소비 분석 약관을 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

const toggleAll = (event) => {
  const checked = event.target.checked;

  agreements.value.forEach((agreement) => {
    checkedMap[agreement.agreementId] = checked;
  });

  saveDraft();
};

const changeAgreement = (agreementId, checked) => {
  checkedMap[agreementId] = checked;
  saveDraft();
};

const showAgreementDetail = (agreementId) => {
  saveDraft();

  router.push({
    name: 'analysis-agreement-detail',
    params: { agreementId },
  });
};

const submitAgreement = async () => {
  if (!requiredChecked.value) {
    message.value = '필수 약관에 모두 동의해 주세요.';
    return;
  }

  saving.value = true;
  message.value = '';

  try {
    const payload = agreements.value.map((agreement) => ({
      agreementId: agreement.agreementId,
      agreedYn: checkedMap[agreement.agreementId] ? 'Y' : 'N',
    }));

    await analysisAgreementApi.saveAgreements(payload);
    sessionStorage.removeItem(DRAFT_KEY);

    await router.replace({ name: 'analysis-main' });
  } catch (error) {
    message.value = getAnalysisErrorMessage(
        error,
        '소비 분석 약관 동의 처리에 실패했습니다.',
    );
  } finally {
    saving.value = false;
  }
};

const goBack = () => {
  sessionStorage.removeItem(DRAFT_KEY);
  router.back();
};

onMounted(loadAgreements);
</script>

<style scoped>
@import "@/components/common/common/common.css";
@import "@/components/common/common/layout.css";

.analysis-agreement-page {
  background: var(--color-bg-page);
}

.analysis-header {
  flex-shrink: 0;
  margin-top: 24px;
}

.analysis-header h1 {
  margin: 0 0 16px;
  color: var(--color-text-main);
}

.analysis-header p {
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

.divider {
  height: 1px;
  margin: 12px 0 8px;
  background: var(--color-divider);
}

.bottom-divider {
  margin-top: 8px;
}

.status-message {
  padding-top: 80px;
  color: var(--color-text-sub);
  text-align: center;
}

.error-message {
  margin: 16px 0 0;
  color: var(--color-error);
}
</style>