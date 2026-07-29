<template>
  <div class="kb-container py-3">
    <!-- 메인 배너 -->
    <div class="p-4 mb-4 bg-dark text-white rounded-4 shadow-sm position-relative overflow-hidden">
      <div class="position-relative z-1">
        <span class="badge bg-warning text-dark px-3 py-1.5 rounded-pill fw-bold mb-2">KB Pay</span>
        <h3 class="fw-bold mb-0">KB Pay 메인</h3>
      </div>
    </div>

    <!-- 메뉴 카드 -->
    <div class="row g-3 mb-4">
      <div class="col-md-6">
        <router-link to="/wallet" class="text-decoration-none">
          <div class="card border-0 shadow-sm rounded-4 p-4 h-100 kb-hover-card">
            <div class="d-flex align-items-center mb-3">
              <div class="icon-circle bg-warning text-dark me-3 rounded-circle d-flex align-items-center justify-content-center">
                <i class="bi bi-wallet2 fs-4"></i>
              </div>
              <div>
                <h5 class="fw-bold text-dark mb-0">KB Pay 지갑</h5>
              </div>
            </div>
            <div class="d-flex justify-content-between align-items-center mt-3 text-warning fw-bold small">
              <span>지갑 관리하기</span>
              <i class="bi bi-arrow-right"></i>
            </div>
          </div>
        </router-link>
      </div>

      <div class="col-md-6">
        <router-link to="/remittance" class="text-decoration-none">
          <div class="card border-0 shadow-sm rounded-4 p-4 h-100 kb-hover-card">
            <div class="d-flex align-items-center mb-3">
              <div class="icon-circle bg-dark text-warning me-3 rounded-circle d-flex align-items-center justify-content-center">
                <i class="bi bi-send-fill fs-4"></i>
              </div>
              <div>
                <h5 class="fw-bold text-dark mb-0">송금하기</h5>
              </div>
            </div>
            <div class="d-flex justify-content-between align-items-center mt-3 text-dark fw-bold small">
              <span>송금 바로가기</span>
              <i class="bi bi-arrow-right"></i>
            </div>
          </div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup></script>

<style scoped>
.kb-container {
  max-width: 600px;
  margin: 0 auto;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, sans-serif;
}
.icon-circle {
  width: 48px;
  height: 48px;
}
.kb-hover-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.kb-hover-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.08) !important;
}
</style>
