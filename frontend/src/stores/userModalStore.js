import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useModalStore = defineStore('modal', () => {
  const isOpen = ref(false);
  const title = ref('알림');
  const message = ref('');
  const confirmText = ref('확인');
  const cancelText = ref('취소');
  const showCancel = ref(false);

  let resolveFunction = null; // 사용자의 선택(true/false)을 기억할 변수

  // 1. 단일 확인 팝업 (버튼 1개)
  const showAlert = (msg, tit = '알림') => {
    title.value = tit;
    message.value = msg;
    confirmText.value = '확인';
    showCancel.value = false;
    isOpen.value = true;
  };

  // 2. '예/아니오' 선택 팝업 (버튼 2개, Promise 반환)
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
    isOpen.value = true;

    return new Promise((resolve) => {
      resolveFunction = resolve;
    });
  };

  // 확인 버튼을 눌렀을 때
  const handleConfirm = () => {
    isOpen.value = false;
    if (resolveFunction) {
      resolveFunction(true);
      resolveFunction = null;
    }
  };

  // 취소 버튼을 눌렀을 때
  const handleCancel = () => {
    isOpen.value = false;
    if (resolveFunction) {
      resolveFunction(false);
      resolveFunction = null;
    }
  };

  return {
    isOpen,
    title,
    message,
    confirmText,
    cancelText,
    showCancel,
    showAlert,
    showConfirm,
    handleConfirm,
    handleCancel,
  };
});
