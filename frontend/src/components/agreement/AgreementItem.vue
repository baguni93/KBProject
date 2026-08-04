<template>
  <div class="agreement-item">
    <label class="agreement-label">
      <input
          :checked="agreement.agreed"
          type="checkbox"
          @change="change"
      />

      <span class="check-box"></span>

      <span class="agreement-name">
        {{ agreement.agreementName }}
        {{ agreement.requiredYn === 'Y' ? '(필수)' : '(선택)' }}
      </span>
    </label>

    <button class="detail-button" type="button" @click="showDetail">
      &gt;
    </button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const props = defineProps({
  agreement: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(['change']);
const router = useRouter();

// 체크 변경
const change = (event) => {
  emit('change', {
    agreementType: props.agreement.agreementType,
    agreed: event.target.checked,
  });
};

// 상세 이동
const showDetail = () => {
  router.push(`/signup/agreement/${props.agreement.agreementType}`);
};
</script>

<style scoped>
.agreement-item {
  display: flex;
  align-items: center;
  min-height: 56px;
}

.agreement-label {
  display: flex;
  flex: 1;
  align-items: center;
  cursor: pointer;
}

.agreement-label input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
}

.check-box {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  margin-right: 14px;
  border: 1px solid #999999;
  border-radius: 6px;
  background: #ffffff;
}

.agreement-label input:checked + .check-box {
  border-color: #ffbc2e;
  background: #ffbc2e;
}

.agreement-label input:checked + .check-box::after {
  display: block;
  width: 8px;
  height: 14px;
  margin: 4px 0 0 9px;
  border: solid #ffffff;
  border-width: 0 2px 2px 0;
  content: '';
  transform: rotate(45deg);
}

.agreement-name {
  color: #222222;
  font-size: 16px;
}

.detail-button {
  padding: 8px;
  border: 0;
  background: transparent;
  color: #777777;
  font-size: 25px;
  line-height: 1;
}
</style>