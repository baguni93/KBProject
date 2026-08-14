<template>
  <Teleport to=".app">
    <div
      v-if="show"
      class="modal-overlay"
      @click.self="$emit('close')"
    >
      <div class="bottom-sheet-content">
        <div class="sheet-handle-bar" @click="$emit('close')"></div>
        <div class="confirm-header">
          <h5 class="text-18-bold title-m0">송금 정보 최종 확인</h5>
          <button type="button" class="close-btn" @click="$emit('close')">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <div class="confirm-body">
          <div class="target-card">
            <p class="target-label text-13">받는 사람 / 계좌</p>
            <h4 class="target-name text-20-bold">
              {{ receiverName }}
            </h4>
            <p class="target-acc text-15">
              {{ bankName }} {{ accountNumber }}
            </p>
          </div>

          <div class="amount-card text-center">
            <p class="amt-label text-13">최종 출금 금액</p>
            <h3 class="amt-val text-28-bold">
              {{ formatCurrency(amount) }} 원
            </h3>
          </div>
        </div>

        <div class="confirm-footer">
          <button
            type="button"
            class="btn-primary text-18-bold"
            @click="$emit('confirm')"
          >
            송금 승인하기 (비밀번호 입력)
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  receiverName: {
    type: String,
    default: "",
  },
  bankName: {
    type: String,
    default: "",
  },
  accountNumber: {
    type: String,
    default: "",
  },
  amount: {
    type: Number,
    default: 0,
  },
  formatCurrency: {
    type: Function,
    required: true,
  },
});

defineEmits(["close", "confirm"]);
</script>

<style scoped>
.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  z-index: 99999;
  display: flex;
  align-items: flex-end;
  border-radius: 24px;
  overflow: hidden;
}

@media (max-width: 430px) {
  .modal-overlay {
    border-radius: 0;
  }
}

.bottom-sheet-content {
  width: 100%;
  background-color: #ffffff;
  border-top-left-radius: 24px;
  border-top-right-radius: 24px;
  padding: 16px 24px 32px;
  box-sizing: border-box;
  animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.sheet-handle-bar {
  width: 40px;
  height: 4px;
  background-color: #e0e0e0;
  border-radius: 2px;
  margin: 0 auto 16px;
  cursor: pointer;
}

.confirm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title-m0 {
  margin: 0;
}

.close-btn {
  border: none;
  background: none;
  font-size: 20px;
  color: #888888;
  cursor: pointer;
}

.confirm-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.target-card {
  background-color: #f8f9fa;
  border-radius: 16px;
  padding: 16px;
}

.target-label {
  margin: 0 0 4px;
  color: #777777;
}

.target-name {
  margin: 0 0 2px;
  color: #111111;
}

.target-acc {
  margin: 0;
  color: #555555;
}

.amount-card {
  background-color: rgba(255, 188, 0, 0.08);
  border: 1px solid rgba(255, 188, 0, 0.3);
  border-radius: 16px;
  padding: 20px;
}

.amt-label {
  margin: 0 0 4px;
  color: #777777;
}

.amt-val {
  margin: 0;
  color: #111111;
}

.btn-primary {
  width: 100%;
  height: 52px;
  border: none;
  background-color: #ffbc00;
  color: #111111;
  border-radius: 14px;
  font-weight: 700;
  cursor: pointer;
}
</style>
