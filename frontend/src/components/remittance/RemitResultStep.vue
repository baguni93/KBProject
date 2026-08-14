<template>
  <div class="complete-step-wrap">
    <div class="success-icon-circle">
      <i class="fa-solid fa-check"></i>
    </div>
    <h4 class="text-20-bold m-0">
      {{
        remitType === "DUTCH"
          ? "정산 요청이 성공적으로 완료되었습니다!"
          : "송금이 완료되었습니다!"
      }}
    </h4>
    <p class="text-28-bold success-amt">
      {{ formatCurrency(remitAmount) }} 원
    </p>

    <div class="complete-detail-card">
      <div class="detail-row text-13">
        <span class="lbl">받는 사람</span>
        <span class="val text-13-bold">{{ receiverName }}</span>
      </div>
      <div v-if="remitType !== 'DUTCH'" class="detail-row text-13">
        <span class="lbl">입금 계좌</span>
        <span class="val text-13-bold"
          >{{ bankName }} {{ accountNumber }}</span
        >
      </div>
    </div>

    <div class="next-btn-wrap" style="width: 100%; margin-top: 24px">
      <button
        class="bottom-btn text-18-bold"
        @click="$emit('resetAll')"
      >
        확인 (새로운 송금하기)
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  remitType: {
    type: String,
    default: "ACCOUNT",
  },
  remitAmount: {
    type: Number,
    default: 0,
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
  formatCurrency: {
    type: Function,
    required: true,
  },
});

defineEmits(["resetAll"]);
</script>

<style scoped>
.complete-step-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 40px 20px;
}

.success-icon-circle {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background-color: #2e7d32;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-bottom: 16px;
}

.success-amt {
  color: #ffbc00;
  margin: 12px 0 20px;
}

.complete-detail-card {
  width: 100%;
  background-color: var(--color-bg-screen, #f8f9fa);
  border-radius: 16px;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
}

.lbl {
  color: #777777;
}

.val {
  color: #111111;
}

.bottom-btn {
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
