<template>
  <Teleport to="body">
    <Transition name="bottom">
      <div v-if="modelValue" class="modal-background" @click="close">
        <div class="bottom-sheet" @click.stop>
          <button class="menu-item" @click="edit">
            <i class="fa-solid fa-pen"></i>
            수정하기
          </button>

          <button class="menu-item delete" @click="remove">
            <i class="fa-solid fa-trash"></i>
            삭제하기
          </button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true,
  },
});

const emit = defineEmits(['update:modelValue', 'edit', 'delete']);

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
</script>

<style scoped>
.bottom-enter-active,
.bottom-leave-active {
  transition: opacity 0.25s ease;
}

.bottom-enter-active .bottom-sheet,
.bottom-leave-active .bottom-sheet {
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.bottom-enter-from .bottom-sheet,
.bottom-leave-to .bottom-sheet {
  transform: translateY(100%);
}

.bottom-enter-to .bottom-sheet,
.bottom-leave-from .bottom-sheet {
  transform: translateY(0);
}

.modal-background {
  position: fixed;
  inset: 0;

  background: rgba(0, 0, 0, 0.35);

  display: flex;
  align-items: flex-end;
  justify-content: center;

  z-index: 10000;
}

.bottom-sheet {
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

.menu-item.delete {
  color: #e53935;
}
</style>
