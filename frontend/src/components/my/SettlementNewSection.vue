<template>
  <div class="settlement-dashboard">
    <!-- 1. 상단: 전체 정산 요약 카드 -->
    <div class="payment-card summary-card" :class="{ cancelled: isCancelled }">
      <!-- 헤더 -->
      <div
        class="card-header"
        @click="goToProfile(props.settlement.requesterId)"
      >
        <img
          :src="sender.profileImage"
          alt="프로필 이미지"
          class="sender-avatar"
        />

        <div class="header-text">
          <p class="main-text">
            <!-- 취소 -->
            <template v-if="isCancelled">
              <span class="bold">{{ sender.name }}</span>
              님의 정산 요청이 취소되었습니다
            </template>

            <!-- 전체 완료 -->
            <template v-else-if="isAllCompleted">
              <template v-if="userType === 'sender'">
                <span class="bold">{{ sender.name }}</span>
                님의 정산이 완료되었습니다
              </template>

              <template v-else>
                <span class="bold">{{ sender.name }}</span>
                님에게 정산을 완료했습니다
              </template>
            </template>

            <!-- 진행 중 -->
            <template v-else>
              <span class="bold">{{ sender.name }}</span>
              님이 정산을 요청했습니다
            </template>
          </p>

          <p class="memo-text">
            {{ settlementTitle }}
          </p>
        </div>

        <span class="time-text">
          {{ requestTime }}
        </span>
      </div>

      <!-- 요약 중간 박스 -->
      <div
        class="summary-middle-box"
        :class="{
          completed: isAllCompleted,
          cancelled: isCancelled,
        }"
      >
        <!-- 참여자 프로필 -->
        <div class="profile-stack">
          <img
            v-for="(member, index) in members.slice(0, 3)"
            :key="member.id"
            :src="member.profileImage"
            alt="멤버 프로필"
            class="profile-img-stacked"
            :style="{
              zIndex: members.length - index,
              transform: `translateX(-${index * 8}px)`,
            }"
          />

          <div
            v-if="members.length > 3"
            class="profile-img-stacked more-badge"
            :style="{
              zIndex: 0,
              transform: `translateX(-${3 * 8}px)`,
            }"
          >
            +{{ members.length - 3 }}
          </div>
        </div>

        <!-- 금액 / 진행 상황 -->
        <div class="total-info-group">
          <div class="info-item">
            <span class="label">
              {{
                isCancelled
                  ? '취소된 정산 금액'
                  : isAllCompleted
                    ? '전체 완료 총액'
                    : '전체 요청 총액'
              }}
            </span>

            <span
              class="value-text total-amount"
              :class="{
                completed: isAllCompleted,
                cancelled: isCancelled,
              }"
            >
              {{ formatAmount(totalAmount) }}
            </span>
          </div>

          <div class="divider"></div>

          <div class="info-item">
            <span class="label">정산 현황</span>

            <span
              class="value-text status-text"
              :class="{
                complete: isAllCompleted,
                cancelled: isCancelled,
              }"
            >
              {{
                isCancelled
                  ? '취소됨'
                  : `${completedCount} / ${totalMemberCount}`
              }}
            </span>
          </div>
        </div>
      </div>

      <!-- 정산 상태에 따른 버튼 -->
      <!-- 완료 / 취소 상태에서는 버튼을 표시하지 않음 -->
      <template v-if="!isAllCompleted && !isCancelled">
        <!-- 요청자 -->
        <div v-if="userType === 'sender'" class="button-group">
          <button
            v-if="!hasPaidMember"
            class="btn cancel-btn"
            @click="handleCancelAll"
          >
            요청 취소
          </button>

          <div class="remind-button-wrapper">
            <button
              class="btn remind-btn"
              :disabled="isRemindDisabled"
              @click="handleRemindAll"
            >
              <span class="remind-title">리마인드</span>

              <span v-if="isRemindDisabled" class="remind-countdown">
                {{ remindRemainingText }}
              </span>
            </button>
          </div>
        </div>

        <!-- 참여자 -->
        <div v-else class="button-group">
          <button
            class="btn remind-btn"
            :disabled="isPaid"
            @click="handlePayAll"
          >
            {{ isPaid ? '송금 완료' : '송금하기' }}
          </button>
        </div>
      </template>

      <!-- 목록 토글 버튼 -->
      <button class="toggle-btn" @click="toggleMembers">
        <span>
          {{
            isCancelled
              ? '정산 참여자 내역'
              : isAllCompleted
                ? '참여한 친구들 내역'
                : '요청받은 친구들 현황'
          }}
          ({{ totalMemberCount }}명)
        </span>

        <span class="arrow-icon" :class="{ open: isMembersOpen }"> ▼ </span>
      </button>

      <!-- 2. 하단: 멤버별 개별 현황 -->
      <Transition name="accordion">
        <div v-if="isMembersOpen" class="members-section">
          <div
            v-for="member in members"
            :key="member.id"
            class="member-item"
            :class="{
              'is-me': member.id === userId,
            }"
          >
            <!-- 멤버 정보 -->
            <div class="member-info" @click="goToProfile(member.id)">
              <img
                :src="member.profileImage"
                alt="친구 프로필"
                class="member-avatar"
              />

              <div>
                <div class="name-wrapper">
                  <p class="member-name">
                    {{ member.name }}
                  </p>

                  <!-- 나 -->
                  <span v-if="member.id === userId" class="badge-me"> 나 </span>
                </div>

                <p
                  class="member-status-text"
                  :class="{
                    settled: member.isSettled,
                    pending: !member.isSettled,
                    cancelled: isCancelled,
                  }"
                >
                  {{
                    isCancelled
                      ? '정산 취소'
                      : member.isSettled
                        ? '정산 완료'
                        : '입금 대기 중'
                  }}
                </p>
              </div>
            </div>

            <!-- 금액 / 액션 -->
            <div class="member-right">
              <span class="member-amount">
                {{ formatAmount(member.amount) }}
              </span>

              <div class="member-action">
                <!-- 취소 -->
                <span v-if="isCancelled" class="badge-cancelled"> 취소됨 </span>

                <!-- 완료 -->
                <span v-else-if="member.isSettled" class="badge-settled">
                  완료됨
                </span>

                <!-- 미완료 -->
                <template v-else>
                  <!-- 본인이고 참여자인 경우에만 송금 -->
                  <button
                    v-if="userType === 'receiver' && member.id === userId"
                    class="btn-pay-sm"
                    @click="handlePayOne(member)"
                  >
                    송금
                  </button>

                  <span v-else class="badge-pending"> 미완료 </span>
                </template>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </div>

    <!-- 정산 간편비밀번호/PIN 모달 -->
    <RemitPasswordModal
      :show="showPinModal"
      :input-pin="inputPin"
      :error-message="pinErrorMessage"
      :pin-locked="pinLocked"
      @close="closePinModal"
      @enter-pin="enterPin"
      @clear-pin="clearPin"
      @delete-pin="deletePin"
      @forgot-pin="goPinReset"
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth.js';
import { useSettlementStore } from '@/stores/settlement';
import { useRemittanceStore } from '@/stores/remittance';
import { useSignupStore } from '@/stores/signup';
import { formatRelativeDate } from '@/util/data';
import { useModalStore } from '@/stores/userModalStore';
import RemitPasswordModal from '@/components/remittance/RemitPasswordModal.vue';
import walletApi from '@/api/walletApi';

/* =========================
 * Props
 * ========================= */

const props = defineProps({
  settlement: {
    type: Object,
    required: true,
  },
});

/* =========================
 * Store
 * ========================= */

const modalStore = useModalStore();
const authStore = useAuthStore();
const settlementStore = useSettlementStore();
const remittanceStore = useRemittanceStore();
const router = useRouter();

/* =========================
 * 현재 로그인 사용자
 * ========================= */

const userId = authStore.userId;

/* =========================
 * UI 상태
 * ========================= */

const isMembersOpen = ref(false);

/* =========================
 * 사용자 역할
 *
 * sender  : 정산 요청자
 * receiver: 정산 참여자
 * ========================= */

const userType = computed(() => {
  return props.settlement.requesterId === userId ? 'sender' : 'receiver';
});

/* =========================
 * 정산 상태
 * ========================= */

const isAllCompleted = computed(() => {
  return props.settlement.status === 'COMPLETE';
});

const isCancelled = computed(() => {
  return props.settlement.status === 'CANCEL';
});

/* =========================
 * 프로필 이미지 URL
 * ========================= */

const getProfileImage = (fileName) => {
  if (!fileName) {
    return '/images/default-profile.png';
  }

  return `/api/feeds/profile/${fileName}`;
};

/* =========================
 * 정산 요청자
 * ========================= */

const sender = computed(() => ({
  id: props.settlement.requesterId,

  name: props.settlement.profileSimpleVO?.nickname ?? '알 수 없음',

  profileImage: getProfileImage(
    props.settlement.profileSimpleVO?.profileImageName,
  ),
}));

/* =========================
 * 정산 제목
 * ========================= */

const settlementTitle = computed(() => {
  return props.settlement.title ?? '';
});

/* =========================
 * 한 명이라도 정산 완료했는지
 * ========================= */

const hasPaidMember = computed(() => {
  return members.value.some((member) => member.isSettled);
});

/* =========================
 * 전체 멤버
 *
 * requester는 제외
 * ========================= */

const members = computed(() => {
  return (props.settlement.members ?? []).map((member) => ({
    id: member.userId,

    name: member.receiver?.nickname ?? '알 수 없음',

    profileImage: getProfileImage(member.receiver?.profileImageName),

    amount: Number(member.amount ?? 0),

    isSettled: member.status === 'COMPLETE',
  }));
});

/* =========================
 * 현재 로그인 사용자의 정산 정보
 * ========================= */

const currentMember = computed(() => {
  return members.value.find((member) => member.id === userId);
});

const isPaid = computed(() => {
  return currentMember.value?.isSettled ?? false;
});

/* =========================
 * 전체 정산 금액
 * ========================= */

const totalAmount = computed(() => {
  return Number(props.settlement.totalAmount ?? 0);
});

/* =========================
 * 메모
 * ========================= */

const memo = computed(() => {
  return props.settlement.content ?? '';
});

/* =========================
 * 요청 시간
 * ========================= */

const requestTime = computed(() => {
  return formatRelativeDate(props.settlement.createdAt);
});

/* =========================
 * 완료한 참여자 수
 * ========================= */

const completedCount = computed(() => {
  return members.value.filter((member) => member.isSettled).length;
});

/* =========================
 * 실제 정산 참여자 수
 * ========================= */

const totalMemberCount = computed(() => {
  return members.value.length;
});

/* =========================
 * 멤버 목록 토글
 * ========================= */

const toggleMembers = () => {
  isMembersOpen.value = !isMembersOpen.value;
};

/* =========================
 * 전체 취소
 * ========================= */

const handleCancelAll = async () => {
  const res = await modalStore.showConfirm('정산 요청을 취소하시겠습니까?');

  if (!res) return;

  try {
    await settlementStore.cancel({
      settlementId: props.settlement.settlementId,
      userId,
    });

    modalStore.showSuccess('정산 요청 취소가 완료되었습니다.');
  } catch (e) {
    console.error('정산 취소 실패:', e.response?.data ?? e);
  }
};

/* =========================
 * 전체 독촉
 * ========================= */

const isRemindDisabled = computed(() => {
  const lastReminderDate = props.settlement.lastReminderDate;

  if (!lastReminderDate) {
    return false;
  }

  const lastTime = new Date(lastReminderDate).getTime();

  const sixHours = 6 * 60 * 60 * 1000;

  return Date.now() - lastTime < sixHours;
});

const remindRemainingText = computed(() => {
  const lastReminderDate = props.settlement.lastReminderDate;

  if (!lastReminderDate) {
    return '';
  }

  const lastTime = new Date(lastReminderDate).getTime();

  const remaining = 6 * 60 * 60 * 1000 - (Date.now() - lastTime);

  if (remaining <= 0) {
    return '';
  }

  const hours = Math.floor(remaining / (60 * 60 * 1000));

  const minutes = Math.floor((remaining % (60 * 60 * 1000)) / (60 * 1000));

  return hours > 0
    ? `${hours}시간 ${minutes}분 후 가능`
    : `${minutes}분 후 가능`;
});

/* =========================
 * 전체 독촉
 * ========================= */

const handleRemindAll = async () => {
  const res = await modalStore.showConfirm(
    '아직 정산하지 않은 친구들에게\n알림을 보낼까요?',
  );

  if (!res) return;

  try {
    await settlementStore.remine({
      settlementId: props.settlement.settlementId,
      userId,
    });
    // 리마인드 성공 후 정산 목록 최신화
    await settlementStore.getMyList({ userId });
  } catch (e) {
    await modalStore.showAlert(e.error || '알 수 없는 오류가 발생했습니다.');

    console.error('정산 리마인드 실패:', e.response?.data ?? e);
  }
};

/* =========================
 * 정산 결제 (PIN 인증 기반 원클릭 결제)
 * ========================= */

const showPinModal = ref(false);
const inputPin = ref('');
const pinErrorMessage = ref('');
const pinLocked = ref(false);
const isPaying = ref(false);

const goToPayment = () => {
  if (isPaid.value) return;
  inputPin.value = '';
  pinErrorMessage.value = '';
  pinLocked.value = false;
  showPinModal.value = true;
};

const enterPin = async (n) => {
  if (pinLocked.value || isPaying.value) return;
  pinErrorMessage.value = '';

  if (inputPin.value.length < 6) {
    inputPin.value += String(n);
    if (inputPin.value.length === 6) {
      try {
        isPaying.value = true;
        const res = await walletApi.verifyPin(userId, inputPin.value);
        if (res && res.verified) {
          pinErrorMessage.value = '';
          showPinModal.value = false;

          // 1. 백엔드 정산 결제 API 호출 (지갑 차감 + 상대 입금 + 거래내역 + 멤버 상태 COMPLETE)
          await settlementStore.payment({
            settlementId: props.settlement.settlementId,
            userId,
          });

          // 2. 정산 목록 즉시 최신화
          await settlementStore.getMyList({ userId });

          modalStore.showAlert(
            `${props.settlement.title || '정산'} 분담금 송금이 완료되었습니다!`,
            '정산 송금 완료',
          );
        } else {
          pinErrorMessage.value =
            res?.message || '간편비밀번호가 일치하지 않습니다.';
          inputPin.value = '';
          if (
            res?.pinLocked ||
            pinErrorMessage.value.includes('초과') ||
            pinErrorMessage.value.includes('잠겼습니다')
          ) {
            pinLocked.value = true;
          }
        }
      } catch (err) {
        console.error('정산 결제 처리 오류:', err);
        const errMsg =
          err.response?.data?.message ||
          err.message ||
          '정산 결제 처리에 실패했습니다.';
        pinErrorMessage.value = errMsg;
        inputPin.value = '';
      } finally {
        isPaying.value = false;
      }
    }
  }
};

const closePinModal = () => {
  showPinModal.value = false;
  inputPin.value = '';
  pinErrorMessage.value = '';
};

const clearPin = () => {
  inputPin.value = '';
  pinErrorMessage.value = '';
};

const deletePin = () => {
  inputPin.value = inputPin.value.slice(0, -1);
  pinErrorMessage.value = '';
};

const goPinReset = () => {
  showPinModal.value = false;
  inputPin.value = '';
  signupStore.setVerificationPurpose('PIN_RESET');
  router.push('/signup/check');
};

/* =========================
 * 1/N 송금
 * ========================= */

const handlePayAll = () => {
  goToPayment();
};

/* =========================
 * 개별 송금
 * ========================= */

const handlePayOne = (member) => {
  if (member.id !== userId) {
    return;
  }

  goToPayment();
};

/* =========================
 * 프로필 이동
 * ========================= */

const goToProfile = (targetUserId) => {
  if (targetUserId === Number(userId)) {
    return;
  }

  router.push({
    name: 'member-detail',

    params: {
      userId: targetUserId,
    },
  });
};

/* =========================
 * 금액 포맷
 * ========================= */

const formatAmount = (amount) => {
  return Number(amount ?? 0).toLocaleString('en-US', {
    maximumFractionDigits: 2,
  });
};
</script>

<style scoped>
.settlement-dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 5px;
}

.payment-card {
  background-color: #ffffff;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid #f3f4f6;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
}

.payment-card.cancelled {
  background-color: #fafafa;
}

/* =========================
 * 상단 헤더
 * ========================= */

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sender-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid #e5e7eb;
}

.header-text {
  flex: 1;
  min-width: 0;
}

.main-text {
  color: #111827;
  font-size: 13px;
  margin: 0;
}

.bold {
  font-weight: 700;
}

.memo-text {
  color: #4b5563;
  font-size: 13px;
  margin: 2px 0 0 0;
  font-weight: 600;
}

.time-text {
  color: #9ca3af;
  font-size: 11px;
  align-self: flex-start;
  white-space: nowrap;
}

/* =========================
 * 정산 요약
 * ========================= */

.summary-middle-box {
  margin-top: 14px;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition:
    background-color 0.2s,
    border-color 0.2s;
}

.summary-middle-box.completed {
  background-color: #fffdf5;
  border-color: #fde68a;
}

.summary-middle-box.cancelled {
  background-color: #f9fafb;
  border-color: #e5e7eb;
}

/* =========================
 * 참여자 프로필
 * ========================= */

.profile-stack {
  display: flex;
  position: relative;
  padding-left: 8px;
  flex-shrink: 0;
}

.profile-img-stacked {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #ffffff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.more-badge {
  background-color: #f3f4f6;
  color: #4b5563;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
}

/* =========================
 * 금액 / 정산 현황
 * ========================= */

.total-info-group {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
}

.label {
  font-size: 10.5px;
  color: #64748b;
  line-height: 1.2;
  white-space: nowrap;
}

.value-text {
  margin-top: 3px;
  display: inline-flex;
  align-items: center;
  height: 22px;
  white-space: nowrap;
}

.total-amount {
  font-size: 14px;
  font-weight: 800;
  color: #4b5563;
}

.total-amount.completed {
  color: #d97706;
}

.total-amount.cancelled {
  color: #9ca3af;
}

.status-text {
  font-size: 14px;
  font-weight: 800;
  color: #4b5563;
}

.status-text.complete {
  color: #d97706;
}

.status-text.cancelled {
  color: #9ca3af;
}

.divider {
  width: 1px;
  height: 28px;
  background-color: #cbd5e1;
}

/* =========================
 * 버튼 그룹
 * ========================= */

.button-group {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}

.button-group > .cancel-btn,
.remind-button-wrapper {
  flex: 1;
  width: 0;
  min-width: 0;
}

.remind-button-wrapper {
  display: flex;
}

/* =========================
 * 공통 버튼
 * ========================= */

.btn {
  width: 100%;
  height: 48px;
  padding: 0 16px;
  box-sizing: border-box;

  border-radius: 9999px;

  font-size: 14px;
  font-weight: 600;

  cursor: pointer;
  text-align: center;

  transition:
    background-color 0.2s,
    color 0.2s;
}

/* =========================
 * 요청 취소 버튼
 * ========================= */

.cancel-btn {
  background-color: #ffffff;
  border: 1px solid #d1d5db;
  color: #374151;
}

.cancel-btn:hover {
  background-color: #f9fafb;
}

/* =========================
 * 리마인드 버튼
 * ========================= */

.remind-btn {
  background-color: #ffbc2e;
  border: 1px solid transparent;
  color: #1f2937;

  font-weight: 700;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  gap: 2px;

  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.remind-btn:hover {
  background-color: #f5b025;
}

.remind-title {
  font-size: 14px;
  line-height: 16px;
}

.remind-countdown {
  font-size: 9px;
  font-weight: 500;
  color: #6b7280;
  line-height: 11px;
}

.remind-btn:disabled {
  background-color: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
  box-shadow: none;
}

.remind-btn:disabled:hover {
  background-color: #e5e7eb;
}

.remind-btn:disabled .remind-countdown {
  color: #9ca3af;
}

/* =========================
 * 목록 토글
 * ========================= */

.toggle-btn {
  width: 100%;

  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 10px;

  margin-top: 12px;
  padding: 10px 14px;

  display: flex;
  align-items: center;
  justify-content: space-between;

  font-size: 13px;
  font-weight: 600;
  color: #4b5563;

  cursor: pointer;

  transition: background-color 0.2s;
}

.toggle-btn:hover {
  background-color: #f3f4f6;
}

.arrow-icon {
  font-size: 10px;
  transition: transform 0.2s ease;
}

.arrow-icon.open {
  transform: rotate(180deg);
}

/* =========================
 * 멤버 목록
 * ========================= */

.members-section {
  background-color: #ffffff;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid #f3f4f6;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.member-item {
  display: flex;
  align-items: center;
  justify-content: space-between;

  padding: 10px 8px;

  border-bottom: 1px solid #f9fafb;
  border-radius: 8px;

  transition: background-color 0.2s;
}

.member-item.is-me {
  background-color: #fffbeb;
  border-bottom: 1px solid #fef3c7;
}

.member-item:last-child {
  border-bottom: none;
}

/* =========================
 * 멤버 정보
 * ========================= */

.member-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.member-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.name-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
}

.member-name {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.badge-me {
  font-size: 10px;
  font-weight: 700;
  color: #d97706;
  background-color: #fef3c7;
  padding: 1px 5px;
  border-radius: 4px;
}

.member-status-text {
  font-size: 11px;
  margin: 2px 0 0 0;
}

.member-status-text.settled {
  color: #d97706;
}

.member-status-text.pending {
  color: #9ca3af;
}

.member-status-text.cancelled {
  color: #9ca3af;
}

/* =========================
 * 멤버 금액 / 액션
 * ========================= */

.member-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-amount {
  font-size: 13px;
  font-weight: 700;
  color: #111827;
}

/* =========================
 * 정산 완료
 * ========================= */

.badge-settled {
  font-size: 11px;
  font-weight: 600;
  color: #92400e;
  background-color: #fef3c7;
  padding: 4px 8px;
  border-radius: 6px;
}

/* =========================
 * 정산 미완료
 * ========================= */

.badge-pending {
  font-size: 11px;
  font-weight: 600;
  color: #9ca3af;
  background-color: #f3f4f6;
  padding: 4px 8px;
  border-radius: 6px;
}

/* =========================
 * 정산 취소
 * ========================= */

.badge-cancelled {
  font-size: 11px;
  font-weight: 600;
  color: #6b7280;
  background-color: #f3f4f6;
  padding: 4px 8px;
  border-radius: 6px;
}

/* =========================
 * 개별 송금 버튼
 * ========================= */

.btn-pay-sm {
  background-color: #ffbc2e;
  color: #1f2937;

  border: none;

  font-size: 11px;
  font-weight: 700;

  padding: 4px 12px;
  border-radius: 6px;

  cursor: pointer;

  transition: background-color 0.2s;
}

.btn-pay-sm:hover {
  background-color: #f5b025;
}
</style>
