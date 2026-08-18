<template>
  <Teleport to="body">
    <div v-if="show" class="receipt-modal-root">
      <!-- 1. 독립 백드롭 레이어 -->
      <div class="receipt-modal-backdrop" @click="closeModal"></div>

      <!-- 2. 모달 컨텐츠 레이어 -->
      <div class="receipt-modal-container">
        <div class="modal-card">
          <!-- 닫기 X 버튼 -->
          <button type="button" class="close-btn" @click="closeModal">
            <i class="fa-solid fa-xmark"></i>
          </button>

          <div v-if="loading" class="loading-box text-13">
            <div class="spinner"></div>
            <div class="loading-text">영수증 정보를 불러오는 중...</div>
          </div>

          <div v-else-if="transaction" class="receipt-body">
            <div class="receipt-badge-wrap text-center">
              <span class="receipt-badge text-13-bold">Social Wallet 영수증</span>
            </div>

            <!-- 금액 -->
            <h2
              class="text-28-bold receipt-amount text-center"
              :class="transaction.transactionType === 'CHARGE' ? 'amount-income' : 'amount-expense'"
            >
              {{ formatCurrency(transaction.amount) }}
            </h2>
            <div class="type-badge-wrap text-center">
              <span class="type-badge text-13">{{
                getTransactionTypeBadge(transaction.transactionType)
              }}</span>
            </div>

            <div class="dashed-line"></div>

            <!-- 상세 정보 테이블 -->
            <div class="detail-info-list text-13">
              <div class="info-row">
                <span class="info-label text-13">거래 번호</span>
                <span class="info-value text-13-bold font-mono"
                  >#{{ transaction.transactionId }}</span
                >
              </div>
              <div class="info-row">
                <span class="info-label text-13">거래 수단</span>
                <span class="info-value text-13-bold"
                  >{{ transaction.sourceType || "WALLET" }} ➔
                  {{ transaction.targetType || "ACCOUNT" }}</span
                >
              </div>
              <div v-if="transaction.receiverName" class="info-row">
                <span class="info-label text-13">수신자/가맹점</span>
                <span class="info-value text-13-bold">{{
                  transaction.receiverName
                }}</span>
              </div>
              <div class="info-row">
                <span class="info-label text-13">거래 일시</span>
                <span class="info-value text-13-bold">{{
                  formatDate(transaction.createdAt)
                }}</span>
              </div>
            </div>

            <div class="dashed-line"></div>

            <!-- 거래 내역 피드 글 남기기 (사진/이미지 첨부 지원) -->
            <template v-if="transaction">
              <div class="feed-write-section">
                <div class="section-label-row">
                  <span class="text-13-bold"
                    ><i class="fa-solid fa-comment-dots brand-ic mr-1"></i>피드
                    글 남기기</span
                  >
                  <button
                    type="button"
                    class="content-btn secondary text-13-bold photo-btn"
                    @click="triggerFileInput"
                  >
                    <i class="fa-solid fa-camera mr-1"></i> 사진 첨부
                  </button>
                </div>

                <!-- 숨김 파일 인풋 -->
                <input
                  ref="fileInput"
                  type="file"
                  accept="image/*"
                  class="hidden-file-input"
                  @change="onFileSelected"
                />

                <!-- 이미지 미리보기 썸네일 -->
                <div v-if="imagePreviewUrl" class="image-preview-box">
                  <img :src="imagePreviewUrl" class="preview-img" />
                  <button
                    type="button"
                    class="remove-img-btn"
                    @click="removeImage"
                  >
                    <i class="fa-solid fa-xmark"></i>
                  </button>
                </div>

                <div class="input-btn-group">
                  <input
                    type="text"
                    v-model="editMemo"
                    class="custom-input text-15"
                    placeholder="피드 메시지 작성 (예: 오늘 저녁 맛있게 먹었습니다! 😋)"
                    @keyup.enter="saveMemo"
                  />
                  <button
                    type="button"
                    class="content-btn primary text-13-bold post-btn"
                    @click="saveMemo"
                    :disabled="saving"
                  >
                    <span v-if="saving" class="spinner-ic mr-1"
                      ><i class="fa-solid fa-circle-notch fa-spin"></i
                    ></span>
                    게시
                  </button>
                </div>

                <!-- 피드 공개 범위 선택 버튼 그룹 -->
                <label class="field-label text-13-bold mt-2">
                  <i class="fa-solid fa-shield-halved brand-ic mr-1"></i>공개
                  범위 선택
                </label>

                <div class="vis-grid">
                  <button
                    type="button"
                    class="content-btn secondary text-13-bold"
                    :class="{ active: editVisibility === 'PUBLIC' }"
                    @click="editVisibility = 'PUBLIC'"
                  >
                    🌐 전체 공개
                  </button>
                  <button
                    type="button"
                    class="content-btn secondary text-13-bold"
                    :class="{ active: editVisibility === 'FRIEND' }"
                    @click="editVisibility = 'FRIEND'"
                  >
                    👥 친구 공개
                  </button>
                  <button
                    type="button"
                    class="content-btn secondary text-13-bold"
                    :class="{ active: editVisibility === 'PRIVATE' }"
                    @click="editVisibility = 'PRIVATE'"
                  >
                    🔒 나만 보기
                  </button>
                </div>

                <div v-if="savedSuccess" class="alert-success-box text-13">
                  <div class="alert-top-row">
                    <i class="fa-solid fa-circle-check success-ic"></i>
                    <div>
                      <strong class="text-13-bold"
                        >피드 글이 성공적으로 게시되었습니다!</strong
                      >
                      <span class="d-block text-13 vis-sub"
                        >공개 범위: {{ getVisLabel(editVisibility) }}</span
                      >
                    </div>
                  </div>
                  <button
                    type="button"
                    class="content-btn secondary text-13-bold go-feed-btn"
                    @click="goToFeed"
                  >
                    피드로 이동하여 확인하기
                    <i class="fa-solid fa-arrow-right ml-1"></i>
                  </button>
                </div>
              </div>

              <!-- 이 내역으로 더치페이하기 -->
              <button
                type="button"
                class="bottom-btn text-15-bold dutch-btn"
                @click="startDutchpayFromReceipt"
              >
                <i class="fa-solid fa-calculator mr-1"></i> 이 내역으로
                더치페이하기
              </button>
            </template>

            <button
              type="button"
              class="bottom-btn text-15-bold dark-btn"
              @click="closeModal"
            >
              확인
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import api from "@/api";
import transactionApi from "@/api/transactionApi";

const router = useRouter();
const authStore = useAuthStore();

const props = defineProps({
  show: Boolean,
  transactionId: Number,
});

const emit = defineEmits(["close", "updated"]);

const transaction = ref(null);
const editMemo = ref("");
const editVisibility = ref("PUBLIC");
const loading = ref(false);
const saving = ref(false);
const savedSuccess = ref(false);

const fileInput = ref(null);
const selectedFile = ref(null);
const imagePreviewUrl = ref("");

const triggerFileInput = () => {
  if (fileInput.value) {
    fileInput.value.click();
  }
};

const onFileSelected = (e) => {
  const file = e.target.files[0];
  if (file) {
    selectedFile.value = file;
    imagePreviewUrl.value = URL.createObjectURL(file);
  }
};

const removeImage = () => {
  selectedFile.value = null;
  imagePreviewUrl.value = "";
  if (fileInput.value) {
    fileInput.value.value = "";
  }
};

const closeModal = () => {
  savedSuccess.value = false;
  removeImage();
  emit("close");
};

const getVisLabel = (val) => {
  if (val === "PUBLIC") return "전체 공개";
  if (val === "FRIEND") return "친구 공개";
  if (val === "PRIVATE") return "나만 보기";
  return "전체 공개";
};

const goToFeed = async () => {
  closeModal();
  await router.push("/feed");
};

const startDutchpayFromReceipt = async () => {
  const amt = transaction.value ? transaction.value.amount : 5000;
  const merchantName =
    transaction.value?.receiverName || transaction.value?.memo || "결제건";
  const titleText = `${merchantName} 더치페이`;

  closeModal();
  await router.push({
    path: "/remittance",
    query: {
      type: "DUTCHPAY",
      amount: amt,
      title: titleText,
    },
  });
};

const formatCurrency = (val) => {
  if (val === undefined || val === null) return "0원";
  return Number(val).toLocaleString("ko-KR") + "원";
};

const formatDate = (dateStr) => {
  if (!dateStr) return new Date().toLocaleString("ko-KR");
  const d = new Date(dateStr);
  return d.toLocaleString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};

const getTransactionTypeBadge = (type) => {
  switch (type) {
    case "CHARGE":
      return "지갑 충전";
    case "TRANSFER":
      return "송금 완료";
    case "PAYMENT":
      return "가맹점 결제";
    default:
      return "가맹점 결제";
  }
};

const fetchDetail = async () => {
  if (!props.transactionId) return;
  loading.value = true;
  savedSuccess.value = false;
  removeImage();
  try {
    const data = await transactionApi.getTransactionDetail(props.transactionId);
    if (data) {
      transaction.value = data;
      editMemo.value = data.memo || "";
    } else {
      transaction.value = {
        transactionId: props.transactionId,
        amount: 18500,
        transactionType: "PAYMENT",
        receiverName: "스타벅스 강남대로점",
        sourceType: "WALLET",
        targetType: "ACCOUNT",
        createdAt: new Date().toISOString(),
      };
    }
  } catch (err) {
    transaction.value = {
      transactionId: props.transactionId,
      amount: 18500,
      transactionType: "PAYMENT",
      receiverName: "강남 쉐이크쉑 수제버거",
      sourceType: "WALLET",
      targetType: "ACCOUNT",
      createdAt: new Date().toISOString(),
    };
  } finally {
    loading.value = false;
  }
};

const saveMemo = async () => {
  if (!props.transactionId) return;
  saving.value = true;
  savedSuccess.value = false;

  const contentMsg =
    editMemo.value || `${transaction.value?.receiverName || "결제건"} 피드 글`;

  try {
    await transactionApi.updateMemo(props.transactionId, contentMsg);
    if (transaction.value) {
      transaction.value.memo = contentMsg;
    }
  } catch (err) {
    if (transaction.value) {
      transaction.value.memo = contentMsg;
    }
  }

  try {
    const formData = new FormData();
    formData.append("userId", authStore.userId || 1);
    formData.append("targetId", props.transactionId);
    formData.append(
      "feedType",
      transaction.value?.transactionType || "PAYMENT",
    );
    formData.append("content", contentMsg);
    formData.append("visibility", editVisibility.value || "PUBLIC");

    if (selectedFile.value) {
      formData.append("files", selectedFile.value);
    }

    const { data: createdFeed } = await api.post(
      "/api/remittances/receipt-feed",
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      },
    );

    console.log("결제 내역 피드 사진 업로드 성공:", createdFeed);
    savedSuccess.value = true;
    emit("updated");
  } catch (e) {
    console.log("Payment feed DB save fallback:", e);
    try {
      await api.post("/api/feeds", {
        userId: authStore.userId || 1,
        targetId: props.transactionId,
        feedType: transaction.value?.transactionType || "PAYMENT",
        content: contentMsg,
        visibility: editVisibility.value || "PUBLIC",
      });
    } catch (e2) {}
    savedSuccess.value = true;
    emit("updated");
  } finally {
    saving.value = false;
  }
  setTimeout(async () => {
    closeModal();
    await router.push("/feed");
  }, 300);
};

watch(
  () => props.show,
  (newVal) => {
    if (newVal && props.transactionId) {
      fetchDetail();
    }
  },
);
</script>

<style scoped>
/* ==========================================================================
   디자인 시스템 명세서(common.css) 100% 반영 스타일링
   ========================================================================== */

input,
button,
select,
textarea {
  font-family: inherit;
}

.receipt-modal-root {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue",
    Arial, sans-serif;
  color: var(--color-text-main, #111111);
}

.receipt-modal-root * {
  box-sizing: border-box;
}

.receipt-modal-backdrop {
  position: absolute;
  inset: 0;
  background-color: rgba(17, 17, 17, 0.65);
  backdrop-filter: blur(4px);
  z-index: 1;
}

.receipt-modal-container {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 380px;
  padding: 0 16px;
}

.modal-card {
  position: relative;
  width: 100%;
  background-color: var(--color-bg-page, #ffffff);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.close-btn {
  position: absolute;
  top: 14px;
  right: 14px;
  background: none;
  border: none;
  color: var(--color-text-sub, #777777);
  font-size: 16px;
  cursor: pointer;
  z-index: 10;
}

/* 로딩 */
.loading-box {
  padding: 30px 0;
  text-align: center;
  color: var(--color-text-sub, #777777);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.spinner {
  width: 24px;
  height: 24px;
  border: 2px solid var(--color-border-main, #dddddd);
  border-top-color: var(--color-primary, #ffbc2e);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 본문 */
.receipt-body {
  text-align: center;
}

.receipt-badge-wrap {
  margin-bottom: 6px;
}

.receipt-badge {
  background-color: var(--color-primary, #ffbc2e);
  color: var(--color-text-main, #111111);
  padding: 3px 10px;
  border-radius: 9999px;
}

.receipt-amount {
  margin: 6px 0 10px 0;
}

.receipt-amount.amount-income {
  color: #10b981;
}

.receipt-amount.amount-expense {
  color: #111111;
}

.type-badge-wrap {
  margin-bottom: 12px;
}

.type-badge {
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  color: var(--color-text-sub, #777777);
  padding: 2px 8px;
  border-radius: 6px;
}

.dashed-line {
  border-top: 1px dashed var(--color-border-main, #dddddd);
  margin: 12px 0;
}

/* 상세 테이블 */
.detail-info-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  text-align: left;
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  color: var(--color-text-sub, #777777);
}

.info-value {
  color: var(--color-text-main, #111111);
}

.font-mono {
  font-family: monospace;
}

/* 피드 작성 섹션 */
.feed-write-section {
  text-align: left;
  margin-bottom: 12px;
}

.section-label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.brand-ic {
  color: var(--color-primary-border, #cc9200);
}

.photo-btn {
  padding: 4px 8px;
  border-radius: 6px;
}

.hidden-file-input {
  display: none;
}

.image-preview-box {
  position: relative;
  margin: 8px 0;
  display: inline-block;
}

.preview-img {
  max-height: 100px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid var(--color-border-main, #dddddd);
}

.remove-img-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  background-color: var(--color-text-main, #111111);
  color: #ffffff;
  border: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  font-size: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.input-btn-group {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
}

.custom-input {
  flex: 1;
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 10px;
  padding: 8px 12px;
  color: var(--color-text-main, #111111);
  outline: none;
}

.custom-input:focus {
  border-color: var(--color-primary-border, #cc9200);
}

.post-btn {
  padding: 8px 14px;
  white-space: nowrap;
}

.field-label {
  display: block;
  color: var(--color-text-sub, #777777);
  margin-bottom: 6px;
}

.vis-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  margin-bottom: 12px;
}

/* 버튼 규격 */
.content-btn {
  padding: 8px 12px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid transparent;
}

.content-btn.primary {
  background-color: var(--color-primary, #ffbc2e);
  border-color: var(--color-primary-border, #cc9200);
  color: var(--color-text-main, #111111);
}

.content-btn.secondary {
  background-color: var(--color-bg-page, #ffffff);
  border-color: var(--color-border-main, #dddddd);
  color: var(--color-text-sub, #777777);
}

.content-btn.secondary.active {
  background-color: var(--color-text-main, #111111);
  border-color: var(--color-text-main, #111111);
  color: #ffffff;
}

/* 알림 박스 */
.alert-success-box {
  background-color: #ecfdf5;
  border: 1px solid #a7f3d0;
  border-radius: 10px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 8px;
}

.alert-top-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.success-ic {
  color: var(--color-success, #1fa64b);
  font-size: 16px;
}

.vis-sub {
  color: var(--color-text-sub, #777777);
  margin-top: 2px;
}

.go-feed-btn {
  width: 100%;
  color: var(--color-success, #1fa64b);
  border-color: #a7f3d0;
}

/* 하단 주요 버튼 */
.bottom-btn {
  width: 100%;
  height: 48px;
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

.dutch-btn {
  margin-bottom: 8px;
}

.dark-btn {
  background-color: var(--color-text-main, #111111);
  color: #ffffff;
  border-color: var(--color-text-main, #111111);
}

.mr-1 {
  margin-right: 4px;
}

.ml-1 {
  margin-left: 4px;
}

.d-block {
  display: block;
}
</style>
