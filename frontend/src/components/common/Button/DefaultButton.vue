<template>
  <button type="button" @click="$emit('click')">
    {{ text }}
  </button>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    required: true,
  },
});

defineEmits(['click']);
</script>

<style scoped></style>
