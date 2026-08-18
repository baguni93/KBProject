<template>
  <div class="payment-card">
    <div class="card-content">
      <!-- 프로필 이미지 -->
      <img :src="profileImage" alt="Profile" class="profile-img" />

      <!-- 본문 내용 -->
      <div class="text-area">
        <p class="main-text">
          <span class="bold">Ja</span> requested
          <span class="bold">$0.50</span> from
          <span class="bold">You</span>
        </p>

        <p class="time-text">54s</p>
        <p class="memo-text">YouTube video</p>

        <!-- 버튼 영역 -->
        <div class="button-group">
          <button @click="handleDecline" class="btn decline-btn">
            Decline
          </button>
          <button @click="handlePay" class="btn pay-btn">Pay</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';

defineProps({
  profileImage: {
    type: String,
    default: 'https://via.placeholder.com/150',
  },
});

const handleDecline = () => {
  console.log('Decline clicked');
};

const handlePay = () => {
  console.log('Pay clicked');
};
</script>

<style scoped>
.payment-card {
  max-width: 400px;
  margin: 0 auto;
  background-color: #ffffff;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid #f3f4f6;
  font-family: sans-serif;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.card-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.profile-img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.text-area {
  flex: 1;
  min-width: 0;
}

.main-text {
  color: #111827;
  font-size: 14px;
  line-height: 1.4;
  margin: 0;
}

.bold {
  font-weight: 700;
}

.time-text {
  color: #9ca3af;
  font-size: 12px;
  margin: 2px 0 0 0;
}

.memo-text {
  color: #6b7280;
  font-size: 14px;
  margin: 4px 0 0 0;
}

.button-group {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.btn {
  flex: 1;
  padding: 8px 16px;
  border-radius: 9999px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.decline-btn {
  background-color: #ffffff;
  border: 1px solid #d1d5db;
  color: #374151;
}

.decline-btn:hover {
  background-color: #f9fafb;
}

.pay-btn {
  background-color: #2563eb;
  border: 1px solid transparent;
  color: #ffffff;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.pay-btn:hover {
  background-color: #1d4ed8;
}
</style>
