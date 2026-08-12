<template>
  <div class="page-layout page-layout-top complete-page">
    <!-- 배경 장식 -->
    <div class="background-decoration decoration-left"></div>
    <div class="background-decoration decoration-right"></div>

    <main class="page-content content-area">
      <section class="complete-content">
        <!-- 완료 비주얼 -->
        <div class="complete-visual">
          <div class="circle-background"></div>

          <div class="check-circle">
            <i class="fa-solid fa-check"></i>
          </div>

          <!-- 작은 장식 -->
          <span class="deco deco-one"></span>
          <span class="deco deco-two"></span>
          <span class="deco deco-three"></span>
          <span class="deco deco-four"></span>

          <i class="fa-solid fa-star sparkle sparkle-one"></i>
          <i class="fa-solid fa-star sparkle sparkle-two"></i>
        </div>

        <!-- 완료 메시지 -->
        <div class="complete-message">
          <h1 class="text-30-bold">
            회원가입이<br />
            완료되었어요!
          </h1>

          <p class="text-15">
            이제 계좌를 연결하면<br />
            Social Wallet을 이용할 수 있어요.
          </p>
        </div>

        <!-- 다음 단계 안내 -->
        <div class="guide-card">
          <div class="guide-icon">
            <i class="fa-solid fa-building-columns"></i>
          </div>

          <div class="guide-text">
            <strong class="text-15-bold">
              다음은 계좌 연결이에요
            </strong>

            <p class="text-13">
              본인 명의 계좌를 연결해 주세요.
            </p>
          </div>
        </div>
      </section>
    </main>

    <!-- 하단 버튼 -->
    <div class="bottom-btn-area single">
      <button class="bottom-btn" type="button" @click="goAccountConnect">
        계좌 연결하기
      </button>
    </div>
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
@import "@/components/common/common/common.css";
@import "@/components/common/common/layout.css";

.complete-page {
  position: relative;
  overflow: hidden;
  background: linear-gradient(180deg, #fffdf8 0%, #ffffff 42%, #ffffff 100%);
}

.content-area {
  position: relative;
  z-index: 2;
  min-height: 0;
  overflow-y: auto;
  padding-right: 2px;
}

.complete-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 54px;
  text-align: center;
}

/* 배경 장식 */
.background-decoration {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.decoration-left {
  top: -90px;
  left: -110px;
  width: 220px;
  height: 220px;
  background: rgba(255, 205, 91, 0.13);
}

.decoration-right {
  top: 240px;
  right: -110px;
  width: 200px;
  height: 200px;
  background: rgba(176, 164, 255, 0.06);
}

/* 완료 비주얼 */
.complete-visual {
  position: relative;
  width: 138px;
  height: 138px;
  margin-bottom: 28px;
}

.circle-background {
  position: absolute;
  inset: 4px;
  border-radius: 50%;
  background: linear-gradient(
      135deg,
      rgba(255, 188, 46, 0.17),
      rgba(255, 225, 155, 0.07)
  );
}

.check-circle {
  position: absolute;
  top: 24px;
  left: 24px;
  z-index: 2;
  display: flex;
  width: 90px;
  height: 90px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(
      135deg,
      #ffca52 0%,
      var(--color-primary) 65%,
      #f3a711 100%
  );
  box-shadow: 0 14px 30px rgba(255, 188, 46, 0.26);
  color: #ffffff;
}

.check-circle i {
  font-size: 34px;
}

/* 작은 장식 */
.deco {
  position: absolute;
  display: block;
  border-radius: 50%;
}

.deco-one {
  top: 14px;
  right: 10px;
  width: 9px;
  height: 9px;
  background: #ffca52;
}

.deco-two {
  top: 42px;
  left: 3px;
  width: 7px;
  height: 7px;
  background: #9d90ff;
}

.deco-three {
  right: 4px;
  bottom: 27px;
  width: 7px;
  height: 7px;
  background: #7bd6c7;
}

.deco-four {
  bottom: 9px;
  left: 25px;
  width: 8px;
  height: 8px;
  background: #ff9eaa;
}

.sparkle {
  position: absolute;
  color: var(--color-primary);
}

.sparkle-one {
  top: 2px;
  left: 31px;
  font-size: 11px;
  transform: rotate(15deg);
}

.sparkle-two {
  right: 15px;
  bottom: 6px;
  color: #a99df7;
  font-size: 9px;
  transform: rotate(-15deg);
}

/* 완료 메시지 */
.complete-message h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.3;
  letter-spacing: -0.6px;
}

.complete-message p {
  margin: 16px 0 0;
  color: var(--color-text-sub);
  line-height: 1.65;
}

/* 다음 단계 카드 */
.guide-card {
  display: flex;
  width: 100%;
  align-items: center;
  margin-top: 36px;
  padding: 18px;
  border: 1px solid rgba(255, 188, 46, 0.15);
  border-radius: 18px;
  background: linear-gradient(110deg, #fff9ec 0%, #fffdf8 70%, #faf8ff 100%);
  box-sizing: border-box;
  text-align: left;
}

.guide-icon {
  display: flex;
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(255, 188, 46, 0.12);
  color: #d9980d;
  font-size: 17px;
}

.guide-text {
  min-width: 0;
}

.guide-text strong {
  display: block;
  color: var(--color-text-main);
}

.guide-text p {
  margin: 5px 0 0;
  color: var(--color-text-sub);
  line-height: 1.45;
}
</style>