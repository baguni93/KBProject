<template>
  <div class="terms-page">
    <!-- 1. 상단 헤더 영역 -->
    <PageHeader title="개인정보 동의" />

    <!-- 2. 중앙 내용 영역 -->
    <main class="content-area">
      <!-- 전체 동의하기 박스 -->
      <div class="all-agree-box" @click="toggleAll">
        <span class="checkbox" :class="{ checked: isAllChecked }">
          <svg
            v-if="isAllChecked"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="3"
          >
            <polyline points="20 6 9 17 4 12" />
          </svg>
        </span>
        <span class="all-label">전체 동의하기</span>
      </div>

      <!-- 개별 약관 리스트 -->
      <ul class="terms-list">
        <li
          v-for="term in customCardStore.agreements"
          :key="term.agreementId"
          class="term-item"
        >
          <div class="term-left" @click="toggleTerm(term.agreementId)">
            <span class="checkbox" :class="{ checked: term.checked }">
              <svg
                v-if="term.checked"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="3"
              >
                <polyline points="20 6 9 17 4 12" />
              </svg>
            </span>
            <div class="term-texts">
              <p class="term-title">
                {{ term.agreementName }}
                <span
                  :class="(term.required ?? true) ? 'required' : 'optional'"
                >
                  ({{ (term.required ?? true) ? '필수' : '선택' }})
                </span>
              </p>
            </div>
          </div>
          <button
            class="detail-btn"
            type="button"
            @click.stop="openDetail(term)"
          >
            &gt;
          </button>
        </li>
      </ul>

      <!-- 안내 박스 -->
      <div class="notice-box">
        <p>
          수집된 개인정보는 카드 발급 및 서비스 제공 목적으로만 사용되며, 법령에
          따라 안전하게 보호됩니다.
        </p>
      </div>
    </main>

    <!-- 3. 하단 버튼 영역 -->
    <div class="bottom-btn-area single">
      <button
        class="bottom-btn"
        :disabled="!isFormValid"
        type="button"
        @click="submitAgreement"
      >
        동의하고 시작하기
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useCustomCardStore } from '@/stores/customcard';
import { useAuthStore } from '@/stores/auth.js';
import PageHeader from '@/components/common/PageHeader.vue';
const authStore = useAuthStore();
const userId = authStore.userId ?? 1;

const router = useRouter();
const customCardStore = useCustomCardStore();

// 전체 동의 상태 계산
const isAllChecked = computed(() => {
  return (
    customCardStore.agreements.length > 0 &&
    customCardStore.agreements.every((term) => term.checked)
  );
});

// 필수 항목이 모두 체크되었는지 확인 (버튼 활성화 조건)
const isFormValid = computed(() => {
  return customCardStore.agreements
    .filter((term) => term.required ?? true)
    .every((term) => term.checked);
});

// 전체 동의 토글
const toggleAll = () => {
  const nextState = !isAllChecked.value;
  customCardStore.agreements.forEach((term) => {
    term.checked = nextState;
  });
};

// 개별 동의 토글
const toggleTerm = (agreementId) => {
  const target = customCardStore.agreements.find(
    (t) => t.agreementId === agreementId,
  );
  if (target) {
    target.checked = !target.checked;
  }
};

// 상세 약관 보기
const openDetail = (term) => {
  router.push(`/card/detail/${term.agreementId}`);
};

// 이전 화면으로 이동
const goBack = () => {
  router.back();
};

// 동의 완료 후 진행
const submitAgreement = async () => {
  if (!isFormValid.value) return;

  if (!customCardStore.isAgreementAgree) {
    await customCardStore.setAgreementAgree(userId);
  }

  router.push('/card/create');
};
</script>

<style scoped>
.terms-page {
  width: 100%;
  /* 모바일 브라우저 주소창 이슈를 해결하기 위해 dvh 사용 */
  height: 100dvh;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  padding: 12px;
}

/* 상단 헤더 영역 */
.terms-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  position: relative;
  margin-bottom: 24px;
}

.back-button {
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 26px;
  line-height: 1;
  cursor: pointer;
  margin-right: 16px;
}

.terms-header h1 {
  margin: 0;
  color: #111111;
  font-size: 22px;
  font-weight: 700;
}

/* 중앙 내용 영역 */
.content-area {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  box-sizing: border-box;
  padding: 2px;
}

/* 전체 동의 박스 */
.all-agree-box {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background-color: #ffffff;
  cursor: pointer;
  margin-bottom: 24px;
}

.all-label {
  font-size: 16px;
  font-weight: 700;
  color: #111111;
  margin-left: 12px;
}

/* 체크박스 스타일 */
.checkbox {
  width: 24px;
  height: 24px;
  border: 2px solid #d1d5db;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  flex-shrink: 0;
  transition: all 0.2s ease;
}

.checkbox.checked {
  background-color: #ffbc2e;
  border-color: #ffbc2e;
}

.checkbox svg {
  width: 14px;
  height: 14px;
  stroke: #ffffff;
}

/* 약관 리스트 */
.terms-list {
  list-style: none;
  padding: 0;
  margin: 0 0 24px 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.term-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
}

.term-left {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
}

.term-texts {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.term-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #222222;
}

.term-title .required {
  color: #f59e0b;
  font-weight: 600;
}

.term-title .optional {
  color: #9ca3af;
  font-weight: 500;
}

.detail-btn {
  background: transparent;
  border: none;
  font-size: 18px;
  color: #9ca3af;
  cursor: pointer;
  padding: 4px 8px;
}

/* 안내 박스 */
.notice-box {
  background-color: #f3f4f6;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 24px;
}

.notice-box p {
  margin: 0;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.5;
}
</style>
