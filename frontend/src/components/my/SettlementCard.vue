<template>
  <div class="card">
    <CardProfile
      :profile-image-name="settlement.profileSimpleVO.profileImageName"
      :nickname="settlement.profileSimpleVO.nickname"
    >
      <div class="date">
        {{ formatDate(settlement.createdAt) }}
      </div>
    </CardProfile>
    <!-- 내용 -->

    <div class="date">
      {{ settlement.requesterId === userId ? '정산 요청' : '정산 요청 받음' }}
    </div>

    <div class="content">
      {{ settlement.content }}
    </div>

    <div>
      <div class="desc">총 정산 금액 {{ settlement.totalAmount }}원</div>
      <div class="desc">내가 낼 금액 {{ settlement.totalAmount }}원</div>
      <div class="desc">참여 인원 {{ settlement.members.length + 1 }}명</div>
      <div class="desc">
        진행 사항 {{ completeMembers.length + 1 }}/{{
          settlement.members.length + 1
        }}명
      </div>
      <CardProfile
        v-for="memeber in settlement.members"
        :key="memeber.userId"
        :profile-image-name="memeber.receiver.profileImageName"
        :nickname="memeber.receiver.nickname"
      >
        <div class="desc">완료</div>
        <div class="desc">미납</div>
      </CardProfile>
    </div>

    <button class="pay-btn">지불하기</button>
    <button class="pay-btn">리마인드 알림</button>
    <button class="pay-btn">요청 취소</button>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import CardProfile from '../common/CardProfile.vue';

let userId = 3;

const props = defineProps({
  settlement: {
    type: Object,
    required: true,
  },
});

const completeMembers = computed(() =>
  props.settlement.members.filter((x) => x.status === 'COMPLETE'),
);

const formatDate = (time) => {
  const now = new Date();
  const date = new Date(time);

  const diff = now - date;

  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);

  if (seconds < 60) return '방금 전';
  if (minutes < 60) return `${minutes}분 전`;
  if (hours < 24) return `${hours}시간 전`;

  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};
</script>

<style scoped>
.card {
  width: 100%;

  padding: 20px;

  border-radius: 16px;

  background: rgb(136, 226, 238);

  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);

  position: relative;
}

.content {
  margin: 10px 0px 10px 5px;

  font-size: 18px;

  font-weight: bold;
}
</style>
