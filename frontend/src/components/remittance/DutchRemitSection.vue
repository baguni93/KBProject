<template>
  <div class="dutch-remit-container">
    <!-- 1. 선택된 정산 참여자 상단 아바타 스크롤 라인 (로그인 유저 닉네임 동적 노출) -->
    <div class="selected-participants-header-section">
      <div class="participants-avatar-scroll-row">
        <!-- 나 (로그인한 유저) -->
        <div class="participant-avatar-item">
          <div class="avatar-circle-box">
            <img
              :src="myProfileImageUrl"
              class="participant-avatar-img"
              @error="(e) => { e.target.onerror = null; e.target.style.display = 'none'; }"
            />
          </div>
          <span class="participant-name-text text-12-bold">{{ myProfileName }}</span>
        </div>

        <!-- 선택된 친구들 (프로필 동그라미 + 우상단 다크 X 삭제 버튼) -->
        <div
          v-for="fId in selectedFriends"
          :key="fId"
          class="participant-avatar-item"
        >
          <div class="avatar-circle-box">
            <img
              :src="getProfileImageUrl(getFriendObj(fId))"
              class="participant-avatar-img"
              @error="(e) => { e.target.onerror = null; e.target.style.display = 'none'; }"
            />
            <button
              type="button"
              class="remove-avatar-btn"
              @click.stop="$emit('removeFriend', fId)"
              title="참여자 제외"
            >
              <i class="fa-solid fa-xmark"></i>
            </button>
          </div>
          <span class="participant-name-text text-12-bold">
            {{ getFriendName(fId) }}
          </span>
        </div>
      </div>
    </div>

    <!-- 2. 카카오페이 최상단 통합 검색바 -->
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

    <!-- 3. 내 프로필 섹션 (동적 JWT/DB 로그인 유저 닉네임 노출) -->
    <div class="form-field-group" style="margin-top: 10px;">
      <span class="field-sec-title text-13-bold">내 프로필</span>
      <div class="toss-friend-list">
        <div class="toss-friend-row my-profile-row">
          <div class="toss-friend-avatar-wrap">
            <img
              :src="myProfileImageUrl"
              class="toss-friend-avatar-img"
              @error="(e) => { e.target.onerror = null; e.target.style.display = 'none'; }"
            />
          </div>
          <div class="toss-friend-text-area">
            <span class="toss-friend-name text-15-bold">{{ myProfileName }}</span>
          </div>
          <div class="selected-check-area">
            <i class="fa-solid fa-circle-check sel-ic"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- 4. 내 친구 목록 (카카오페이 1:1 보더리스) -->
    <div class="form-field-group" style="margin-top: 14px;">
      <span class="field-sec-title text-13-bold">친구</span>
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
              @error="(e) => { e.target.onerror = null; e.target.style.display = 'none'; }"
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
    default: "/api/feeds/profile/default_profile.png",
  },
  myProfileName: {
    type: String,
    default: "내 프로필",
  },
  getProfileImageUrl: {
    type: Function,
    default: (friend) => friend?.avatarUrl || "/api/feeds/profile/default_profile.png",
  },
  getFriendObj: {
    type: Function,
    default: () => null,
  },
  getFriendName: {
    type: Function,
    default: (fId) => "친구",
  },
});

defineEmits(["update:keyword", "removeFriend", "toggleFriend"]);
</script>

<style scoped>
@import "@/components/common/common/common.css";

.dutch-remit-container {
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

/* 상단 선택 참여자 아바타 칩 */
.selected-participants-header-section {
  width: 100%;
  padding-bottom: 2px;
}

.participants-avatar-scroll-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  overflow-x: auto;
  padding: 4px 2px 6px;
  scrollbar-width: none;
}

.participants-avatar-scroll-row::-webkit-scrollbar {
  display: none;
}

.participant-avatar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 56px;
  flex-shrink: 0;
}

.avatar-circle-box {
  position: relative;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #f8fafc;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
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
  background-color: #475569;
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
  background-color: #ef4444;
}

.participant-name-text {
  margin-top: 4px;
  font-size: 12px;
  font-weight: 700;
  color: #0f172a;
  text-align: center;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

/* 카카오페이 1:1 보더리스 라인 리스트 */
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

.selected-check-area {
  display: flex;
  align-items: center;
  font-size: 22px;
}

.sel-ic {
  color: #ffbc2e;
}

.unsel-ic {
  color: #94a3b8;
}
</style>
