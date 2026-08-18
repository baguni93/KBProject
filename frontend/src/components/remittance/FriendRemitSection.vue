<template>
  <div class="friend-remit-container">
    <!-- 1. 카카오페이 최상단 통합 검색바 -->
    <div class="form-field-group">
      <div class="toss-search-bar">
        <i class="fa-solid fa-magnifying-glass search-icon"></i>
        <input
          :value="keyword"
          @input="$emit('update:keyword', $event.target.value)"
          type="text"
          class="toss-search-input text-15-bold"
          placeholder="이름, 닉네임 검색"
        />
      </div>
    </div>

    <!-- 2. 최근 보낸 친구 (카카오페이 1:1 보더리스) -->
    <div v-if="!keyword && recentFriends.length > 0" class="form-field-group" style="margin-top: 10px;">
      <span class="field-sec-title text-13-bold">최근 보낸 친구</span>
      <div class="toss-friend-list">
        <div
          v-for="friend in recentFriends"
          :key="'recent-' + (friend.id || friend.friendUserId || friend.friendId)"
          class="toss-friend-row"
          :class="{ active: selectedFriendId === (friend.id || friend.friendUserId || friend.friendId) }"
          @click="$emit('selectFriend', friend.id || friend.friendUserId || friend.friendId)"
        >
          <div class="toss-friend-avatar-wrap">
            <img
              :src="getProfileImageUrl(friend)"
              class="toss-friend-avatar-img"
              @error="$event.target.src = '/api/feeds/profile/unknown.png'"
            />
          </div>
          <div class="toss-friend-text-area">
            <span class="toss-friend-name text-15-bold">
              {{ friend.name || friend.nickname || friend.receiver?.nickname || friend.receiver?.name || '친구' }}
            </span>
            <span class="toss-friend-sub text-13">
              @{{ friend.username || friend.receiver?.username || friend.nickname || friend.receiver?.nickname || '' }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 내 친구 목록 (카카오페이 1:1 버튼/구분선 없는 깨끗한 가독성) -->
    <div class="form-field-group" style="margin-top: 14px;">
      <span class="field-sec-title text-13-bold">친구</span>
      <div
        v-if="friends.length === 0"
        class="empty-recent-msg text-13"
      >
        등록된 친구가 없습니다.
      </div>
      <div v-else class="toss-friend-list">
        <div
          v-for="friend in friends"
          :key="friend.id || friend.friendUserId || friend.friendId"
          class="toss-friend-row"
          :class="{ active: selectedFriendId === (friend.id || friend.friendUserId || friend.friendId) }"
          @click="$emit('selectFriend', friend.id || friend.friendUserId || friend.friendId)"
        >
          <div class="toss-friend-avatar-wrap">
            <img
              :src="getProfileImageUrl(friend)"
              class="toss-friend-avatar-img"
              @error="$event.target.src = '/api/feeds/profile/unknown.png'"
            />
          </div>
          <div class="toss-friend-text-area">
            <span class="toss-friend-name text-15-bold">
              {{ friend.name || friend.nickname || friend.receiver?.nickname || friend.receiver?.name || '친구' }}
            </span>
            <span class="toss-friend-sub text-13">
              @{{ friend.username || friend.receiver?.username || friend.nickname || friend.receiver?.nickname || '' }}
            </span>
          </div>
        </div>
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
  recentFriends: {
    type: Array,
    default: () => [],
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
    default: (friend) => friend?.avatarUrl || "/api/feeds/profile/default_profile.png",
  },
});

defineEmits(["update:keyword", "selectFriend"]);
</script>

<style scoped>
@import "@/components/common/common/common.css";

.friend-remit-container {
  display: flex;
  flex-direction: column;
  gap: 14px;
  width: 100%;
  box-sizing: border-box;
}

.form-field-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-sec-title {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 2px;
}

/* 카카오페이 최상단 검색바 */
.toss-search-bar {
  display: flex;
  align-items: center;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 0 16px;
  height: 48px;
  transition: all 0.2s ease;
}

.toss-search-bar:focus-within {
  background-color: #ffffff;
  border-color: #ffbc2e;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.15);
}

.search-icon {
  color: #94a3b8;
  font-size: 15px;
  margin-right: 10px;
}

.toss-search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 15px;
  color: #0f172a;
  line-height: 1.4;
}

.toss-search-input::placeholder {
  color: #94a3b8;
}

.empty-recent-msg {
  color: #94a3b8;
  padding: 20px 0;
  text-align: center;
}

/* 카카오페이 1:1 무구분선/무버튼 클린 행 리스트 */
.toss-friend-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.toss-friend-row {
  display: flex;
  align-items: center;
  padding: 10px 4px;
  border: none;
  background-color: transparent;
  border-radius: 14px;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.toss-friend-row:hover,
.toss-friend-row:active {
  background-color: #f8fafc;
}

.toss-friend-avatar-wrap {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  flex-shrink: 0;
  border: 1px solid #f1f5f9;
}

.toss-friend-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.toss-friend-text-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.toss-friend-name {
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.2;
}

.toss-friend-sub {
  color: #64748b;
  font-size: 13px;
  line-height: 1.2;
}
</style>
