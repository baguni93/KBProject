<template>
  <div class="kb-mobile-page subcategory-page">
    <header class="kb-app-header">
      <button class="kb-icon-button" type="button" @click="goBack"><i class="fa-solid fa-chevron-left"></i></button>
      <h1 class="kb-app-header__title">세부 카테고리 선택</h1>
      <span></span>
    </header>
    <div v-if="message" class="kb-toast kb-toast--error">{{ message }}</div>
    <div v-if="loading" class="kb-card kb-loading"><div class="spinner-border kb-spinner"></div><div>세부 카테고리를 불러오는 중이에요.</div></div>
    <template v-else-if="transaction">
      <section class="merchant-card kb-card"><div class="merchant-icon"><i :class="getCategoryIcon(parentCategory?.categoryName)"></i></div><div><span>{{ parentCategory?.categoryName }} 세부 분류</span><strong>{{ transaction.merchantName || '가맹점 정보 없음' }}</strong><small>{{ formatAnalysisDateTime(transaction.createdAt) }}</small></div><strong class="amount">-{{ formatAnalysisNumber(transaction.amount) }}원</strong></section>
      <section class="kb-section"><div class="kb-section-title-row"><h2 class="kb-section-title">어떤 병원에 방문했나요?</h2></div><div class="subcategory-grid kb-card"><button v-for="category in subcategories" :key="category.spendingCategoryId" type="button" :class="{selected:selectedCategoryId===category.spendingCategoryId}" @click="selectedCategoryId=category.spendingCategoryId"><div><i :class="getCategoryIcon(category.categoryName)"></i></div><span>{{ category.categoryName }}</span></button></div></section>
      <button type="button" class="kb-primary-button w-100 complete-button" :disabled="!selectedCategoryId || classifying" @click="classifySelected">{{ classifying ? '분류 중...' : '분류 완료' }}</button>
    </template>
    <div v-else class="kb-card kb-empty-state"><div class="kb-empty-state__icon"><i class="fa-solid fa-circle-exclamation"></i></div><strong>분류할 거래를 찾지 못했어요.</strong></div>
  </div>
</template>
<script setup>
import { computed,onMounted,ref } from 'vue';import { useRoute,useRouter } from 'vue-router';import analysisApi from '@/api/analysisApi';import { formatAnalysisDateTime,formatAnalysisNumber,getAnalysisErrorMessage,getCategoryIcon,normalizeAnalysisPeriod } from '@/util/analysis';
const route=useRoute();const router=useRouter();const transactionId=Number(route.params.transactionId);const period=normalizeAnalysisPeriod(route.query.period);const requestedParentCategoryId=Number(route.query.parentCategoryId);const categories=ref([]);const unclassifiedTransactions=ref([]);const loading=ref(false);const classifying=ref(false);const message=ref('');const selectedCategoryId=ref(null);
const transaction=computed(()=>unclassifiedTransactions.value.find(item=>item.transactionId===transactionId));const parentCategory=computed(()=>categories.value.find(category=>category.spendingCategoryId===requestedParentCategoryId));const subcategories=computed(()=>parentCategory.value?categories.value.filter(category=>category.parentCategoryId===parentCategory.value.spendingCategoryId):[]);
const loadData=async()=>{loading.value=true;try{const[categoryData,transactionData]=await Promise.all([analysisApi.getCategories(),analysisApi.getUnclassifiedTransactions(period)]);categories.value=categoryData.categories??[];unclassifiedTransactions.value=transactionData.transactions??[];}catch(error){message.value=getAnalysisErrorMessage(error,'세부 카테고리 정보를 불러오지 못했습니다.');}finally{loading.value=false;}};
const classifySelected=async()=>{const category=subcategories.value.find(item=>item.spendingCategoryId===selectedCategoryId.value);if(!category||!transaction.value)return;classifying.value=true;message.value='';try{await analysisApi.classifyTransaction(transaction.value.transactionId,category.spendingCategoryId);await router.replace({name:'analysis-classification',query:{period,classified:category.categoryName,returnTo:route.query.returnTo||'analysis-check'}});}catch(error){message.value=getAnalysisErrorMessage(error,'소비 카테고리 분류에 실패했습니다.');}finally{classifying.value=false;}};const goBack=()=>router.push({name:'analysis-classification',query:{period,returnTo:route.query.returnTo||'analysis-check'}});onMounted(loadData);
</script>
<style scoped>
.merchant-card{padding:15px;display:grid;grid-template-columns:44px 1fr auto;align-items:center;gap:11px;box-shadow:none;border:1px solid #eee;}.merchant-icon{width:44px;height:44px;display:flex;align-items:center;justify-content:center;border-radius:14px;background:var(--kb-yellow-soft);color:#d99700;font-size:18px;}.merchant-card span,.merchant-card strong,.merchant-card small{display:block;}.merchant-card span{color:#999;font-size:8px;}.merchant-card>div:nth-child(2)>strong{margin-top:3px;font-size:12px;}.merchant-card small{margin-top:3px;color:#aaa;font-size:8px;}.merchant-card .amount{font-size:13px;color:#e04d4d;}.subcategory-grid{padding:12px;display:grid;grid-template-columns:repeat(2,1fr);gap:9px;box-shadow:none;border:1px solid #eee;}.subcategory-grid button{min-height:76px;border:1px solid transparent;border-radius:13px;background:#fafafa;color:#555;}.subcategory-grid button.selected{border-color:var(--kb-yellow);background:#fff7d8;color:#8e6900;}.subcategory-grid button div{font-size:19px;}.subcategory-grid button span{display:block;margin-top:5px;font-size:10px;font-weight:800;}.complete-button{margin-top:16px;}
</style>
