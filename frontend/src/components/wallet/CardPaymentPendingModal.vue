<template>
  <!-- 하단 [결제] 버튼 1초 롱프레스 시 전역 이벤트(TRIGGER_LONG_PRESS_PAYMENT) 감지기 -->
  <div style="display: none;"></div>
</template>

<script setup>
import { onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
let longPressTimer = null;

const isCenterPayBtn = (target) => {
  if (!target) return false;
  const nav = target.closest(".bottom-nav");
  if (!nav) return false;
  const item = target.closest(".nav-item");
  if (!item) return false;
  const isCenterBox = Boolean(item.querySelector(".icon-box.center"));
  const hasTextPay = item.textContent && item.textContent.includes("결제");
  return isCenterBox || hasTextPay;
};

const startPress = (e) => {
  if (!isCenterPayBtn(e.target)) return;
  cancelPress();
  longPressTimer = setTimeout(() => {
    if (navigator.vibrate) navigator.vibrate(60);
    
    // 지갑 페이지 결제 시스템 전역 트리거 이벤트 발송
    window.dispatchEvent(new CustomEvent("TRIGGER_LONG_PRESS_PAYMENT"));

    // 현재 지갑(/wallet) 페이지가 아닐 경우 지갑 페이지로 이동 후 이벤트 재발송
    if (window.location.pathname !== "/wallet") {
      router.push("/wallet").then(() => {
        setTimeout(() => {
          window.dispatchEvent(new CustomEvent("TRIGGER_LONG_PRESS_PAYMENT"));
        }, 300);
      });
    }
  }, 1000); // 1초간 꾹 누르면 원래 정상 작동하던 진짜 NFC 결제 로직 호출
};

const cancelPress = () => {
  if (longPressTimer) {
    clearTimeout(longPressTimer);
    longPressTimer = null;
  }
};

onMounted(() => {
  window.addEventListener("mousedown", startPress, { capture: true, passive: true });
  window.addEventListener("mouseup", cancelPress, { capture: true, passive: true });
  window.addEventListener("mouseleave", cancelPress, { capture: true, passive: true });
  window.addEventListener("touchstart", startPress, { capture: true, passive: true });
  window.addEventListener("touchend", cancelPress, { capture: true, passive: true });
  window.addEventListener("touchcancel", cancelPress, { capture: true, passive: true });
});

onUnmounted(() => {
  cancelPress();
  window.removeEventListener("mousedown", startPress, { capture: true });
  window.removeEventListener("mouseup", cancelPress, { capture: true });
  window.removeEventListener("mouseleave", cancelPress, { capture: true });
  window.removeEventListener("touchstart", startPress, { capture: true });
  window.removeEventListener("touchend", cancelPress, { capture: true });
  window.removeEventListener("touchcancel", cancelPress, { capture: true });
});
</script>
