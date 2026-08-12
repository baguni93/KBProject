<template>
  <div class="kb-mobile-page entry-page">
    <PageHeader title="내 소비 분석" :showBack="false" />

    <div class="entry-content-start">
      <section class="entry-card kb-card">
      <div v-if="loading" class="kb-loading">
        <div class="spinner-border kb-spinner" role="status"></div>
        <div class="text-13">소비 분석 이용 정보를 확인하고 있어요.</div>
      </div>
      <template v-else>
        <div class="entry-icon"><i class="fa-solid fa-circle-exclamation"></i></div>
        <h2 class="text-20-bold">소비 분석 화면으로 이동하지 못했어요</h2>
        <p class="text-13">{{ message }}</p>
        <button type="button" class="content-btn primary" @click="checkAgreement">
          다시 확인하기
        </button>
      </template>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import analysisAgreementApi from '@/api/analysisAgreementApi';
import { getAnalysisErrorMessage } from '@/util/analysis';
import PageHeader from '@/components/common/PageHeader.vue';

const router = useRouter();
const loading = ref(true);
const message = ref('');

const checkAgreement = async () => {
  loading.value = true;
  message.value = '';

  try {
    const status = await analysisAgreementApi.getStatus();
    await router.replace({
      name: status.agreed ? 'analysis-main' : 'analysis-agreement',
    });
  } catch (error) {
    message.value = getAnalysisErrorMessage(
      error,
      '소비 분석 약관 상태를 확인하지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

onMounted(checkAgreement);
</script>

<style scoped>
.entry-page {
  padding-bottom: 34px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.entry-content-start {
  /*
   * 팀 협의 후 PageHeader와 첫 콘텐츠 사이 간격을 적용할 경우
   * 아래 주석을 해제합니다.
   * margin-top: 14px;
   */
}

.entry-card {
  margin-top: 18px;
  padding: 42px 22px;
  text-align: center;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.entry-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 68px;
  height: 68px;
  margin: 0 auto 16px;
  border-radius: 24px;
  background: #fff3cf;
  color: #d99b00;
  font-size: 28px;
}

.entry-card h2 {
  margin: 0;
  line-height: 1.45;
}

.entry-card p {
  margin: 9px 0 20px;
  color: var(--color-text-sub);
  line-height: 1.6;
}
</style>
