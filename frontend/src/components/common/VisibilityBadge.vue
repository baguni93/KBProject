<template>
  <span class="visibility">
    <i :class="icon"></i>
    {{ text }}
  </span>
</template>

<script setup>
import { computed } from 'vue';
import config from '@/config/feed';

const props = defineProps({
  visibility: {
    type: String,
    required: true,
  },
});

const visibilityMap = config.visibilityMap;

const icon = computed(() => {
  return visibilityMap[props.visibility]?.icon ?? '';
});

const text = computed(() => {
  return visibilityMap[props.visibility]?.text ?? '';
});
</script>

<style scoped>
.visibility {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
</style>
