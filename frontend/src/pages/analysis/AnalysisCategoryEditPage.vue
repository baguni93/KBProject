<template>
  <div class="kb-mobile-page category-edit-page">
    <header class="kb-app-header">
      <button class="kb-icon-button" type="button" @click="goBack"><i class="fa-solid fa-chevron-left"></i></button>
      <h1 class="kb-app-header__title">카테고리 수정</h1>
      <span></span>
    </header>

    <div v-if="message" :class="['kb-toast', messageType === 'success' ? 'kb-toast--success' : 'kb-toast--error']">{{ message }}</div>
    <div v-if="loading" class="kb-card kb-loading"><div class="spinner-border kb-spinner"></div><div>거래 정보를 불러오는 중이에요.</div></div>

    <template v-else-if="transaction">
      <section class="edit-target kb-card">
        <div class="edit-icon"><i :class="getCategoryIcon(transaction.parentCategoryName || transaction.categoryName)"></i></div>
        <div class="edit-info"><span>카테고리를 수정할 거래</span><strong>{{ transaction.merchantName || '가맹점 정보 없음' }}</strong><small>{{ formatAnalysisDateTime(transaction.createdAt) }}</small></div>
        <div class="edit-amount">-{{ formatAnalysisNumber(transaction.amount) }}원</div>
      </section>

      <div class="current-category">현재 카테고리 <strong>{{ transaction.categoryName || '미분류' }}</strong></div>

      <section class="kb-section">
        <div class="kb-section-title-row"><h2 class="kb-section-title">대분류 선택</h2></div>
        <div class="top-grid kb-card">
          <button v-for="category in topCategories" :key="category.spendingCategoryId" type="button" :class="{ selected: selectedTopCategoryId === category.spendingCategoryId }" @click="selectTopCategory(category)">
            <div><i :class="getCategoryIcon(category.categoryName)"></i></div><span>{{ category.categoryName }}</span>
          </button>
        </div>
      </section>

      <section v-if="selectedTopCategory && childCategories.length" class="kb-section">
        <div class="kb-section-title-row"><h2 class="kb-section-title">{{ selectedTopCategory.categoryName }} 세부 항목</h2></div>
        <div class="child-grid kb-card">
          <button v-for="category in childCategories" :key="category.spendingCategoryId" type="button" :class="{ selected: selectedCategoryId === category.spendingCategoryId }" @click="selectedCategoryId = category.spendingCategoryId">
            <i :class="getCategoryIcon(category.categoryName)"></i><span>{{ category.categoryName }}</span>
          </button>
        </div>
      </section>

      <button type="button" class="kb-primary-button w-100 save-button" :disabled="!selectedCategoryId || saving" @click="saveCategory">{{ saving ? '수정 중...' : '카테고리 수정 완료' }}</button>
    </template>

    <div v-else class="kb-card kb-empty-state"><div class="kb-empty-state__icon"><i class="fa-solid fa-circle-exclamation"></i></div><strong>수정할 거래를 찾지 못했어요.</strong></div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import analysisApi from '@/api/analysisApi';
import { formatAnalysisDateTime, formatAnalysisNumber, getAnalysisErrorMessage, getCategoryIcon, normalizeAnalysisPeriod } from '@/util/analysis';

const route = useRoute(); const router = useRouter();
const transactionId = Number(route.params.transactionId); const period = normalizeAnalysisPeriod(route.query.period);
const categories = ref([]); const transactions = ref([]); const loading = ref(false); const saving = ref(false); const message = ref(''); const messageType = ref('success');
const selectedTopCategoryId = ref(null); const selectedCategoryId = ref(null);
const transaction = computed(() => transactions.value.find((item) => item.transactionId === transactionId));
const topCategories = computed(() => categories.value.filter((item) => item.parentCategoryId == null));
const selectedTopCategory = computed(() => topCategories.value.find((item) => item.spendingCategoryId === selectedTopCategoryId.value));
const childCategories = computed(() => selectedTopCategory.value ? categories.value.filter((item) => item.parentCategoryId === selectedTopCategory.value.spendingCategoryId) : []);

const selectTopCategory = (category) => {
  selectedTopCategoryId.value = category.spendingCategoryId;
  const children = categories.value.filter((item) => item.parentCategoryId === category.spendingCategoryId);
  selectedCategoryId.value = children.length ? null : category.spendingCategoryId;
};

const initializeSelection = () => {
  if (!transaction.value?.spendingCategoryId) return;
  selectedTopCategoryId.value = transaction.value.parentCategoryId ?? transaction.value.spendingCategoryId;
  selectedCategoryId.value = transaction.value.spendingCategoryId;
};

const loadData = async () => {
  loading.value = true;
  try {
    const [categoryData, transactionData] = await Promise.all([analysisApi.getCategories(), analysisApi.getTransactions(period)]);
    categories.value = categoryData.categories ?? [];
    transactions.value = transactionData.transactions ?? [];
    initializeSelection();
  } catch (error) { messageType.value = 'error'; message.value = getAnalysisErrorMessage(error, '카테고리 수정 정보를 불러오지 못했습니다.'); }
  finally { loading.value = false; }
};

const saveCategory = async () => {
  if (!selectedCategoryId.value) return;
  saving.value = true; message.value = '';
  try {
    await analysisApi.classifyTransaction(transactionId, selectedCategoryId.value);
    messageType.value = 'success'; message.value = '카테고리가 수정되었습니다.';
    setTimeout(() => router.push({ name: 'analysis-main' }), 400);
  } catch (error) { messageType.value = 'error'; message.value = getAnalysisErrorMessage(error, '카테고리 수정에 실패했습니다.'); }
  finally { saving.value = false; }
};
const goBack = () => router.back();
onMounted(loadData);
</script>

<style scoped>
.edit-target{padding:15px;display:grid;grid-template-columns:44px 1fr auto;align-items:center;gap:11px;box-shadow:none;border:1px solid #eee}.edit-icon{width:44px;height:44px;display:flex;align-items:center;justify-content:center;border-radius:14px;background:var(--kb-yellow-soft);color:#d79500;font-size:17px}.edit-info{min-width:0}.edit-info span,.edit-info strong,.edit-info small{display:block}.edit-info span{color:#999;font-size:8px}.edit-info strong{margin-top:3px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:12px}.edit-info small{margin-top:3px;color:#aaa;font-size:8px}.edit-amount{font-size:13px;font-weight:900;color:#e04d4d}.current-category{margin:10px 2px 0;color:#888;font-size:10px}.current-category strong{margin-left:4px;color:#9a7200}.top-grid{padding:12px;display:grid;grid-template-columns:repeat(4,1fr);gap:8px;box-shadow:none;border:1px solid #eee}.top-grid button{min-height:70px;border:1px solid transparent;border-radius:12px;background:#fafafa;color:#555}.top-grid button.selected,.child-grid button.selected{border-color:var(--kb-yellow);background:#fff7d7;color:#8f6800}.top-grid button div{font-size:17px}.top-grid button span{display:block;margin-top:4px;font-size:9px;font-weight:800}.child-grid{padding:12px;display:grid;grid-template-columns:repeat(2,1fr);gap:8px;box-shadow:none;border:1px solid #eee}.child-grid button{height:58px;border:1px solid transparent;border-radius:12px;background:#fafafa;color:#555}.child-grid button i{margin-right:7px}.child-grid button span{font-size:10px;font-weight:800}.save-button{margin-top:17px}@media(max-width:380px){.top-grid{grid-template-columns:repeat(3,1fr)}}
</style>
