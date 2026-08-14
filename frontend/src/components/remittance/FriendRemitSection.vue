<template>
  <div>
    <div class="search-box-wrap">
      <i class="fa-solid fa-magnifying-glass search-ic"></i>
      <input
        :value="keyword"
        @input="$emit('update:keyword', $event.target.value)"
        type="text"
        class="search-input text-15"
        placeholder="친구 이름 또는 프로필 ID 입력..."
      />
    </div>

    <div class="form-field-group">
      <label class="field-label text-13-bold">내 친구 목록</label>
      <div
        v-if="friends.length === 0"
        class="empty-recent-msg text-13"
      >
        등록된 친구가 없습니다.
      </div>
      <div
        v-for="friend in friends"
        :key="friend.id"
        class="friend-card-item"
        :class="{ active: selectedFriendId === friend.id }"
        @click="$emit('selectFriend', friend.id)"
      >
        <div class="friend-item-left">
          <img
            :src="getProfileImageUrl(friend)"
            class="friend-avatar-img"
            @error="$event.target.style.display = 'none'"
          />
          <div>
            <p class="friend-name text-15-bold">{{ friend.name }}</p>
            <p class="friend-sub text-13">@{{ friend.username }}</p>
          </div>
        </div>

        <div
          v-if="selectedFriendId === friend.id"
          class="selected-badge-wrap"
        >
          <span class="sel-tag text-13-bold">선택됨</span>
          <i class="fa-solid fa-circle-check sel-ic"></i>
        </div>
        <i v-else class="fa-regular fa-circle unsel-ic"></i>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  keyword: {
    type: String,
    default: "",
  },
  friends: {
    type: Array,
    default: () => [],
  },
  selectedFriendId: {
    type: [Number, String],
    default: null,
  },
  getProfileImageUrl: {
    type: Function,
    required: true,
  },
});

defineEmits(["update:keyword", "selectFriend"]);
</script>

<style scoped>
.search-box-wrap {
  position: relative;
  margin-bottom: 20px;
}

.search-ic {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #888888;
}

.search-input {
  width: 100%;
  height: 48px;
  border: 1px solid var(--color-border-main, #ededed);
  border-radius: 12px;
  padding: 0 16px 0 44px;
  background-color: var(--color-bg-screen, #f8f9fa);
  outline: none;
  box-sizing: border-box;
}

.search-input:focus {
  border-color: #ffbc00;
  background-color: #ffffff;
}

.form-field-group {
  margin-bottom: 20px;
}

.field-label {
  display: block;
  margin-bottom: 8px;
  color: var(--color-text-main, #111111);
}

.empty-recent-msg {
  color: #888888;
  padding: 16px 0;
  text-align: center;
}

.friend-card-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border: 1px solid var(--color-border-main, #ededed);
  border-radius: 14px;
  background-color: #ffffff;
  margin-bottom: 8px;
  cursor: pointer;
}

.friend-card-item.active {
  border-color: #ffbc00;
  background-color: rgba(255, 188, 0, 0.05);
}

.friend-item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.friend-avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  background-color: #e0e0e0;
}

.friend-name {
  margin: 0;
  color: #111111;
}

.friend-sub {
  margin: 2px 0 0;
  color: #888888;
}

.selected-badge-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
}

.sel-tag {
  color: #ffbc00;
}

.sel-ic {
  color: #ffbc00;
  font-size: 18px;
}

.unsel-ic {
  color: #cccccc;
  font-size: 18px;
}
</style>
