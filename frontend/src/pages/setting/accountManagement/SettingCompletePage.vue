<template>
  <div class="complete-page">
    <main class="complete-container">
      <section class="complete-content">
        <div class="complete-visual">
          <div class="check-circle">✓</div>

          <span class="confetti confetti-one"></span>
          <span class="confetti confetti-two"></span>
          <span class="confetti confetti-three"></span>
          <span class="confetti confetti-four"></span>
          <span class="confetti confetti-five"></span>
          <span class="confetti confetti-six"></span>
        </div>

        <h1 v-html="completeInfo.title"></h1>

        <p v-html="completeInfo.description"></p>

        <section class="guide-area">
          <div class="guide-icon">
            {{ completeInfo.icon }}
          </div>

          <div>
            <strong>{{ completeInfo.guideTitle }}</strong>
            <p>{{ completeInfo.guideDescription }}</p>
          </div>
        </section>
      </section>

      <button class="confirm-button" type="button" @click="complete">
        {{ completeInfo.buttonText }}
      </button>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

// 완료 유형
const completeType = computed(() => String(route.query.type || 'PIN_CHANGE'));

// 완료 화면 정보
const completeInfo = computed(() => {
  const completeData = {
    NAME_CHANGE: {
      title: '이름이<br />변경되었어요!',
      description: '변경된 이름으로<br />서비스를 이용할 수 있어요.',
      icon: '👤',
      guideTitle: '새로운 이름이 적용되었어요',
      guideDescription: '마이페이지에서 변경된 이름을 확인할 수 있어요.',
      buttonText: '확인',
      path: '/setting/account-management',
    },
    PHONE_CHANGE: {
      title: '휴대폰 번호가<br />변경되었어요!',
      description: '새로운 휴대폰 번호로<br />로그인과 본인인증을 진행해 주세요.',
      icon: '📱',
      guideTitle: '새 번호가 적용되었어요',
      guideDescription: '다음 로그인부터 새로운 번호를 사용해 주세요.',
      buttonText: '확인',
      path: '/setting/account-management',
    },
    PIN_CHANGE: {
      title: '간편비밀번호가<br />변경되었어요!',
      description: '새로운 간편비밀번호로<br />안전하게 서비스를 이용해 주세요.',
      icon: '🔒',
      guideTitle: '새로운 PIN이 적용되었어요',
      guideDescription: '이전 간편비밀번호는 더 이상 사용할 수 없어요.',
      buttonText: '확인',
      path: '/setting/account-management',
    },
    WITHDRAW: {
      title: '회원탈퇴가<br />완료되었어요',
      description: '그동안 서비스를<br />이용해 주셔서 감사합니다.',
      icon: '👋',
      guideTitle: '계정 이용이 종료되었어요',
      guideDescription: '탈퇴 후 정책에 따라 일정 시간 동안 재가입이 제한될 수 있어요.',
      buttonText: '확인',
      path: '/intro',
    },
  };

  return completeData[completeType.value] || completeData.PIN_CHANGE;
});

// 완료 후 이동
const complete = async () => {
  await router.replace(completeInfo.value.path);
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
  width: 100%;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  padding: 10px 28px 140px;
  background: #ffffff;
  box-sizing: border-box;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.complete-container::-webkit-scrollbar {
  display: none;
}

.complete-content {
  padding-top: 38px;
  text-align: center;
}

.complete-visual {
  position: relative;
  width: 170px;
  height: 170px;
  margin: 0 auto 36px;
}

.check-circle {
  position: absolute;
  top: 34px;
  left: 50%;
  display: flex;
  width: 104px;
  height: 104px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #ffbc2e;
  box-shadow: 0 20px 38px rgba(255, 188, 46, 0.28);
  color: #ffffff;
  font-size: 54px;
  font-weight: 500;
  transform: translateX(-50%);
}

.confetti {
  position: absolute;
  width: 7px;
  height: 18px;
  border-radius: 4px;
}

.confetti-one {
  top: 16px;
  left: 22px;
  background: #ffbc2e;
  transform: rotate(-35deg);
}

.confetti-two {
  top: 8px;
  right: 28px;
  background: #6e8cff;
  transform: rotate(38deg);
}

.confetti-three {
  top: 76px;
  left: 2px;
  background: #67ca76;
  transform: rotate(54deg);
}

.confetti-four {
  top: 72px;
  right: 0;
  background: #f08ebf;
  transform: rotate(-48deg);
}

.confetti-five {
  bottom: 12px;
  left: 27px;
  background: #ff8a65;
  transform: rotate(26deg);
}

.confetti-six {
  right: 30px;
  bottom: 8px;
  background: #ffbc2e;
  transform: rotate(-26deg);
}

.complete-content h1 {
  margin: 0;
  color: #111111;
  font-size: 25px;
  font-weight: 800;
  line-height: 1.4;
  letter-spacing: -0.7px;
}

.complete-content > p {
  margin: 16px 0 0;
  color: #777777;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.6;
}

.guide-area {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 42px;
  padding: 16px;
  border-radius: 14px;
  background: #fff9e9;
  text-align: left;
}

.guide-icon {
  display: flex;
  flex: none;
  width: 46px;
  height: 46px;
  align-items: center;
  justify-content: center;
  border-radius: 15px;
  background: #ffbc2e;
  font-size: 20px;
}

.guide-area > div:last-child {
  min-width: 0;
}

.guide-area strong {
  display: block;
  color: #222222;
  font-size: 14px;
  font-weight: 800;
}

.guide-area p {
  margin: 6px 0 0;
  color: #888888;
  font-size: 11px;
  line-height: 1.5;
  word-break: keep-all;
}

.confirm-button {
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

.confirm-button:active {
  background: #f2aa10;
}

@media (max-width: 360px) {
  .complete-container {
    padding-right: 20px;
    padding-left: 20px;
  }

  .confirm-button {
    right: 20px;
    left: 20px;
  }
}
</style>