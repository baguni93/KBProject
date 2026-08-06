<template>
  <div class="analysis-agreement-page">
    <main class="analysis-agreement-container">
      <section class="agreement-header">
        <h1>약관 동의</h1>
        <p>소비 분석을 위해 약관에<br />동의해주세요.</p>
      </section>

      <div v-if="message" class="error-message">{{ message }}</div>

      <div v-if="loading" class="loading-area">
        <div class="spinner-border" role="status"></div>
        <span>소비 분석 약관을 불러오는 중이에요.</span>
      </div>

      <template v-else>
        <section class="agreement-section">
          <label class="all-agreement">
            <input type="checkbox" :checked="allChecked" @change="toggleAll" />
            <span class="check-box"></span>
            <strong>전체 동의</strong>
          </label>

          <div class="divider"></div>

          <AgreementCheckItem
            v-for="agreement in agreements"
            :key="agreement.agreementId"
            :model-value="Boolean(checkedMap[agreement.agreementId])"
            :title="agreement.agreementName"
            :detail="agreement.agreementContent"
            :required="agreement.requiredYn === 'Y'"
            :expanded="expandedIds.has(agreement.agreementId)"
            @update:model-value="(checked) => { checkedMap[agreement.agreementId] = checked; }"
            @toggle-detail="toggleExpanded(agreement.agreementId)"
          />

          <div class="divider bottom-divider"></div>
        </section>

        <button
          type="button"
          class="submit-button"
          :disabled="!requiredChecked || saving"
          @click="submitAgreement"
        >
          {{ saving ? '동의 처리 중...' : '동의하고 소비 분석 시작하기' }}
        </button>
      </template>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import analysisAgreementApi from '@/api/analysisAgreementApi';
import AgreementCheckItem from '@/components/common/AgreementCheckItem.vue';
import { getAnalysisErrorMessage } from '@/util/analysis';

const router = useRouter();
const agreements = ref([]);
const checkedMap = reactive({});
const expandedIds = ref(new Set());
const loading = ref(false);
const saving = ref(false);
const message = ref('');

const requiredChecked = computed(() =>
  agreements.value
    .filter((agreement) => agreement.requiredYn === 'Y')
    .every((agreement) => checkedMap[agreement.agreementId]),
);

const allChecked = computed(() =>
  agreements.value.length > 0 &&
  agreements.value.every((agreement) => checkedMap[agreement.agreementId]),
);

const loadAgreements = async () => {
  loading.value = true;
  message.value = '';

  try {
    const result = await analysisAgreementApi.getAgreements();
    agreements.value = result.agreements ?? [];
    agreements.value.forEach((agreement) => {
      checkedMap[agreement.agreementId] = agreement.agreedYn === 'Y';
    });
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
};

const toggleExpanded = (agreementId) => {
  const next = new Set(expandedIds.value);
  if (next.has(agreementId)) next.delete(agreementId);
  else next.add(agreementId);
  expandedIds.value = next;
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

onMounted(loadAgreements);
</script>

<style scoped>
.analysis-agreement-page {
  display: flex;
  justify-content: center;
  min-height: 100vh;
  padding: 24px 0;
  background: #f4f4f4;
  overflow: auto;
}

.analysis-agreement-container {
  position: relative;
  display: flex;
  flex: none;
  flex-direction: column;
  width: 390px;
  min-height: 844px;
  margin: 0;
  padding: 60px 28px 116px;
  background: #ffffff;
  overflow-y: auto;
}

.agreement-header h1 {
  margin: 0 0 28px;
  color: #111111;
  font-size: 30px;
  font-weight: 700;
}

.agreement-header p {
  margin: 0;
  color: #777777;
  font-size: 20px;
  font-weight: 600;
  line-height: 1.35;
}

.agreement-section {
  margin-top: 66px;
}

.all-agreement,
.agreement-label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.all-agreement {
  min-height: 48px;
}

.all-agreement input,
.agreement-label input {
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
}

.all-agreement input:checked + .check-box,
.agreement-label input:checked + .check-box {
  border-color: #ffbc2e;
  background: #ffbc2e;
}

.all-agreement input:checked + .check-box::after,
.agreement-label input:checked + .check-box::after {
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
  color: #111111;
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
  margin-top: 20px;
  color: #d32f2f;
  font-size: 14px;
}

.loading-area {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 66px;
  color: #777777;
  font-size: 14px;
}

.submit-button {
  position: absolute;
  right: 28px;
  bottom: 28px;
  left: 28px;
  width: auto;
  height: 58px;
  border: 1px solid #cc9200;
  border-radius: 10px;
  background: #ffbc2e;
  color: #111111;
  font-size: 17px;
  font-weight: 700;
  cursor: pointer;
}

.submit-button:disabled {
  border-color: #dddddd;
  background: #eeeeee;
  color: #999999;
  cursor: not-allowed;
}

@media (max-width: 430px) {
  .analysis-agreement-page {
    padding: 0;
    background: #ffffff;
  }

  .analysis-agreement-container {
    width: 100%;
    min-height: 100vh;
  }
}
</style>
