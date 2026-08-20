<template>
  <Transition name="task-push">
    <div v-if="currentTask" class="task-push" @click="handleClick(currentTask)">
      <!-- 아이콘 -->
      <div class="task-icon">✓</div>

      <!-- 내용 -->
      <div class="task-content">
        <div class="task-title">
          {{ getTitle(currentTask) }}
        </div>

        <div class="task-message">
          {{ currentTask.message }}
        </div>
      </div>

      <!-- 닫기 -->
      <button class="task-close" @click.stop="close">×</button>
    </div>
  </Transition>
</template>

<script setup>
import { computed, onBeforeUnmount, watch } from 'vue';
import { useTaskStore } from '@/stores/task';
import { useRouter } from 'vue-router';

const router = useRouter();
const taskStore = useTaskStore();

const currentTask = computed(() => {
  return taskStore.taskQueue[0] ?? null;
});

let hideTimer = null;

const getTitle = (task) => {
  switch (task.taskType) {
    case 'ANALYSIS_COMPLETE':
      return 'AI 소비패턴 분석 완료';

    case 'EVENT_COMPLETE':
      return '이벤트 완료';

    case 'CARD_RECOMMEND':
      return '맞춤 카드 추천 분석 완료';

    case 'INSURANCE_RECOMMEND':
      return '맞춤 보험 추천 분석 완료';

    case 'PAYMENT_COMPLETE':
      return '결제 완료';
    case 'PHONE_AUTH_SEND':
      return '휴대폰 인증번호';
    default:
      return '작업 완료';
  }
};

const close = () => {
  clearHideTimer();

  taskStore.removeTaskEvent();
};

const handleClick = (task) => {
  console.log(task);
  clearHideTimer();
  task.callback?.();
  taskStore.removeTaskEvent();

  switch (task.taskType) {
    case 'ANALYSIS_COMPLETE':
      router.push('/analysis/main?period=12');

      break;
    case 'EVENT_COMPLETE':
      router.push('/event/list');
      break;
    case 'CARD_RECOMMEND':
      router.push({
        name: 'card-recommendation',
        params: {
          spendingAnalysisId: task.targetId,
        },
      });
      break;
    case 'INSURANCE_RECOMMEND':
      router.push({
        name: 'insurance-recommendation',
        params: {
          spendingAnalysisId: task.targetId,
        },
      });
      break;
    case 'PAYMENT_COMPLETE':
      break;
    case 'PHONE_AUTH_SEND':
      break;
    default:
      return '작업 완료';
  }
};

const clearHideTimer = () => {
  if (hideTimer) {
    clearTimeout(hideTimer);
    hideTimer = null;
  }
};

watch(currentTask, (task) => {
  clearHideTimer();

  if (!task) {
    return;
  }

  hideTimer = setTimeout(() => {
    taskStore.removeTaskEvent();
    hideTimer = null;
  }, 5000);
});

onBeforeUnmount(() => {
  clearHideTimer();
});
</script>

<style scoped>
.task-push {
  position: absolute;

  /*
   * 앱 상단에서 내려오는 위치
   */
  top: 14px;
  left: 14px;
  right: 14px;

  /*
   * 메시지 2줄까지 고려
   */
  min-height: 78px;

  display: flex;
  align-items: center;

  gap: 12px;

  padding: 13px 14px;

  box-sizing: border-box;

  background: #ffffff;

  border: 1px solid #e5e7eb;
  border-radius: 18px;

  box-shadow:
    0 10px 30px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.06);

  cursor: pointer;

  z-index: 9000;
}

/* =========================
   아이콘
========================= */

.task-icon {
  position: relative;

  width: 42px;
  height: 42px;

  flex-shrink: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  border-radius: 50%;

  background: linear-gradient(145deg, #ffd15c, var(--color-primary));

  color: #ffffff;

  font-size: 20px;
  font-weight: 700;

  box-shadow:
    0 6px 16px rgba(255, 188, 46, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);

  animation: taskIconPop 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes taskIconPop {
  0% {
    opacity: 0;
    transform: scale(0.5);
  }

  70% {
    transform: scale(1.08);
  }

  100% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 체크 주변 은은한 링 */

.task-icon::before {
  content: '';

  position: absolute;

  inset: -4px;

  border-radius: 50%;

  border: 1px solid rgba(255, 188, 46, 0.2);
}

/* =========================
   내용
========================= */

.task-content {
  flex: 1;
  min-width: 0;

  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 제목 */

.task-title {
  margin-bottom: 4px;

  font-size: 14px;
  font-weight: 700;

  line-height: 1.3;

  color: #111827;
}

/* 메시지
   최대 2줄
*/

.task-message {
  display: -webkit-box;

  overflow: hidden;

  font-size: 13px;
  font-weight: 400;

  line-height: 1.45;

  color: #6b7280;

  white-space: pre-line;

  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

/* =========================
   닫기 버튼
========================= */

.task-close {
  width: 28px;
  height: 28px;

  flex-shrink: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  padding: 0;

  border: none;
  border-radius: 50%;

  background: transparent;

  color: #9ca3af;

  font-size: 22px;
  font-weight: 400;

  line-height: 1;

  cursor: pointer;

  transition:
    background 0.15s ease,
    color 0.15s ease;
}

.task-close:hover {
  background: #f3f4f6;
  color: #374151;
}

/* =========================
   Push 등장 / 퇴장
========================= */

/*
 * 처음에는 위쪽에 숨어 있다가
 * 아래로 내려오는 효과
 */

.task-push-enter-active {
  transition:
    opacity 0.3s ease,
    transform 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}

.task-push-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}

/* 등장 시작 */

.task-push-enter-from {
  opacity: 0;

  transform: translateY(-100%);
}

/* 퇴장 */

.task-push-leave-to {
  opacity: 0;

  transform: translateY(-30px);
}
</style>
