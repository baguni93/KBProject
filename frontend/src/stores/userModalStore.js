import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useModalStore = defineStore('modal', () => {
  const isOpen = ref(false);

  const title = ref('알림');
  const message = ref('');

  const confirmText = ref('확인');
  const cancelText = ref('취소');

  const showCancel = ref(false);

  // 성공 모달 여부
  const isSuccess = ref(false);

  let resolveFunction = null;

  // 1. 일반 알림
  const showAlert = (msg, tit = '알림') => {
    title.value = tit;
    message.value = msg;

    confirmText.value = '확인';

    showCancel.value = false;

    // 일반 알림
    isSuccess.value = false;

    isOpen.value = true;
  };

  // 2. 성공 알림
  const showSuccess = (msg, tit = '완료') => {
    title.value = tit;
    message.value = msg;

    confirmText.value = '확인';

    showCancel.value = false;

    // 성공 모달
    isSuccess.value = true;

    isOpen.value = true;

    return new Promise((resolve) => {
      resolveFunction = resolve;
    });
  };

  // 3. 확인/취소
  const showConfirm = (
    msg,
    tit = '확인',
    confirmMsg = '확인',
    cancelMsg = '취소',
  ) => {
    title.value = tit;
    message.value = msg;

    confirmText.value = confirmMsg;
    cancelText.value = cancelMsg;

    showCancel.value = true;

    // 확인 모달이므로 성공 상태 해제
    isSuccess.value = false;

    isOpen.value = true;

    return new Promise((resolve) => {
      resolveFunction = resolve;
    });
  };

  // 확인
  const handleConfirm = () => {
    isOpen.value = false;

    if (resolveFunction) {
      resolveFunction(true);
      resolveFunction = null;
    }

    // 다음 모달을 위해 초기화
    isSuccess.value = false;
  };

  // 취소
  const handleCancel = () => {
    isOpen.value = false;

    if (resolveFunction) {
      resolveFunction(false);
      resolveFunction = null;
    }

    isSuccess.value = false;
  };

  return {
    isOpen,

    title,
    message,

    confirmText,
    cancelText,

    showCancel,

    isSuccess,

    showAlert,
    showSuccess,
    showConfirm,

    handleConfirm,
    handleCancel,
  };
});
