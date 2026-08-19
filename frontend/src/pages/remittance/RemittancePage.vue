<template>
  <div class="remit-container">
    <!-- 공통 서브 화면 헤더 -->
    <PageHeader
      :title="headerTitleText"
      :show-back="!remitSuccess"
      @back="handleBack"
    />

    <!-- 공통 탭 바 (STEP 1) -->
    <CommonTabBar
      v-if="currentStep === 1 && !remitSuccess"
      v-model="remitType"
      :tabs="tabOptions"
    />



    <!-- 본문 가변 스크롤 영역 -->
    <div class="card-body-scroll">
      <div v-if="remitSuccess" class="step-content-wrap">
        <RemitResultStep
          :remit-type="remitType"
          :remit-amount="remitAmount"
          :receiver-name="accountForm.receiverName || selectedFriendObj?.name || '수취인'"
          :bank-name="getBankName(accountForm.bankCode)"
          :account-number="accountForm.accountNumber"
          :dutch-room-title="dutchRoomTitle"
          :selected-dutch-friends="selectedDutchFriends"
          :remit-memo="remitMemo"
          :format-currency="formatCurrency"
          @reset-all="resetAllRemitForm"
        />
      </div>

      <template v-else>
        <!-- ==========================================
             [STEP 1] 계좌 입력 / 친구 선택 / 더치페이 생성
        ========================================== -->
        <div v-if="currentStep === 1" class="step-content-wrap">
          <!-- 1-A. 계좌 송금 -->
          <AccountRemitSection
            v-if="remitType === 'ACCOUNT'"
            v-model:account-number="accountForm.accountNumber"
            v-model:bank-code="accountForm.bankCode"
            :bank-options="bankOptions"
            :recent-accounts="recentAccounts"
            :get-bank-logo-file-name="getBankLogoFileName"
            :get-bank-name="getBankName"
            :format-currency="formatCurrency"
            @select-recent="selectRecentAccountItem"
          />

          <!-- 1-B. 친구 송금 -->
          <FriendRemitSection
            v-else-if="remitType === 'FRIEND'"
            v-model:keyword="friendSearchKeyword"
            :recent-friends="recentFriends"
            :friends="filteredFriends"
            :selected-friend-id="selectedFriendId"
            :get-profile-image-url="getProfileImageUrl"
            @select-friend="selectFriendAndProceed"
          />

          <!-- 1-C. 더치페이 정산 친구 선택 -->
          <DutchRemitSection
            v-else-if="remitType === 'DUTCH'"
            v-model:keyword="dutchFriendSearchKeyword"
            :selected-friends="selectedDutchFriends"
            :friends="filteredDutchFriends"
            :my-profile-image-url="myProfileImageUrl"
            :my-profile-name="myProfileName"
            :get-profile-image-url="getProfileImageUrl"
            :get-friend-obj="getFriendObj"
            :get-friend-name="getFriendName"
            @remove-friend="removeDutchFriend"
            @toggle-friend="toggleDutchFriend"
          />

          <div
            v-if="remitType === 'ACCOUNT' || remitType === 'DUTCH'"
            class="next-btn-wrap"
          >
            <button
              class="bottom-btn text-18-bold"
              :disabled="!canProceedStep1"
              @click="goToStep2"
            >
              <template v-if="remitType === 'DUTCH'">
                {{ selectedDutchFriends.length > 0 ? `${selectedDutchFriends.length + 1}명 선택 완료` : '1명 선택 완료' }}
              </template>
              <template v-else>
                다음
              </template>
            </button>
          </div>
        </div>

        <!-- ==========================================
             [STEP 2] 금액 입력 & 정산 분배
        ========================================== -->
        <div v-else-if="currentStep === 2" class="step-content-wrap">
          <RemitAmountStep
            :remit-type="remitType"
            :account-form="accountForm"
            :selected-friend-obj="selectedFriendObj"
            :my-balance="myBalance"
            :my-account-name="'페이머니'"
            :remit-amount="remitAmount"
            :remit-amount-display="remitAmountDisplay"
            :is-category-expanded="isCategoryExpanded"
            :category-list="categoryList"
            :displayed-category-list="displayedCategoryList"
            v-model:selected-category-id="selectedCategoryId"
            v-model:dutch-split-mode="dutchSplitMode"
            :selected-dutch-friends="selectedDutchFriends"
            :custom-dutch-amounts="customDutchAmounts"
            :my-profile-image-url="myProfileImageUrl"
            :my-profile-name="myProfileName"
            :get-bank-logo-file-name="getBankLogoFileName"
            :get-bank-name="getBankName"
            :get-friend-name="getFriendName"
            :get-friend-obj="getFriendObj"
            :get-profile-image-url="getProfileImageUrl"
            :format-currency="formatCurrency"
            @on-amount-input="onAmountInput"
            @add-amount="remitAmount += $event"
            @set-all-balance="remitAmount = myBalance"
            @toggle-category-expanded="isCategoryExpanded = !isCategoryExpanded"
            @open-tx-select="openTxSelectStep"
            @edit-friends="currentStep = 1"
            @remove-friend="removeDutchFriend"
          />

          <div class="next-btn-wrap">
            <button
              class="bottom-btn text-18-bold"
              :disabled="!remitAmount || remitAmount <= 0"
              @click="proceedFromStep2"
            >
              <template v-if="remitType === 'DUTCH'">
                확인
              </template>
              <template v-else>
                다음
              </template>
            </button>
          </div>
        </div>

        <!-- ==========================================
             [STEP 3 - 송금 전용 (계좌/친구)] 카테고리 / 피드 / 공개범위 / 사진 첨부
        ========================================== -->
        <RemitStep3FeedForm
          v-else-if="remitType !== 'DUTCH' && currentStep === 3"
          :remit-type="remitType"
          :selected-friend-obj="selectedFriendObj"
          :account-form="accountForm"
          :remit-amount="remitAmount"
          :is-category-expanded="isCategoryExpanded"
          :category-list="categoryList"
          :displayed-category-list="displayedCategoryList"
          v-model:selected-category-id="selectedCategoryId"
          v-model:remit-memo="remitMemo"
          v-model:remit-visibility="remitVisibility"
          :selected-file="selectedFile"
          :image-preview-url="imagePreviewUrl"
          :format-currency="formatCurrency"
          @toggle-category-expanded="isCategoryExpanded = !isCategoryExpanded"
          @remove-file="removeSelectedFile"
          @file-change="handleFileChange"
          @submit="submitRemittance"
        />

        <!-- ==========================================
             [STEP 3 - 정산 전용 (더치페이)] 내 결제 내역에서 선택
        ========================================== -->
        <div
          v-else-if="remitType === 'DUTCH' && currentStep === 3"
          class="step-content-wrap"
        >
          <div class="tx-select-step-head">
            <h3 class="text-18-bold" style="margin: 0">정산할 내 결제 건 선택</h3>
            <p class="text-13" style="color: #777; margin: 4px 0 0 0">
              정산에 포함할 내 결제 내역을 클릭 선택하세요.
            </p>
          </div>

          <div class="tx-list-container scrollable-tx-area">
            <div v-if="userTxList.length === 0" class="empty-recent-msg text-13">
              불러올 수 있는 결제 내역이 없습니다.
            </div>
            <div v-else class="date-item-list">
              <div
                v-for="tx in userTxList"
                :key="tx.id"
                class="tx-item-wrapper"
                @click="toggleTxSelection(tx.id)"
              >
                <div
                  class="tx-item-row-card"
                  :class="{ active: selectedTxIds.includes(tx.id) }"
                >
                  <div class="tx-item-left">
                    <div class="icon-circle text-15-bold">
                      <i class="fa-solid fa-store"></i>
                    </div>
                    <div class="tx-info-text">
                      <div class="tx-item-title text-15-bold">{{ tx.title }}</div>
                      <div class="tx-item-sub text-13">{{ tx.date }}</div>
                    </div>
                  </div>
                  <div
                    class="tx-item-right"
                    style="display: flex; align-items: center; gap: 10px"
                  >
                    <div class="tx-amount text-15-bold" style="color: #111">
                      -{{ formatCurrency(tx.amount) }}원
                    </div>
                    <div class="select-check-ic">
                      <i
                        class="fa-circle-check"
                        :class="
                          selectedTxIds.includes(tx.id)
                            ? 'fa-solid active-kb'
                            : 'fa-regular uncheck'
                        "
                      ></i>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="fixed-bottom-btn-wrap">
            <button
              class="bottom-btn text-18-bold"
              :disabled="selectedTxIds.length === 0"
              @click="confirmTxSelection"
            >
              {{ selectedTxIds.length }}개 결제 건 적용 (총
              {{ formatCurrency(selectedTxTotalAmount) }}원)
              <i class="fa-solid fa-arrow-right"></i>
            </button>
          </div>
        </div>

        <!-- ==========================================
             [STEP 4 - 정산 전용 (더치페이)] 총 정보 요약 & 피드 작성
        ========================================== -->
        <!-- ==========================================
             [STEP 4 - 정산 전용 (더치페이)] 분리된 전용 컴포넌트 (DutchCreateSummaryStep)
        ========================================== -->
        <DutchCreateSummaryStep
          v-else-if="remitType === 'DUTCH' && currentStep === 4"
          v-model:dutch-room-title="dutchRoomTitle"
          v-model:remit-memo="remitMemo"
          :remit-amount="remitAmount"
          :selected-dutch-friends="selectedDutchFriends"
          :my-profile-name="myProfileName"
          :get-friend-name="getFriendName"
          :image-preview-url="imagePreviewUrl"
          :format-currency="formatCurrency"
          @file-change="handleFileChange"
          @remove-file="removeSelectedFile"
          @submit="submitRemittance"
        />
      </template>
    </div>

    <!-- 송금 최종 확인 모달 (Teleport to body 적용) -->
    <RemitConfirmModal
      :show="showConfirmModal"
      :receiver-name="accountForm.receiverName || selectedFriendObj?.name || '수취인'"
      :bank-name="getBankName(accountForm.bankCode)"
      :account-number="accountForm.accountNumber"
      :amount="remitAmount"
      :format-currency="formatCurrency"
      @close="showConfirmModal = false"
      @confirm="confirmRemittanceWithPassword"
    />

    <!-- 비밀번호/PIN 인증 모달 (Teleport to body 적용) -->
    <RemitPasswordModal
      :show="showPasswordModal"
      :input-pin="inputPinCode"
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
import { ref, computed, onMounted, reactive, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { useProfileStore } from "@/stores/profile";
import { useSignupStore } from "@/stores/signup";
import { getProfile as fetchUserProfile, getProfileImage } from "@/api/profileApi";
import walletApi from "@/api/walletApi";
import friendApi from "@/api/friend";
import transactionApi from "@/api/transactionApi";
import remittanceApi from "@/api/remittanceApi";
import analysisApi from "@/api/analysisApi";

import PageHeader from "@/components/common/PageHeader.vue";
import CommonTabBar from "@/components/common/CommonTabBar.vue";
import SpendingCategorySelector from "@/components/common/SpendingCategorySelector.vue";

import AccountRemitSection from "@/components/remittance/AccountRemitSection.vue";
import FriendRemitSection from "@/components/remittance/FriendRemitSection.vue";
import DutchRemitSection from "@/components/remittance/DutchRemitSection.vue";
import RemitAmountStep from "@/components/remittance/RemitAmountStep.vue";
import RemitStep3FeedForm from "@/components/remittance/RemitStep3FeedForm.vue";
import DutchCreateSummaryStep from "@/components/remittance/DutchCreateSummaryStep.vue";
import RemitConfirmModal from "@/components/remittance/RemitConfirmModal.vue";
import RemitPasswordModal from "@/components/remittance/RemitPasswordModal.vue";
import RemitResultStep from "@/components/remittance/RemitResultStep.vue";
import { useModalStore } from "@/stores/userModalStore";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const signupStore = useSignupStore();
const modalStore = useModalStore();

const pinErrorMessage = ref("");
const pinLocked = ref(false);

const currentStep = ref(1);
const remitType = ref("ACCOUNT");

const tabOptions = [
  { label: "계좌 송금", value: "ACCOUNT" },
  { label: "친구 송금", value: "FRIEND" },
  { label: "정산", value: "DUTCH" },
];

const headerTitleText = computed(() => {
  if (remitType.value === "ACCOUNT") return "계좌 송금하기";
  if (remitType.value === "FRIEND") return "친구 송금하기";
  if (remitType.value === "DUTCH") return "정산 요청 개설";
  return "송금하기";
});

const handleBack = () => {
  if (currentStep.value > 1) {
    currentStep.value--;
  } else {
    router.back();
  }
};

const accountForm = reactive({
  accountNumber: "",
  bankCode: "",
  receiverName: "",
});

// 계좌번호가 비어있으면 선택된 은행도 자동 초기화
watch(
  () => accountForm.accountNumber,
  (newVal) => {
    if (!newVal || newVal.trim() === "") {
      accountForm.bankCode = "";
    }
  }
);

const bankOptions = [
  { code: "004", name: "KB국민", fileName: "kb.png" },
  { code: "088", name: "신한", fileName: "shinhan.png" },
  { code: "020", name: "우리", fileName: "woori.png" },
  { code: "081", name: "하나", fileName: "hana.png" },
  { code: "003", name: "IBK기업", fileName: "ibk.png" },
  { code: "011", name: "NH농협", fileName: "nh.png" },
  { code: "090", name: "카카오뱅크", fileName: "kakaobank.png" },
  { code: "089", name: "케이뱅크", fileName: "kbank.png" },
  { code: "092", name: "토스뱅크", fileName: "tossbank.png" },
  { code: "023", name: "SC제일", fileName: "sc.png" },
];

const getBankName = (code) => {
  const found = bankOptions.find((b) => b.code === code);
  if (found) return found.name;
  if (!code) return "은행 미선택";
  const str = String(code);
  if (str.includes("신한")) return "신한";
  if (str.includes("국민") || str.includes("KB")) return "KB국민";
  if (str.includes("하나")) return "하나";
  if (str.includes("우리")) return "우리";
  return str;
};

const getBankLogoFileName = (bank) => {
  if (!bank) return "kb.png";
  const str = String(bank);
  if (str.includes("신한") || str === "SH" || str === "088") return "shinhan.png";
  if (str.includes("KB") || str.includes("국민") || str === "KB" || str === "004") return "kb.png";
  if (str.includes("하나") || str === "HN" || str === "081") return "hana.png";
  if (str.includes("우리") || str === "WR" || str === "020") return "woori.png";
  if (str.includes("기업") || str.includes("IBK") || str === "003") return "ibk.png";
  if (str.includes("농협") || str.includes("NH") || str === "011") return "nh.png";
  if (str.includes("카카오") || str === "090") return "kakaobank.png";
  if (str.includes("케이뱅크") || str === "089") return "kbank.png";
  if (str.includes("토스") || str === "092") return "tossbank.png";
  if (str.includes("제일") || str.includes("SC") || str === "023") return "sc.png";
  return "kb.png";
};

const recentAccounts = ref([]);

const selectRecentAccountItem = (item) => {
  accountForm.accountNumber = item.accountNumber || "";
  accountForm.bankCode = item.bankCode || "004";
  accountForm.receiverName = item.ownerName || item.receiverName || item.name || "";
  currentStep.value = 2;
};

const friendSearchKeyword = ref("");
const selectedFriendId = ref(null);
const friendList = ref([]);

const selectFriendAndProceed = (fId) => {
  selectedFriendId.value = fId;
  currentStep.value = 2;
};

const selectedFriendObj = computed(() => {
  return friendList.value.find((f) => f.id === selectedFriendId.value);
});

const filteredFriends = computed(() => {
  if (!friendSearchKeyword.value) return friendList.value;
  return friendList.value.filter(
    (f) =>
      (f.name && f.name.includes(friendSearchKeyword.value)) ||
      (f.username && f.username.includes(friendSearchKeyword.value))
  );
});

const profileStore = useProfileStore();
const myProfile = ref(null);
const userProfileBlobUrl = ref("");

const myProfileName = computed(() => {
  return (
    myProfile.value?.nickname ||
    myProfile.value?.name ||
    myProfile.value?.userName ||
    authStore.userName ||
    authStore.user?.userName ||
    "내 프로필"
  );
});

const getProfileImageUrl = (friend) => {
  if (!friend) return "/api/feeds/profile/default_profile.png";
  if (friend.avatarUrl) return friend.avatarUrl;
  const imgName =
    friend.storedName ||
    friend.originalName ||
    friend.profileImageName ||
    friend.profileImage ||
    friend.profileImg;
  if (imgName) {
    if (imgName.startsWith("http") || imgName.startsWith("/")) return imgName;
    return `/api/feeds/profile/${imgName}`;
  }
  return "/api/feeds/profile/default_profile.png";
};

const myProfileImageUrl = computed(() => {
  if (userProfileBlobUrl.value) return userProfileBlobUrl.value;
  if (myProfile.value?.avatarUrl) return myProfile.value.avatarUrl;
  if (myProfile.value?.url) return myProfile.value.url;

  const pName =
    myProfile.value?.storedName ||
    myProfile.value?.originalName ||
    myProfile.value?.imageName ||
    myProfile.value?.profileImageName ||
    myProfile.value?.profileImage ||
    authStore.user?.profileImageName ||
    authStore.user?.profileImage;

  if (pName && pName !== "default_profile.png") {
    if (pName.startsWith("http") || pName.startsWith("blob:")) return pName;
    if (pName.startsWith("/")) return pName;
    return `/api/feeds/profile/${pName}`;
  }

  return "/api/feeds/profile/profile_1.png";
});

const getFriendObj = (fId) => friendList.value.find((f) => f.id === fId);
const getFriendName = (fId) => {
  const f = getFriendObj(fId);
  return f ? f.name : "친구";
};

const dutchRoomTitle = ref("");
const selectedDutchFriends = ref([]);
const dutchFriendSearchKeyword = ref("");

const filteredDutchFriends = computed(() => {
  if (!dutchFriendSearchKeyword.value) return friendList.value;
  const kw = dutchFriendSearchKeyword.value.trim().toLowerCase();
  return friendList.value.filter(
    (f) =>
      (f.name && f.name.toLowerCase().includes(kw)) ||
      (f.username && f.username.toLowerCase().includes(kw))
  );
});

const toggleDutchFriend = (id) => {
  const idx = selectedDutchFriends.value.indexOf(id);
  if (idx > -1) {
    selectedDutchFriends.value.splice(idx, 1);
  } else {
    selectedDutchFriends.value.push(id);
  }
};

const removeDutchFriend = (id) => {
  const idx = selectedDutchFriends.value.indexOf(id);
  if (idx > -1) {
    selectedDutchFriends.value.splice(idx, 1);
  }
};

const dutchSplitMode = ref("EQUAL");
const customDutchAmounts = reactive({});

const canProceedStep1 = computed(() => {
  if (remitType.value === "ACCOUNT") return accountForm.accountNumber.length >= 8;
  if (remitType.value === "FRIEND") return !!selectedFriendId.value;
  if (remitType.value === "DUTCH") return selectedDutchFriends.value.length > 0;
  return false;
});

const goToStep2 = () => {
  if (canProceedStep1.value) {
    currentStep.value = 2;
  }
};

const remitAmount = ref(0);
const myBalance = ref(0);

const remitAmountDisplay = computed(() => {
  if (!remitAmount.value) return "";
  return Number(remitAmount.value).toLocaleString("ko-KR");
});

const onAmountInput = (e) => {
  const raw = e.target.value.replace(/[^0-9]/g, "");
  remitAmount.value = raw ? parseInt(raw, 10) : 0;
};

const isCategoryExpanded = ref(false);
const categoryList = ref([
  { spendingCategoryId: 1, categoryName: "식비" },
  { spendingCategoryId: 2, categoryName: "카페" },
  { spendingCategoryId: 3, categoryName: "생활" },
  { spendingCategoryId: 4, categoryName: "온라인쇼핑" },
  { spendingCategoryId: 5, categoryName: "뷰티/미용" },
  { spendingCategoryId: 6, categoryName: "교통" },
  { spendingCategoryId: 7, categoryName: "자동차" },
  { spendingCategoryId: 8, categoryName: "주거/통신" },
  { spendingCategoryId: 9, categoryName: "금융" },
  { spendingCategoryId: 10, categoryName: "여행" },
  { spendingCategoryId: 11, categoryName: "교육" },
  { spendingCategoryId: 12, categoryName: "반려동물" },
  { spendingCategoryId: 13, categoryName: "병원" },
]);

const displayedCategoryList = computed(() => {
  return isCategoryExpanded.value ? categoryList.value : categoryList.value.slice(0, 4);
});

const selectedCategoryId = ref(null);

const proceedFromStep2 = () => {
  if (remitAmount.value > 0) {
    if (remitType.value === "DUTCH") {
      currentStep.value = 4; // 결제 건 선택 화면(Step 3) 튕김 차단! 바로 정산 개설(Step 4) 직행!
    } else {
      currentStep.value = 3;
    }
  }
};

const remitMemo = ref("");
const remitVisibility = ref("PUBLIC");
const selectedFile = ref(null);
const imagePreviewUrl = ref(null);

const handleFileChange = (e) => {
  const file = e.target.files[0];
  if (file) {
    selectedFile.value = file;
    imagePreviewUrl.value = URL.createObjectURL(file);
  }
};

const removeSelectedFile = () => {
  selectedFile.value = null;
  imagePreviewUrl.value = null;
};

const userTxList = ref([]);
const selectedTxIds = ref([]);

const toggleTxSelection = (id) => {
  const idx = selectedTxIds.value.indexOf(id);
  if (idx > -1) {
    selectedTxIds.value.splice(idx, 1);
  } else {
    selectedTxIds.value.push(id);
  }
};

const selectedTxTotalAmount = computed(() => {
  return userTxList.value
    .filter((t) => selectedTxIds.value.includes(t.id))
    .reduce((sum, t) => sum + t.amount, 0);
});

const openTxSelectStep = async () => {
  currentStep.value = 3;
  selectedTxIds.value = [];
  try {
    const userId = authStore.userId || 1;
    if (transactionApi && transactionApi.getTransactions) {
      const data = await transactionApi.getTransactions(userId);
      if (data && Array.isArray(data)) {
        const payItems = data.filter((t) => {
          const typeStr = (t.transactionType || t.type || t.txType || "").toUpperCase();
          const merchantName = (t.merchantName || t.merchant_name || t.storeName || "").trim();

          // PAYMENT 타입만 허용
          if (typeStr !== "PAYMENT") return false;

          // merchantName이 없으면 가맹점 결제가 아니므로 제외
          if (!merchantName) return false;

          return true;
        });

        userTxList.value = payItems.map((t) => ({
          id: t.transactionId || t.id,
          title: t.merchantName || t.merchant_name || t.storeName,
          amount: Math.abs(t.amount || 0),
          date: t.createdAt || t.transactionDate || t.date || "",
        }));
      }
    }
  } catch (e) {
    console.log("거래내역 로드 예외", e);
  }
};

const confirmTxSelection = () => {
  const selectedItems = userTxList.value.filter((t) =>
    selectedTxIds.value.includes(t.id)
  );
  if (selectedItems.length === 0) return;

  // 가장 금액이 큰 결제건 가맹점명 기준 스마트 제목 생성
  const sorted = [...selectedItems].sort((a, b) => (b.amount || 0) - (a.amount || 0));
  const topTitle = sorted[0].title || "가맹점 결제";
  const extraCount = selectedItems.length - 1;

  dutchRoomTitle.value = extraCount > 0 ? `${topTitle} 외 ${extraCount}건` : topTitle;

  remitAmount.value = selectedTxTotalAmount.value;
  if (remitType.value === "DUTCH") {
    currentStep.value = 4;
  }
};

const showConfirmModal = ref(false);
const showPasswordModal = ref(false);
const inputPinCode = ref("");
const remitSuccess = ref(false);

const BAD_WORDS = [
  "시발", "씨발", "개새끼", "병신", "지랄", "존나", "졸라", "미친", "새끼", "꺼져",
  "쌰Protected", "fuck", "shit", "bitch", "asshole", "새끼야", "개새", "엠창", "느금마"
];

const containsProfanity = (text) => {
  if (!text) return false;
  const lower = text.toLowerCase();
  return BAD_WORDS.some((word) => lower.includes(word));
};

const submitRemittance = () => {
  // 피드 메모 및 정산 제목 욕설/비속어 필터링 검증
  if (containsProfanity(remitMemo.value) || containsProfanity(dutchRoomTitle.value)) {
    modalStore.showAlert("⚠️ 입력하신 내용에 올바르지 않은 표현(비속어/욕설)이 포함되어 있습니다. 내용을 수정해주세요.", "입력 내용 안내");
    return;
  }
  showConfirmModal.value = true;
};

const confirmRemittanceWithPassword = () => {
  showConfirmModal.value = false;
  inputPinCode.value = "";
  pinErrorMessage.value = "";
  pinLocked.value = false;
  showPasswordModal.value = true;
};

const closePinModal = () => {
  showPasswordModal.value = false;
  inputPinCode.value = "";
  pinErrorMessage.value = "";
};

const clearPinCode = () => {
  inputPinCode.value = "";
  pinErrorMessage.value = "";
};

const deletePinCode = () => {
  inputPinCode.value = inputPinCode.value.slice(0, -1);
  pinErrorMessage.value = "";
};

const goPinReset = () => {
  showPasswordModal.value = false;
  inputPinCode.value = "";
  pinErrorMessage.value = "";
  signupStore.setVerificationPurpose('PIN_RESET');
  router.push('/signup/check');
};

const enterPinCode = async (n) => {
  if (pinLocked.value) return;

  pinErrorMessage.value = "";
  if (inputPinCode.value.length < 6) {
    inputPinCode.value += String(n);
    if (inputPinCode.value.length === 6) {
      try {
        const userId = authStore.userId || 1;
        const res = await walletApi.verifyPin(userId, inputPinCode.value);
        if (res && res.verified) {
          pinErrorMessage.value = "";
          showPasswordModal.value = false;
          await executeRemittance();
        } else {
          pinErrorMessage.value = res?.message || "간편비밀번호가 일치하지 않습니다.";
          inputPinCode.value = "";
          if (res?.pinLocked || pinErrorMessage.value.includes("초과") || pinErrorMessage.value.includes("잠겼습니다")) {
            pinLocked.value = true;
          }
        }
      } catch (pinErr) {
        console.error("PIN 인증 실패:", pinErr);
        const errData = pinErr.response?.data;
        pinErrorMessage.value = (typeof errData === 'string' ? errData : errData?.message) || pinErr.message || "간편비밀번호가 일치하지 않습니다.";
        inputPinCode.value = "";
        if (pinErrorMessage.value.includes("초과") || pinErrorMessage.value.includes("잠겼습니다")) {
          pinLocked.value = true;
        }
      }
    }
  }
};

const resetAllRemitForm = () => {
  currentStep.value = 1;
  remitType.value = "ACCOUNT";
  accountForm.accountNumber = "";
  accountForm.bankCode = "";
  accountForm.receiverName = "";
  selectedFriendId.value = null;
  selectedDutchFriends.value = [];
  remitAmount.value = 0;
  remitMemo.value = "";
  selectedFile.value = null;
  imagePreviewUrl.value = null;
  remitSuccess.value = false;
};

const formatCurrency = (val) => new Intl.NumberFormat("ko-KR").format(val || 0);

const loadRemitInitData = async () => {
  try {
    const userId = authStore.userId;

    try {
      if (analysisApi && analysisApi.getCategories) {
        const cats = await analysisApi.getCategories();
        if (cats && Array.isArray(cats)) {
          categoryList.value = cats;
        }
      }
    } catch (catErr) {
      console.log("카테고리 목록 로드 예외", catErr);
    }

    if (!userId) return;

    try {
      if (fetchUserProfile) {
        const pData = await fetchUserProfile();
        if (pData) {
          myProfile.value = pData;
        }
      } else if (profileStore && profileStore.getProfile) {
        const pData = await profileStore.getProfile(userId);
        if (pData) {
          myProfile.value = pData;
        }
      }
    } catch (pErr) {
      console.log("내 프로필 로드 예외", pErr);
    }

    try {
      if (getProfileImage) {
        const imgUrl = await getProfileImage();
        if (imgUrl) {
          userProfileBlobUrl.value = imgUrl;
        }
      }
    } catch (imgErr) {
      console.log("내 프로필 이미지 로드 예외", imgErr);
    }

    try {
      if (walletApi && walletApi.getWalletByUserId) {
        const wInfo = await walletApi.getWalletByUserId(userId);
        if (wInfo) {
          myBalance.value =
            wInfo.balance ?? wInfo.amount ?? wInfo.money ?? wInfo.pointMoney ?? 0;
        }
      }
    } catch (wErr) {
      console.log("지갑 잔액 조회 예외", wErr);
    }

    try {
      if (remittanceApi && remittanceApi.getBankRemittanceInfo) {
        const bInfo = await remittanceApi.getBankRemittanceInfo(userId);
        if (bInfo) {
          const rList =
            bInfo.recentRemittances ||
            bInfo.recentAccounts ||
            bInfo.recents ||
            (Array.isArray(bInfo) ? bInfo : []);
          if (rList.length > 0) {
            recentAccounts.value = rList.map((r) => ({
              id: r.id || r.remittanceId || r.accountNumber,
              receiverName: r.ownerName || r.receiverName || r.name || "",
              ownerName: r.ownerName || r.receiverName || "",
              bankName: r.bankName || "KB국민",
              bankCode: r.bankCode || "004",
              accountNumber: r.accountNumber || "",
              amount: r.amount || 0,
              date: r.date || "",
            }));
          }
        }
      }
    } catch (bErr) {
      console.log("최근 송금 계좌 조회 예외", bErr);
    }

    try {
      if (friendApi && friendApi.getFriendList) {
        const fRes = await friendApi.getFriendList(userId);
        let list = [];
        if (Array.isArray(fRes)) {
          list = fRes;
        } else if (fRes && Array.isArray(fRes.friends)) {
          list = fRes.friends;
        } else if (fRes && Array.isArray(fRes.data)) {
          list = fRes.data;
        }

        if (list.length > 0) {
          const map = new Map();
          list.forEach((f, idx) => {
            const receiverObj = f.receiver || f.friendMember || f.member || f.friend || f;
            const fId = f.friendUserId || f.friendId || f.id || receiverObj.userId || receiverObj.id || idx + 1;
            const fNickname = receiverObj.nickname || receiverObj.name || receiverObj.userName || f.nickname || f.name || `친구${fId}`;
            const fUsername = receiverObj.username || receiverObj.loginId || receiverObj.userLoginId || f.username || `user_${fId}`;
            const fImgName = receiverObj.profileImageName || receiverObj.profileImage || receiverObj.profileImg || f.profileImageName || f.profileImage || f.profileImg || "";

            if (!map.has(fId)) {
              map.set(fId, {
                id: fId,
                name: fNickname,
                username: fUsername,
                initials: (fNickname || "친구").slice(0, 2),
                profileImageName: fImgName,
                avatarUrl: fImgName
                  ? fImgName.startsWith("http") || fImgName.startsWith("/")
                    ? fImgName
                    : `/api/feeds/profile/${fImgName}`
                  : "/api/feeds/profile/default_profile.png",
              });
            }
          });
          friendList.value = Array.from(map.values());
        }
      }
    } catch (fErr) {
      console.log("친구 목록 조회 예외", fErr);
    }
  } catch (err) {
    console.log("초기 데이터 로드 예외", err);
  }
};

onMounted(() => {
  if (route.query.type) {
    remitType.value = route.query.type.toUpperCase();
  }
  loadRemitInitData();
});
</script>

<style scoped>
input,
button,
select,
textarea {
  font-family: inherit;
}

.remit-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0 16px;
  background-color: var(--color-bg-page, #ffffff);
  box-sizing: border-box;
}

/* 상단 탭바 (Option 1: 세련된 알약 형태의 슬라이딩 탭 스위치) */
.remit-container :deep(.common-tab-bar) {
  background-color: #f1f5f9;
  border-bottom: none;
  padding: 4px;
  border-radius: 16px;
  height: 48px;
  margin: 8px 0 0;
  width: 100%;
  box-sizing: border-box;
}

.remit-container :deep(.common-tab-btn) {
  height: 40px;
  border-radius: 12px;
  color: #64748b;
  font-weight: 600;
  font-size: 15px;
  transition: all 0.2s ease;
}

.remit-container :deep(.common-tab-btn.active) {
  background-color: #ffffff;
  color: #0f172a;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.remit-container :deep(.common-tab-btn.active::after) {
  display: none;
}

.card-body-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
}

.step-content-wrap {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.next-btn-wrap {
  margin-top: 24px;
  width: 100%;
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

.bottom-btn:disabled {
  background-color: #e0e0e0;
  color: #a0a0a0;
  cursor: not-allowed;
}

.tx-select-step-head {
  margin-bottom: 16px;
}

.scrollable-tx-area {
  max-height: 400px;
  overflow-y: auto;
  margin-bottom: 20px;
}

.date-item-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tx-item-row-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border: 1px solid #ededed;
  border-radius: 14px;
  background-color: #ffffff;
  cursor: pointer;
}

.tx-item-row-card.active {
  border-color: #ffbc00;
  background-color: rgba(255, 188, 0, 0.05);
}

.tx-item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tx-item-title {
  color: #111111;
}

.tx-item-sub {
  color: #888888;
}

.active-kb {
  color: #ffbc00;
}

.uncheck {
  color: #cccccc;
}

/* ========================================
   Step 진행 단계 인디케이터 바
======================================== */
.step-progress-bar-wrap {
  padding: 12px 20px 8px 20px;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  gap: 12px;
}

.step-progress-track {
  flex: 1;
  height: 6px;
  background-color: #f1f3f5;
  border-radius: 3px;
  overflow: hidden;
}

.step-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ffd15c, #ffbc00);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.step-label-text {
  color: #ffbc00;
  white-space: nowrap;
  font-size: 13px;
}

.step-label-text span:last-child {
  color: #999999;
  margin-left: 2px;
}

/* ========================================
   캡처 2번 레퍼런스: 카카오페이 정산하기 100% 동일 다크 디자인
======================================== */
.kakaopay-dark-wrapper {
  background-color: #19191b;
  min-height: 100%;
  padding: 24px 20px 36px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  color: #ffffff;
  margin: -16px -16px 0 -16px;
}

.kakaopay-title-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 10px;
}

.kakaopay-title-edit {
  background: transparent;
  border: none;
  color: #8e8e93;
  font-size: 15px;
  font-weight: 600;
  text-align: center;
  outline: none;
  width: 140px;
}

.kakaopay-title-edit:focus {
  color: #ffffff;
  border-bottom: 1px solid #fee500;
}

.edit-pencil-ic {
  color: #8e8e93;
  font-size: 13px;
}

.kakaopay-total-display {
  font-size: 32px;
  font-weight: 800;
  text-align: center;
  margin: 12px 0 28px;
  color: #ffffff;
  letter-spacing: -0.5px;
}

.kakaopay-card-box {
  background-color: #25262a;
  border-radius: 20px;
  padding: 20px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 14px;
}

.card-box-left {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.icon-utensils-box {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background-color: #33343a;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #ff6b4a;
}

.card-info-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-title-text {
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
}

.item-amount-text {
  font-size: 20px;
  font-weight: 800;
  color: #ffffff;
}

.item-members-text {
  font-size: 13px;
  color: #8e8e93;
}

.btn-card-close {
  background: transparent;
  border: none;
  color: #54555a;
  font-size: 20px;
  cursor: pointer;
}

.btn-kakaopay-outline {
  width: 100%;
  height: 52px;
  background-color: #25262a;
  border: 1px solid #33343a;
  border-radius: 16px;
  color: #ffffff;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  margin-bottom: 24px;
}

.kakaopay-feed-box {
  background-color: #25262a;
  border-radius: 20px;
  padding: 16px;
  margin-bottom: 32px;
}

.kakaopay-memo-textarea {
  width: 100%;
  background-color: #19191b;
  border: 1px solid #33343a;
  border-radius: 14px;
  padding: 12px;
  color: #ffffff;
  font-size: 14px;
  box-sizing: border-box;
  resize: none;
  outline: none;
}

.kakaopay-memo-textarea:focus {
  border-color: #fee500;
}

.photo-attach-wrapper {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.photo-btn-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background-color: #33343a;
  color: #8e8e93;
  font-size: 13px;
  font-weight: 600;
  padding: 8px 14px;
  border-radius: 20px;
  cursor: pointer;
}

.preview-img-box {
  position: relative;
  width: 44px;
  height: 44px;
  border-radius: 8px;
  overflow: hidden;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.btn-del-img {
  position: absolute;
  top: 2px;
  right: 2px;
  background: rgba(0,0,0,0.6);
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 16px;
  height: 16px;
  font-size: 10px;
  cursor: pointer;
}

.kakaopay-bottom-submit {
  margin-top: auto;
  padding-top: 16px;
}

.btn-kakaopay-yellow {
  width: 100%;
  height: 56px;
  background-color: #fee500;
  color: #111111;
  border: none;
  border-radius: 28px;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(254, 229, 0, 0.2);
}
</style>
