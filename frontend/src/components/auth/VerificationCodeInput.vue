<template>
  <div class="code-input-wrap" @click="focusInput">
    <div
        v-for="index in 6"
        :key="index"
        class="code-box"
        :class="{
        filled: modelValue.length >= index,
        expired,
      }"
    >
      {{ modelValue[index - 1] || '' }}
    </div>

    <input
        ref="inputRef"
        :value="modelValue"
        class="hidden-input"
        inputmode="numeric"
        maxlength="6"
        type="text"
        @input="change"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue';

defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  expired: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['update:modelValue']);
const inputRef = ref(null);

// 인증번호 변경
const change = (event) => {
  const value = event.target.value.replace(/[^0-9]/g, '').slice(0, 6);
  emit('update:modelValue', value);
};

// 입력창 포커스
const focusInput = () => {
  inputRef.value?.focus();
};
</script>

<style scoped>
.code-input-wrap {
  position: relative;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
  width: 100%;
  cursor: text;
}

.code-box {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 58px;
  border: 1px solid #dddddd;
  border-radius: 9px;
  background: #ffffff;
  color: #222222;
  font-size: 22px;
  font-weight: 700;
}

.code-box.filled {
  border-color: #ffbc2e;
}

.code-box.expired {
  border-color: #e53935;
}

.hidden-input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
}
</style>