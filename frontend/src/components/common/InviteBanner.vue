<template>
  <!-- 이벤트 배너 -->
  <section class="invite-banner">
    <div class="banner-content">
      <h3>친구 초대 이벤트 🎉</h3>

      <p>친구를 초대하면 2,000원 지급!</p>

      <button>참여하기 →</button>
    </div>

    <div class="banner-image">🎁</div>
  </section>
</template>

<script setup></script>

<style scoped>
/* 이벤트 카드 */
.invite-banner {
  height: 110px;

  width: 100%;

  box-sizing: border-box;

  border-radius: 18px;

  padding: 16px 18px;

  display: flex;

  justify-content: space-between;

  align-items: center;

  background: linear-gradient(135deg, #fff1ca, #ffe3a0);

  margin-bottom: 14px;

  overflow: hidden;
}

.banner-content {
  display: flex;

  flex-direction: column;

  gap: 5px;
}

.banner-content h3 {
  margin: 0;

  font-size: 15px;

  font-weight: 700;
}

.banner-content p {
  margin: 0;

  font-size: 12px;

  color: #555;
}

.banner-content button {
  margin-top: 6px;

  width: 75px;

  height: 25px;

  border: none;

  border-radius: 20px;

  background: white;

  font-size: 11px;

  font-weight: 600;
}

.banner-image {
  font-size: 42px;

  transform: rotate(-10deg);
}
</style>
