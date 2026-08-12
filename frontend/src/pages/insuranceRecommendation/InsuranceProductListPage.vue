<template>
  <div class="kb-mobile-page insurance-product-list-page">
    <PageHeader
      title="전체 보험 둘러보기"
      :custom-back="true"
      @back="goBack"
    />

    <section class="browse-intro kb-card">
      <div class="browse-intro__icon">
        <i class="fa-solid fa-shield-heart"></i>
      </div>
      <div>
        <h2 class="text-20-bold">필요한 보험을 직접 찾아보세요</h2>
        <p class="text-13">KB손해보험 상품을 카테고리별로 확인할 수 있어요.</p>
      </div>
    </section>

    <section class="category-section">
      <div class="category-tabs" role="tablist" aria-label="보험 카테고리">
        <button
          v-for="option in categoryOptions"
          :key="option.value || 'ALL'"
          type="button"
          :class="['category-tab', 'text-13-bold', { active: selectedCategory === option.value }]"
          @click="changeCategory(option.value)"
        >
          {{ option.label }}
        </button>
      </div>
    </section>

    <div v-if="loading" class="kb-card kb-loading">
      <div class="spinner-border kb-spinner"></div>
      <div class="text-13">보험상품을 불러오고 있어요.</div>
    </div>

    <template v-else-if="productData">
      <section class="kb-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title text-20-bold">
            {{ selectedCategoryLabel }} 보험
          </h2>
          <span class="result-count text-13">{{ products.length }}개</span>
        </div>

        <div v-if="products.length" class="product-list">
          <button
            v-for="product in products"
            :key="product.insuranceProductId"
            type="button"
            class="product-card kb-card"
            @click="openProductDetail(product.insuranceProductId)"
          >
            <div class="product-image">
              <img
                v-if="getImage(product) && !isImageFailed(product.insuranceProductId)"
                :src="getImage(product)"
                :alt="`${product.insuranceName} 대표 이미지`"
                referrerpolicy="no-referrer"
                loading="lazy"
                @error.stop="markImageFailed(product.insuranceProductId)"
              />
              <div v-else class="product-placeholder" aria-hidden="true">
                <i :class="getInsuranceCategoryIcon(product.insuranceCategory)"></i>
              </div>
            </div>

            <div class="product-copy">
              <span class="category-chip text-13-bold">{{ product.insuranceCategory }}</span>
              <h3 class="text-15-bold">{{ product.insuranceName }}</h3>
              <p class="text-13">{{ product.insuranceDescription }}</p>
              <div class="product-premium">
                <span class="text-13">예상 월 보험료</span>
                <strong class="text-15-bold">
                  {{ formatInsuranceAmount(product.monthlyPremium) }}원
                </strong>
              </div>
            </div>

            <i class="fa-solid fa-chevron-right product-chevron" aria-hidden="true"></i>
          </button>
        </div>

        <div v-else class="kb-card kb-empty-state">
          <div class="kb-empty-state__icon">
            <i class="fa-solid fa-shield-heart"></i>
          </div>
          <strong class="text-15-bold">해당 카테고리의 보험상품이 없습니다.</strong>
        </div>
      </section>
    </template>

    <div v-else class="kb-card kb-empty-state error-state">
      <div class="kb-empty-state__icon">
        <i class="fa-solid fa-triangle-exclamation"></i>
      </div>
      <strong class="text-15-bold">보험상품을 불러오지 못했습니다.</strong>
      <p class="text-13">{{ message }}</p>
      <button type="button" class="content-btn primary" @click="loadProducts">
        다시 시도
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import insuranceRecommendationApi from '@/api/insuranceRecommendationApi';
import {
  INSURANCE_CATEGORY_OPTIONS,
  formatInsuranceAmount,
  getInsuranceCategoryIcon,
  getInsuranceImageUrl,
  getInsuranceRecommendationErrorMessage,
} from '@/util/insuranceRecommendation';

const route = useRoute();
const router = useRouter();
const categoryOptions = INSURANCE_CATEGORY_OPTIONS;

const selectedCategory = ref(
  categoryOptions.some((item) => item.value === route.query.category)
    ? route.query.category
    : '',
);
const productData = ref(null);
const loading = ref(false);
const failedImages = ref({});
const message = ref('');

const products = computed(() => productData.value?.products ?? []);
const selectedCategoryLabel = computed(
  () => categoryOptions.find((item) => item.value === selectedCategory.value)?.label ?? '전체',
);

const getImage = (product) => getInsuranceImageUrl(product?.insuranceImage);
const isImageFailed = (insuranceProductId) => Boolean(failedImages.value[insuranceProductId]);
const markImageFailed = (insuranceProductId) => {
  failedImages.value = {
    ...failedImages.value,
    [insuranceProductId]: true,
  };
};

const loadProducts = async () => {
  loading.value = true;
  message.value = '';
  failedImages.value = {};

  try {
    productData.value = await insuranceRecommendationApi.getProducts(
      selectedCategory.value,
    );
  } catch (error) {
    productData.value = null;
    message.value = getInsuranceRecommendationErrorMessage(
      error,
      '보험상품을 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

const changeCategory = async (category) => {
  if (selectedCategory.value === category) return;

  selectedCategory.value = category;
  await router.replace({
    name: 'insurance-product-list',
    query: {
      ...(route.query.spendingAnalysisId
        ? { spendingAnalysisId: route.query.spendingAnalysisId }
        : {}),
      ...(category ? { category } : {}),
    },
  });
  await loadProducts();
};

const openProductDetail = (insuranceProductId) =>
  router.push({
    name: 'insurance-product-detail',
    params: { insuranceProductId },
    query: {
      from: 'products',
      ...(route.query.spendingAnalysisId
        ? { spendingAnalysisId: route.query.spendingAnalysisId }
        : {}),
      ...(selectedCategory.value
        ? { category: selectedCategory.value }
        : {}),
    },
  });

const goBack = () => {
  const spendingAnalysisId = Number(route.query.spendingAnalysisId);

  if (Number.isInteger(spendingAnalysisId) && spendingAnalysisId > 0) {
    router.push({
      name: 'insurance-recommendation',
      params: { spendingAnalysisId },
    });
    return;
  }

  router.back();
};

onMounted(loadProducts);
</script>

<style scoped>
.insurance-product-list-page {
  padding-bottom: 36px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.browse-intro {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 19px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.browse-intro__icon {
  width: 50px;
  height: 50px;
  flex: 0 0 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 17px;
  background: #fff3c4;
  color: #d99a00;
  font-size: 22px;
}

.browse-intro h2 {
  margin: 0 0 4px;
}

.browse-intro p {
  margin: 0;
  color: var(--color-text-sub);
}

.category-section {
  margin: 16px -16px 0;
  overflow: hidden;
}

.category-tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 0 16px 10px;
  scrollbar-width: none;
}

.category-tabs::-webkit-scrollbar {
  display: none;
}

.category-tab {
  flex: 0 0 auto;
  min-height: 38px;
  padding: 0 14px;
  border: 1px solid var(--color-border-main);
  border-radius: 999px;
  background: var(--color-bg-page);
  color: var(--color-text-sub);
}

.category-tab.active {
  border-color: #f0b200;
  background: #fff4c7;
  color: #6f5500;
}

.result-count {
  color: var(--color-text-muted);
}

.product-list {
  display: grid;
  gap: 10px;
}

.product-card {
  position: relative;
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr) 18px;
  gap: 13px;
  width: 100%;
  padding: 14px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  text-align: left;
  box-shadow: none;
}

.product-image {
  width: 92px;
  height: 92px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 15px;
  background: #fbfaf6;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.product-placeholder {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
  background: #fff3c4;
  color: #d99a00;
  font-size: 24px;
}

.product-copy {
  min-width: 0;
}

.category-chip {
  display: inline-flex;
  padding: 4px 8px;
  border-radius: 999px;
  background: #fff3cf;
  color: #866300;
}

.product-copy h3 {
  margin: 7px 0 4px;
  line-height: 1.4;
}

.product-copy p {
  display: -webkit-box;
  overflow: hidden;
  margin: 0;
  color: var(--color-text-sub);
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.product-premium {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  margin-top: 9px;
}

.product-premium span {
  color: var(--color-text-muted);
}

.product-chevron {
  align-self: center;
  color: #b0b0b0;
  font-size: 13px;
}

.error-state {
  margin-top: 16px;
}

.error-state p {
  margin: 7px 0 0;
  color: var(--color-text-sub);
}

.error-state .content-btn {
  margin-top: 16px;
}

@media (min-width: 540px) {
  .category-section {
    margin-right: -20px;
    margin-left: -20px;
  }

  .category-tabs {
    padding-right: 20px;
    padding-left: 20px;
  }
}
</style>
