<template>
  <div class="logout-page">
    <main class="logout-container">
      <header class="page-header">
        <button
            class="back-button"
            type="button"
            @click="goBack"
        >
          &lt;
        </button>

        <h1>로그아웃</h1>

        <div class="header-empty"></div>
      </header>

      <section class="logout-content">
        <div class="logout-visual">
          <div class="door-icon">🚪</div>
          <div class="arrow-icon">→</div>
        </div>

        <h2>로그아웃하시겠어요?</h2>

        <p>
          현재 기기에서만 로그아웃되며,<br />
          다시 로그인하면 서비스를 계속 이용할 수 있어요.
        </p>
      </section>

      <div class="button-area">
        <button
            type="button"
            class="cancel-button"
            :disabled="loading"
            @click="goBack"
        >
          취소
        </button>

        <button
            type="button"
            class="logout-button"
            :disabled="loading"
            @click="handleLogout"
        >
          {{ loading ? '로그아웃 중...' : '로그아웃' }}
        </button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const loading = ref(false);

// 이전 화면
const goBack = () => {
  router.back();
};

const handleLogout = async () => {
  if (loading.value) return;

  try {
    loading.value = true;

    await authStore.logout();

    await router.replace('/intro');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.logout-page {
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.logout-container {
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  padding: 10px 28px 30px;
  background: #ffffff;
  box-sizing: border-box;
}

.page-header {
  display: grid;
  grid-template-columns: 38px 1fr 38px;
  min-height: 44px;
  align-items: center;
  flex-shrink: 0;
}

.page-header h1 {
  margin: 0;
  color: #222222;
  font-size: 17px;
  font-weight: 700;
  text-align: center;
}

.back-button {
  justify-self: start;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 27px;
  line-height: 1;
  cursor: pointer;
}

.header-empty {
  width: 38px;
}

.logout-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  padding-top: 105px;
  text-align: center;
}

.logout-visual {
  position: relative;
  display: flex;
  width: 150px;
  height: 130px;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
}

.logout-visual::before {
  position: absolute;
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: #fff5d8;
  content: '';
}

.door-icon {
  position: relative;
  z-index: 1;
  margin-left: -20px;
  font-size: 72px;
  line-height: 1;
}

.arrow-icon {
  position: absolute;
  z-index: 2;
  right: 18px;
  color: #ffbc2e;
  font-size: 42px;
  font-weight: 700;
}

.logout-content h2 {
  margin: 0;
  color: #111111;
  font-size: 24px;
  font-weight: 700;
}

.logout-content p {
  margin: 24px 0 0;
  color: #888888;
  font-size: 13px;
  line-height: 1.6;
}

.button-area {
  display: grid;
  flex-shrink: 0;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.cancel-button,
.logout-button {
  height: 56px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
}

.cancel-button {
  border: 1px solid #aaaaaa;
  background: #ffffff;
  color: #111111;
}

.logout-button {
  border: 1px solid #cc9200;
  background: #ffbc2e;
  color: #111111;
}

.cancel-button:disabled,
.logout-button:disabled {
  cursor: default;
  opacity: 0.6;
}
</style>