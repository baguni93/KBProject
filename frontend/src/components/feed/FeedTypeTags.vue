<template>
  <div class="tag" :class="feedTypeClass">[{{ feedTypeText }}]</div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  feed: {
    type: Object,
    required: true,
  },
});

const feedTypeMap = {
  PAYMENT: {
    text: '결제',
    class: 'payment',
  },

  TRANSFER: {
    text: '송금',
    class: 'transfer',
  },

  SETTLEMENT: {
    text: '정산 완료',
    class: 'settlement',
  },

  CARD: {
    text: '커스텀 카드',
    class: 'customcard',
  },

  EVENT: {
    text: '이벤트',
    class: 'event',
  },

  ANALYSIS: {
    text: '소비패턴 분석',
    class: 'analysis',
  },
};

const feedTypeText = computed(() => {
  return feedTypeMap[props.feed.feedType]?.text ?? '';
});

const feedTypeClass = computed(() => {
  return feedTypeMap[props.feed.feedType]?.class ?? '';
});
</script>

<style scoped>
.tag {
  margin: 2px 0 2px 0px;
  font-size: 15px;
  font-weight: 700;
}

.payment {
  color: blue;
}

.transfer {
  color: green;
}

.settlement {
  color: orange;
}

.customcard {
  color: purple;
}

.event {
  color: greenyellow;
}

.analysis {
  color: blue;
}
</style>
