<template>
  <div class="pin-input">
    <button
        v-for="index in length"
        :key="index"
        class="pin-circle"
        :class="{ active: modelValue.length >= index }"
        type="button"
    ></button>
  </div>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  length: {
    type: Number,
    default: 6,
  },
});
</script>

<style scoped>
.pin-input {
  display: flex;
  justify-content: center;
  gap: 18px;
}

.pin-circle {
  width: 18px;
  height: 18px;
  padding: 0;
  border: 1px solid #cccccc;
  border-radius: 50%;
  background: #ffffff;
}

.pin-circle.active {
  border-color: #222222;
  background: #222222;
}
</style>