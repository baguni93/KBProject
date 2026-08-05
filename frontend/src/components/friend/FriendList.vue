<template>
  <div class="friend-list">
    <FriendListCard
      v-for="friend in friends"
      :key="friend.friendId"
      :profile-image-name="friend.receiver.profileImageName"
      :nickname="friend.receiver.nickname"
      :friendId="friend.friendId"
      :friendUserId="friend.friendUserId"
    />
  </div>

  <EmptyList v-if="friends.length === 0" desc="보낸 친구 요청이 없습니다." />
</template>

<script setup>
import FriendListCard from '../friend/FriendListCard.vue';
import EmptyList from '@/components/common/EmptyList.vue';
const props = defineProps({
  friends: {
    type: Array,
    required: true,
  },
});
</script>

<style scoped>
.friend {
  display: flex;
  align-items: center;

  gap: 12px;

  padding: 12px 0;
}

.profile {
  width: 45px;
  height: 45px;

  border-radius: 50%;
}

.info {
  flex: 1;
}

.nickname {
  font-weight: 600;
}

button {
  border: none;

  background: #eee;

  border-radius: 20px;

  padding: 7px 14px;
}

.friend-list {
  display: flex;
  flex-direction: column;
  gap: 12px; /* 카드 사이 위아래 간격 */
}
.empty {
  text-align: center;

  color: #999;

  padding: 30px 0;

  font-size: 14px;
}
</style>
