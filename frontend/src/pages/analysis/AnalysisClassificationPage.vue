<template>
  <div class="kb-mobile-page classification-page">
    <header class="kb-app-header">
      <button class="kb-icon-button" type="button" aria-label="뒤로가기" @click="goToCheck"><i class="fa-solid fa-chevron-left"></i></button>
      <h1 class="kb-app-header__title">소비 카테고리 분류</h1>
      <span></span>
    </header>

    <div v-if="message" :class="['kb-toast', messageType === 'success' ? 'kb-toast--success' : 'kb-toast--error']">{{ message }}</div>

    <div v-if="loading" class="kb-card kb-loading"><div class="spinner-border kb-spinner"></div><div>미분류 거래를 불러오는 중이에요.</div></div>

    <template v-else-if="currentTransaction">
      <section class="target-card kb-card">
        <div class="target-question">?</div>
        <div class="target-info">
          <span>어떤 카테고리에 해당할까요?</span>
          <strong>{{ currentTransaction.merchantName || '가맹점 정보 없음' }}</strong>
          <small>{{ formatAnalysisDateTime(currentTransaction.createdAt) }}</small>
        </div>
        <div class="target-amount">-{{ formatAnalysisNumber(currentTransaction.amount) }}원</div>
      </section>

      <div class="remaining-caption"><i class="fa-solid fa-circle-info"></i> {{ periodLabel }} 미분류 거래 {{ unclassifiedData?.unclassifiedCount ?? 0 }}건</div>

      <section class="kb-section">
        <div class="kb-section-title-row"><h2 class="kb-section-title">카테고리 선택</h2><span>하나를 선택해 주세요</span></div>
        <div class="category-grid kb-card">
          <button
            v-for="category in topCategories"
            :key="category.spendingCategoryId"
            type="button"
            :class="['category-button', { selected: selectedCategoryId === category.spendingCategoryId }]"
            :disabled="classifying"
            @click="selectedCategoryId = category.spendingCategoryId"
          >
            <div class="category-button__icon"><i :class="getCategoryIcon(category.categoryName)"></i></div>
            <span>{{ category.categoryName }}</span>
            <small v-if="hasChildren(category.spendingCategoryId)">세부 선택</small>
          </button>
        </div>
      </section>

      <button type="button" class="kb-primary-button w-100 complete-button" :disabled="!selectedCategoryId || classifying" @click="completeSelection">
        {{ classifying ? '분류 중...' : '분류 완료' }}
      </button>
    </template>

    <section v-else class="done-card kb-card">
      <div class="done-icon"><i class="fa-solid fa-check"></i></div>
      <h2>모든 거래를 분류했어요!</h2>
      <p>분석 화면에서 최신 소비 패턴을 확인해 보세요.</p>
      <button type="button" class="kb-primary-button w-100" @click="goToCheck">분석 가능 여부 다시 확인하기</button>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import analysisApi from '@/api/analysisApi';
import { formatAnalysisDateTime, formatAnalysisNumber, getAnalysisErrorMessage, getCategoryIcon, normalizeAnalysisPeriod } from '@/util/analysis';
const route = useRoute(); const router = useRouter();
const period = ref(normalizeAnalysisPeriod(route.query.period)); const categories = ref([]); const unclassifiedData = ref(null); const loading = ref(false); const classifying = ref(false); const message = ref(''); const messageType = ref('success'); const selectedCategoryId = ref(null);
const currentTransaction = computed(() => unclassifiedData.value?.transactions?.[0] ?? null);
const topCategories = computed(() => categories.value.filter((category) => category.parentCategoryId == null));
const periodLabel = computed(() => unclassifiedData.value?.periodLabel ?? `최근 ${period.value}개월`);
const goToCheck = () => router.push({ name: 'analysis-check', query: { period: period.value } });
const childCategories = (parentCategoryId) => categories.value.filter((category) => category.parentCategoryId === parentCategoryId);
const hasChildren = (parentCategoryId) => childCategories(parentCategoryId).length > 0;
const loadData = async () => { loading.value = true; message.value = ''; selectedCategoryId.value = null; try { const [categoryData, transactionData] = await Promise.all([analysisApi.getCategories(), analysisApi.getUnclassifiedTransactions(period.value)]); categories.value = categoryData.categories ?? []; unclassifiedData.value = transactionData; if (route.query.classified) { messageType.value = 'success'; message.value = `${route.query.classified} 카테고리로 분류했습니다.`; await router.replace({ name:'analysis-classification', query:{ period:period.value, returnTo:route.query.returnTo||'analysis-check' } }); } } catch (error) { messageType.value='error'; message.value=getAnalysisErrorMessage(error,'미분류 거래 정보를 불러오지 못했습니다.'); } finally { loading.value=false; } };
const classifyCurrentTransaction = async (category) => { if (!currentTransaction.value) return; classifying.value=true; message.value=''; try { const result=await analysisApi.classifyTransaction(currentTransaction.value.transactionId,category.spendingCategoryId); messageType.value='success'; message.value=result.message; unclassifiedData.value=await analysisApi.getUnclassifiedTransactions(period.value); selectedCategoryId.value=null; } catch(error){messageType.value='error';message.value=getAnalysisErrorMessage(error,'소비 카테고리 분류에 실패했습니다.');} finally{classifying.value=false;} };
const completeSelection = async () => { const category=categories.value.find((item)=>item.spendingCategoryId===selectedCategoryId.value); if(!category)return; if(hasChildren(category.spendingCategoryId)){ await router.push({name:'analysis-subcategory',params:{transactionId:currentTransaction.value.transactionId},query:{period:period.value,parentCategoryId:category.spendingCategoryId,returnTo:route.query.returnTo||'analysis-check'}}); return;} await classifyCurrentTransaction(category); };
onMounted(loadData);
</script>

<style scoped>
.target-card{padding:16px;display:grid;grid-template-columns:46px 1fr auto;align-items:center;gap:11px;box-shadow:none;border:1px solid #eee;}.target-question{width:46px;height:46px;display:flex;align-items:center;justify-content:center;border-radius:16px;background:var(--kb-yellow-soft);color:#d99500;font-size:24px;font-weight:900;}.target-info{min-width:0;}.target-info span,.target-info strong,.target-info small{display:block;}.target-info span{color:#888;font-size:9px;}.target-info strong{margin-top:3px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:13px;}.target-info small{margin-top:3px;color:#aaa;font-size:8px;}.target-amount{font-size:14px;font-weight:900;color:#e04d4d;}.remaining-caption{margin:10px 2px 0;color:#8d7a46;font-size:10px;}.remaining-caption i{margin-right:3px;color:var(--kb-yellow-strong);}.kb-section-title-row>span{color:#999;font-size:9px;}.category-grid{padding:12px;display:grid;grid-template-columns:repeat(4,1fr);gap:8px;box-shadow:none;border:1px solid #eee;}.category-button{min-height:76px;padding:9px 3px;border:1px solid transparent;border-radius:12px;background:#fafafa;color:#555;}.category-button.selected{border-color:var(--kb-yellow);background:#fff7d7;color:#8c6800;box-shadow:0 0 0 1px var(--kb-yellow) inset;}.category-button__icon{height:29px;display:flex;align-items:center;justify-content:center;font-size:17px;}.category-button span{display:block;margin-top:3px;font-size:9px;font-weight:800;}.category-button small{display:block;margin-top:1px;color:#999;font-size:7px;}.complete-button{margin-top:16px;}.done-card{margin-top:18px;padding:38px 22px;text-align:center;box-shadow:none;border:1px solid #eee;}.done-icon{width:68px;height:68px;margin:0 auto 15px;display:flex;align-items:center;justify-content:center;border-radius:24px;background:#eaf8f1;color:#1f9d62;font-size:29px;}.done-card h2{font-size:18px;font-weight:900;}.done-card p{margin:7px 0 20px;color:#777;font-size:11px;}@media(max-width:380px){.category-grid{grid-template-columns:repeat(3,1fr)}}
</style>
