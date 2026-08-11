<template>
  <div class="benefit-selection-wrapper">
    <!-- 3. 혜택 선택 패키지 리스트 영역 -->
    <section class="benefit-list-section">
      <ul class="card-list">
        <li
          v-for="benefitPack in filteredBenefits"
          :key="benefitPack.id"
          class="benefit-card-item"
          :class="{ selected: selectedBenefitId === benefitPack.id }"
          @click="selectBenefit(benefitPack)"
        >
          <div class="card-header-row">
            <span class="card-badge">인기 혜택 팩</span>
            <span class="select-indicator">
              {{ selectedBenefitId === benefitPack.id ? '선택됨' : '' }}
            </span>
          </div>

          <h3 class="card-name text-15-bold">{{ benefitPack.name }}</h3>

          <div class="benefit-box">
            <div
              v-for="(item, index) in benefitPack.items"
              :key="index"
              class="benefit-row"
            >
              <span class="benefit-text text-13">{{ item.text }}</span>
              <span class="highlight text-13">{{ item.highlight }}</span>
            </div>
          </div>
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';

// 부모(CardEditor)의 슬롯에서 :tab="currentTab"으로 전달받는 값
const props = defineProps({
  tab: {
    type: String,
    default: 'all',
  },
});

const emit = defineEmits(['select-benefit', 'update:isValid']);
const router = useRouter();
const selectedBenefitId = ref(null);

const selectBenefit = (benefitPack) => {
  selectedBenefitId.value = benefitPack.id;
  emit('select-benefit', benefitPack);
  emit('update:isValid', true);
};

// 더미 혜택 패키지 데이터
const benefitList = ref([
  {
    id: 1,
    type: 'lifestyle',
    name: '디지털 플렉스 팩',
    items: [
      { text: '넷플릭스·유튜브프리미엄 구독', highlight: '50% 할인' },
      { text: '스타벅스 및 투썸플레이스', highlight: '20% 할인' },
      { text: '배달의민족·쿠팡이츠 배달앱', highlight: '10% 할인' },
    ],
  },
  {
    id: 2,
    type: 'shopping',
    name: '트렌디 쇼퍼 팩',
    items: [
      { text: '올리브영 및 무신사 스토어', highlight: '15% 할인' },
      { text: '에이블리·지그재그 패션 플랫폼', highlight: '10% 할인' },
      { text: '네이버페이 간편결제 결제 시', highlight: '5% 적립' },
    ],
  },
  {
    id: 3,
    type: 'daily',
    name: '데일리 라이프 팩',
    items: [
      { text: '대중교통(버스/지하철) 및 택시', highlight: '10% 할인' },
      { text: 'CU·GS25 편의점 결제', highlight: '10% 할인' },
      { text: '올영세일 및 다이소 결제', highlight: '5% 할인' },
    ],
  },
  {
    id: 4,
    type: 'lifestyle',
    name: '글로벌 트래블 팩',
    items: [
      { text: '해외 온/오프라인 가맹점 수수료', highlight: '전액 면제' },
      { text: '공항 라운지 무료 이용', highlight: '연 1회' },
      { text: '철도(KTX/SRT) 및 고속버스', highlight: '10% 할인' },
    ],
  },
]);

// 부모에게 받은 탭 값에 따라 리스트 필터링 ('all'일 때는 전체 노출)
const filteredBenefits = computed(() => {
  if (props.tab === 'all' || !props.tab) {
    return benefitList.value;
  }
  return benefitList.value.filter((item) => item.type === props.tab);
});
</script>

<style scoped>
.text-15-bold {
  font-size: 14px;
  font-weight: 700;
}
.text-13 {
  font-size: 12px;
  font-weight: 500;
}

.benefit-selection-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-sizing: border-box;
}

/* 2. AI 분석 유도 섹션 */
.ai-pattern-box {
  background: var(--color-bg-page, #ffffff);
  border: 1.5px solid var(--color-border-main, #dddddd);
  border-radius: 16px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.ai-pattern-box:hover {
  border-color: var(--color-primary, #ffbc2e);
  transform: translateY(-2px);
}

.ai-left {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
  flex: 1;
}

.ai-badge {
  font-size: 11px;
  font-weight: 600;
  color: #d97706;
}

.ai-title {
  margin: 0;
  color: var(--color-text-main, #111111);
}

.ai-sub {
  margin: 0;
  color: var(--color-text-sub, #777777);
  line-height: 1.3;
}

.ai-right {
  flex-shrink: 0;
  margin-left: 10px;
}

.ai-btn {
  background-color: var(--color-primary, #ffbc2e);
  color: var(--color-text-main, #111111);
  border: 1px solid var(--color-primary-border, #cc9200);
  font-size: 11px;
  font-weight: 600;
  padding: 6px 10px;
  border-radius: 8px;
  cursor: pointer;
  white-space: nowrap;
}

/* 3. 혜택 리스트 영역 */
.benefit-list-section {
  display: flex;
  flex-direction: column;
}

.card-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.benefit-card-item {
  background-color: var(--color-bg-page, #ffffff);
  padding: 16px;
  border-radius: 16px;
  border: 1.5px solid var(--color-border-main, #dddddd);
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  transition: all 0.2s ease;
}

.benefit-card-item:hover {
  border-color: var(--color-primary, #ffbc2e);
  transform: translateY(-2px);
}

.benefit-card-item.selected {
  border-color: var(--color-primary, #ffbc2e);
  background-color: #fffdf4;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-badge {
  font-size: 11px;
  font-weight: 600;
  color: #d97706;
  background-color: #fef3c7;
  padding: 2px 6px;
  border-radius: 4px;
}

.select-indicator {
  font-size: 11px;
  font-weight: 600;
  color: #9ca3af;
}

.benefit-card-item.selected .select-indicator {
  color: #d97706;
}

.card-name {
  color: var(--color-text-main, #111111);
  margin: 0 0 12px 0;
}

.benefit-box {
  background-color: #f9fafb;
  border-radius: 12px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  border: 1px solid #f3f4f6;
}

.benefit-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.benefit-text {
  color: var(--color-text-sub, #777777);
}

.highlight {
  color: #d97706;
  font-weight: 700;
}
</style>
