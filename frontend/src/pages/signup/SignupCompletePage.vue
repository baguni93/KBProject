<template>
  <div class="complete-page">
    <main class="complete-container">
      <section class="complete-content">
        <div class="complete-visual">
          <div class="wallet-card">
            <div class="wallet-logo">W</div>
            <div class="wallet-line"></div>
            <div class="wallet-line short"></div>
          </div>

          <div class="check-badge">✓</div>

          <span class="confetti confetti-one"></span>
          <span class="confetti confetti-two"></span>
          <span class="confetti confetti-three"></span>
          <span class="confetti confetti-four"></span>
          <span class="confetti confetti-five"></span>
          <span class="confetti confetti-six"></span>
        </div>

        <h1>회원가입이 완료되었어요!</h1>

        <p class="complete-description">
          이제 계좌를 연결하면<br />
          Social Wallet을 바로 이용할 수 있어요.
        </p>

        <section class="account-guide">
          <div class="guide-icon">₩</div>

          <div class="guide-text">
            <strong>계좌 연결이 필요해요</strong>
            <p>
              송금과 결제 서비스를 이용하려면<br />
              본인 명의 계좌를 연결해 주세요.
            </p>
          </div>
        </section>
      </section>

      <button class="connect-button" type="button" @click="goAccountConnect">
        계좌 연결하기
      </button>
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

// 계좌 연결 화면 이동
const goAccountConnect = () => {
  router.push('/setting/account/connect');
};
</script>

<style scoped>
.complete-page {
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.complete-container {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-height: 0;
  padding: 40px 28px 140px;
  background: #ffffff;
  box-sizing: border-box;
}

.complete-content {
  text-align: center;
}

.complete-visual {
  position: relative;
  width: 180px;
  height: 180px;
  margin: 18px auto 34px;
}

.wallet-card {
  position: absolute;
  top: 42px;
  left: 50%;
  width: 130px;
  height: 94px;
  padding: 20px;
  border-radius: 22px;
  background: linear-gradient(145deg, #ffca49, #ffb212);
  box-shadow: 0 20px 38px rgba(255, 181, 18, 0.26);
  text-align: left;
  transform: translateX(-50%) rotate(-5deg);
  box-sizing: border-box;
}

.wallet-logo {
  display: flex;
  width: 34px;
  height: 34px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #222222;
  color: #ffffff;
  font-size: 16px;
  font-weight: 800;
}

.wallet-line {
  width: 66px;
  height: 7px;
  margin-top: 14px;
  border-radius: 6px;
  background: rgba(34, 34, 34, 0.7);
}

.wallet-line.short {
  width: 42px;
  margin-top: 7px;
  background: rgba(34, 34, 34, 0.4);
}

.check-badge {
  position: absolute;
  right: 12px;
  bottom: 20px;
  display: flex;
  width: 58px;
  height: 58px;
  align-items: center;
  justify-content: center;
  border: 6px solid #ffffff;
  border-radius: 50%;
  background: #222222;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.16);
  color: #ffffff;
  font-size: 28px;
  font-weight: 800;
  box-sizing: border-box;
}

.confetti {
  position: absolute;
  width: 7px;
  height: 18px;
  border-radius: 4px;
}

.confetti-one {
  top: 20px;
  left: 26px;
  background: #ffbc2e;
  transform: rotate(-32deg);
}

.confetti-two {
  top: 12px;
  right: 30px;
  background: #6e8cff;
  transform: rotate(38deg);
}

.confetti-three {
  top: 78px;
  left: 4px;
  background: #63c975;
  transform: rotate(55deg);
}

.confetti-four {
  top: 70px;
  right: 2px;
  background: #f08ebf;
  transform: rotate(-48deg);
}

.confetti-five {
  bottom: 14px;
  left: 30px;
  background: #ff8b65;
  transform: rotate(28deg);
}

.confetti-six {
  right: 34px;
  bottom: 6px;
  background: #ffbc2e;
  transform: rotate(-25deg);
}

.complete-content h1 {
  margin: 0;
  color: #111111;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.8px;
}

.complete-description {
  margin: 18px 0 0;
  color: #777777;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.5;
}

.account-guide {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 42px;
  padding: 20px;
  border: 1px solid #f1e2b5;
  border-radius: 18px;
  background: #fff9e9;
  text-align: left;
}

.guide-icon {
  display: flex;
  flex: none;
  width: 48px;
  height: 48px;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: #ffbc2e;
  color: #ffffff;
  font-size: 23px;
  font-weight: 800;
}

.guide-text {
  min-width: 0;
}

.guide-text strong {
  display: block;
  color: #222222;
  font-size: 15px;
  font-weight: 800;
}

.guide-text p {
  margin: 7px 0 0;
  color: #777777;
  font-size: 12px;
  line-height: 1.55;
}

.connect-button {
  position: absolute;
  right: 28px;
  bottom: 58px;
  left: 28px;
  width: auto;
  height: 58px;
  margin: 0;
  border: 1px solid #cc9200;
  border-radius: 10px;
  background: #ffbc2e;
  color: #111111;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
}

.connect-button:active {
  background: #f2aa10;
}
</style>