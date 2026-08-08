<template>
  <div class="signup-page">
    <!-- 1. 중앙 내용 영역 (기존 스타일 및 정렬 그대로 유지) -->
    <main class="content-area">
      <div class="member-content">
        <div class="member-visual">
          <div class="visual-card">
            <span class="person-head"></span>
            <span class="person-body"></span>

            <span class="plus-badge">+</span>
          </div>

          <span class="confetti confetti-one"></span>
          <span class="confetti confetti-two"></span>
          <span class="confetti confetti-three"></span>
          <span class="confetti confetti-four"></span>
        </div>

        <h1>신규 회원이에요!</h1>

        <p class="member-description">
          간편비밀번호를 설정하고<br />
          Social Wallet을 시작해 보세요.
        </p>

        <section class="guide-list">
          <article class="guide-item">
            <div class="guide-icon">🔒</div>

            <div>
              <strong>안전한 금융 서비스</strong>
              <p>간편비밀번호로 안전하게 이용할 수 있어요.</p>
            </div>
          </article>

          <article class="guide-item">
            <div class="guide-icon">⚡</div>

            <div>
              <strong>빠르고 간편하게</strong>
              <p>복잡한 인증 없이 쉽고 빠르게 이용해요.</p>
            </div>
          </article>

          <article class="guide-item">
            <div class="guide-icon">💳</div>

            <div>
              <strong>소셜 금융을 더 편리하게</strong>
              <p>송금과 결제, 금융 생활을 한곳에서 관리해요.</p>
            </div>
          </article>
        </section>
      </div>
    </main>

    <!-- 2. 하단 버튼 영역 (다른 페이지들과 완벽히 일치하는 위치) -->
    <div class="button-area">
      <button class="next-btn" type="button" @click="goPin">다음</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

// PIN 설정 화면 이동
const goPin = () => {
  router.push('/signup/pin');
};
</script>

<style scoped>
.signup-page {
  width: 100%;
  height: 100vh;
  height: 100dvh;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  /* 다른 회원가입 페이지들과 완전히 동일한 패딩 적용 */
  padding: 36px 24px 70px;
  background: #ffffff;
}

/* 중앙 내용 영역 (내부 스크롤 가능하며 기존 디자인 보존) */
.content-area {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  box-sizing: border-box;
  padding-right: 2px;
}

.member-content {
  text-align: center;
}

.member-visual {
  position: relative;
  width: 150px;
  height: 150px;
  margin: 18px auto 32px;
}

.visual-card {
  position: absolute;
  top: 28px;
  left: 50%;
  width: 112px;
  height: 104px;
  border-radius: 28px;
  background: linear-gradient(145deg, #fff4cf, #ffe59a);
  box-shadow: 0 18px 34px rgba(255, 188, 46, 0.22);
  transform: translateX(-50%);
}

.person-head {
  position: absolute;
  top: 25px;
  left: 50%;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #555555;
  transform: translateX(-50%);
}

.person-body {
  position: absolute;
  bottom: 17px;
  left: 50%;
  width: 58px;
  height: 36px;
  border-radius: 30px 30px 14px 14px;
  background: #333333;
  transform: translateX(-50%);
}

.plus-badge {
  position: absolute;
  top: -14px;
  right: -14px;
  display: flex;
  width: 44px;
  height: 44px;
  align-items: center;
  justify-content: center;
  border: 5px solid #ffffff;
  border-radius: 50%;
  background: #ffbc2e;
  color: #ffffff;
  font-size: 30px;
  font-weight: 700;
  line-height: 1;
  box-sizing: border-box;
}

.confetti {
  position: absolute;
  width: 8px;
  height: 18px;
  border-radius: 4px;
}

.confetti-one {
  top: 18px;
  left: 18px;
  background: #ffbc2e;
  transform: rotate(-35deg);
}

.confetti-two {
  top: 12px;
  right: 22px;
  background: #6c8cff;
  transform: rotate(42deg);
}

.confetti-three {
  bottom: 16px;
  left: 12px;
  background: #63ca72;
  transform: rotate(30deg);
}

.confetti-four {
  right: 10px;
  bottom: 24px;
  background: #ff8a65;
  transform: rotate(-28deg);
}

.member-content h1 {
  margin: 0;
  color: #111111;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.7px;
}

.member-description {
  margin: 18px 0 0;
  color: #777777;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.5;
}

.guide-list {
  margin-top: 42px;
  padding: 6px 18px;
  border-radius: 20px;
  background: #fafafa;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.05);
  text-align: left;
}

.guide-item {
  display: flex;
  min-height: 86px;
  align-items: center;
  gap: 14px;
  border-bottom: 1px solid #eeeeee;
}

.guide-item:last-child {
  border-bottom: 0;
}

.guide-icon {
  display: flex;
  flex: none;
  width: 42px;
  height: 42px;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: #fff2c9;
  color: #bc7d00;
  font-size: 17px;
  font-weight: 800;
}

.guide-item strong {
  display: block;
  color: #222222;
  font-size: 15px;
  font-weight: 700;
}

.guide-item p {
  margin: 5px 0 0;
  color: #888888;
  font-size: 12px;
  line-height: 1.4;
}

/* 하단 버튼 영역 (다른 페이지들과 동일한 규격) */
.button-area {
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 16px;
  background: #ffffff;
}

.next-btn {
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 14px;
  background: #ffbc2e;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.next-btn:active {
  background: #f3aa0b;
}
</style>
