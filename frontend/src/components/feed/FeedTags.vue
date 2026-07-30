<template>
  <div class="tags">
    <div class="tag" :class="transactionTypeClass">
      #{{ transactionTypeText }}
    </div>

    <div class="tag user-tag">#{{ feed.transaction.receiver.nickname }}</div>

    <div class="tag category-tag" :class="randomCategoryColor">
      #{{ feed.transaction.category.categoryName }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  feed: {
    type: Object,
    required: true,
  },
});

const transactionTypeMap = {
  PAYMENT: {
    text: '결제',
    class: 'payment',
  },

  TRANSFER: {
    text: '송금',
    class: 'transfer',
  },

  SETTLEMENT: {
    text: '정산',
    class: 'settlement',
  },
};

const transactionTypeText = computed(() => {
  return transactionTypeMap[props.feed.transaction.transactionType]?.text ?? '';
});

const transactionTypeClass = computed(() => {
  return (
    transactionTypeMap[props.feed.transaction.transactionType]?.class ?? ''
  );
});

/* 카테고리 태그 랜덤 색상 */
const categoryColors = [
  'category-blue',
  'category-green',
  'category-orange',
  'category-purple',
  'category-pink',
];

const randomCategoryColor = computed(() => {
  const index = Math.floor(Math.random() * categoryColors.length);

  return categoryColors[index];
});
</script>

<style scoped>
.tags {
  display: flex;
  flex-wrap: wrap;

  gap: 8px;

  margin-top: 14px;
}

.tag {
  display: inline-flex;

  align-items: center;

  padding: 6px 12px;

  border-radius: 999px;

  font-size: 13px;

  font-weight: 600;
}

/* 결제 */
.payment {
  background: #e8f1ff;
  border: 1px solid #c9ddff;
  color: #3478ff;
}

/* 송금 */
.transfer {
  background: #e8fff3;
  border: 1px solid #bff0d4;
  color: #16a05d;
}

/* 정산 */
.settlement {
  background: #f3ebff;
  border: 1px solid #ddc8ff;
  color: #7a42d8;
}

/* 사용자 */
.user-tag {
  background: #fff0f5;
  border: 1px solid #ffd1df;
  color: #ff5a7a;
}

/* 카테고리 랜덤 색상 */
.category-blue {
  background: #e8f1ff;
  border: 1px solid #c9ddff;
  color: #3478ff;
}

.category-green {
  background: #e8fff3;
  border: 1px solid #bff0d4;
  color: #16a05d;
}

.category-orange {
  background: #fff1e8;
  border: 1px solid #ffd4b8;
  color: #ff7a2f;
}

.category-purple {
  background: #f3ebff;
  border: 1px solid #ddc8ff;
  color: #7a42d8;
}

.category-pink {
  background: #fff0f5;
  border: 1px solid #ffd1df;
  color: #ff5a7a;
}
</style>
