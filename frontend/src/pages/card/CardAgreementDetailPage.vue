<template>
  <div class="page">
    <!-- 1. 상단 헤더 영역 -->
    <header class="header-area">
      <PageHeader
        :title="
          loading ? '불러오는 중...' : agreement.agreementName || '약관 상세'
        "
        :custom-back="true"
        @back="goBack"
      />
      <div v-if="!loading && !errorMessage" class="badge-wrapper">
        <span
          :class="[
            'agreement-type',
            (agreement.required ?? true) ? 'required' : 'optional',
          ]"
        >
          {{ (agreement.required ?? true) ? '필수 약관' : '선택 약관' }}
        </span>
      </div>
    </header>

    <!-- 2. 중앙 내용 영역 -->
    <main class="content-area">
      <section v-if="loading" class="status-message">
        약관을 불러오는 중입니다.
      </section>

      <section v-else-if="errorMessage" class="status-message error-message">
        {{ errorMessage }}
      </section>

      <template v-else>
        <div class="agreement-scroll">
          {{ agreement.agreementContent }}
        </div>

        <label class="consent-label">
          <input
            :checked="isAgreed"
            type="checkbox"
            @change="changeAgreement"
          />
          <span class="check-box"></span>
          <span>위 약관에 동의합니다.</span>
        </label>
      </template>
    </main>

    <!-- 3. 하단 버튼 영역 -->
    <div class="button-area">
      <button class="next-btn" type="button" @click="goBack">확인</button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCustomCardStore } from '@/stores/customcard';
import PageHeader from '@/components/common/PageHeader.vue';

const route = useRoute();
const router = useRouter();
const customCardStore = useCustomCardStore();

const agreement = ref({});
const loading = ref(false);
const errorMessage = ref('');

// 현재 라우트의 agreementId를 기반으로 스토어에서 동의 여부 확인
const isAgreed = computed(() => {
  const targetId = Number(route.params.agreementId);
  const item = customCardStore.agreements.find(
    (term) => term.agreementId === targetId,
  );
  return item?.checked ?? false;
});

// 약관 상세 조회 (스토어에 이미 데이터가 있다면 활용하거나 API 호출)
const loadAgreement = async () => {
  try {
    loading.value = true;
    const targetId = Number(route.params.agreementId);

    // 스토어에 해당 약관이 이미 존재하면 바로 가져오기
    const found = customCardStore.agreements.find(
      (term) => term.agreementId === targetId,
    );

    if (found) {
      agreement.value = found;
    } else {
      // 필요시 API 호출 로직 추가 (현재는 스토어 데이터 기준으로 처리)
      errorMessage.value = '약관 정보를 찾을 수 없습니다.';
    }
  } catch (error) {
    console.error(error);
    errorMessage.value = '약관 내용을 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

// 약관 동의 상태 변경
const changeAgreement = (event) => {
  const targetId = Number(route.params.agreementId);
  const target = customCardStore.agreements.find(
    (term) => term.agreementId === targetId,
  );
  if (target) {
    target.checked = event.target.checked;
  }
};

// 이전 화면으로 이동
const goBack = () => {
  router.back();
};

onMounted(loadAgreement);
</script>

<style scoped>
.page {
  width: 100%;
  /* 모바일 브라우저 주소창 이슈를 해결하기 위해 dvh 사용 */
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  padding: 0 20px 30px;
}

.header-area {
  flex-shrink: 0;
}

.badge-wrapper {
  margin-top: 8px;
  padding: 0 4px;
}

.agreement-type {
  display: inline-block;
  font-size: 14px;
  font-weight: 600;
}

.required {
  color: #ef3d3d;
}

.optional {
  color: #777777;
}

/* 중앙 콘텐츠 영역 */
.content-area {
  flex: 1;
  min-height: 0;
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.agreement-scroll {
  flex: 1;
  min-height: 0;
  padding: 16px;
  border: 1px solid #dddddd;
  border-radius: 12px;
  color: #333333;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap; /* 이 속성이 줄바꿈(\n)을 화면에 반영해 줍니다! */
  overflow-y: auto;
  box-sizing: border-box;
}

.consent-label {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  min-height: 52px;
  margin-top: 12px;
  padding: 0 4px;
  cursor: pointer;
}

.consent-label input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
}

.check-box {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  margin-right: 12px;
  border: 1px solid #999999;
  border-radius: 6px;
  background: #ffffff;
  box-sizing: border-box;
}

.consent-label input:checked + .check-box {
  border-color: #ffbc2e;
  background: #ffbc2e;
}

.consent-label input:checked + .check-box::after {
  display: block;
  width: 6px;
  height: 12px;
  margin: 3px 0 0 8px;
  border: solid #ffffff;
  border-width: 0 2px 2px 0;
  content: '';
  transform: rotate(45deg);
}

.consent-label span:last-child {
  color: #222222;
  font-size: 15px;
  font-weight: 600;
}

/* 하단 버튼 영역 */
.button-area {
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px 0 8px;
  background: #ffffff;
}

.next-btn {
  width: 85%;
  height: 46px;
  border: none;
  border-radius: 14px;
  background: #ffc400;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.next-btn:active {
  background: #f3aa0b;
}

.status-message {
  padding-top: 100px;
  color: #777777;
  text-align: center;
}

.error-message {
  color: #d32f2f;
}
</style>
