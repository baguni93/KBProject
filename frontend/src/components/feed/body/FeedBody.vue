<template>
  <component :is="componentMap[feed.feedType]" :feed="feed" />
</template>

<script setup>
import PaymentFeedBody from './PaymentFeedBody.vue';
import TransferFeedBody from './TransferFeedBody.vue';
import SettlementFeedBody from './SettlementFeedBody.vue';
import EventFeedBody from './EventFeedBody.vue';
import CardFeedBody from './CardFeedBody.vue';
import AnalysisFeedBody from './AnalysisFeedBody.vue';

defineProps({
  feed: {
    type: Object,
    required: true,
  },
});

const componentMap = {
  PAYMENT: PaymentFeedBody,
  TRANSFER: TransferFeedBody,
  SETTLEMENT: SettlementFeedBody,
  EVENT: EventFeedBody,
  CARD: CardFeedBody,
  ANALYSIS: AnalysisFeedBody,
};
</script>
