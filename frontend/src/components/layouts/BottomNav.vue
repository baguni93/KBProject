<template>
  <nav class="bottom-nav-bar fixed-bottom shadow-lg">
    <div class="bottom-nav-container d-flex justify-content-around align-items-center">
      <!-- 1. 피드 -->
      <router-link to="/feed/public" class="nav-item-btn d-flex flex-column align-items-center" active-class="active">
        <i class="bi bi-house-door-fill fs-5"></i>
        <span class="nav-label">피드</span>
      </router-link>

      <!-- 2. 송금 -->
      <router-link to="/remittance" class="nav-item-btn d-flex flex-column align-items-center" active-class="active">
        <i class="bi bi-send-fill fs-5"></i>
        <span class="nav-label">송금</span>
      </router-link>

      <!-- 3. 결제 (중앙 메인 시그니처 탭) -->
      <router-link to="/wallet" class="nav-item-btn main-pay-btn d-flex flex-column align-items-center justify-content-center" active-class="active">
        <div class="pay-btn-circle d-flex align-items-center justify-content-center shadow-md">
          <i class="bi bi-qr-code-scan fs-4 text-dark"></i>
        </div>
        <span class="nav-label font-outfit fw-bold mt-1 text-dark">결제</span>
      </router-link>

      <!-- 4. 금융 -->
      <router-link to="/wallet" class="nav-item-btn d-flex flex-column align-items-center" active-class="active-sub">
        <i class="bi bi-graph-up-arrow fs-5"></i>
        <span class="nav-label">금융</span>
      </router-link>

      <!-- 5. 마이 -->
      <router-link to="/auth/profile" class="nav-item-btn d-flex flex-column align-items-center" active-class="active">
        <i class="bi bi-person-fill fs-5"></i>
        <span class="nav-label">마이</span>
      </router-link>
    </div>
  </nav>
</template>

<script setup>
</script>

<style scoped>
.bottom-nav-bar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  padding: 8px 0 10px;
  z-index: 1030;
}

.bottom-nav-container {
  max-width: 480px;
  margin: 0 auto;
  position: relative;
}

.nav-item-btn {
  color: #94A3B8;
  text-decoration: none;
  font-size: 0.72rem;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  width: 60px;
}

.nav-item-btn:hover {
  color: #1E1E24;
}

.nav-item-btn.active {
  color: #1E1E24 !important;
  font-weight: 800;
}

.nav-item-btn.active i {
  color: #FFBC00 !important;
  transform: translateY(-2px);
}

.main-pay-btn {
  margin-top: -24px;
}

.pay-btn-circle {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #FFBC00 0%, #FF9900 100%);
  border-radius: 50%;
  border: 3px solid #ffffff;
  box-shadow: 0 8px 20px rgba(255, 188, 0, 0.4) !important;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.main-pay-btn:hover .pay-btn-circle {
  transform: scale(1.08);
  box-shadow: 0 12px 28px rgba(255, 188, 0, 0.5) !important;
}

.nav-label {
  letter-spacing: -0.2px;
}
</style>
