<template>
  <div class="special-picker">
    <button
      v-for="image in backgrounds"
      :key="image"
      class="special-item"
      :class="{ active: modelValue === image }"
      :style="{
        backgroundImage: `url(${image})`,
      }"
      @click="selectImage(image)"
    >
      <span v-if="modelValue === image"></span>
    </button>
  </div>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    default: '',
  },
});

const emit = defineEmits(['update:modelValue']);

const backgrounds = [
  '/images/card_edit_background/aurora.png',
  '/images/card_edit_background/liquid.png',
  '/images/card_edit_background/glass.png',
  '/images/card_edit_background/geometry.png',
  '/images/card_edit_background/nature.png',
  '/images/card_edit_background/luxury.png',
];

const selectImage = (image) => {
  emit('update:modelValue', image);
};
</script>

<style scoped>
.special-picker {
  width: 100%;

  max-width: 330px;

  display: grid;

  grid-template-columns: repeat(3, 1fr);

  gap: 14px;

  margin: 0 auto;

  /*
    hover 확대 공간 확보
  */
  padding: 6px 4px;

  box-sizing: border-box;
}

.special-item {
  position: relative;

  height: 58px;

  border: none;

  border-radius: 14px;

  cursor: pointer;

  background-size: cover;

  background-position: center;

  overflow: visible;

  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;

  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
}

/* hover */
.special-item:hover {
  transform: translateY(-4px);

  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.18);
}

/* 선택 */
.special-item.active {
  transform: translateY(-2px);

  box-shadow:
    0 0 0 3px white,
    0 0 0 5px #ffc400,
    0 10px 20px rgba(255, 196, 0, 0.35);
}

/* 선택 표시 */
.special-item span {
  position: absolute;

  width: 10px;

  height: 10px;

  right: 8px;

  top: 8px;

  border-radius: 50%;

  background: white;

  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.25);
}
</style>
