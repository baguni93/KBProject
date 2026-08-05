<template>
  <div
    class="search-bar"
    :class="{ clickable: !editable }"
    @click="handleClick"
  >
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="18"
      height="18"
      fill="none"
      viewBox="0 0 24 24"
      stroke="currentColor"
    >
      <path
        stroke-linecap="round"
        stroke-linejoin="round"
        stroke-width="2"
        d="M21 21l-4.35-4.35M11 18a7 7 0 100-14 7 7 0 000 14z"
      />
    </svg>

    <input
      v-if="editable"
      v-model="model"
      :placeholder="placeholder"
      @keyup.enter="$emit('search', model)"
    />

    <span v-else>
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
  gap: 10px;

  width: 100%;
  height: 48px;

  padding: 0 16px;

  background: #f5f5f5;

  border-radius: 14px;
}

.search-bar svg {
  color: #888;
  flex-shrink: 0;
}

input {
  flex: 1;

  border: none;
  outline: none;

  background: transparent;

  font-size: 15px;
}

span {
  color: #999;
  font-size: 15px;
}
</style>
