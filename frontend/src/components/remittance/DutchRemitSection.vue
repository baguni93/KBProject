<template>
  <div class="dutch-remit-container">
    <!-- 1. 선택된 정산 참여자 상단 아바타 스크롤 라인 (레퍼런스 디자인 100% 반영) -->
    <div v-if="selectedFriends.length > 0" class="selected-participants-header-section">
      <div class="participants-avatar-scroll-row">
        <!-- 나 (기본 참여자) -->
        <div class="participant-avatar-item">
          <div class="avatar-circle-box">
            <img
              :src="myProfileImageUrl"
              class="participant-avatar-img"
              @error="$event.target.src = '/api/feeds/profile/unknown.png'"
            />
          </div>
          <span class="participant-name-text text-13-bold">나</span>
        </div>

        <!-- 선택된 친구들 (프로필 동그라미 상단 + 우상단 X 버튼 + 하단 이름/닉네임) -->
        <div
          v-for="fId in selectedFriends"
          :key="fId"
          class="participant-avatar-item"
        >
          <div class="avatar-circle-box">
            <img
              :src="getProfileImageUrl(getFriendObj(fId))"
              class="participant-avatar-img"
              @error="$event.target.src = '/api/feeds/profile/unknown.png'"
            />
            <button
              type="button"
              class="remove-avatar-btn"
              @click.stop="$emit('removeFriend', fId)"
            >
              <i class="fa-solid fa-xmark"></i>
            </button>
          </div>
          <span class="participant-name-text text-13-bold">
            {{ getFriendName(fId) }}
          </span>
        </div>
      </div>
    </div>

    <!-- 2. 검색바 -->
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

    <!-- 3. 내 친구 목록 -->
    <div class="form-field-group">
      <span class="field-sec-title text-15-bold">친구</span>
      <div
        v-if="friends.length === 0"
        class="empty-recent-msg text-13"
      >
        검색 결과 또는 등록된 친구가 없습니다.
      </div>
      <div v-else class="toss-friend-list">
        <div
          v-for="friend in friends"
          :key="friend.id || friend.friendUserId || friend.friendId"
          class="toss-friend-row"
          :class="{
            'dutch-active': selectedFriends.includes(friend.id || friend.friendUserId || friend.friendId),
          }"
          @click="$emit('toggleFriend', friend.id || friend.friendUserId || friend.friendId)"
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
          <div class="selected-check-area">
            <i
              v-if="selectedFriends.includes(friend.id || friend.friendUserId || friend.friendId)"
              class="fa-solid fa-circle-check sel-ic"
            ></i>
            <i v-else class="fa-regular fa-circle unsel-ic"></i>
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
@import "@/components/common/common/common.css";

.dutch-remit-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  box-sizing: border-box;
}

.form-field-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* =========================================
   상단 선택된 참여자 프로필 아바타 라인 (레퍼런스 디자인 반영)
========================================= */
.selected-participants-header-section {
  width: 100%;
  padding-bottom: 4px;
}

.participants-avatar-scroll-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  overflow-x: auto;
  padding: 4px 2px 8px;
  scrollbar-width: none;
}

.participants-avatar-scroll-row::-webkit-scrollbar {
  display: none;
}

.participant-avatar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 68px;
  flex-shrink: 0;
}

.avatar-circle-box {
  position: relative;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background-color: #f8f9fa;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.participant-avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.remove-avatar-btn {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background-color: #4a5568;
  color: #ffffff;
  border: 1.5px solid #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  cursor: pointer;
  padding: 0;
  transition: background-color 0.15s ease;
}

.remove-avatar-btn:hover {
  background-color: #e53e3e;
}

.participant-name-text {
  margin-top: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #111111;
  text-align: center;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* =========================================
   검색바 & 리스트
========================================= */
.toss-search-bar {
  display: flex;
  align-items: center;
  background-color: #f7fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 0 16px;
  height: 50px;
  transition: all 0.2s ease;
}

.toss-search-bar:focus-within {
  background-color: #ffffff;
  border-color: #ffbc2e;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.15);
}

.search-icon {
  color: #a0aec0;
  font-size: 15px;
  margin-right: 10px;
}

.toss-search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 15px;
  color: #111111;
  line-height: 1.4;
}

.toss-search-input::placeholder {
  color: #a0aec0;
}

.field-sec-title {
  color: #111111;
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 2px;
}

.empty-recent-msg {
  color: #a0aec0;
  padding: 20px 0;
  text-align: center;
}

.toss-friend-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.toss-friend-row {
  display: flex;
  align-items: center;
  padding: 12px 10px;
  border-radius: 14px;
  background-color: transparent;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.toss-friend-row:hover,
.toss-friend-row:active {
  background-color: #f7fafc;
}

.toss-friend-row.dutch-active {
  background-color: #fffdf8;
}

.toss-friend-avatar-wrap {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  flex-shrink: 0;
  border: 1px solid #edf2f7;
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
  gap: 3px;
  min-width: 0;
}

.toss-friend-name {
  color: #111111;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.2;
}

.toss-friend-sub {
  color: #718096;
  font-size: 13px;
  line-height: 1.2;
}

.selected-check-area {
  display: flex;
  align-items: center;
  font-size: 20px;
}

.sel-ic {
  color: #ffbc2e;
}

.unsel-ic {
  color: #cbd5e0;
}
</style>
