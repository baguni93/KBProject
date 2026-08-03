<template>
  <div class="kb-mobile-page agreement-page">
    <header class="kb-app-header">
      <button class="kb-icon-button" type="button" aria-label="뒤로가기" @click="goBack">
        <i class="fa-solid fa-chevron-left"></i>
      </button>
      <h1 class="kb-app-header__title">개인정보 이용 동의</h1>
      <span></span>
    </header>

    <div v-if="message" class="kb-toast kb-toast--error">{{ message }}</div>

    <div v-if="loading" class="kb-card kb-loading agreement-loading">
      <div class="spinner-border kb-spinner" role="status"></div>
      <div>소비 분석 약관을 불러오는 중이에요.</div>
    </div>

    <template v-else>
      <section class="agreement-intro">
        <div class="intro-icon"><i class="fa-solid fa-user-shield"></i></div>
        <h2>맞춤형 소비 분석 서비스를<br />시작하려면 동의가 필요해요</h2>
        <p>안전하게 소비정보를 분석하고 결과를 제공하기 위해<br />아래 내용을 확인해 주세요.</p>
      </section>

      <section class="agreement-list kb-card">
        <label class="all-agreement-row">
          <input type="checkbox" :checked="allChecked" @change="toggleAll" />
          <span class="custom-check"><i class="fa-solid fa-check"></i></span>
          <span>
            <strong>전체 동의</strong>
            <small>필수·선택 약관을 모두 동의합니다.</small>
          </span>
        </label>

        <div class="agreement-divider"></div>

        <article
          v-for="agreement in agreements"
          :key="agreement.agreementId"
          class="agreement-item"
        >
          <div class="agreement-item__top">
            <label>
              <input
                v-model="checkedMap[agreement.agreementId]"
                type="checkbox"
              />
              <span class="custom-check"><i class="fa-solid fa-check"></i></span>
              <span class="agreement-name">
                <em :class="agreement.requiredYn === 'Y' ? 'required' : 'optional'">
                  [{{ agreement.requiredYn === 'Y' ? '필수' : '선택' }}]
                </em>
                {{ agreement.agreementName }}
              </span>
            </label>
            <button type="button" @click="toggleExpanded(agreement.agreementId)">
              <i
                :class="expandedIds.has(agreement.agreementId)
                  ? 'fa-solid fa-chevron-up'
                  : 'fa-solid fa-chevron-down'"
              ></i>
            </button>
          </div>
          <p v-if="expandedIds.has(agreement.agreementId)" class="agreement-content">
            {{ agreement.agreementContent }}
          </p>
        </article>
      </section>

      <button
        type="button"
        class="kb-primary-button agreement-submit"
        :disabled="!requiredChecked || saving"
        @click="submitAgreement"
      >
        {{ saving ? '동의 처리 중...' : '동의하고 소비 분석 시작하기' }}
      </button>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import analysisAgreementApi from '@/api/analysisAgreementApi';
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

const goBack = () => router.back();

onMounted(loadAgreements);
</script>

<style scoped>
.agreement-page{padding-bottom:34px}.agreement-loading{margin-top:16px}.agreement-intro{padding:28px 16px 22px;text-align:center}.intro-icon{width:72px;height:72px;margin:0 auto 15px;display:flex;align-items:center;justify-content:center;border-radius:25px;background:#fff4ce;color:#d99a00;font-size:30px}.agreement-intro h2{margin:0;font-size:19px;font-weight:900;line-height:1.45;letter-spacing:-.5px}.agreement-intro p{margin:10px 0 0;color:#777;font-size:10px;line-height:1.65}.agreement-list{padding:4px 16px;border:1px solid #ececec;box-shadow:none}.all-agreement-row,.agreement-item__top label{display:flex;align-items:center;gap:10px;cursor:pointer}.all-agreement-row{min-height:68px}.all-agreement-row input,.agreement-item__top input{position:absolute;opacity:0;pointer-events:none}.custom-check{width:23px;height:23px;display:inline-flex;align-items:center;justify-content:center;flex:0 0 23px;border:1.5px solid #d5d5d5;border-radius:50%;background:#fff;color:transparent;font-size:11px}.all-agreement-row input:checked+.custom-check,.agreement-item__top input:checked+.custom-check{border-color:var(--kb-yellow);background:var(--kb-yellow);color:#222}.all-agreement-row strong,.all-agreement-row small{display:block}.all-agreement-row strong{font-size:13px;font-weight:900}.all-agreement-row small{margin-top:3px;color:#999;font-size:8px}.agreement-divider{height:1px;background:#ededed}.agreement-item{padding:14px 0;border-bottom:1px solid #f2f2f2}.agreement-item:last-child{border-bottom:0}.agreement-item__top{display:flex;align-items:center;justify-content:space-between;gap:8px}.agreement-item__top label{min-width:0;flex:1}.agreement-name{min-width:0;font-size:10px;font-weight:800}.agreement-name em{margin-right:3px;font-style:normal}.agreement-name .required{color:#c78e00}.agreement-name .optional{color:#888}.agreement-item__top button{width:30px;height:30px;border:0;background:transparent;color:#999;font-size:10px}.agreement-content{margin:10px 0 0 33px;padding:10px 12px;border-radius:10px;background:#f7f7f7;color:#777;font-size:9px;line-height:1.65}.agreement-submit{width:100%;margin-top:18px}.agreement-submit:disabled{opacity:.45}
</style>
