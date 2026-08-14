<template>
  <div
    class="search-bar"
    :class="{ clickable: !editable }"
    @click="handleClick"
  >
    <i class="fa-solid fa-magnifying-glass search-icon"></i>

    <input
      v-if="editable"
      v-model="model"
      placeholder="사용자 검색"
      @keyup.enter="$emit('search', model)"
    />

    <span v-else class="placeholder-text">
      {{ placeholder }}
    </span>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const model = defineModel();

const props = defineProps({
  placeholder: {
    type: String,
    default: '사용자 검색',
  },
  editable: {
    type: Boolean,
    default: false,
  },
});

defineEmits(['search']);

const router = useRouter();

const handleClick = () => {
  if (!props.editable) {
    router.push('/search');
  }
};
</script>

<style scoped>
.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 48px;
  padding: 0 16px;

  background: #e2edf6;
  border-radius: 24px;

  cursor: pointer;
  box-sizing: border-box;
}

.search-icon {
  color: #555;
  font-size: 16px;
  flex-shrink: 0;
}

.search-bar input {
  flex: 1;
  height: 100%;

  border: none;
  outline: none;
  background: transparent;

  font-size: 15px;
  padding: 0;
  margin: 0;
}

.search-bar span {
  color: #888;
  font-size: 15px;
  margin: 0;
  padding: 0;
}

.placeholder-text {
  color: #667085;
  font-size: 15px;
  font-weight: 500;
}
</style>
