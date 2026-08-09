<template>
  <div class="color-picker">
    <div class="color-list">
      <button
        v-for="color in colors"
        :key="color"
        class="color-item"
        :class="{ active: modelValue === color }"
        :style="{
          backgroundColor: color,
          '--active-color': color,
        }"
        @click="selectColor(color)"
      >
        <span v-if="modelValue === color"></span>
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    default: '#FFB000',
  },
});

const emit = defineEmits(['update:modelValue']);

const colors = [
  '#FFB000', // 노랑
  '#6679D8', // 블루
  '#4CC9C0', // 민트
  '#FF6699', // 핑크
  '#2E3347', // 네이비
  '#00A8D8', // 하늘
];

const selectColor = (color) => {
  emit('update:modelValue', color);
};
</script>

<style scoped>
.color-picker {
  width: 100%;

  padding: 8px 0;
}

.color-list {
  width: 100%;

  display: flex;

  justify-content: center;

  align-items: center;

  gap: 12px;

  padding: 6px 0;
}

.color-item {
  position: relative;

  width: 44px;

  height: 44px;

  border-radius: 12px;

  border: none;

  cursor: pointer;

  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;

  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.12);
}

.color-item:hover {
  transform: translateY(-3px);

  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.18);
}

.color-item.active {
  transform: scale(1.05);

  box-shadow:
    0 0 0 3px white,
    0 0 0 5px var(--active-color),
    0 8px 20px rgba(0, 0, 0, 0.18);
}

/* 선택 표시 */
.color-item span {
  position: absolute;

  width: 10px;

  height: 10px;

  right: 8px; /* 기존 7px */

  top: 8px; /* 기존 7px */

  border-radius: 50%;

  background: white;

  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}
</style>
