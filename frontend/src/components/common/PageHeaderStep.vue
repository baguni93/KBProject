<template>
  <header class="header">
    <i class="fa-solid fa-arrow-left" @click="goBack"></i>

    <h2>{{ title }}</h2>
  </header>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: '',
  },
});

const emit = defineEmits(['goBack']);

const goBack = () => {
  emit('goBack');
};
</script>

<style scoped>
.header {
  display: flex;

  align-items: center;

  gap: 15px;

  margin-bottom: 20px;
}

.header i {
  cursor: pointer;

  font-size: 18px;
}

.header h2 {
  margin: 0;

  font-size: 15px;
}
</style>
