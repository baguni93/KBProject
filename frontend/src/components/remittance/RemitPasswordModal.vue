<template>
  <Teleport to=".app">
    <Transition name="modal">
      <div
        v-if="show"
        class="overlay modal-overlay"
        @click.self="handleClose"
      >
        <section class="pin-modal">
          <!-- 닫기 버튼 -->
          <button type="button" class="pin-modal-close-btn" @click="handleClose">
            <i class="fa-solid fa-xmark"></i>
          </button>

          <!-- 상단 잠금 아이콘 (팀원 회원탈퇴 모달과 100% 동일 규격) -->
          <div class="pin-icon">
            <i class="fa-solid fa-lock"></i>
          </div>

          <h3 class="text-20-bold">{{ title }}</h3>

          <p class="pin-description text-13" v-html="description"></p>

          <!-- 6자리 PIN Box 디스플레이 (클릭 시 키보드 포커스) -->
          <div
            class="pin-boxes"
            :class="{ error: !!errorMessage }"
            role="button"
            tabindex="0"
            @click="focusPinInput"
            @keydown.enter="focusPinInput"
          >
            <div
              v-for="index in 6"
              :key="index"
              class="pin-box"
              :class="{
                filled: effectivePin.length >= index,
                active: effectivePin.length === index - 1 && !errorMessage,
              }"
            >
              <span v-if="effectivePin.length >= index" class="pin-dot"></span>
            </div>

            <!-- 숨겨진 숫자 입력 인풋 (모바일/PC 키보드 연동) -->
            <input
              ref="pinInputRef"
              :value="effectivePin"
              class="hidden-pin-input"
              inputmode="numeric"
              maxlength="6"
              pattern="[0-9]*"
              type="password"
              autocomplete="current-password"
              @input="onInputChange"
            />
          </div>

          <!-- 안내 및 에러 메시지 영역 -->
          <div class="pin-message-area">
            <p v-if="errorMessage" class="pin-error-message text-13">
              {{ errorMessage }}
            </p>
            <p v-else class="pin-helper-message">
              입력한 비밀번호는 본인 확인에만 사용돼요.
            </p>
          </div>

          <!-- 확인 버튼 (6자리 입력 시 노란색 활성화) -->
          <button
            class="pin-confirm-button"
            :disabled="effectivePin.length !== 6"
            type="button"
            @click="handleConfirm"
          >
            확인
          </button>

          <!-- 간편비밀번호 재설정 링크 -->
          <button
            v-if="!pinLocked"
            class="pin-forgot-link text-13"
            type="button"
            @click="$emit('forgotPin')"
          >
            간편비밀번호를 잊으셨나요?
          </button>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick } from "vue";

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  inputPin: {
    type: String,
    default: "",
  },
  errorMessage: {
    type: String,
    default: "",
  },
  pinLocked: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: "간편비밀번호 확인",
  },
  description: {
    type: String,
    default: "본인 확인을 위해 현재 간편비밀번호<br />6자리를 입력해 주세요.",
  },
});

const emit = defineEmits([
  "close",
  "enterPin",
  "clearPin",
  "deletePin",
  "forgotPin",
  "confirm",
  "update:inputPin",
]);

const pinInputRef = ref(null);
const internalPin = ref("");

const effectivePin = computed(() => {
  return props.inputPin !== undefined && props.inputPin !== ""
    ? props.inputPin
    : internalPin.value;
});

const focusPinInput = async () => {
  await nextTick();
  pinInputRef.value?.focus();
};

const onInputChange = (event) => {
  const cleanVal = event.target.value.replace(/[^0-9]/g, "").slice(0, 6);
  internalPin.value = cleanVal;
  emit("update:inputPin", cleanVal);

  if (cleanVal.length > (props.inputPin || "").length) {
    const lastChar = cleanVal.slice(-1);
    emit("enterPin", Number(lastChar));
  } else if (cleanVal.length < (props.inputPin || "").length) {
    emit("deletePin");
  }

  if (event.target.value !== cleanVal) {
    event.target.value = cleanVal;
  }
};

const handleClose = () => {
  internalPin.value = "";
  emit("clearPin");
  emit("close");
};

const handleConfirm = () => {
  if (effectivePin.value.length === 6) {
    emit("confirm", effectivePin.value);
  }
};

watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      internalPin.value = props.inputPin || "";
      focusPinInput();
    } else {
      internalPin.value = "";
    }
  }
);
</script>

<style scoped>
@import "@/components/common/common/common.css";

/* 중앙 Modal Overlay */
.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
}

/* PIN Modal Card (WithdrawPage.vue와 100% 동일 규격) */
.pin-modal {
  position: relative;
  width: 100%;
  max-width: 360px;
  padding: 26px 22px 22px;
  border-radius: 22px;
  background: var(--color-bg-page, #ffffff);
  box-sizing: border-box;
  text-align: center;
  box-shadow: 0 22px 60px rgba(0, 0, 0, 0.2);
}

.pin-modal-close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  border: 0;
  background: transparent;
  color: var(--color-text-muted, #888888);
  font-size: 16px;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pin-icon {
  display: flex;
  width: 52px;
  height: 52px;
  align-items: center;
  justify-content: center;
  margin: 0 auto 17px;
  border-radius: 17px;
  background: #fff4d7;
  color: var(--color-primary-active, #d49500);
  font-size: 20px;
}

.pin-modal h3 {
  margin: 0;
  color: var(--color-text-main, #111111);
}

.pin-description {
  margin: 9px 0 0;
  color: var(--color-text-muted, #777777);
  font-weight: 400;
  line-height: 1.55;
}

.pin-boxes {
  position: relative;
  display: grid;
  width: 100%;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 7px;
  margin-top: 25px;
  cursor: text;
}

.pin-box {
  display: flex;
  height: 48px;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border-main, #e0e0e0);
  border-radius: 11px;
  background: var(--color-bg-screen, #f8f9fa);
  box-sizing: border-box;
  transition: all 0.2s ease;
}

.pin-box.active {
  border-color: var(--color-primary, #ffbc2e);
  background: #fffaf0;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.1);
}

.pin-box.filled {
  border-color: var(--color-primary, #ffbc2e);
  background: #fff8e5;
}

.pin-boxes.error .pin-box {
  border-color: var(--color-error, #e53935);
  background: var(--color-bg-page, #ffffff);
  box-shadow: 0 0 0 3px rgba(229, 57, 53, 0.07);
}

.pin-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-text-main, #111111);
}

.hidden-pin-input {
  position: absolute;
  width: 1px;
  height: 1px;
  border: 0;
  opacity: 0;
  pointer-events: none;
}

.pin-message-area {
  min-height: 36px;
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pin-error-message,
.pin-helper-message {
  margin: 0;
  font-size: 13px;
  font-weight: 400;
  line-height: 1.45;
}

.pin-error-message {
  color: var(--color-error, #e53935);
}

.pin-helper-message {
  color: var(--color-text-muted, #777777);
}

.pin-confirm-button {
  width: 100%;
  height: 48px;
  border: 0;
  border-radius: 12px;
  background: var(--color-primary, #ffbc2e);
  color: var(--color-text-main, #111111);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pin-confirm-button:disabled {
  background: var(--color-bg-disabled, #e0e0e0);
  color: var(--color-text-disabled, #999999);
  cursor: not-allowed;
}

.pin-forgot-link {
  display: block;
  width: 100%;
  margin-top: 14px;
  background: transparent;
  border: none;
  color: var(--color-text-muted, #888888);
  font-size: 13px;
  text-decoration: underline;
  cursor: pointer;
  padding: 0;
}
</style>
