<template>
  <div class="remit-container" :class="{ 'is-result-mode': isResultPage }">
    <!-- 공통 서브 화면 헤더 (결제 완료 화면에서는 카드 추가 완료 화면처럼 헤더 숨김) -->
    <PageHeader
      v-if="!isResultPage"
      :title="headerTitleText"
      :show-back="!remittanceStore.remitSuccess"
      :custom-back="true"
      @back="handleBack"
    />

    <!-- 상단 3탭 메인 메뉴 (Step 1에서만 표시) -->
    <CommonTabBar
      v-if="isStepOne && !remittanceStore.remitSuccess"
      :model-value="remittanceStore.remitType"
      :tabs="tabOptions"
      @update:model-value="handleTabChange"
    />

    <!-- 본문 가변 스크롤 영역 (하위 라우트 페이지 렌더링) -->
    <div class="card-body-scroll" :class="{ 'is-result-mode': isResultPage }">
      <router-view />
    </div>

    <!-- 송금/정산 최종 확인 모달 -->
    <RemitConfirmModal
      :show="remittanceStore.showConfirmModal"
      :remit-type="remittanceStore.remitType"
      :selected-dutch-friends="remittanceStore.selectedDutchFriends"
      :get-friend-name="remittanceStore.getFriendName"
      :receiver-name="remittanceStore.accountForm.receiverName || remittanceStore.selectedFriendObj?.name || '수취인'"
      :bank-name="remittanceStore.getBankName(remittanceStore.accountForm.bankCode)"
      :account-number="remittanceStore.accountForm.accountNumber"
      :amount="remittanceStore.remitAmount"
      :format-currency="remittanceStore.formatCurrency"
      @close="remittanceStore.showConfirmModal = false"
      @confirm="confirmRemittanceWithPassword"
    />

    <!-- 비밀번호/PIN 인증 모달 -->
    <RemitPasswordModal
      :show="remittanceStore.showPasswordModal"
      :input-pin="remittanceStore.inputPinCode"
      :error-message="pinErrorMessage"
      :pin-locked="pinLocked"
      @close="closePinModal"
      @enter-pin="enterPinCode"
      @clear-pin="clearPinCode"
      @delete-pin="deletePinCode"
      @forgot-pin="goPinReset"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useRemittanceStore } from "@/stores/remittance";
import { useSignupStore } from "@/stores/signup";
import PageHeader from "@/components/common/PageHeader.vue";
import CommonTabBar from "@/components/common/CommonTabBar.vue";
import RemitConfirmModal from "@/components/remittance/RemitConfirmModal.vue";
import RemitPasswordModal from "@/components/remittance/RemitPasswordModal.vue";
import walletApi from "@/api/walletApi";

const route = useRoute();
const router = useRouter();
const remittanceStore = useRemittanceStore();
const signupStore = useSignupStore();

const pinErrorMessage = ref("");
const pinLocked = ref(false);

const tabOptions = [
  { label: "계좌 송금", value: "ACCOUNT" },
  { label: "친구 송금", value: "FRIEND" },
  { label: "정산", value: "DUTCH" },
];

// 결과(완료) 페이지 여부 확인
const isResultPage = computed(() => {
  return route.path.includes('/result') || remittanceStore.remitSuccess;
});

// Step 1 경로 여부 확인
const isStepOne = computed(() => {
  const p = route.path;
  return p === '/remittance/account' || p === '/remittance/friend' || p === '/remittance/dutch' || p === '/remittance';
});

const headerTitleText = computed(() => {
  const p = route.path;
  if (p.includes('/account')) return "계좌 송금하기";
  if (p.includes('/friend')) return "친구 송금하기";
  if (p.includes('/dutch')) return "정산 요청 개설";
  return "송금하기";
});

const handleTabChange = (newVal) => {
  remittanceStore.resetAll();
  remittanceStore.remitType = newVal;
  if (newVal === "ACCOUNT") router.replace('/remittance/account');
  else if (newVal === "FRIEND") router.replace('/remittance/friend');
  else if (newVal === "DUTCH") router.replace('/remittance/dutch');
};

const syncRemitTypeFromRoute = (path) => {
  if (path.includes('/dutch')) {
    remittanceStore.remitType = 'DUTCH';
  } else if (path.includes('/friend')) {
    remittanceStore.remitType = 'FRIEND';
  } else if (path.includes('/account')) {
    remittanceStore.remitType = 'ACCOUNT';
  }
};

watch(() => route.path, (newPath, oldPath) => {
  syncRemitTypeFromRoute(newPath);

  // Step 1 메인 탭 전환 시에만 폼 데이터 초기화
  const isStepOnePath = newPath === '/remittance/account' || newPath === '/remittance/friend' || newPath === '/remittance/dutch' || newPath === '/remittance';
  const wasStepOnePath = oldPath === '/remittance/account' || oldPath === '/remittance/friend' || oldPath === '/remittance/dutch' || oldPath === '/remittance';

  if (isStepOnePath && wasStepOnePath && newPath !== oldPath) {
    remittanceStore.resetAll();
    syncRemitTypeFromRoute(newPath);
  }
});

const handleBack = () => {
  if (window.history.state && window.history.state.back) {
    router.back();
  } else {
    const p = route.path;
    if (p.includes('/amount')) {
      if (p.includes('/account')) router.replace('/remittance/account');
      else if (p.includes('/friend')) router.replace('/remittance/friend');
      else if (p.includes('/dutch')) router.replace('/remittance/dutch/transactions');
    } else if (p.includes('/transactions')) {
      router.replace('/remittance/dutch');
    } else if (p.includes('/feed') || p.includes('/summary')) {
      if (p.includes('/account')) router.replace('/remittance/account/amount');
      else if (p.includes('/friend')) router.replace('/remittance/friend/amount');
      else if (p.includes('/dutch')) router.replace('/remittance/dutch/amount');
    } else {
      router.replace('/wallet');
    }
  }
};

const confirmRemittanceWithPassword = () => {
  remittanceStore.showConfirmModal = false;
  remittanceStore.inputPinCode = "";
  pinErrorMessage.value = "";
  pinLocked.value = false;
  remittanceStore.showPasswordModal = true;
};

const closePinModal = () => {
  remittanceStore.showPasswordModal = false;
  remittanceStore.inputPinCode = "";
  pinErrorMessage.value = "";
};

const clearPinCode = () => {
  remittanceStore.inputPinCode = "";
  pinErrorMessage.value = "";
};

const deletePinCode = () => {
  remittanceStore.inputPinCode = remittanceStore.inputPinCode.slice(0, -1);
  pinErrorMessage.value = "";
};

const goPinReset = () => {
  remittanceStore.showPasswordModal = false;
  remittanceStore.inputPinCode = "";
  pinErrorMessage.value = "";
  signupStore.setVerificationPurpose('PIN_RESET');
  router.push('/signup/check');
};

const enterPinCode = async (n) => {
  if (pinLocked.value) return;

  pinErrorMessage.value = "";
  if (remittanceStore.inputPinCode.length < 6) {
    remittanceStore.inputPinCode += String(n);
    if (remittanceStore.inputPinCode.length === 6) {
      try {
        const userId = remittanceStore.authStore?.userId || 1;
        const res = await walletApi.verifyPin(userId, remittanceStore.inputPinCode);
        if (res && res.verified) {
          pinErrorMessage.value = "";
          remittanceStore.showPasswordModal = false;
          await remittanceStore.executeRemittance();
          if (remittanceStore.remitSuccess) {
            if (remittanceStore.remitType === 'ACCOUNT') {
              router.push('/remittance/account/result');
            } else if (remittanceStore.remitType === 'FRIEND') {
              router.push('/remittance/friend/result');
            } else if (remittanceStore.remitType === 'DUTCH') {
              router.push('/remittance/dutch/result');
            }
          }
        } else {
          pinErrorMessage.value = res?.message || "간편비밀번호가 일치하지 않습니다.";
          remittanceStore.inputPinCode = "";
          if (res?.pinLocked || pinErrorMessage.value.includes("초과") || pinErrorMessage.value.includes("잠겼습니다")) {
            pinLocked.value = true;
          }
        }
      } catch (pinErr) {
        console.error("PIN 인증 실패:", pinErr);
        const errData = pinErr.response?.data;
        pinErrorMessage.value = (typeof errData === 'string' ? errData : errData?.message) || pinErr.message || "간편비밀번호가 일치하지 않습니다.";
        remittanceStore.inputPinCode = "";
        if (pinErrorMessage.value.includes("초과") || pinErrorMessage.value.includes("잠겼습니다")) {
          pinLocked.value = true;
        }
      }
    }
  }
};

onMounted(async () => {
  await remittanceStore.loadInitData();

  // 쿼리 파라미터가 있는 경우 자동 바인딩
  const qFriendId = route.query.friendId;
  if (qFriendId) {
    remittanceStore.selectedFriendId = Number(qFriendId);
    remittanceStore.remitType = "FRIEND";
    router.push('/remittance/friend');
  } else {
    syncRemitTypeFromRoute(route.path);
  }
});
</script>

<style scoped>
.remit-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  padding: 0 16px;
  background-color: var(--color-bg-page, #ffffff);
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

.card-body-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
  box-sizing: border-box;
}

.card-body-scroll::-webkit-scrollbar {
  display: none;
}

.remit-container.is-result-mode {
  padding: 0 !important;
}

.card-body-scroll.is-result-mode {
  padding: 0 !important;
  overflow: hidden !important;
}
</style>
