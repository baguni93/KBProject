<template>
  <div>
    <div class="search-box-wrap">
      <i class="fa-solid fa-magnifying-glass search-ic"></i>
      <input
        :value="keyword"
        @input="$emit('update:keyword', $event.target.value)"
        type="text"
        class="search-input text-15"
        placeholder="정산할 친구 이름 검색..."
      />
    </div>

    <div class="form-field-group">
      <label class="field-label text-13-bold"
        >선택된 정산 참여자 (총
        {{ selectedFriends.length + 1 }}명)</label
      >
      <div class="selected-tags-flex">
        <div class="my-tag-badge text-13-bold">
          <img
            :src="myProfileImageUrl"
            class="tag-avatar-img"
            @error="$event.target.style.display = 'none'"
          />
          <span>나</span>
        </div>
        <div
          v-for="fId in selectedFriends"
          :key="fId"
          class="friend-tag-badge text-13-bold"
        >
          <img
            :src="getProfileImageUrl(getFriendObj(fId))"
            class="tag-avatar-img"
            @error="$event.target.style.display = 'none'"
          />
          <span>{{ getFriendName(fId) }}</span>
          <i
            class="fa-solid fa-xmark del-ic"
            @click="$emit('removeFriend', fId)"
          ></i>
        </div>
      </div>
    </div>

    <div class="form-field-group">
      <label class="field-label text-13-bold">함께 정산할 친구 선택</label>
      <div
        v-if="friends.length === 0"
        class="empty-recent-msg text-13"
      >
        검색 결과 또는 등록된 친구가 없습니다.
      </div>
      <div
        v-for="friend in friends"
        :key="friend.id"
        class="friend-card-item"
        :class="{
          'dutch-active': selectedFriends.includes(friend.id),
        }"
        @click="$emit('toggleFriend', friend.id)"
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
          v-if="selectedFriends.includes(friend.id)"
          class="selected-badge-wrap"
        >
          <span class="dutch-tag text-13-bold">정산 참여</span>
          <i class="fa-solid fa-circle-check dutch-ic"></i>
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
  selectedFriends: {
    type: Array,
    default: () => [],
  },
  friends: {
    type: Array,
    default: () => [],
  },
  myProfileImageUrl: {
    type: String,
    default: "",
  },
  getProfileImageUrl: {
    type: Function,
    required: true,
  },
  getFriendObj: {
    type: Function,
    required: true,
  },
  getFriendName: {
    type: Function,
    required: true,
  },
});

defineEmits(["update:keyword", "removeFriend", "toggleFriend"]);
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

.form-field-group {
  margin-bottom: 20px;
}

.field-label {
  display: block;
  margin-bottom: 8px;
  color: var(--color-text-main, #111111);
}

.selected-tags-flex {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.my-tag-badge,
.friend-tag-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  background-color: #f0f0f0;
}

.friend-tag-badge {
  background-color: rgba(255, 188, 0, 0.15);
  color: #111111;
}

.tag-avatar-img {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
}

.del-ic {
  cursor: pointer;
  color: #888888;
  font-size: 12px;
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

.friend-card-item.dutch-active {
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
}

.friend-name {
  margin: 0;
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

.dutch-tag {
  color: #ffbc00;
}

.dutch-ic {
  color: #ffbc00;
  font-size: 18px;
}

.unsel-ic {
  color: #cccccc;
  font-size: 18px;
}
</style>
