<template>
  <div class="gradient-picker">
    <button
      v-for="gradient in gradients"
      :key="gradient.name"
      class="gradient-item"
      :class="{ active: modelValue === gradient.value }"
      :style="{
        background: gradient.value,
        '--accent': gradient.color,
      }"
      @click="selectGradient(gradient.value)"
    >
      <span v-if="modelValue === gradient.value"></span>
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
const gradients = [
  {
    name: 'Blue Purple',
    value: 'linear-gradient(135deg,#667eea,#764ba2)',
    color: '#667eea',
  },

  {
    name: 'Pink',
    value: 'linear-gradient(135deg,#f093fb,#f5576c)',
    color: '#f5576c',
  },

  {
    name: 'Sky',
    value: 'linear-gradient(135deg,#38bdf8,#22d3ee)',
    color: '#38bdf8',
  },

  {
    name: 'Sunset',
    value: 'linear-gradient(135deg,#ff9a9e,#fad0c4)',
    color: '#ff9a9e',
  },

  {
    name: 'Ocean',
    value: 'linear-gradient(135deg,#0f766e,#2563eb)',
    color: '#0f766e',
  },

  {
    name: 'Lavender',
    value: 'linear-gradient(135deg,#c084fc,#f5d0fe)',
    color: '#c084fc',
  },

  {
    name: 'Cream',
    value: 'linear-gradient(135deg,#fed7aa,#fecaca)',
    color: '#fed7aa',
  },

  {
    name: 'Mint',
    value: 'linear-gradient(135deg,#86efac,#67e8f9)',
    color: '#34d399',
  },

  {
    name: 'Light',
    value: 'linear-gradient(135deg,#fde68a,#d1fae5)',
    color: '#facc15',
  },
];

const selectGradient = (value) => {
  emit('update:modelValue', value);
};
</script>

<style scoped>
.gradient-picker {
  width: 100%;

  max-width: 330px;

  display: grid;

  grid-template-columns: repeat(3, 1fr);

  gap: 12px;

  margin: 0 auto;

  padding: 8px 4px;
}

.gradient-item {
  position: relative;

  height: 58px;

  border: none;

  border-radius: 14px;

  cursor: pointer;

  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;

  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);

  overflow: visible;
}

.gradient-item:hover {
  transform: translateY(-3px);

  box-shadow: 0 8px 18px color-mix(in srgb, var(--accent) 35%, transparent);
}

.gradient-item.active {
  box-shadow:
    0 0 0 3px white,
    0 0 0 5px var(--accent),
    0 8px 20px color-mix(in srgb, var(--accent) 35%, transparent);
}

.gradient-item span {
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
