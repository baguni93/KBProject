import { defineStore } from 'pinia';
import { ref, computed, reactive, watch } from 'vue';
import remittanceApi from '@/api/remittanceApi';
import friendApi from '@/api/friend';
import walletApi from '@/api/walletApi';
import analysisApi from '@/api/analysisApi';
import transactionApi from '@/api/transactionApi';
import * as accountApi from '@/api/accountApi';
import { useAuthStore } from '@/stores/auth';
import { useProfileStore } from '@/stores/profile';
import { useSettlementStore } from '@/stores/settlement';
import { useModalStore } from '@/stores/userModalStore';

export const useRemittanceStore = defineStore('remittance', () => {
  const authStore = useAuthStore();
  const profileStore = useProfileStore();
  const settlementStore = useSettlementStore();
  const modalStore = useModalStore();

  // 1. 송금 기본 상태
  const remitType = ref('ACCOUNT'); // 'ACCOUNT' | 'FRIEND' | 'DUTCH'
  
  // 계좌 송금 폼
  const accountForm = reactive({
    bankCode: '',
    accountNumber: '',
    receiverName: '',
  });

  // 계좌번호가 지워지면 은행 선택도 자동으로 초기화
  watch(
    () => accountForm.accountNumber,
    (newVal) => {
      if (!newVal || newVal.trim() === '') {
        accountForm.bankCode = '';
      }
    }
  );

  // 친구 송금 폼
  const selectedFriendId = ref(null);
  const selectedFriendObj = ref(null);
  const friendList = ref([]);
  const friendSearchKeyword = ref('');

  // 정산 송금 관련 상태
  const settlementId = ref(null);
  const settlementRequesterId = ref(null);

  const filteredFriends = computed(() => {
    if (!friendSearchKeyword.value.trim()) return friendList.value;
    const kw = friendSearchKeyword.value.toLowerCase();
    return friendList.value.filter(
      (f) =>
        (f.name && f.name.toLowerCase().includes(kw)) ||
        (f.nickname && f.nickname.toLowerCase().includes(kw)) ||
        (f.username && f.username.toLowerCase().includes(kw))
    );
  });

  const DEFAULT_AVATAR = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23cbd5e1'%3E%3Cpath d='M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z'/%3E%3C/svg%3E";

  const getProfileImageUrl = (friend) => {
    if (!friend) return DEFAULT_AVATAR;
    if (friend.url) return friend.url;
    const imgName =
      friend.profileImageName ||
      friend.profileImage ||
      friend.profileImg ||
      friend.receiver?.profileImageName ||
      friend.receiver?.profileImage ||
      friend.friend?.profileImageName ||
      friend.user?.profileImageName;
    if (imgName && imgName !== 'character1.png') {
      if (imgName.startsWith("http") || imgName.startsWith("/")) return imgName;
      return `/api/feeds/profile/${imgName}`;
    }
    if (friend.avatarUrl && !friend.avatarUrl.includes("default_profile")) return friend.avatarUrl;
    return DEFAULT_AVATAR;
  };

  const myProfileImageUrl = computed(() => {
    const pUrl = profileStore.profile?.url || profileStore.profile?.profileImageUrl || profileStore.profile?.imageUrl;
    if (pUrl) return pUrl;

    const pName = profileStore.profile?.imageName || profileStore.profile?.profileImageName || profileStore.profile?.image || authStore.user?.profileImageName || authStore.user?.profileImage || authStore.user?.profileImg;
    if (pName && pName !== 'character1.png') {
      if (pName.startsWith("http") || pName.startsWith("/")) return pName;
      return `/api/feeds/profile/${pName}`;
    }
    return DEFAULT_AVATAR;
  });

  const myProfileName = computed(() => {
    const authNick = authStore.user?.nickname || authStore.nickname;
    if (authNick && authNick !== '김국민') return authNick;

    const authName = authStore.userName || authStore.user?.userName;
    if (authName && authName !== '김국민') return authName;

    const profNick = profileStore.profile?.nickname;
    if (profNick && profNick !== '김국민') return profNick;

    return authNick || authName || profNick || "내 프로필";
  });

  const getFriendObj = (fId) => {
    return friendList.value.find((f) => (f.id === fId || f.friendId === fId || f.friendUserId === fId || f.userId === fId || f.receiver?.id === fId)) || {};
  };

  // 더치페이 N빵 정산 폼
  const selectedDutchFriends = ref([]);
  const dutchRoomTitle = ref('');
  const userTxList = ref([]);
  const selectedTxIds = ref([]);

  // 금액 & 피드 작성 정보
  const remitAmount = ref(0);
  const selectedCategoryId = ref(null);
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
  const remitMemo = ref('');
  const remitVisibility = ref('PUBLIC');
  const selectedFile = ref(null);
  const imagePreviewUrl = ref(null);
  const selectedFiles = ref([]);
  const imagePreviewUrls = ref([]);

  // 사용자 잔액 및 전자지갑(페이머니) 데이터
  const walletBalance = ref(0);
  const myBalance = ref(0);
  const primaryAccountBalance = ref(0);
  const primaryAccountName = ref('KB국민 주거래통장');
  const myAccountName = ref('페이머니');
  const bankList = ref([]);
  const recentAccounts = ref([]);

  // 출금 가능한 총 잔액 (전자지갑 잔액 + 대표계좌 자동충전 가능 잔액)
  const totalAvailableBalance = computed(() => {
    return (walletBalance.value || 0) + (primaryAccountBalance.value || 0);
  });

  // 지갑 잔액 부족으로 대표계좌에서 자동 충전이 필요한 금액
  const autoChargeAmount = computed(() => {
    if (remitAmount.value > walletBalance.value && remitAmount.value <= totalAvailableBalance.value) {
      return remitAmount.value - walletBalance.value;
    }
    return 0;
  });

  // 자동 충전 필요 여부
  const isAutoChargeNeeded = computed(() => {
    return autoChargeAmount.value > 0;
  });

  // 출금 가능 총 잔액 초과 여부
  const isExceedBalance = computed(() => {
    return remitAmount.value > totalAvailableBalance.value;
  });

  // 모달 및 완료 상태
  const showConfirmModal = ref(false);
  const showPasswordModal = ref(false);
  const inputPinCode = ref('');
  const remitSuccess = ref(false);
  const isSubmitting = ref(false);

  // 계산된 속성
  const selectedTxTotalAmount = computed(() => {
    return userTxList.value
      .filter((t) => selectedTxIds.value.includes(t.id))
      .reduce((sum, t) => sum + (t.amount || 0), 0);
  });

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
    if (!code) return "";
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

  const getFriendName = (fId) => {
    const f = friendList.value.find((item) => (item.id === fId || item.friendId === fId || item.friendUserId === fId || item.userId === fId || item.receiver?.id === fId));
    if (f) {
      return f.name || f.nickname || f.receiver?.nickname || f.receiver?.name || f.username || f.receiver?.username || `친구 ${fId}`;
    }
    return `친구 ${fId}`;
  };

  const formatCurrency = (val) => new Intl.NumberFormat("ko-KR").format(val || 0);

  const loadInitData = async () => {
    try {
      const userId = Number(authStore.userId);
      if (!userId) return;

      // 내 프로필 로드
      try {
        await profileStore.getProfile(userId);
      } catch (pErr) {
        console.log("프로필 로드 예외", pErr);
      }

      // 카테고리 목록
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

      // 1. 대표 계좌 및 계좌 잔액 조회
      try {
        if (accountApi && accountApi.getAccounts) {
          const accounts = await accountApi.getAccounts();
          if (accounts && Array.isArray(accounts) && accounts.length > 0) {
            const primary = accounts.find((a) => a.primaryYn === 'Y') || accounts[0];
            if (primary) {
              primaryAccountBalance.value = Number(primary.balance || 0);
              primaryAccountName.value = `${primary.bankName || 'KB국민'} ${primary.accountNumber ? '(' + primary.accountNumber.slice(-4) + ')' : ''}`;
            }
          }
        }
      } catch (accErr) {
        console.log("연결 계좌 목록 로드 예외", accErr);
      }

      // 2. 지갑 잔액 조회 (송금 기본 출금처: 페이머니)
      try {
        if (walletApi && walletApi.getWalletByUserId) {
          const wInfo = await walletApi.getWalletByUserId(userId);
          if (wInfo) {
            walletBalance.value = Number(wInfo.balance !== undefined ? wInfo.balance : wInfo.amount || 0);
            myBalance.value = walletBalance.value;
            myAccountName.value = '페이머니';
          }
        }
      } catch (wErr) {
        console.log("지갑 정보 조회 예외", wErr);
      }

      // 최근 계좌 & 은행 목록
      try {
        if (remittanceApi && remittanceApi.getBankRemittanceInfo) {
          const bInfo = await remittanceApi.getBankRemittanceInfo(userId);
          if (bInfo) {
            if (bInfo.banks && Array.isArray(bInfo.banks)) {
              bankList.value = bInfo.banks;
            }
            if (bInfo.recentAccounts && Array.isArray(bInfo.recentAccounts) && bInfo.recentAccounts.length > 0) {
              recentAccounts.value = bInfo.recentAccounts.map((r) => ({
                id: r.id || r.remittanceId || r.accountNumber,
                accountNumber: r.accountNumber,
                bankCode: r.bankCode || "004",
                bankName: r.bankName || "KB국민",
                receiverName: r.ownerName || r.receiverName || r.name || "수취인",
              }));
            } else {
              recentAccounts.value = [];
            }
          }
        }
      } catch (bankErr) {
        console.log("은행 정보 조회 예외", bankErr);
        recentAccounts.value = [];
      }

      // 친구 목록
      try {
        if (friendApi && friendApi.getFriendList) {
          const fData = await friendApi.getFriendList(userId);
          if (fData && Array.isArray(fData)) {
            friendList.value = fData.map((f) => {
              const fId =
                f.friendUserId ||
                f.id ||
                f.friendId ||
                f.receiver?.id ||
                f.receiver?.userId ||
                f.friend?.id ||
                f.user?.id;
              const fName =
                f.nickname ||
                f.receiver?.nickname ||
                f.friend?.nickname ||
                f.name ||
                f.receiver?.name ||
                f.friend?.name ||
                f.user?.nickname ||
                f.user?.name ||
                f.username ||
                f.receiver?.username ||
                "친구";
              const fUsername =
                f.username ||
                f.receiver?.username ||
                f.friend?.username ||
                f.user?.username ||
                f.nickname ||
                f.receiver?.nickname ||
                "";
              const fProfileImg =
                f.profileImageName ||
                f.profileImage ||
                f.profileImg ||
                f.receiver?.profileImageName ||
                f.receiver?.profileImage ||
                f.friend?.profileImageName ||
                f.user?.profileImageName ||
                "default.png";

              return {
                ...f,
                id: fId,
                friendId: f.friendId || fId,
                friendUserId: f.friendUserId || fId,
                name: fName,
                nickname: fName,
                username: fUsername,
                profileImageName: fProfileImg,
                avatarUrl:
                  f.avatarUrl && !f.avatarUrl.includes("default_profile")
                    ? f.avatarUrl
                    : (fProfileImg
                      ? fProfileImg.startsWith("http") || fProfileImg.startsWith("/")
                        ? fProfileImg
                        : `/api/feeds/profile/${fProfileImg}`
                      : "/api/feeds/profile/unknown.png"),
              };
            });
          }
        }
      } catch (fErr) {
        console.log("친구 목록 조회 예외", fErr);
      }

    } catch (err) {
      console.log("송금 초기 데이터 로드 예외", err);
    }
  };

  // 거래 내역 로드 (더치페이용)
  const loadUserTransactions = async () => {
    try {
      const userId = Number(authStore.userId);
      if (!userId) {
        userTxList.value = [];
        return;
      }
      if (transactionApi && transactionApi.getTransactions) {
        const data = await transactionApi.getTransactions(userId);
        if (data && Array.isArray(data)) {
          const payItems = data.filter((t) => {
            const typeStr = (t.transactionType || t.type || "").toUpperCase();
            return (
              typeStr.includes("PAY") ||
              typeStr === "" ||
              (!typeStr.includes("CHARGE") && !typeStr.includes("TRANSFER"))
            );
          });

          userTxList.value = payItems.map((t) => ({
            id: t.transactionId || t.id,
            title: t.merchantName || t.title || "결제 건",
            date: t.transactionDate ? t.transactionDate.substring(0, 10) : "최근",
            amount: t.amount || 0,
            category: t.categoryName || "기타",
          }));
        }
      }
    } catch (err) {
      console.log("거래내역 로드 예외", err);
    }
  };

  // 3. 사진 파일 처리 (다중 선택 지원)
  const handleFileChange = (e) => {
    const files = e.target?.files ? Array.from(e.target.files) : (Array.isArray(e) ? e : (e ? [e] : []));
    files.forEach((file) => {
      selectedFiles.value.push(file);
      imagePreviewUrls.value.push(URL.createObjectURL(file));
    });
    if (selectedFiles.value.length > 0) {
      selectedFile.value = selectedFiles.value[0];
      imagePreviewUrl.value = imagePreviewUrls.value[0];
    }
  };

  const removeSelectedFile = (index) => {
    if (typeof index === "number" && index >= 0) {
      selectedFiles.value.splice(index, 1);
      imagePreviewUrls.value.splice(index, 1);
    } else {
      selectedFiles.value = [];
      imagePreviewUrls.value = [];
    }
    selectedFile.value = selectedFiles.value.length > 0 ? selectedFiles.value[0] : null;
    imagePreviewUrl.value = imagePreviewUrls.value.length > 0 ? imagePreviewUrls.value[0] : null;
  };

  // 4. 송금 실행 API 호출
  const executeRemittance = async () => {
    isSubmitting.value = true;
    try {
      const userId = Number(authStore.userId);
      if (!userId) {
        throw new Error('로그인 세션이 만료되었습니다. 다시 로그인해주세요.');
      }

      // ── DUTCH(정산) 분기 ──────────────────────────
      if (remitType.value === 'DUTCH') {
        const friendCount = selectedDutchFriends.value.length;
        const perAmount = friendCount > 0
          ? Math.ceil(remitAmount.value / (friendCount + 1))
          : remitAmount.value;

        let rawTitle = dutchRoomTitle.value || remitMemo.value || '더치페이 정산';
        if (rawTitle.length > 20) rawTitle = rawTitle.substring(0, 20);

        let rawContent = remitMemo.value || rawTitle;
        if (rawContent.length > 20) rawContent = rawContent.substring(0, 20);

        const memberPayloadList = selectedDutchFriends.value.map((f) => {
          let targetUserId = 0;
          if (typeof f === 'number') {
            const friendObj = getFriendObj(f);
            targetUserId = friendObj.friendUserId || friendObj.userId || friendObj.friendId || friendObj.id || f;
          } else if (typeof f === 'object' && f !== null) {
            targetUserId = f.friendUserId || f.userId || f.friendId || f.id || 0;
          } else {
            targetUserId = Number(f) || 0;
          }

          return {
            userId: Number(targetUserId),
            amount: perAmount,
          };
        }).filter((m) => m.userId > 0);

        const settlementPayload = {
          requesterId: Number(userId),
          title: rawTitle,
          content: rawContent,
          totalAmount: Number(remitAmount.value),
          spendingCategoryId: Number(selectedCategoryId.value) || 1,
          settlementType: 'EQUAL',
          members: memberPayloadList,
        };

        console.log('Sending settlement payload:', settlementPayload);

        if (remittanceApi && remittanceApi.createSettlement) {
          const res = await remittanceApi.createSettlement(settlementPayload);
          console.log('Settlement created successfully:', res);
          // 정산 목록 즉시 갱신
          try {
            await settlementStore.getMyList({ userId });
          } catch (listErr) {
            console.log('정산 목록 갱신 예외:', listErr);
          }
        }
        remitSuccess.value = true;
        return;
      }

      // ── ACCOUNT / FRIEND / DUTCH_PAY 송금 분기 ───────────────
      const isAccount = remitType.value === 'ACCOUNT';
      const friendNick = selectedFriendObj.value?.nickname || selectedFriendObj.value?.name || selectedFriendObj.value?.username || '친구';
      const receiverNameVal = isAccount
        ? (accountForm.receiverName || '수취인')
        : (accountForm.receiverName || friendNick);

      const targetSettlementId = settlementId.value;

      const payload = {
        userId: userId,
        walletId: userId,
        receiverId: isAccount ? null : selectedFriendId.value,
        receiverName: receiverNameVal,
        merchantName: receiverNameVal,
        amount: Number(remitAmount.value),
        spendingCategoryId: selectedCategoryId.value || 1,
        memo: remitMemo.value || '송금 완료',
        content: remitMemo.value || '송금 완료',
        receiverType: isAccount ? 'ACCOUNT' : 'WALLET',
        bankCode: accountForm.bankCode || '004',
        accountNumber: accountForm.accountNumber || '',
        visibility: remitVisibility.value || 'PUBLIC',
        settlementId: targetSettlementId || null,
        isSettlement: !!targetSettlementId,
        files: selectedFiles.value.length > 0 ? selectedFiles.value : (selectedFile.value ? [selectedFile.value] : []),
        file: selectedFile.value,
      };

      if (remittanceApi && remittanceApi.sendMoney) {
        await remittanceApi.sendMoney(payload);
      }

      // 송금 완료 후 즉시 지갑 및 계좌 잔액 최신 상태로 갱신
      try {
        await loadInitData();
      } catch (initErr) {
        console.log('송금 후 잔액 갱신 예외:', initErr);
      }

      // 정산 송금인 경우 정산 상태 갱신 트리거
      if (targetSettlementId) {
        try {
          console.log('Triggering settlement payment PATCH for settlementId:', targetSettlementId, 'userId:', userId);
          await settlementStore.payment({
            settlementId: targetSettlementId,
            userId,
          });
          await settlementStore.getMyList({ userId });
        } catch (sErr) {
          console.warn('정산 완료 API 호출 결과:', sErr);
        }
      }

      remitSuccess.value = true;
    } catch (err) {
      console.error('송금/정산 처리 중 예외 발생:', err);
      remitSuccess.value = false;
      const msg = err.response?.data?.message || err.message || '송금 처리에 실패했습니다.';
      modalStore.showAlert('송금 실패: ' + msg, '송금 처리 오류');
      throw err;
    } finally {
      isSubmitting.value = false;
    }
  };

  // 5. 상태 완전 초기화 (메모, 이미지, 금액, 수취인 등 지우기)
  const resetAll = () => {
    accountForm.accountNumber = "";
    accountForm.bankCode = "";
    accountForm.receiverName = "";
    selectedFriendId.value = null;
    selectedFriendObj.value = null;
    settlementId.value = null;
    settlementRequesterId.value = null;
    selectedDutchFriends.value = [];
    dutchRoomTitle.value = "";
    selectedTxIds.value = [];
    remitAmount.value = 0;
    remitMemo.value = "";
    selectedFile.value = null;
    imagePreviewUrl.value = null;
    selectedFiles.value = [];
    imagePreviewUrls.value = [];
    friendSearchKeyword.value = "";
    selectedCategoryId.value = 1;
    showConfirmModal.value = false;
    showPasswordModal.value = false;
    inputPinCode.value = "";
    remitSuccess.value = false;
  };

  const recentFriends = computed(() => {
    if (!userTxList.value || userTxList.value.length === 0 || !friendList.value || friendList.value.length === 0) {
      return [];
    }

    const transferTxs = userTxList.value.filter(
      (tx) => (tx.transactionType === "TRANSFER" || tx.type === "TRANSFER") && (tx.receiverId || tx.receiveId)
    );

    if (transferTxs.length === 0) {
      return [];
    }

    const seenIds = new Set();
    const result = [];

    for (const tx of transferTxs) {
      const recId = tx.receiverId || tx.receiveId;
      if (recId && !seenIds.has(recId)) {
        seenIds.add(recId);
        const f = friendList.value.find(
          (item) =>
            item.id === recId ||
            item.friendId === recId ||
            item.friendUserId === recId ||
            item.userId === recId ||
            item.receiver?.id === recId
        );
        if (f) {
          result.push(f);
        }
      }
      if (result.length >= 5) break;
    }

    return result;
  });

  return {
    remitType,
    accountForm,
    selectedFriendId,
    selectedFriendObj,
    friendList,
    friendSearchKeyword,
    filteredFriends,
    getProfileImageUrl,
    myProfileImageUrl,
    myProfileName,
    getFriendObj,
    selectedDutchFriends,
    dutchRoomTitle,
    userTxList,
    selectedTxIds,
    remitAmount,
    selectedCategoryId,
    categoryList,
    remitMemo,
    remitVisibility,
    selectedFile,
    imagePreviewUrl,
    selectedFiles,
    imagePreviewUrls,
    myBalance,
    walletBalance,
    primaryAccountBalance,
    primaryAccountName,
    myAccountName,
    totalAvailableBalance,
    isAutoChargeNeeded,
    autoChargeAmount,
    isExceedBalance,
    bankList,
    recentAccounts,
    recentFriends,
    showConfirmModal,
    showPasswordModal,
    inputPinCode,
    remitSuccess,
    isSubmitting,
    selectedTxTotalAmount,
    bankOptions,
    getBankName,
    getBankLogoFileName,
    getFriendName,
    formatCurrency,
    loadInitData,
    loadUserTransactions,
    handleFileChange,
    removeSelectedFile,
    executeRemittance,
    resetAll,
  };
});
