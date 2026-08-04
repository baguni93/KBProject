import { computed, ref } from 'vue';
import { defineStore } from 'pinia';

export const useSignupStore = defineStore('signup', () => {
    // 약관 정보
    const agreements = ref([]);

    // 휴대폰 인증 정보
    const phoneAuth = ref({
        userName: '',
        birthDate: '',
        carrierCode: '',
        phoneNumber: '',
        verificationPurpose: 'SIGN_UP',
        verificationCode: '',
    });

    // 인증번호 만료 시간
    const expiresIn = ref(180);

    // 개발용 인증번호
    const developmentCode = ref('');

    // 가입 여부
    const memberStatus = ref('');

    // PIN 정보
    const pin = ref('');
    const pinConfirmed = ref(false);

    // 전체 약관 선택 여부
    const isAllChecked = computed(() => {
        return agreements.value.length > 0 && agreements.value.every((agreement) => agreement.agreed);
    });

    // 필수 약관 선택 여부
    const isRequiredChecked = computed(() => {
        return agreements.value
            .filter((agreement) => agreement.requiredYn === 'Y')
            .every((agreement) => agreement.agreed);
    });

    // 약관 목록 저장
    const setAgreements = (agreementList) => {
        agreements.value = agreementList.map((agreement) => {
            const savedAgreement = agreements.value.find((item) => item.agreementType === agreement.agreementType);

            return { ...agreement, agreed: savedAgreement?.agreed ?? false };
        });
    };

    // 개별 약관 동의
    const setAgreementChecked = (agreementType, checked) => {
        const agreement = agreements.value.find((item) => item.agreementType === agreementType);

        if (agreement) agreement.agreed = checked;
    };

    // 전체 약관 동의
    const setAllAgreements = (checked) => {
        agreements.value.forEach((agreement) => {
            agreement.agreed = checked;
        });
    };

    // 휴대폰 인증 정보 저장
    const setPhoneAuth = (phoneAuthData) => {
        phoneAuth.value = { ...phoneAuth.value, ...phoneAuthData };
    };

    // 인증 목적 저장
    const setVerificationPurpose = (verificationPurpose) => {
        phoneAuth.value.verificationPurpose = verificationPurpose;
    };

    // 인증번호 저장
    const setVerificationCode = (verificationCode) => {
        phoneAuth.value.verificationCode = verificationCode;
    };

    // 인증번호 만료 시간 저장
    const setExpiresIn = (seconds) => {
        expiresIn.value = seconds;
    };

    // 개발용 인증번호 저장
    const setDevelopmentCode = (verificationCode) => {
        developmentCode.value = verificationCode;
    };

    // 가입 여부 저장
    const setMemberStatus = (status) => {
        memberStatus.value = status;
    };

    // PIN 저장
    const setPin = (pinValue) => {
        pin.value = pinValue;
    };

    // PIN 확인 상태 저장
    const setPinConfirmed = (confirmed) => {
        pinConfirmed.value = confirmed;
    };

    // 회원가입 정보 초기화
    const reset = () => {
        agreements.value = [];

        phoneAuth.value = {
            userName: '',
            birthDate: '',
            carrierCode: '',
            phoneNumber: '',
            verificationPurpose: 'SIGN_UP',
            verificationCode: '',
        };

        expiresIn.value = 180;
        developmentCode.value = '';
        memberStatus.value = '';
        pin.value = '';
        pinConfirmed.value = false;
    };

    return {
        agreements,
        phoneAuth,
        expiresIn,
        developmentCode,
        memberStatus,
        pin,
        pinConfirmed,
        isAllChecked,
        isRequiredChecked,
        setAgreements,
        setAgreementChecked,
        setAllAgreements,
        setPhoneAuth,
        setVerificationPurpose,
        setVerificationCode,
        setExpiresIn,
        setDevelopmentCode,
        setMemberStatus,
        setPin,
        setPinConfirmed,
        reset,
    };
});