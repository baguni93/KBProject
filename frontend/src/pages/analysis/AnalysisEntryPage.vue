<template>
  <div class="kb-mobile-page entry-page">
    <header class="kb-app-header">
      <span></span>
      <h1 class="kb-app-header__title">내 소비 분석</h1>
      <span></span>
    </header>

    <section class="entry-card kb-card">
      <div v-if="loading" class="kb-loading">
        <div class="spinner-border kb-spinner" role="status"></div>
        <div>소비 분석 이용 정보를 확인하고 있어요.</div>
      </div>
      <template v-else>
        <div class="entry-icon"><i class="fa-solid fa-circle-exclamation"></i></div>
        <h2>소비 분석 화면으로 이동하지 못했어요</h2>
        <p>{{ message }}</p>
        <button type="button" class="kb-primary-button" @click="checkAgreement">
          다시 확인하기
        </button>
      </template>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import analysisAgreementApi from '@/api/analysisAgreementApi';
import { getAnalysisErrorMessage } from '@/util/analysis';

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
.entry-page{padding-bottom:34px}.entry-card{margin-top:18px;padding:42px 22px;text-align:center;border:1px solid #eee;box-shadow:none}.entry-icon{width:68px;height:68px;margin:0 auto 16px;display:flex;align-items:center;justify-content:center;border-radius:24px;background:#fff3cf;color:#d99b00;font-size:28px}.entry-card h2{margin:0;font-size:17px;font-weight:900}.entry-card p{margin:9px 0 20px;color:#777;font-size:11px;line-height:1.6}.entry-card button{width:100%}
</style>
