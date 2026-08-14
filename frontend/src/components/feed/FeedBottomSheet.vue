<template>
  <Transition name="bottom-sheet">
    <div v-if="modelValue" class="modal-background" @click="close">
      <div class="sheet" @click.stop>
        <!-- 내 피드 -->
        <template v-if="isMine">
          <button class="menu-item" @click="edit">
            <i class="fa-solid fa-pen"></i>
            수정하기
          </button>

          <button class="menu-item delete" @click="remove">
            <i class="fa-solid fa-trash"></i>
            삭제하기
          </button>
        </template>

        <!-- 다른 사람 피드 -->
        <template v-else>
          <button class="menu-item report" @click="report">
            <i class="fa-solid fa-flag"></i>
            신고하기
          </button>
        </template>
      </div>
    </div>
  </Transition>
</template>

<script setup>
defineProps({
  modelValue: {
    type: Boolean,
    required: true,
  },

  isMine: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['update:modelValue', 'edit', 'delete', 'report']);

const close = () => {
  emit('update:modelValue', false);
};

const edit = () => {
  emit('edit');
  close();
};

const remove = () => {
  emit('delete');
  close();
};

const report = () => {
  emit('report');
  close();
};
</script>

<style scoped>
.bottom-sheet-enter-active {
  transition:
    opacity 0.25s ease,
    transform 0.25s ease;
}

/* 나갈 때 */
.bottom-sheet-leave-active {
  transition:
    opacity 0.15s ease,
    transform 0.15s ease;
}

.bottom-sheet-enter-from,
.bottom-sheet-leave-to {
  opacity: 0;
}

.bottom-sheet-enter-from .sheet,
.bottom-sheet-leave-to .sheet {
  transform: translateY(100%);
}

.bottom-sheet-enter-to .sheet,
.bottom-sheet-leave-from .sheet {
  transform: translateY(0);
}

/* 배경 */
.modal-background {
  position: absolute;
  inset: 0;

  background: rgba(0, 0, 0, 0.35);

  display: flex;
  align-items: flex-end;
  justify-content: center;

  z-index: 10000;
}

/* 실제 Bottom Sheet */
.sheet {
  width: 430px;
  max-width: 100%;

  background: white;

  border-radius: 20px 20px 0 0;

  padding: 20px;

  display: flex;
  flex-direction: column;
  gap: 8px;

  box-sizing: border-box;
}

/* 메뉴 */
.menu-item {
  width: 100%;
  height: 55px;

  border: none;
  border-radius: 12px;

  background: #f5f5f5;

  display: flex;
  align-items: center;
  gap: 12px;

  padding: 0 20px;

  font-size: 16px;

  cursor: pointer;
}

.menu-item.delete,
.menu-item.report {
  color: #e53935;
}
</style>
