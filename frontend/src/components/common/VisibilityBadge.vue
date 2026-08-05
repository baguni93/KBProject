<template>
  <span class="visibility">
    <i :class="icon"></i>
    {{ text }}
  </span>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  visibility: {
    type: String,
    required: true,
  },
});

const visibilityMap = {
  PUBLIC: {
    icon: 'fa-solid fa-earth-americas',
    text: '공개',
  },
  FRIEND: {
    icon: 'fa-solid fa-user-group',
    text: '친구',
  },
  PRIVATE: {
    icon: 'fa-solid fa-lock',
    text: '비공개',
  },
};

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
