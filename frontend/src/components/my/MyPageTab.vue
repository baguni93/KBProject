<template>
  <div class="tab">
    <button
      class="tab-item"
      :class="{ active: modelValue === 'feed' }"
      @click="$emit('update:modelValue', 'feed')"
    >
      내 피드
    </button>

    <button
      class="tab-item"
      :class="{ active: modelValue === 'wallet' }"
      @click="$emit('update:modelValue', 'wallet')"
    >
      내 지갑/카드
    </button>

    <div
      class="indicator"
      :style="{
        transform:
          modelValue === 'feed' ? 'translateX(0%)' : 'translateX(100%)',
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
  height: 52px;
  background: #fff;
  border-bottom: 1px solid #ececec;
}

.tab-item {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 16px;
  font-weight: 700;
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
  width: 50%;
  height: 3px;
  background-color: #ffbc2e;
  transition: transform 0.25s ease-in-out;
}
</style>
