<template>
  <Teleport to=".app">
    <transition name="push-slide">
      <div v-if="notification.visible" class="mobile-push-notification-banner">
        <div class="push-header-line">
          <div class="push-app-info">
            <div class="app-icon-badge">
              <i :class="[notification.icon || 'fa-solid fa-wallet', 'text-white']"></i>
            </div>
            <span class="text-13-bold text-white">Scoula Pay</span>
          </div>
          <span class="text-11 text-muted">{{ notification.time || '방금 전' }}</span>
        </div>
        <div class="push-content-body">
          <div class="push-title text-14-bold text-white">{{ notification.title }}</div>
          <div class="push-msg text-13 text-light">{{ notification.message }}</div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
defineProps({
  notification: {
    type: Object,
    required: true,
    default: () => ({
      visible: false,
      title: "",
      message: "",
      icon: "fa-solid fa-wallet",
      time: "방금 전",
    }),
  },
});
</script>

<style scoped>
.mobile-push-notification-banner {
  position: absolute;
  top: 16px;
  left: 16px;
  right: 16px;
  background: rgba(28, 30, 38, 0.96);
  backdrop-filter: blur(12px);
  border-radius: 18px;
  padding: 14px 16px;
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.15);
  z-index: 999999;
  display: flex;
  flex-direction: column;
  gap: 6px;
  box-sizing: border-box;
}

.push-header-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.push-app-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-icon-badge {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background-color: #ffbc2e;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
}

.push-content-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.push-title {
  color: #ffffff;
}

.push-msg {
  color: #dddddd;
  line-height: 1.35;
}

.push-slide-enter-active,
.push-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.push-slide-enter-from,
.push-slide-leave-to {
  transform: translateY(-100px);
  opacity: 0;
}
</style>
