<template>
  <div class="tab">
    <!-- 피드 -->
    <button
      class="tab-item"
      :class="{ active: modelValue === 'feed' }"
      @click="$emit('update:modelValue', 'feed')"
    >
      피드
    </button>

    <!-- 정산 -->
    <button
      class="tab-item"
      :class="{ active: modelValue === 'settlement' }"
      @click="$emit('update:modelValue', 'settlement')"
    >
      정산
    </button>

    <!-- 지갑/카드 -->
    <button
      class="tab-item"
      :class="{ active: modelValue === 'wallet' }"
      @click="$emit('update:modelValue', 'wallet')"
    >
      지갑/카드
    </button>

    <!-- 활성 탭 indicator -->
    <div
      class="indicator"
      :style="{
        transform:
          modelValue === 'feed'
            ? 'translateX(0%)'
            : modelValue === 'settlement'
              ? 'translateX(100%)'
              : 'translateX(200%)',
      }"
    />
  </div>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    required: true,
  },
});

defineEmits(['update:modelValue']);
</script>

<style scoped>
.tab {
  position: relative;
  display: flex;
  width: 100%;
  height: 52px;
  background: #fff;
  border-bottom: 1px solid #ececec;
}

.tab-item {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  font-weight: 500;
  color: #999;
  cursor: pointer;
  transition: color 0.2s;
}

.tab-item.active {
  color: #111;
}

.indicator {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 33.3333%;
  height: 3px;
  background-color: #ffbc2e;
  transition: transform 0.25s ease-in-out;
}
</style>
