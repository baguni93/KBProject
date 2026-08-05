<template>
  <div class="actions">
    <button class="action-btn" @click="$router.push('/friends?tab=list')">
      <i class="fa-solid fa-user-group"></i>
      친구 {{ friendCount }}
    </button>

    <button class="action-btn" @click="$router.push('/friends?tab=request')">
      <i class="fa-solid fa-user-plus"></i>
      친구 요청
    </button>
  </div>
</template>

<script setup>
defineProps({
  friendCount: {
    type: Number,
    default: 0,
  },
});
</script>

<style scoped>
.actions {
  display: flex;

  justify-content: center;

  gap: 10px;
}

/* 버튼 */
.action-btn {
  display: flex;
  align-items: center;

  gap: 6px;

  border: 1px solid #ddd;

  background: white;

  border-radius: 20px;

  padding: 8px 14px;

  font-size: 14px;

  color: #333;

  cursor: pointer;

  transition: 0.15s;
}

/* 아이콘 */
.action-btn i {
  font-size: 13px;
}

/* hover */
.action-btn:hover {
  background: #f7f7f7;
}

/* 클릭 */
.action-btn:active {
  transform: scale(0.95);
}
</style>
