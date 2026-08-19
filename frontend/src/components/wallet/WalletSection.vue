<template>
  <div class="wallet-section-wrapper">
    <!-- 1. 내가 지금 사용하고 있는 카드 섹션 -->
    <div class="my-wallet-card-section">
      <div class="card-section-header">
        <h3 class="section-main-title">내가 지금 사용하고 있는 카드</h3>
        <router-link to="/setting/cards" class="header-action-link">
          카드 변경하기 <i class="fa-solid fa-chevron-right text-11"></i>
        </router-link>
      </div>

      <!-- 대표 카드 비주얼 (커스텀/등록 카드 이미지 동적 표시) -->
      <div
        class="rep-card-visual"
        :class="{ 'has-image': !!repCardImage }"
        @click="goToCardPage"
      >
        <template v-if="repCardImage">
          <img :src="repCardImage" class="rep-card-img" alt="대표 카드 이미지" @error="onCardImgError" />
        </template>
        <template v-else>
          <div class="card-top-row">
            <span class="card-brand-logo">{{ repCardName }}</span>
          </div>
          <div class="card-chip-row">
            <div class="ic-chip-icon">
              <i class="fa-solid fa-microchip"></i>
            </div>
            <i class="fa-solid fa-wifi contactless-ic"></i>
          </div>
          <div class="card-number-row">
            {{ primaryCardNumber }}
          </div>
        </template>
      </div>
    </div>

    <!-- 2. 지금 사용하고 있는 계좌 섹션 -->
    <div class="my-wallet-account-section">
      <div class="account-section-header">
        <h3 class="section-main-title">지금 사용 하고 있는 계좌</h3>
        <router-link to="/setting/accounts" class="header-action-link">
          계좌 변경하기 <i class="fa-solid fa-chevron-right text-11"></i>
        </router-link>
      </div>

      <!-- 대표 계좌 보유 카드 -->
      <div class="rep-account-card">
        <div class="acc-top-info">
          <div class="acc-bank-left">
            <img
              :src="kbLogoUrl"
              class="bank-logo-img"
              alt="KB국민은행 로고"
            />
            <div class="bank-name-wrap">
              <span class="bank-name">{{ primaryBankName }}</span>
              <span class="acc-badge">대표계좌</span>
            </div>
          </div>
          <span class="acc-num">{{ maskAccountNumber(primaryAccountNumber) }}</span>
        </div>

        <div class="acc-balance-bottom">
          <div class="balance-left">
            <span class="balance-label">보유 잔액</span>
            <div class="balance-val-row">
              <span class="balance-amt">
                {{ isBalanceHidden ? '*** 원' : formatCurrency(myBalance) }}
              </span>
              <button type="button" class="eye-toggle-btn" @click="isBalanceHidden = !isBalanceHidden">
                <i :class="isBalanceHidden ? 'fa-solid fa-eye-slash' : 'fa-solid fa-eye'"></i>
              </button>
            </div>
          </div>
          <div class="acc-action-btns">
            <button type="button" class="btn-charge" @click="goToCharge">충전</button>
            <button type="button" class="btn-withdraw" @click="goToRemittance">송금</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 거래내역 섹션 -->
    <div class="my-wallet-tx-section">
      <div class="tx-section-header">
        <h3 class="section-main-title tx-sec-title">거래내역</h3>
        <router-link to="/transactions" class="header-action-link">
          전체보기 <i class="fa-solid fa-chevron-right text-11"></i>
        </router-link>
      </div>

      <!-- 4개 필터 탭 (전체 | 충전 | 송금 | 결제) -->
      <div class="filter-chip-grid">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          type="button"
          class="filter-chip-btn"
          :class="{ active: selectedType === tab.value }"
          @click="changeTab(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="tx-subheader-row">
        <span class="subheader-lbl">최근 거래 내역</span>
      </div>

      <!-- 거래 내역 목록 -->
      <div v-if="loading" class="loading-box text-12">
        <div class="spinner"></div>
        <div class="loading-text">내역을 불러오는 중...</div>
      </div>

      <div v-else-if="transactions.length === 0" class="empty-tx-box">
        <i class="fa-solid fa-receipt empty-icon"></i>
        <p class="empty-text text-12">최근 거래 내역이 없습니다</p>
      </div>

      <div v-else class="tx-card-list">
        <div
          v-for="item in transactions.slice(0, 5)"
          :key="item.transactionId"
          class="tx-card-item"
          @click="openReceipt(item.transactionId)"
        >
          <div class="tx-item-left">
            <div :class="['tx-icon-circle-md', getTypeIconBgClass(item)]">
              <i :class="getTypeIcon(item)" :style="{ color: getTypeIconColor(item) }"></i>
            </div>
            <div class="tx-item-info">
              <span class="tx-item-title">{{ getItemTitleWithCategory(item) }}</span>
              <span class="tx-item-sub">{{ formatDate(item.createdAt) }} · {{ getItemMethodSubtext(item) }}</span>
            </div>
          </div>

          <div class="tx-item-right">
            <span :class="['tx-item-amt', getAmountClass(item)]">
              {{ getAmountPrefix(item) }}{{ formatCurrency(item.amount) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 하단 중앙 플로팅 (+) 버튼: 거래내역 전체보기 페이지로 이동 -->
    <div class="floating-add-btn-wrap">
      <button type="button" class="floating-add-btn" title="거래내역 전체보기" @click="goToTransactionList">
        <i class="fa-solid fa-plus"></i>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { useRemittanceStore } from "@/stores/remittance";
import transactionApi from "@/api/transactionApi";
import { getPrimaryCard, getCards } from "@/api/cardApi";
import api from "@/api";

const router = useRouter();
const authStore = useAuthStore();
const remittanceStore = useRemittanceStore();

const props = defineProps({
  userId: {
    type: Number,
    default: null,
  },
});

const emit = defineEmits(["open-receipt"]);

const selectedType = ref("");
const transactions = ref([]);
const loading = ref(false);
const isBalanceHidden = ref(false);

const repCard = ref(null);
const repCardImage = ref(null);
const repCardName = ref("KB국민카드");
const primaryCardNumber = ref("1234 **** **** 7890");
const primaryBankName = ref("KB국민은행");
const primaryAccountNumber = ref("110-111-111111");
const kbLogoUrl = ref('/api/banks/logo/kb.png');
const accountBalance = ref(null);

const myBalance = computed(() => {
  if (accountBalance.value !== null && accountBalance.value !== undefined) {
    return accountBalance.value;
  }
  if (remittanceStore.myBalance !== undefined && remittanceStore.myBalance !== null && remittanceStore.myBalance !== 0) {
    return remittanceStore.myBalance;
  }
  return 510000;
});

const tabs = [
  { label: "전체", value: "" },
  { label: "충전", value: "CHARGE" },
  { label: "송금", value: "TRANSFER" },
  { label: "결제", value: "PAYMENT" },
];

const maskAccountNumber = (acc) => {
  if (!acc) return "110-***-***111";
  const str = String(acc).replace(/[^0-9]/g, "");
  if (str.length < 6) return acc;
  const head = str.slice(0, 3);
  const tail = str.slice(-4);
  return `${head}-***-${tail}`;
};

const kbCardImageMap = {
  "KB Pay 노리2 체크카드 (KB국민카드)": "/images/cards/nori2.png",
  "KB국민 톡톡MyPoint 카드": "/images/cards/toktok.png",
  "KB국민 굿데이 ALL 카드": "/images/cards/goodday.png",
  "KB국민 청춘대로 톡톡카드": "/images/cards/chungchun.png",
  "KB국민 My WEISH 카드": "/images/cards/weish.png",
  "KB국민 Easy Link 카드": "/images/cards/easylink.png",
};

const resolveCardImg = (card) => {
  if (!card) return null;
  if (card.cardImageUrl) return card.cardImageUrl;
  if (card.cardImage) return card.cardImage;
  if (card.customCardImageUrl) return card.customCardImageUrl;
  if (card.imageUrl) return card.imageUrl;
  if (card.cardImg) return card.cardImg;
  if (card.cardImageName) {
    return card.cardImageName.startsWith('/') || card.cardImageName.startsWith('http')
      ? card.cardImageName
      : `/images/cards/${card.cardImageName}`;
  }
  if (card.cardName && kbCardImageMap[card.cardName]) {
    return kbCardImageMap[card.cardName];
  }
  if (card.cardName) {
    for (const [name, img] of Object.entries(kbCardImageMap)) {
      if (card.cardName.includes(name) || name.includes(card.cardName)) {
        return img;
      }
    }
  }
  return null;
};

const onCardImgError = () => {
  repCardImage.value = null;
};

const loadPrimaryCard = async () => {
  try {
    const targetUserId = props.userId || Number(authStore.userId);
    if (!targetUserId) return;
    let primaryCardObj = null;

    try {
      const cardData = await getPrimaryCard(targetUserId);
      if (cardData && (cardData.cardName || cardData.cardNumber || cardData.maskedCardNumber)) {
        primaryCardObj = cardData;
      }
    } catch (e) {
      console.log("getPrimaryCard API 예외:", e);
    }

    const allCards = await getCards();
    if (allCards && Array.isArray(allCards) && allCards.length > 0) {
      const foundPrimary = allCards.find(c => c.representYn === 'Y' || c.isPrimary === 'Y' || c.isPrimary === true) || allCards[0];
      if (!primaryCardObj) {
        primaryCardObj = foundPrimary;
      } else {
        primaryCardObj = { ...foundPrimary, ...primaryCardObj };
        if (!resolveCardImg(primaryCardObj)) {
          primaryCardObj.cardImageUrl = resolveCardImg(foundPrimary);
        }
      }
    }

    if (primaryCardObj) {
      repCard.value = primaryCardObj;
      if (primaryCardObj.cardName || primaryCardObj.alias) {
        repCardName.value = primaryCardObj.cardName || primaryCardObj.alias;
      }
      if (primaryCardObj.cardNumber || primaryCardObj.maskedCardNumber) {
        primaryCardNumber.value = primaryCardObj.cardNumber || primaryCardObj.maskedCardNumber;
      }
      repCardImage.value = resolveCardImg(primaryCardObj);
    } else {
      repCardImage.value = null;
    }
  } catch (e) {
    console.log("대표 카드 로드 예외:", e);
    repCardImage.value = null;
  }
};

const loadPrimaryAccount = async () => {
  try {
    const { data: accList } = await api.get('/api/users/accounts');
    if (accList && Array.isArray(accList) && accList.length > 0) {
      const primaryAcc =
        accList.find(
          (a) =>
            a.isPrimary === "Y" ||
            a.isPrimary === true ||
            a.primaryYn === "Y",
        ) || accList[0];
      if (primaryAcc) {
        if (primaryAcc.accountNumber || primaryAcc.accountNo) {
          primaryAccountNumber.value = primaryAcc.accountNumber || primaryAcc.accountNo;
        }
        if (primaryAcc.bankName) {
          primaryBankName.value = primaryAcc.bankName;
        }
        if (primaryAcc.bankCode && remittanceStore.getBankLogoFileName) {
          const fn = remittanceStore.getBankLogoFileName(primaryAcc.bankCode);
          kbLogoUrl.value = `/api/banks/logo/${fn}`;
        }
        if (primaryAcc.balance !== undefined && primaryAcc.balance !== null) {
          accountBalance.value = primaryAcc.balance;
        }
      }
    }
  } catch (e) {
    console.log("대표 계좌 로드 예외 (기본 계좌 정보 사용):", e);
  }
};

const formatCurrency = (val) => {
  if (val === undefined || val === null) return "0원";
  return Number(val).toLocaleString("ko-KR") + "원";
};

const formatDate = (dateStr) => {
  if (!dateStr) return "";
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return dateStr;
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  const hours = String(d.getHours()).padStart(2, "0");
  const minutes = String(d.getMinutes()).padStart(2, "0");
  return `${month}.${day} ${hours}:${minutes}`;
};

const isIncome = (item) => {
  if (!item) return false;
  const targetUid = props.userId || Number(authStore.userId);
  const type = (item.transactionType || item.type || "").toUpperCase();
  if (type.includes("CHARGE")) return true;
  if ((type.includes("TRANSFER") || type.includes("REMIT")) && Number(item.receiveId) === Number(targetUid)) {
    return true;
  }
  return false;
};

const isSettlement = (item) => {
  if (!item) return false;
  const type = (item.transactionType || item.type || "").toUpperCase();
  return type.includes("SETTLEMENT") || (item.settlementId && Number(item.settlementId) > 0);
};

const getItemTitleWithCategory = (item) => {
  const tType = (item.transactionType || item.type || "").toUpperCase();
  let baseTitle = item.merchantName || item.merchant_name || item.receiverName || item.title || item.memo || "";
  
  if (tType === "CHARGE") {
    return (baseTitle && baseTitle !== "충전") ? baseTitle : "전자지갑 충전";
  }
  if (tType === "TRANSFER" || tType === "REMIT" || isSettlement(item)) {
    if (isSettlement(item)) {
      if (isIncome(item)) {
        return item.senderName ? `${item.senderName} (정산 받음)` : "더치페이 정산 받음";
      }
      const rec = item.receiverName || item.merchantName || "정산 요청자";
      return `${rec} (정산 보냄)`;
    }
    if (isIncome(item)) {
      return item.senderName ? `${item.senderName}에게 받음` : "송금 받음";
    }
    if (baseTitle.startsWith("송금 (") && baseTitle.endsWith(")")) {
      baseTitle = baseTitle.substring(4, baseTitle.length - 1);
    }
    if (!baseTitle || baseTitle === "송금 완료" || baseTitle === "송금" || baseTitle === "수취인") {
      baseTitle = (item.receiverName && item.receiverName !== "수취인") ? item.receiverName : (item.memo || "송금");
    }
    return baseTitle.endsWith("송금") ? baseTitle : `${baseTitle} 송금`;
  }
  return baseTitle || "가맹점 결제";
};

const getItemMethodSubtext = (item) => {
  const tType = (item.transactionType || item.type || "").toUpperCase();
  if (tType === "CHARGE") return item.bankName || "연동계좌";
  if (isSettlement(item)) return "더치페이 정산";
  if (tType === "TRANSFER" || tType === "REMIT") {
    if (isIncome(item)) return "페이머니 입금";
    return item.sourceType === "ACCOUNT" ? "연동계좌" : "페이머니";
  }
  return "체크카드";
};

const getTypeIcon = (item) => {
  if (isSettlement(item)) return "fa-solid fa-users";
  if (isIncome(item)) return "fa-solid fa-wallet";
  const type = typeof item === "object" ? item.transactionType : item;
  const tStr = (type || "").toUpperCase();
  if (tStr.includes("CHARGE")) return "fa-solid fa-wallet";
  if (tStr.includes("TRANSFER") || tStr.includes("REMIT")) return "fa-solid fa-paper-plane";
  return "fa-solid fa-mug-hot";
};

const getTypeIconBgClass = (item) => {
  if (isSettlement(item)) return "bg-purple";
  if (isIncome(item)) return "bg-amber";
  const type = typeof item === "object" ? item.transactionType : item;
  const tStr = (type || "").toUpperCase();
  if (tStr.includes("CHARGE")) return "bg-amber";
  if (tStr.includes("TRANSFER") || tStr.includes("REMIT")) return "bg-blue";
  return "bg-green";
};

const getTypeIconColor = (item) => {
  if (isSettlement(item)) return "#7c3aed";
  if (isIncome(item)) return "#d97706";
  const type = typeof item === "object" ? item.transactionType : item;
  const tStr = (type || "").toUpperCase();
  if (tStr.includes("CHARGE")) return "#d97706";
  if (tStr.includes("TRANSFER") || tStr.includes("REMIT")) return "#3182ce";
  return "#10b981";
};

const getAmountClass = (item) => {
  return isIncome(item) ? "+" : "-";
};

const getAmountPrefix = (item) => {
  return isIncome(item) ? "+" : "-";
};

const changeTab = (val) => {
  selectedType.value = val;
  fetchTransactions();
};

const fetchTransactions = async () => {
  loading.value = true;
  try {
    const targetUserId = props.userId || Number(authStore.userId);
    if (!targetUserId) {
      transactions.value = [];
      return;
    }
    const list = await transactionApi.getTransactions(targetUserId);
    if (list && Array.isArray(list)) {
      let filtered = list;
      if (selectedType.value) {
        filtered = list.filter((t) => {
          const typeStr = (t.transactionType || t.type || "").toUpperCase();
          if (selectedType.value === "CHARGE") return typeStr.includes("CHARGE");
          if (selectedType.value === "TRANSFER") return typeStr.includes("TRANSFER") || typeStr.includes("REMIT");
          if (selectedType.value === "PAYMENT") return typeStr.includes("PAY");
          return true;
        });
      }

      filtered.sort((a, b) => {
        const dateA = new Date(a.createdAt || a.transactionDate || a.date || 0);
        const dateB = new Date(b.createdAt || b.transactionDate || b.date || 0);
        return dateB - dateA;
      });

      transactions.value = filtered;
    }
  } catch (err) {
    console.log("WalletSection 거래내역 로드 예외", err);
  } finally {
    loading.value = false;
  }
};

const openReceipt = (item, type) => {
  if (typeof item === 'object' && item.transactionId) {
    const tType = (item.transactionType || item.type || type || "PAYMENT").toUpperCase();
    router.push({
      path: `/transactions/receipt/${item.transactionId}`,
      query: {
        type: tType,
        title: item.merchantName || item.receiverName || item.merchant_name || "",
        amount: item.amount || 0,
        createdAt: item.createdAt || ""
      }
    });
  } else if (item) {
    router.push({
      path: `/transactions/receipt/${item}`,
      query: { type: type || 'PAYMENT' }
    });
  }
};

const goToCardPage = () => {
  router.push('/setting/cards');
};

const goToCharge = () => {
  router.push('/wallet?view=CHARGE');
};

const goToRemittance = () => {
  router.push('/remittance/account');
};

const goToTransactionList = () => {
  router.push('/transactions');
};

onMounted(async () => {
  await remittanceStore.loadInitData();
  await loadPrimaryCard();
  await loadPrimaryAccount();
  await fetchTransactions();
});
</script>

<style scoped>
@import "@/components/common/common/common.css";

.wallet-section-wrapper {
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding: 16px 0 60px;
  box-sizing: border-box;
  text-align: left;
  position: relative;
  width: 100%;
}

.section-main-title {
  color: #111111;
  font-size: 15px;
  font-weight: 700;
  margin: 0;
  white-space: nowrap;
}

.header-action-link {
  color: #718096;
  font-size: 12px;
  font-weight: 500;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 2px;
  white-space: nowrap;
  flex-shrink: 0;
  transition: color 0.2s ease;
}

.header-action-link:hover {
  color: #111111;
}

/* =========================================
   1. 내가 지금 사용하고 있는 카드 섹션
========================================= */
.my-wallet-card-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.rep-card-visual {
  background: linear-gradient(135deg, #ffbc2e 0%, #f5a623 100%);
  border-radius: 16px;
  height: 160px;
  padding: 18px 20px;
  box-sizing: border-box;
  color: #ffffff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(255, 188, 46, 0.28);
  transition: transform 0.2s ease;
}

.rep-card-visual.has-image {
  background: #f8f9fa;
  height: auto;
  aspect-ratio: 1.586 / 1;
  max-height: 220px;
  padding: 0;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
}

.rep-card-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 14px;
  display: block;
}

.rep-card-visual:hover {
  transform: translateY(-2px);
}

.card-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-brand-logo {
  font-size: 13px;
  font-weight: 800;
  letter-spacing: -0.3px;
}

.card-chip-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ic-chip-icon {
  width: 28px;
  height: 20px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #d97706;
  font-size: 11px;
}

.contactless-ic {
  font-size: 13px;
  opacity: 0.9;
}

.card-number-row {
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 1.5px;
}

/* =========================================
   2. 지금 사용 하고 있는 계좌 섹션
========================================= */
.my-wallet-account-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.account-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.rep-account-card {
  background: #ffffff;
  border: 1px solid #edf2f7;
  border-radius: 16px;
  padding: 16px 18px;
  box-sizing: border-box;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.acc-top-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.acc-bank-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bank-logo-img {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  object-fit: cover;
}

.bank-name-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
}

.bank-name {
  color: #111111;
  font-size: 13px;
  font-weight: 700;
}

.acc-badge {
  background-color: #fef3c7;
  color: #b45309;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 6px;
}

.acc-num {
  color: #718096;
  font-size: 12px;
}

.acc-balance-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.balance-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.balance-label {
  color: #718096;
  font-size: 11px;
}

.balance-val-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.balance-amt {
  color: #111111;
  font-size: 17px;
  font-weight: 700;
}

.eye-toggle-btn {
  border: none;
  background: none;
  color: #a0aec0;
  cursor: pointer;
  padding: 0;
  font-size: 13px;
}

.acc-action-btns {
  display: flex;
  gap: 6px;
}

.btn-charge {
  height: 32px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background-color: #f7fafc;
  color: #2d3748;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.btn-withdraw {
  height: 32px;
  padding: 0 12px;
  border-radius: 8px;
  border: none;
  background-color: #ffbc2e;
  color: #111111;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

/* =========================================
   3. 거래내역 섹션
========================================= */
.my-wallet-tx-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 4px;
}

.tx-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.tx-sec-title {
  font-size: 15px;
}

.filter-chip-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
}

.filter-chip-btn {
  height: 34px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background-color: #f7fafc;
  color: #718096;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-chip-btn.active {
  background-color: #ffbc2e;
  border-color: #ffbc2e;
  color: #111111;
}

.tx-subheader-row {
  margin-top: 2px;
}

.subheader-lbl {
  color: #718096;
  font-size: 12px;
}

/* 거래 내역 리스트 카드 */
.tx-card-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tx-card-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  border: 1px solid #edf2f7;
  border-radius: 14px;
  padding: 12px 14px;
  box-sizing: border-box;
  cursor: pointer;
}

.tx-item-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tx-icon-circle-md {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}

.tx-icon-circle-md.bg-green {
  background-color: #e6fffa;
}

.tx-icon-circle-md.bg-amber {
  background-color: #fffbe6;
}

.tx-icon-circle-md.bg-blue {
  background-color: #ebf8ff;
}

.tx-icon-circle-md.bg-purple {
  background-color: #f5f3ff;
}

.tx-item-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.tx-item-title {
  color: #111111;
  font-size: 13px;
  font-weight: 700;
}

.tx-item-sub {
  color: #a0aec0;
  font-size: 11px;
}

.tx-item-amt {
  font-size: 14px;
  font-weight: 700;
}

.tx-item-amt.plus {
  color: #10b981;
}

.tx-item-amt.minus {
  color: #111111;
}

/* 로딩 & 빈 상태 */
.loading-box {
  padding: 24px 0;
  text-align: center;
  color: #718096;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid #e2e8f0;
  border-top-color: #ffbc2e;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-tx-box {
  padding: 28px 0;
  text-align: center;
}

.empty-icon {
  font-size: 24px;
  color: #cbd5e0;
  margin-bottom: 6px;
  display: block;
}

.empty-text {
  color: #718096;
  margin: 0;
}

/* 4. 플로팅 (+) 버튼 */
.floating-add-btn-wrap {
  display: flex;
  justify-content: center;
  margin-top: 6px;
}

.floating-add-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background-color: #ffbc2e;
  color: #111111;
  border: none;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(255, 188, 46, 0.35);
  transition: transform 0.2s ease;
}

.floating-add-btn:hover {
  transform: scale(1.05);
}
</style>
