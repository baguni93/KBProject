<template>
  <div class="card-add-root">
    <PageHeader title="카드 등록" />

    <div class="card-add-body">
      <div class="samsung-pay-head">
        <h3 class="samsung-head-title text-22-bold">카드 번호를 입력하세요</h3>
        <p class="samsung-head-sub text-13">본인 명의의 카드만 등록할 수 있습니다.</p>
      </div>

      <div class="form-card-clean">
        <form id="cardAddForm" @submit.prevent="submitCard">
          <!-- 1. 카드 번호 -->
          <div class="samsung-pay-form-group">
            <label class="samsung-label text-13-bold">카드 번호</label>
            <input
              v-model="cardForm.cardNum"
              type="text"
              class="samsung-input text-16-bold"
              placeholder="0000 - 0000 - 0000 - 0000"
              maxLength="19"
              required
              @input="onCardNumInput"
            />
          </div>

          <!-- 2. 만료일 -->
          <div class="samsung-pay-form-group">
            <label class="samsung-label text-13-bold">만료일 (MM / YY)</label>
            <input
              v-model="cardForm.expiry"
              type="text"
              class="samsung-input text-16-bold"
              placeholder="02 / 31"
              maxLength="5"
              required
              @input="formatExpiry"
            />
          </div>

          <!-- 3. 보안 코드 (CVC/CVV) -->
          <div class="samsung-pay-form-group">
            <label class="samsung-label text-13-bold">보안 코드 (CVC/CVV)</label>
            <input
              v-model="cardForm.cvc"
              type="password"
              class="samsung-input text-16-bold"
              placeholder="카드 뒷면 3자리 숫자"
              maxLength="3"
              required
            />
          </div>

          <!-- 4. 카드 비밀번호 처음 2자리 -->
          <div class="samsung-pay-form-group">
            <label class="samsung-label text-13-bold">카드 비밀번호 처음 2자리</label>
            <input
              v-model="cardForm.cardPassword"
              type="password"
              class="samsung-input text-16-bold"
              placeholder="비밀번호 앞 2자리 (**)"
              maxLength="2"
              required
              @input="onPasswordInput"
            />
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import PageHeader from "@/components/common/PageHeader.vue";
import api from "@/api";
import { registerCard } from "@/api/cardApi";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const authStore = useAuthStore();
const submitting = ref(false);

const cardForm = ref({
  cardName: "KB국민카드",
  cardAlias: "",
  cardNum: "",
  expiry: "",
  cvc: "",
  cardPassword: "",
});

const cardPreviewImg = ref("");
const isImgError = ref(false);

const handleImgError = () => {
  isImgError.value = true;
};

// 카드번호 입력 처리 (하이픈 자동 서식)
const onCardNumInput = (e) => {
  let val = e.target.value.replace(/\D/g, "");
  if (val.length > 16) val = val.slice(0, 16);
  cardForm.value.cardNum = val.replace(/(\d{4})(?=\d)/g, "$1-");
};

// 비밀번호 2자리 완성 시 자동 제출
const onPasswordInput = () => {
  if (
    cardForm.value.cardPassword &&
    cardForm.value.cardPassword.length === 2 &&
    isFormValid.value &&
    !submitting.value
  ) {
    submitCard();
  }
};

// 필수 입력값 엄격 검증 (카드번호 16자리, 유효기간 숫자 4자리, CVC 3자리, 비밀번호 2자리)
const isFormValid = computed(() => {
  const cleanNum = cardForm.value.cardNum.replace(/\D/g, "");
  const cleanExpiry = cardForm.value.expiry.replace(/\D/g, "");
  const cleanCvc = cardForm.value.cvc.replace(/\D/g, "");
  const cleanPassword = cardForm.value.cardPassword.replace(/\D/g, "");

  return (
    cleanNum.length === 16 &&
    cleanExpiry.length === 4 &&
    cleanCvc.length === 3 &&
    cleanPassword.length === 2
  );
});

const formatExpiry = (e) => {
  let val = e.target.value.replace(/\D/g, "");
  if (val.length > 4) val = val.slice(0, 4);
  if (val.length >= 3) {
    cardForm.value.expiry = `${val.slice(0, 2)}/${val.slice(2)}`;
  } else {
    cardForm.value.expiry = val;
  }
};

const submitCard = async () => {
  if (!isFormValid.value || submitting.value) return;
  submitting.value = true;
  try {
    let rawExpiry = (cardForm.value.expiry || "")
      .trim()
      .replace(/[^0-9/]/g, "");
    if (rawExpiry.includes("/")) {
      const parts = rawExpiry.split("/");
      let mm = parts[0].padStart(2, "0");
      let yy = parts[1] || "28";
      if (yy.length === 4) yy = yy.slice(-2);
      rawExpiry = `${mm}/${yy}`.slice(0, 5);
    } else if (rawExpiry.length === 4) {
      rawExpiry = `${rawExpiry.slice(0, 2)}/${rawExpiry.slice(2)}`.slice(0, 5);
    } else {
      rawExpiry = "12/28";
    }

    const payload = {
      userId: authStore.userId || 1,
      cardName: cardForm.value.cardName,
      cardNum: cardForm.value.cardNum || "9410-1234-5678-9999",
      expiryDate: rawExpiry,
      cvv: (cardForm.value.cvc || "777").slice(0, 3),
      // Pad card password to 4 digits (default "1234")
      cardPassword: (cardForm.value.cardPassword || "1234").padEnd(4, "0"),
      cardImageName: cardPreviewImg.value || "",
    };

    await registerCard(payload);
    router.push("/wallet");
  } catch (err) {
    console.error("카드 등록 실패:", err);
    const msg =
      err.response?.data?.message || err.message || "카드 등록에 실패했습니다.";
    alert(`카드 등록 실패: ${msg}`);
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.card-add-root {
  display: flex;
  flex-direction: column;
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 0;
  background-color: var(--color-bg-page, #ffffff);
  color: var(--color-text-main, #111111);
  box-sizing: border-box;
  overflow: hidden;
}

.samsung-pay-head {
  margin-top: 12px;
  margin-bottom: 48px;
}

.samsung-head-title {
  margin: 0 0 8px 0;
  color: #111111;
  letter-spacing: -0.5px;
}

.samsung-head-sub {
  margin: 0;
  color: #777777;
}

.form-card-clean {
  display: flex;
  flex-direction: column;
}

#cardAddForm {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.samsung-pay-form-group {
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border: 1px solid #dcdce2;
  border-radius: 18px;
  padding: 16px 20px;
  margin-bottom: 20px;
  transition: all 0.2s ease;
}

.samsung-pay-form-group:last-child {
  margin-bottom: 0;
}

.samsung-pay-form-group:focus-within {
  border-color: #2b7fff;
  box-shadow: 0 0 0 3px rgba(43, 127, 255, 0.15);
}

.samsung-label {
  color: #777777;
  font-size: 12px;
  margin-bottom: 4px;
}

.samsung-input {
  border: none;
  outline: none;
  background: transparent;
  width: 100%;
  font-size: 17px;
  color: #111111;
  padding: 2px 0;
}

.samsung-input::placeholder {
  color: #cccccc;
  font-weight: 500;
}

.card-add-header {
  flex-shrink: 0;
  position: relative;
  z-index: 50;
  width: 100%;
  height: 48px;
  background-color: var(--color-bg-page, #ffffff);
  border-bottom: 1px solid var(--color-divider, #ededed);
}

.header-inner {
  display: grid;
  grid-template-columns: 70px 1fr 38px;
  width: 100%;
  height: 48px;
  align-items: center;
  padding: 0 20px;
  box-sizing: border-box;
}

.back-btn {
  display: flex;
  align-items: center;
  justify-self: start;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--color-text-sub, #777777);
  cursor: pointer;
}

.header-title {
  margin: 0;
  color: var(--color-text-main, #111111);
  text-align: center;
}

.close-x-btn {
  display: flex;
  width: 38px;
  height: 38px;
  align-items: center;
  justify-content: center;
  justify-self: end;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--color-text-sub, #777777);
  font-size: 18px;
  cursor: pointer;
}

.card-add-body {
  flex: 1;
  min-height: 0;
  width: 100%;
  padding: 20px 20px 30px;
  background-color: var(--color-bg-page, #ffffff);
  box-sizing: border-box;
  overflow-y: auto;
}

.form-card {
  width: 100%;
  margin: 0;
  padding: 0;
  border: 0;
  background-color: var(--color-bg-page, #ffffff);
  box-sizing: border-box;
}

.card-preview-plate-clean {
  position: relative;
  display: flex;
  width: 100%;
  min-height: 190px;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin-bottom: 24px;
  padding: 0;
  border-radius: 14px;
  background: transparent;
  box-shadow: none;
  box-sizing: border-box;
  overflow: hidden;
}

.card-bg-full-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center;
  z-index: 1;
  padding: 0;
}

.card-img-placeholder-empty {
  width: 100%;
  height: 190px;
}

.form-group {
  margin-bottom: 20px;
  text-align: left;
}

.label-with-badge {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.form-label {
  display: block;
  margin-bottom: 6px;
  color: var(--color-text-main, #111111);
}

.brand-ic {
  color: var(--color-primary-border, #cc9200);
}

.kb-input {
  width: 100%;
  height: 48px;
  padding: 0 14px;
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 10px;
  background-color: var(--color-bg-page, #ffffff);
  color: var(--color-text-main, #111111);
  outline: none;
  box-sizing: border-box;
}

.kb-input::placeholder {
  color: var(--color-text-disabled, #aaaaaa);
}

.kb-input:focus {
  border-color: var(--color-primary-border, #cc9200);
}

.auth-fields-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 24px;
  text-align: left;
}

.field-item {
  display: flex;
  flex-direction: column;
}

.field-item .form-label {
  white-space: nowrap;
  font-size: 12px;
}

.field-item .kb-input {
  padding: 0 8px;
}

.form-btn-row {
  flex-shrink: 0;
  position: relative;
  z-index: 20;
  display: grid;
  grid-template-columns: 0.8fr 1.6fr;
  gap: 8px;
  padding: 12px 20px 20px;
  background-color: var(--color-bg-page, #ffffff);
  border-top: 1px solid var(--color-divider, #ededed);
}

.cancel-btn {
  height: 50px;
}

.bottom-btn {
  width: 100%;
  height: 50px;
  background-color: var(--color-primary, #ffbc2e);
  color: var(--color-text-main, #111111);
  border: 1px solid var(--color-primary-border, #cc9200);
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.bottom-btn:disabled {
  background-color: var(--color-bg-disabled, #eeeeee);
  border-color: var(--color-border-main, #dddddd);
  color: var(--color-text-disabled, #aaaaaa);
  cursor: not-allowed;
}

.content-btn.secondary {
  background-color: var(--color-bg-page, #ffffff);
  border-color: var(--color-border-main, #dddddd);
  color: var(--color-text-sub, #777777);
}

.mr-1 {
  margin-right: 4px;
}

.flex-1 {
  flex: 1;
}

.fade-in {
  animation: fade-in 0.25s ease-in-out;
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: scale(0.96);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
