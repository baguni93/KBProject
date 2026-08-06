<template>
  <article class="agreement-item">
    <div class="agreement-row">
      <label class="agreement-label">
        <input
          class="agreement-input"
          type="checkbox"
          :checked="modelValue"
          :disabled="disabled"
          @change="handleChange"
        />

        <span class="agreement-checkbox" aria-hidden="true"></span>

        <span class="agreement-text">
          <span class="agreement-name">{{ title }}</span>
          <span v-if="required" class="agreement-required">(필수)</span>
          <span v-else-if="showOptional" class="agreement-optional">(선택)</span>
        </span>
      </label>

      <button
        v-if="showDetailButton"
        type="button"
        class="agreement-detail-button"
        :aria-expanded="detailMode === 'expand' ? expanded : undefined"
        :aria-label="`${title} 상세보기`"
        @click="openDetail"
      >
        <span :class="{ expanded: detailMode === 'expand' && expanded }">&gt;</span>
      </button>
    </div>

    <div
      v-if="detailMode === 'expand' && expanded"
      class="agreement-detail"
    >
      <slot name="detail">
        <p>{{ detail }}</p>
      </slot>
    </div>
  </article>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    required: true,
  },
  detail: {
    type: String,
    default: '',
  },
  required: {
    type: Boolean,
    default: false,
  },
  showOptional: {
    type: Boolean,
    default: true,
  },
  showDetailButton: {
    type: Boolean,
    default: true,
  },
  detailMode: {
    type: String,
    default: 'expand',
    validator: (value) => ['expand', 'navigate'].includes(value),
  },
  expanded: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits([
  'update:modelValue',
  'toggle-detail',
  'open-detail',
]);

const handleChange = (event) => {
  emit('update:modelValue', event.target.checked);
};

const openDetail = () => {
  if (props.detailMode === 'navigate') {
    emit('open-detail');
    return;
  }
  emit('toggle-detail');
};
</script>

<style scoped>
.agreement-item {
  border-bottom: 1px solid #eeeeee;
}

.agreement-row {
  display: flex;
  align-items: center;
  min-height: 56px;
  gap: 10px;
}

.agreement-label {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.agreement-input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
  pointer-events: none;
}

.agreement-checkbox {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  margin-right: 14px;
  border: 1px solid #999999;
  border-radius: 6px;
  background: #ffffff;
}

.agreement-input:checked + .agreement-checkbox {
  border-color: #ffbc2e;
  background: #ffbc2e;
}

.agreement-input:checked + .agreement-checkbox::after {
  display: block;
  width: 8px;
  height: 14px;
  margin: 4px 0 0 9px;
  border: solid #ffffff;
  border-width: 0 2px 2px 0;
  content: '';
  transform: rotate(45deg);
}

.agreement-input:focus-visible + .agreement-checkbox {
  outline: 2px solid #222222;
  outline-offset: 2px;
}

.agreement-input:disabled + .agreement-checkbox {
  border-color: #dddddd;
  background: #eeeeee;
}

.agreement-text {
  min-width: 0;
  color: #222222;
  font-size: 16px;
  line-height: 1.45;
  word-break: keep-all;
}

.agreement-required,
.agreement-optional {
  margin-left: 4px;
  color: inherit;
  font-size: inherit;
  font-weight: inherit;
}

.agreement-detail-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #777777;
  font-size: 25px;
  line-height: 1;
  cursor: pointer;
}

.agreement-detail-button span {
  display: inline-block;
  transition: transform 0.2s ease;
}

.agreement-detail-button span.expanded {
  transform: rotate(90deg);
}

.agreement-detail {
  margin: 0 0 12px 42px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #f7f7f7;
  color: #666666;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-line;
}

.agreement-detail p {
  margin: 0;
}
</style>
