<template>
  <div class="page">
    <div>정산 지불 페이지</div>

    <button @click="onclickPayment()">지불하기</button>

    <div v-if="settlement">
      {{ settlement.content }}
    </div>

    <div v-if="settlement">
      {{
        `${settlement.members
          .find((member) => member.userId === userId)
          ?.amount.toLocaleString()}원`
      }}

      <div>
        {{
          `${
            settlement.members.find((member) => member.userId === userId)
              ?.receiver.nickname
          }님`
        }}
        이
        {{ `${settlement.profileSimpleVO.nickname}님` }} 에게
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useSettlementStore } from '@/stores/settlement';

import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
const userId = userStore.userId;

const router = useRouter();
const route = useRoute();

const settlementStore = useSettlementStore();

const settlementId = Number(route.params.settlementId);

const settlement = computed(() =>
  settlementStore.settlements.find(
    (item) => item.settlementId === settlementId,
  ),
);

const onclickPayment = async () => {
  try {
    await settlementStore.payment({
      settlementId,
      userId,
    });

    router.push('/mypage');
  } catch (e) {
    console.log(e.response?.data);
  }
};
</script>

<style scoped>
.page {
  width: 100%;
  min-height: 100%;
}
</style>
