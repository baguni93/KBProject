<template>
  <div class="card-add-root">
    <div class="card-add-header">
      <div class="header-inner">
        <button class="back-btn text-13-bold" @click="$router.push('/wallet')">
          <i class="fa-solid fa-chevron-left mr-1"></i> 지갑
        </button>
        <h4 class="header-title text-18-bold">KB국민카드 등록</h4>
        <button
          class="close-x-btn"
          @click="$router.push('/wallet')"
          title="닫기"
        >
          <i class="fa-solid fa-xmark"></i>
        </button>
      </div>
    </div>

    <div class="card-add-body">
      <div class="form-card">
        <div class="card-preview-plate-clean">
          <img
            v-if="cardPreviewImg && !isImgError"
            :src="cardPreviewImg"
            :alt="cardForm.cardName"
            class="card-bg-full-img fade-in"
            referrerpolicy="no-referrer"
            @error="handleImgError"
          />
          <div v-else class="card-img-placeholder-empty"></div>
        </div>

        <form id="cardAddForm" @submit.prevent="submitCard">
          <div class="form-group">
            <div class="label-with-badge">
              <label class="form-label text-13-bold">카드 번호 (16자리)</label>
            </div>
            <input
              v-model="cardForm.cardNum"
              type="text"
              class="kb-input text-15-bold"
              placeholder="'-' 없이 16자리 숫자 입력"
              maxLength="19"
              required
              @input="onCardNumInput"
            />
          </div>

          <div class="form-group">
            <label class="form-label text-13-bold">
              <i class="fa-solid fa-tag brand-ic mr-1"></i>카드 별칭 (선택)
            </label>
            <input
              v-model="cardForm.cardAlias"
              type="text"
              class="kb-input text-15"
              placeholder="예: 메인 KB 체크카드, 생활비 카드"
              maxLength="20"
            />
          </div>

          <div class="auth-fields-grid">
            <div class="field-item">
              <label class="form-label text-13-bold">유효기간</label>
              <input
                v-model="cardForm.expiry"
                type="text"
                class="kb-input text-13-bold text-center"
                placeholder="MM/YY"
                maxLength="5"
                required
                @input="formatExpiry"
              />
            </div>

            <div class="field-item">
              <label class="form-label text-13-bold">CVC 번호</label>
              <input
                v-model="cardForm.cvc"
                type="password"
                class="kb-input text-13-bold text-center"
                placeholder="3자리"
                maxLength="3"
                required
              />
            </div>

            <div class="field-item">
              <label class="form-label text-13-bold">비밀번호</label>
              <input
                v-model="cardForm.cardPassword"
                type="password"
                class="kb-input text-13-bold text-center"
                placeholder="앞 2자리"
                maxLength="2"
                required
              />
            </div>
          </div>
        </form>
      </div>
    </div>

    <div class="form-btn-row">
      <button
        type="button"
        class="content-btn secondary cancel-btn text-15-bold"
        @click="$router.push('/wallet')"
      >
        취소
      </button>
      <button
        type="submit"
        form="cardAddForm"
        class="bottom-btn text-18-bold flex-1"
        :disabled="!isFormValid || submitting"
      >
        <span v-if="submitting" class="spinner-ic mr-1"
          ><i class="fa-solid fa-circle-notch fa-spin"></i
        ></span>
        카드 등록 완료
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
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

// BIN (앞 6자리) 기반 자동 조회 및 수집된 로컬 이미지 매핑 로직
const onCardNumInput = async (e) => {
  let val = e.target.value.replace(/\D/g, "");
  if (val.length > 16) val = val.slice(0, 16);
  cardForm.value.cardNum = val.replace(/(\d{4})(?=\d)/g, "$1-");

  if (val.length >= 6) {
    const bin = val.slice(0, 6);
    let detectedName = "KB국민 신용/체크카드";
    let matchedImageFileName = "09297_img.png"; // 기본 매핑 이미지 (노리2 등)

    try {
      const { data } = await api.get(`/api/cards/bin/${bin}`);
      if (data) {
        if (data.cardName) detectedName = data.cardName;
        if (data.imageUrl) {
          matchedImageFileName = data.imageUrl;
        }
      }
    } catch (err) {
      console.log("BIN API 조회 예외 발생, 로컬 패턴 규칙 매핑 적용");
      if (bin.startsWith("53")) {
        matchedImageFileName = "09129_img.png"; // 톡톡MyPoint
        detectedName = "KB국민 톡톡MyPoint 카드";
      } else if (bin.startsWith("45")) {
        matchedImageFileName = "09297_img.png"; // 노리2 체크
        detectedName = "KB Pay 노리2 체크카드";
      } else if (bin.startsWith("9")) {
        matchedImageFileName = "09800_img.png";
        detectedName = "KB국민 프리미엄 카드";
      }
    }

    cardForm.value.cardName = detectedName;
    cardPreviewImg.value = `/api/feeds/cardImage/${matchedImageFileName}`;
    isImgError.value = false;
  } else {
    cardForm.value.cardName = "KB국민카드";
    cardPreviewImg.value = "";
    isImgError.value = false;
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
      cardPassword: cardForm.value.cardPassword || "12",
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
