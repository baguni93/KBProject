<template>
  <div class="card" :class="{ completed: isSettlementComplete }">
    <!-- 프로필 -->
    <CardProfile
      :user-id="settlement.requesterId"
      :profile-image-name="settlement.profileSimpleVO?.profileImageName"
      :nickname="settlement.profileSimpleVO?.nickname"
      :created-at="settlement.createdAt"
      :show-visibility="false"
    />

    <!-- 정산 요청/완료 상태 뱃지 -->
    <div class="request-type" :class="{ completed: isSettlementComplete }">
      {{ requestTypeText }}
    </div>

    <!-- 정산 제목 및 내용 -->
    <div class="content text-16-bold" v-if="settlement.title">
      {{ settlement.title }}
    </div>
    <div class="content" v-if="settlement.content && settlement.content !== settlement.title" style="color: #64748b; font-size: 14px; margin-top: 4px;">
      {{ settlement.content }}
    </div>

    <!-- 정산 완료 안내 -->
    <div v-if="isSettlementComplete" class="complete-badge">
      🎉 모든 참여자의 정산이 완료되었습니다.
    </div>

    <!-- 정보 -->
    <div class="summary">
      <div class="summary-card">
        <div class="label">총 정산금액</div>
        <div class="value">{{ settlement.totalAmount.toLocaleString() }}원</div>
      </div>

      <div class="summary-card">
        <div class="label">
          {{ settlement.requesterId === userId ? '참여인원' : '내가 낼 금액' }}
        </div>

        <div class="value">
          {{
            settlement.requesterId === userId
              ? `${settlement.members.length + 1}명`
              : `${settlement.members
                  .find((member) => member.userId === userId)
                  ?.amount.toLocaleString()}원`
          }}
        </div>
      </div>

      <div class="summary-card progress-card">
        <div class="label">진행 상황</div>

        <div class="value">
          {{ completeMembers.length + 1 }}/{{ settlement.members.length + 1 }}
        </div>

        <div class="progress">
          <div
            class="progress-bar"
            :class="{ completed: isSettlementComplete }"
            :style="{
              width: `${((completeMembers.length + 1) / (settlement.members.length + 1)) * 100}%`,
            }"
          ></div>
        </div>
      </div>
    </div>

    <!-- 참여자 -->
    <div class="member-list">
      <SettlementMember
        :user-id="settlement.requesterId"
        :profile-image-name="settlement.profileSimpleVO.profileImageName"
        :nickname="settlement.profileSimpleVO.nickname"
        status="COMPLETE"
      />

      <SettlementMember
        v-for="member in settlement.members"
        :key="member.userId"
        :user-id="member.userId"
        :profile-image-name="member.receiver.profileImageName"
        :nickname="member.receiver.nickname"
        :status="member.status"
      />
    </div>

    <!-- 버튼 -->
    <div class="button-group">
      <!-- 정산 완료 -->
      <template v-if="isSettlementComplete">
        <button class="btn complete-btn" disabled>✔ 정산 완료</button>
      </template>

      <!-- 요청자 -->
      <template v-else-if="settlement.requesterId === userId">
        <button class="btn secondary" @click="onclickRemine">
          리마인드 알림
        </button>

        <button class="btn danger" @click="onclickCancel">요청 취소</button>
      </template>

      <!-- 참여자 -->
      <button
        v-else
        class="btn"
        :class="isPaymentComplete ? 'disabled' : 'primary'"
        :disabled="isPaymentComplete"
        @click="!isPaymentComplete && onclickPayment()"
      >
        {{ isPaymentComplete ? '지불 완료' : '지불하기' }}
      </button>
    </div>
  </div>
</template>
<script setup>
import { computed } from 'vue';
import CardProfile from '../common/CardProfile.vue';
import SettlementMember from './SettlementMember.vue';
import { useSettlementStore } from '@/stores/settlement';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth.js';
const authStore = useAuthStore();
const userId = authStore.userId;

const router = useRouter();
const settlementStore = useSettlementStore();

const props = defineProps({
  settlement: {
    type: Object,
    required: true,
  },
});

// 정산 완료 여부
const isSettlementComplete = computed(
  () => props.settlement.status === 'COMPLETE',
);

// 상단 제목
const requestTypeText = computed(() => {
  if (isSettlementComplete.value) {
    return '정산 완료';
  }

  return props.settlement.requesterId === userId
    ? '정산 요청'
    : '정산 요청 받음';
});

// 내가 결제했는지
const isPaymentComplete = computed(() => {
  const member = props.settlement.members.find(
    (member) => member.userId === userId,
  );

  return member?.status === 'COMPLETE';
});

// 완료한 멤버
const completeMembers = computed(() =>
  props.settlement.members.filter((member) => member.status === 'COMPLETE'),
);

// 리마인드
const onclickRemine = async () => {
  try {
    await settlementStore.remine({
      settlementId: props.settlement.settlementId,
      userId,
    });
  } catch (e) {
    console.log(e.response?.data);
  }
};

// 취소
const onclickCancel = async () => {
  try {
    await settlementStore.cancel({
      settlementId: props.settlement.settlementId,
      userId,
    });
  } catch (e) {
    console.log(e.response?.data);
  }
};

// 결제
const onclickPayment = async () => {
  router.push({
    name: 'settlement/payment/:settlementId',
    params: {
      settlementId: props.settlement.settlementId,
    },
  });
};
</script>
<style scoped>
.card {
  width: 100%;

  padding: 16px;

  border-radius: 18px;

  background: #fff;

  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);

  position: relative;
}

/* 완료 카드 */
.card.completed {
  background: #f8fff9;
  border: 1px solid #d9f7df;
}

/* 제목 */
.request-type {
  margin-top: 12px;

  font-size: 14px;

  font-weight: 700;

  color: #ff8a00;
}

.request-type.completed {
  color: #16a34a;
}

/* 완료 메시지 */
.complete-badge {
  margin: 12px 0;

  padding: 10px;

  border-radius: 12px;

  background: #f0fdf4;

  color: #15803d;

  font-size: 13px;

  font-weight: 700;

  text-align: center;
}

/* 내용 */
.content {
  margin: 10px 0 14px;

  font-size: 16px;

  font-weight: 700;

  line-height: 1.4;
}

/* 금액 영역 */
.summary {
  display: flex;

  gap: 8px;

  margin-bottom: 14px;
}

.summary-card {
  flex: 1;

  padding: 10px;

  border-radius: 12px;

  background: #f8f8f8;
}

.label {
  font-size: 11px;

  color: #999;

  margin-bottom: 4px;
}

.value {
  font-size: 14px;

  font-weight: 700;

  color: #222;
}

/* 진행 */
.progress-card {
  display: flex;

  flex-direction: column;
}

.progress {
  margin-top: 6px;

  width: 100%;

  height: 5px;

  background: #eaeaea;

  border-radius: 999px;

  overflow: hidden;
}

.progress-bar {
  height: 100%;

  background: #ffb020;

  border-radius: 999px;

  transition: 0.3s;
}

.progress-bar.completed {
  background: #22c55e;
}

/* 참여자 */
.member-list {
  display: flex;

  align-items: center;

  margin-bottom: 16px;
}

/* 버튼 */
.button-group {
  display: flex;

  gap: 8px;
}

.btn {
  flex: 1;

  height: 38px;

  border-radius: 12px;

  font-size: 13px;

  font-weight: 700;

  cursor: pointer;
}

.primary {
  background: #ffd54f;

  border: none;
}

.primary:hover {
  background: #ffca28;
}

.secondary {
  background: #fff;

  border: 1px solid #ddd;
}

.danger {
  background: #fff;

  color: #ef4444;

  border: 1px solid #fecaca;
}

.disabled {
  background: #eee;

  color: #999;

  border: none;
}

.complete-btn {
  width: 100%;

  background: #22c55e;

  color: white;

  border: none;
}
</style>
