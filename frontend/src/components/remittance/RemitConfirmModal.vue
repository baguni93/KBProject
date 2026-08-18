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
          <h5 class="text-18-bold title-m0">
            {{ remitType === 'DUTCH' ? '정산 요청 확인' : '송금 정보 최종 확인' }}
          </h5>
        </div>

        <div class="confirm-body">
          <template v-if="remitType === 'DUTCH'">
            <div class="dutch-modal-title-box text-center">
              <h3 class="text-22-bold m-0" style="color: #111111; line-height: 1.3;">
                총 {{ formatCurrency(amount) }}원을<br />정산 요청할까요?
              </h3>
            </div>

            <div class="dutch-modal-members-card">
              <div class="dutch-modal-row text-15-bold">
                <span class="member-name">나</span>
                <span class="member-amt">{{ formatCurrency(Math.floor(amount / ((selectedDutchFriends?.length || 0) + 1))) }}원</span>
              </div>
              <div
                v-for="fId in selectedDutchFriends"
                :key="fId"
                class="dutch-modal-row text-15-bold"
              >
                <span class="member-name">{{ getFriendName ? getFriendName(fId) : '친구' }}</span>
                <span class="member-amt">{{ formatCurrency(Math.floor(amount / ((selectedDutchFriends?.length || 0) + 1))) }}원</span>
              </div>
            </div>
          </template>

          <template v-else>
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
          </template>
        </div>

        <div class="bottom-btn-area double complete-button-area" style="margin-top: 20px;">
          <button
            type="button"
            class="bottom-btn secondary-button text-16-bold"
            @click="$emit('close')"
          >
            취소
          </button>
          <button
            type="button"
            class="bottom-btn primary-button text-16-bold"
            @click="$emit('confirm')"
          >
            {{ remitType === 'DUTCH' ? '요청하기' : '송금하기' }}
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
  remitType: {
    type: String,
    default: "ACCOUNT",
  },
  selectedDutchFriends: {
    type: Array,
    default: () => [],
  },
  getFriendName: {
    type: Function,
    default: () => "친구",
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
@import "@/components/common/common/common.css";

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

.bottom-sheet-content {
  width: 100%;
  background-color: #ffffff;
  border-top-left-radius: 24px;
  border-top-right-radius: 24px;
  padding: 16px 24px 32px;
  box-sizing: border-box;
  position: relative;
}

.sheet-handle-bar {
  width: 40px;
  height: 4px;
  border-radius: 2px;
  background-color: #e2e8f0;
  margin: 0 auto 16px;
  cursor: pointer;
}

.confirm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #a0aec0;
  cursor: pointer;
}

.dutch-modal-title-box {
  margin: 12px 0 20px;
}

.dutch-modal-members-card {
  background-color: #f8f9fa;
  border-radius: 16px;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dutch-modal-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.member-name {
  color: #111111;
}

.member-amt {
  color: #ffbc2e;
}

.target-card {
  background-color: #f8f9fa;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
}

.target-label {
  color: #718096;
  margin: 0 0 6px;
}

.target-name {
  margin: 0;
  color: #111111;
}

.target-acc {
  margin: 4px 0 0;
  color: #4a5568;
}

.amount-card {
  background-color: #fffdf8;
  border: 1px solid #ffbc2e;
  border-radius: 16px;
  padding: 16px;
}

.amt-label {
  color: #718096;
  margin: 0 0 4px;
}

.amt-val {
  margin: 0;
  color: #111111;
}

.complete-button-area {
  width: 100%;
}

.secondary-button {
  border: 1px solid #e2e8f0 !important;
  background: #ffffff !important;
  color: #111111 !important;
  height: 52px;
  border-radius: 14px;
}

.primary-button {
  border: none !important;
  background: #ffbc2e !important;
  color: #111111 !important;
  height: 52px;
  border-radius: 14px;
}
</style>
