import { computed, ref } from 'vue';
import { defineStore } from 'pinia';

const initAccountForm = {
    bankCode: '',
    bankName: '',
    bankLogoUrl: '',
    accountNumber: '',
    accountHolder: '',
    verificationId: null,
    verificationCode: '',
    developmentCode: '',
};

export const useAccountStore = defineStore('account', () => {
    const accounts = ref([]);
    const banks = ref([]);
    const accountForm = ref({ ...initAccountForm });

    // 사용자 번호 조회
    const userId = computed(() => {
        const signupUserId = sessionStorage.getItem('signupUserId');

        if (signupUserId) return Number(signupUserId);

        const auth = localStorage.getItem('auth');

        if (!auth) return null;

        try {
            const authData = JSON.parse(auth);
            return Number(authData.userId || authData.user?.username) || null;
        } catch (error) {
            console.error(error);
            return null;
        }
    });

    // 선택 은행 저장
    const setBank = (bank) => {
        accountForm.value.bankCode = bank.bankCode;
        accountForm.value.bankName = bank.bankName;
        accountForm.value.bankLogoUrl = bank.bankLogoUrl;
    };

    // 계좌정보 저장
    const setAccountInfo = (accountData) => {
        accountForm.value.accountNumber = accountData.accountNumber;
        accountForm.value.accountHolder = accountData.accountHolder;
    };

    // 인증정보 저장
    const setVerification = (verificationData) => {
        accountForm.value.verificationId = verificationData.verificationId;
        accountForm.value.developmentCode = verificationData.verificationCode;
    };

    // 인증번호 입력값 저장
    const setVerificationCode = (verificationCode) => {
        accountForm.value.verificationCode = verificationCode;
    };

    // 연결 계좌 목록 저장
    const setAccounts = (accountList) => {
        accounts.value = accountList;
    };

    // 은행 목록 저장
    const setBanks = (bankList) => {
        banks.value = bankList;
    };

    // 계좌 연결 정보 초기화
    const resetAccountForm = () => {
        accountForm.value = { ...initAccountForm };
    };

    return {
        accounts,
        banks,
        accountForm,
        userId,
        setBank,
        setAccountInfo,
        setVerification,
        setVerificationCode,
        setAccounts,
        setBanks,
        resetAccountForm,
    };
});