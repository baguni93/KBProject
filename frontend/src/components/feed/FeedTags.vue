<template>
  <div class="tags">
    <div class="tag category-tag" :class="randomCategoryColor">
      #{{ categoryName }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  categoryName: {
    type: String,
    required: true,
  },
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
  gap: 6px;
  margin-top: 1px;
  margin-left: 0px;
}

.tag {
  display: inline-flex;
  align-items: center;

  padding: 4px 9px;

  border-radius: 7px;

  font-size: 10px;

  font-weight: 600;
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
