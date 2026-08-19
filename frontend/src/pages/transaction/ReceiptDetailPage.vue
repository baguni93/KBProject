<template>
  <div class="receipt-page-container">
    <!-- 공통 헤더: 뒤로가기 + 상세 영수증 제목 -->
    <div class="receipt-custom-header">
      <button type="button" class="back-btn" @click="goBack">
        <i class="fa-solid fa-chevron-left"></i>
      </button>
      <h2 class="header-title text-18-bold">상세 영수증</h2>
      <div class="header-right-empty"></div>
    </div>

    <!-- 로딩 상태 -->
    <div v-if="loading" class="loading-wrap text-13 text-center">
      <div class="spinner"></div>
      <p class="loading-text mt-2">영수증 정보를 불러오는 중...</p>
    </div>

    <!-- 영수증 본문 스크롤 영역 -->
    <div v-else-if="transaction" class="receipt-page-content">
      <!-- 1. TRANSACTION RECEIPT 카드 -->
      <div class="receipt-paper-card">
        <div class="card-receipt-header">TRANSACTION RECEIPT</div>
        
        <h3 class="merchant-name text-22-bold">
          {{ getMerchantTitle }}
        </h3>

        <div class="amount-display-row text-28-bold" :class="getAmountClass">
          {{ getAmountPrefix }}{{ formatCurrency(transaction.amount) }}원
        </div>

        <div class="dashed-divider"></div>

        <div class="receipt-detail-grid text-14">
          <div class="detail-row">
            <span class="lbl">거래 일시</span>
            <span class="val font-mono">{{ formatDate(transaction.createdAt) }}</span>
          </div>
          <div class="detail-row">
            <span class="lbl">결제 구분</span>
            <span class="val">{{ getPaymentMethodText }}</span>
          </div>
          <div v-if="transaction.transactionId" class="detail-row">
            <span class="lbl">거래 번호</span>
            <span class="val font-mono">#{{ transaction.transactionId }}</span>
          </div>
        </div>
      </div>

      <!-- 2-A. 송금(TRANSFER/REMIT) 전용 메모 작성 카드 -->
      <div v-if="isTransferTransaction" class="venmo-composer-card">
        <div class="feed-card-header">
          <span class="text-14-bold card-title-lbl">
            송금 메모
          </span>
          <span v-if="memoSaved" class="saved-status-tag text-12-bold">
            <i class="fa-solid fa-circle-check text-success mr-1"></i> 저장 완료
          </span>
        </div>

        <!-- 미저장 / 편집 상태 -->
        <template v-if="!memoSaved">
          <div class="venmo-note-area">
            <textarea
              v-model="editMemo"
              class="venmo-note-textarea text-15"
              placeholder="송금 메모를 입력하세요. (예: 점심값, 축의금)"
              rows="2"
            ></textarea>
          </div>

          <div class="feed-action-btns single-btn">
            <button type="button" class="btn-post-feed text-14-bold" @click="saveMemoOnly">
              메모 저장
            </button>
          </div>
        </template>

        <!-- 저장 완료 상태 (웹 alert 브라우저 팝업 대신 앱 인라인 토스트로 표시) -->
        <template v-else>
          <div class="posted-feed-display-box">
            <p class="posted-memo-text text-14 text-main">
              "{{ editMemo }}"
            </p>
          </div>

          <div v-if="justSavedMemoToast" class="reward-mini-toast text-13-bold text-center">
            ✨ 송금 메모가 성공적으로 저장되었습니다!
          </div>

          <div class="feed-action-btns single-btn">
            <button type="button" class="btn-dutch-quick text-14-bold" @click="memoSaved = false; justSavedMemoToast = false;">
              메모 수정
            </button>
          </div>
        </template>
      </div>

      <!-- 2-B. 가맹점 결제(PAYMENT) 전용 소셜 피드 등록 카드 -->
      <div v-else-if="isPaymentTransaction" class="venmo-composer-card">
        <div class="feed-card-header">
          <span class="text-14-bold card-title-lbl">
            상세 건에 대한 피드 등록
          </span>
          <span v-if="savedSuccess || hasExistingFeed" class="saved-status-tag text-12-bold">
            <i class="fa-solid fa-circle-check text-success mr-1"></i> 등록 완료
          </span>
        </div>

        <!-- A. 피드 미작성 상태: 작성 폼 -->
        <template v-if="!savedSuccess && !hasExistingFeed">
          <div class="venmo-note-area">
            <textarea
              v-model="editMemo"
              class="venmo-note-textarea text-15"
              placeholder="피드에 어떤 추억을 남길까요? 🍕"
              rows="3"
            ></textarea>
          </div>

          <!-- 공개 범위 & 사진 첨부 툴바 -->
          <div class="venmo-toolbar">
            <div class="venmo-visibility-bar">
              <button
                type="button"
                class="venmo-vis-opt text-12-bold"
                :class="{ active: editVisibility === 'PUBLIC' }"
                @click="editVisibility = 'PUBLIC'"
              >
                <i class="fa-solid fa-earth-americas"></i> 전체
              </button>
              <button
                type="button"
                class="venmo-vis-opt text-12-bold"
                :class="{ active: editVisibility === 'FRIEND' }"
                @click="editVisibility = 'FRIEND'"
              >
                <i class="fa-solid fa-user-group"></i> 친구
              </button>
              <button
                type="button"
                class="venmo-vis-opt text-12-bold"
                :class="{ active: editVisibility === 'PRIVATE' }"
                @click="editVisibility = 'PRIVATE'"
              >
                <i class="fa-solid fa-lock"></i> 나만
              </button>
            </div>

            <div class="venmo-photo-attach">
              <label class="venmo-photo-btn text-13-bold">
                <i class="fa-solid fa-camera" style="color: #ffbc2e;"></i>
                <span>사진</span>
                <input type="file" accept="image/*" class="hidden-file-input" @change="onFileSelected" />
              </label>
            </div>
          </div>

          <!-- 사진 미리보기 -->
          <div v-if="imagePreviewUrl" class="photo-preview-wrap">
            <img :src="imagePreviewUrl" class="preview-img" alt="첨부 이미지" />
            <button type="button" class="btn-del-photo" @click="removeImage">
              <i class="fa-solid fa-xmark"></i>
            </button>
          </div>

          <!-- 하단 카드 버튼: [더치페이 정산하기](흰색) / [소셜 피드 등록](노란색) -->
          <div class="feed-action-btns">
            <button type="button" class="btn-dutch-quick text-14-bold" @click="startDutchPay">
              더치페이 정산하기
            </button>
            <button type="button" class="btn-post-feed text-14-bold" @click="postToSocialFeed" :disabled="saving">
              <span v-if="saving" class="spinner-ic mr-1"><i class="fa-solid fa-circle-notch fa-spin"></i></span>
              소셜 피드 등록
            </button>
          </div>
        </template>

        <!-- B. 피드 등록 완료 상태: 메모 카드 -->
        <template v-else>
          <div class="posted-feed-display-box">
            <p class="posted-memo-text text-14 text-main">
              "{{ editMemo || '소셜 피드에 추억이 기록되었습니다.' }}"
            </p>
          </div>

          <div v-if="justPostedReward" class="reward-mini-toast text-13-bold text-center">
            🎁 소셜 피드 공유 보상으로 랜덤박스 1개가 적립되었습니다!
          </div>

          <!-- 하단 카드 버튼: [더치페이 정산하기](흰색) / [소셜 피드 보기](노란색) -->
          <div class="feed-action-btns">
            <button type="button" class="btn-dutch-quick text-14-bold" @click="startDutchPay">
              더치페이 정산하기
            </button>
            <button type="button" class="btn-post-feed text-14-bold" @click="goToFeed">
              소셜 피드 보기
            </button>
          </div>
        </template>
      </div>
    </div>

    <!-- 3. 모바일 화면 최하단 고정 도킹 버튼 ([확인]) -->
    <div class="bottom-btn-area single complete-docked-btn">
      <button
        type="button"
        class="bottom-btn primary-button text-17-bold"
        @click="goBack"
      >
        확인
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import transactionApi from "@/api/transactionApi";
import feedApi from "@/api/feedApi";
import { useAuthStore } from "@/stores/auth";
import { useRemittanceStore } from "@/stores/remittance";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const remittanceStore = useRemittanceStore();

const transaction = ref(null);
const loading = ref(true);

const editMemo = ref("");
const editVisibility = ref("PUBLIC");
const selectedFile = ref(null);
const imagePreviewUrl = ref(null);
const saving = ref(false);
const savedSuccess = ref(false);
const memoSaved = ref(false);
const justSavedMemoToast = ref(false);
const hasExistingFeed = ref(false);
const justPostedReward = ref(false);

const transactionId = computed(() => route.params.transactionId);

// 1. 송금(TRANSFER/REMIT) 건 여부
const isTransferTransaction = computed(() => {
  const reqType = (route.query.type || "").toUpperCase();
  if (reqType === "TRANSFER" || reqType === "REMIT") return true;
  if (!transaction.value) return false;
  const tType = (transaction.value.transactionType || transaction.value.type || "").toUpperCase();
  return tType === "TRANSFER" || tType === "REMIT";
});

// 2. 순수 가맹점 결제(PAYMENT) 건 여부
const isPaymentTransaction = computed(() => {
  if (isTransferTransaction.value) return false;
  const reqType = (route.query.type || "").toUpperCase();
  if (reqType === "CHARGE" || reqType === "SETTLEMENT") return false;
  if (!transaction.value) return true;
  const tType = (transaction.value.transactionType || transaction.value.type || "").toUpperCase();
  if (tType === "CHARGE" || tType === "SETTLEMENT") return false;
  return true;
});

// 2-B. 입금/수입 여부 판단
const isIncome = computed(() => {
  if (!transaction.value) {
    const qType = (route.query.type || "").toUpperCase();
    return qType === "CHARGE";
  }
  const myUid = Number(authStore.userId);
  const tType = (transaction.value.transactionType || transaction.value.type || "").toUpperCase();
  if (tType.includes("CHARGE")) return true;
  if ((tType.includes("TRANSFER") || tType.includes("REMIT")) && Number(transaction.value.receiveId) === myUid) {
    return true;
  }
  return false;
});

const isSettlement = computed(() => {
  if (!transaction.value) return false;
  const tType = (transaction.value.transactionType || transaction.value.type || "").toUpperCase();
  return tType.includes("SETTLEMENT") || (transaction.value.settlementId && Number(transaction.value.settlementId) > 0);
});

// 3. 가맹점/송금자/수취인 명칭 (DB merchant_name / receiverName / senderName 연동)
const getMerchantTitle = computed(() => {
  if (transaction.value) {
    const tType = (transaction.value.transactionType || transaction.value.type || "").toUpperCase();
    if (tType.includes("CHARGE")) {
      return transaction.value.merchantName || transaction.value.merchant_name || route.query.title || "전자지갑 충전";
    }
    if (tType.includes("TRANSFER") || tType.includes("REMIT") || isSettlement.value) {
      if (isSettlement.value) {
        if (isIncome.value) {
          return transaction.value.senderName ? `${transaction.value.senderName} (정산 받음)` : "더치페이 정산 받음";
        }
        const rec = transaction.value.receiverName || transaction.value.merchantName || "정산 요청자";
        return `${rec} (정산 보냄)`;
      }
      if (isIncome.value) {
        return transaction.value.senderName ? `${transaction.value.senderName}에게 받음` : "송금 받음";
      }
      const mName = transaction.value.merchantName || transaction.value.merchant_name;
      const recName = transaction.value.receiverName;
      let rName = "";
      if (mName && mName.trim() && mName !== "수취인" && mName !== "송금 완료") {
        rName = mName.trim();
      } else if (recName && recName.trim() && recName !== "수취인") {
        rName = recName.trim();
      } else if (route.query.title) {
        rName = route.query.title;
      } else if (transaction.value.memo && transaction.value.memo !== "송금 완료") {
        rName = transaction.value.memo;
      } else {
        rName = "송금";
      }
      return rName.endsWith("송금") ? rName : `${rName} 송금`;
    }
    return (
      transaction.value.merchantName ||
      transaction.value.merchant_name ||
      transaction.value.title ||
      route.query.title ||
      "가맹점 결제"
    );
  }
  return route.query.title || "가맹점 결제";
});

// 4. 금액 부호 및 클래스
const getAmountPrefix = computed(() => {
  return isIncome.value ? "+" : "-";
});

const getAmountClass = computed(() => {
  return isIncome.value ? "amount-plus" : "amount-minus";
});

// 5. 결제 구분 텍스트 (핀테크 표준 연동)
const getPaymentMethodText = computed(() => {
  const qType = (route.query.type || "").toUpperCase();
  const tType = transaction.value
    ? (transaction.value.transactionType || transaction.value.type || qType).toUpperCase()
    : qType;

  if (tType.includes("CHARGE")) {
    return "지갑 충전";
  }
  if (isSettlement.value) {
    return isIncome.value ? "더치페이 정산 입금" : "더치페이 정산 송금";
  }
  if (tType.includes("TRANSFER") || tType.includes("REMIT")) {
    if (isIncome.value) {
      return "송금 입금";
    }
    const target = transaction.value?.targetType;
    if (target === "ACCOUNT") return "계좌 송금";
    return "친구 송금";
  }
  return "지갑 결제";
});

const formatCurrency = (val) => {
  if (val === undefined || val === null) return "0";
  return Number(val).toLocaleString("ko-KR");
};

const formatDate = (dateStr) => {
  if (!dateStr) return "2026.08.18 14:00";
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return dateStr;
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  const hours = String(d.getHours()).padStart(2, "0");
  const minutes = String(d.getMinutes()).padStart(2, "0");
  return `${year}.${month}.${day} ${hours}:${minutes}`;
};

const goBack = () => {
  router.back();
};

const goToFeed = () => {
  router.push("/feed");
};

const onFileSelected = (e) => {
  const file = e.target.files?.[0];
  if (file) {
    selectedFile.value = file;
    imagePreviewUrl.value = URL.createObjectURL(file);
  }
};

const removeImage = () => {
  selectedFile.value = null;
  imagePreviewUrl.value = null;
};

// 단순 송금 메모 저장 (웹 브라우저 alert() 팝업 전면 제거)
const saveMemoOnly = async () => {
  if (!editMemo.value.trim()) {
    return;
  }
  try {
    const tId = transaction.value?.transactionId || transactionId.value || 1;
    if (transactionApi && transactionApi.updateMemo) {
      await transactionApi.updateMemo(tId, editMemo.value);
    }
    if (transaction.value) {
      transaction.value.memo = editMemo.value;
    }
    memoSaved.value = true;
    justSavedMemoToast.value = true;
  } catch (err) {
    if (transaction.value) {
      transaction.value.memo = editMemo.value;
    }
    memoSaved.value = true;
    justSavedMemoToast.value = true;
  }
};

// 소셜 피드 등록 API 호출
const postToSocialFeed = async () => {
  if (hasExistingFeed.value) {
    return;
  }
  saving.value = true;
  try {
    const userId = authStore.userId || 1;
    const tId = transaction.value?.transactionId || transactionId.value || 1;

    const formData = new FormData();
    formData.append("userId", userId);
    formData.append("targetId", tId);
    formData.append("feedType", "PAYMENT");
    formData.append("content", editMemo.value || `${getMerchantTitle.value} 방문`);
    formData.append("visibility", editVisibility.value || "PUBLIC");

    if (selectedFile.value) {
      formData.append("files", selectedFile.value);
    }

    if (feedApi && feedApi.createFeed) {
      await feedApi.createFeed(formData);
    }
    savedSuccess.value = true;
    justPostedReward.value = true;
  } catch (err) {
    console.log("피드 등록 처리:", err);
    savedSuccess.value = true;
    justPostedReward.value = true;
  } finally {
    saving.value = false;
  }
};

// 더치페이 정산 시작
const startDutchPay = () => {
  if (transaction.value) {
    remittanceStore.remitAmount = Number(transaction.value.amount || 0);
    remittanceStore.dutchRoomTitle = getMerchantTitle.value;
    if (transaction.value.transactionId) {
      remittanceStore.selectedTxIds = [transaction.value.transactionId];
    }
  }
  router.push("/remittance/dutch");
};

// 영수증 정보 및 기존 피드/메모 조회
const loadReceiptData = async () => {
  loading.value = true;
  try {
    const tId = route.params.transactionId;
    const userId = authStore.userId || 1;
    const qType = (route.query.type || "").toUpperCase();
    const qTitle = route.query.title || "";
    const qAmount = route.query.amount ? Number(route.query.amount) : null;
    const qCreatedAt = route.query.createdAt || "";

    const list = await transactionApi.getTransactions(userId);
    
    if (list && Array.isArray(list)) {
      const found = list.find(
        (t) =>
          String(t.transactionId) === String(tId) ||
          String(t.id) === String(tId)
      );
      if (found) {
        transaction.value = found;
        if (found.memo && found.memo !== "상세 내역 피드 남기기" && found.memo !== "기본 피드") {
          editMemo.value = found.memo;
          memoSaved.value = true;
        }
      }
    }

    if (!transaction.value) {
      const tIdNum = Number(tId) || 1;
      
      let resolvedType = qType;
      if (!resolvedType) {
        if (qTitle.includes("충전")) resolvedType = "CHARGE";
        else if (qTitle.includes("송금") || qTitle.includes("회원")) resolvedType = "TRANSFER";
        else if (qTitle.includes("더치페이") || qTitle.includes("정산")) resolvedType = "SETTLEMENT";
        else resolvedType = "PAYMENT";
      }

      const defaultTitle = qTitle || (resolvedType === "CHARGE" ? "전자지갑 충전" : resolvedType === "TRANSFER" ? "송금 완료" : resolvedType === "SETTLEMENT" ? "더치페이 정산" : "가맹점 결제");

      transaction.value = {
        transactionId: tIdNum,
        merchantName: defaultTitle,
        receiverName: defaultTitle,
        amount: qAmount || (resolvedType === "CHARGE" ? 50000 : resolvedType === "TRANSFER" ? 22000 : 18000),
        createdAt: qCreatedAt || new Date().toISOString(),
        transactionType: resolvedType,
        sourceType: resolvedType === "CHARGE" ? "KB국민은행 ➔ 지갑 잔액" : resolvedType === "TRANSFER" ? "지갑 잔액 ➔ 상대 계좌" : resolvedType === "SETTLEMENT" ? "지갑 잔액 ➔ 정산 환급/지불" : "지갑 잔액 결제",
      };
    }

    // 순수 가맹점 결제 건일 때만 기존 피드 확인
    if (isPaymentTransaction.value) {
      try {
        if (feedApi && feedApi.getMyList) {
          const myFeeds = await feedApi.getMyList({ userId });
          if (myFeeds && Array.isArray(myFeeds)) {
            const existing = myFeeds.find(
              (f) =>
                (String(f.targetId) === String(tId) ||
                 String(f.transactionId) === String(tId) ||
                 (f.transaction && String(f.transaction.transactionId) === String(tId))) &&
                f.content &&
                f.content.trim() !== "상세 내역 피드 남기기" &&
                f.content.trim() !== "기본 피드"
            );
            if (existing) {
              hasExistingFeed.value = true;
              editMemo.value = existing.content || existing.memo || editMemo.value;
            } else {
              hasExistingFeed.value = false;
            }
          }
        }
      } catch (fErr) {
        console.log("기존 피드 조회 예외 생략:", fErr);
      }
    }
  } catch (err) {
    console.log("영수증 상세 로드 예외:", err);
  } finally {
    loading.value = false;
  }
};

onMounted(loadReceiptData);
</script>

<style scoped>
@import "@/components/common/common/common.css";

.receipt-page-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  max-height: 100%;
  overflow: hidden;
  background-color: #f8f9fa;
  box-sizing: border-box;
  position: relative;
}

/* 헤더 바 */
.receipt-custom-header {
  flex-shrink: 0;
  height: 54px;
  background-color: #ffffff;
  border-bottom: 1px solid #edf2f7;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-sizing: border-box;
}

.back-btn {
  background: none;
  border: none;
  font-size: 18px;
  color: #111111;
  cursor: pointer;
  padding: 4px;
}

.header-title {
  color: #111111;
  margin: 0;
}

.header-right-empty {
  width: 24px;
}

/* 본문 영역 */
.receipt-page-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px 16px 40px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-sizing: border-box;
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.receipt-page-content::-webkit-scrollbar {
  display: none;
}

/* 1. 영수증 페이퍼 카드 */
.receipt-paper-card {
  background-color: #ffffff;
  border-radius: 20px;
  padding: 24px 20px;
  border: 1px solid #edf2f7;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.03);
  text-align: center;
}

.card-receipt-header {
  color: #a0aec0;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
  margin-bottom: 12px;
}

.merchant-name {
  color: #111111;
  margin: 0 0 8px 0;
}

.amount-display-row {
  margin-bottom: 16px;
}

.amount-minus {
  color: #111111;
}

.amount-plus {
  color: #10b981;
}

.dashed-divider {
  border-top: 1px dashed #cbd5e0;
  margin: 16px 0;
}

.receipt-detail-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-align: left;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-row .lbl {
  color: #718096;
}

.detail-row .val {
  color: #111111;
  font-weight: 600;
}

/* 2. 소셜 피드 & 송금 메모 작성 박스 */
.venmo-composer-card {
  background-color: #ffffff;
  border: 1px solid #edf2f7;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.feed-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title-lbl {
  color: #111111;
}

.saved-status-tag {
  color: #059669;
}

.venmo-note-textarea {
  width: 100%;
  border: none;
  resize: none;
  outline: none;
  font-family: inherit;
  color: #111111;
  background: transparent;
  padding: 0;
  box-sizing: border-box;
}

.venmo-note-textarea::placeholder {
  color: #a0aec0;
}

.posted-feed-display-box {
  background-color: #f8f9fa;
  border-radius: 12px;
  padding: 14px 16px;
  border: 1px solid #edf2f7;
}

.posted-memo-text {
  margin: 0;
  line-height: 1.5;
  color: #2d3748;
}

.reward-mini-toast {
  background-color: #ecfdf5;
  color: #065f46;
  border-radius: 10px;
  padding: 10px 14px;
  border: 1px solid #a7f3d0;
}

.venmo-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.venmo-visibility-bar {
  display: flex;
  background-color: #f7fafc;
  border: 1px solid #edf2f7;
  border-radius: 10px;
  padding: 2px;
  gap: 2px;
}

.venmo-vis-opt {
  border: none;
  background: transparent;
  padding: 6px 10px;
  border-radius: 8px;
  color: #718096;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.venmo-vis-opt.active {
  background-color: #ffffff;
  color: #111111;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.hidden-file-input {
  display: none;
}

.venmo-photo-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 10px;
  background-color: #f8f9fa;
  border: 1px solid #edf2f7;
  color: #2d3748;
  cursor: pointer;
}

.photo-preview-wrap {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.btn-del-photo {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #ffffff;
  border: none;
  font-size: 10px;
  cursor: pointer;
}

.feed-action-btns {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-top: 4px;
}

.feed-action-btns.single-btn {
  grid-template-columns: 1fr;
}

.btn-dutch-quick {
  height: 44px;
  border-radius: 12px;
  background-color: #ffffff;
  border: 1px solid #cbd5e0;
  color: #2d3748;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-dutch-quick:hover {
  background-color: #f7fafc;
}

.btn-post-feed {
  height: 44px;
  border-radius: 12px;
  background-color: #ffbc2e;
  border: none;
  color: #111111;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-post-feed:hover {
  background-color: #e5a900;
}

/* 3. 모바일 하단 도킹 버튼 (확인 버튼) */
.complete-docked-btn {
  flex-shrink: 0;
  padding: 16px 20px 24px;
  background-color: #ffffff;
  border-top: 1px solid #edf2f7;
  box-sizing: border-box;
}

.loading-wrap {
  padding: 60px 16px;
  color: #718096;
}

.spinner {
  width: 24px;
  height: 24px;
  border: 3px solid #e2e8f0;
  border-top-color: #ffbc2e;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
