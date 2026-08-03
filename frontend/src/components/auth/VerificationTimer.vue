<template>
  <span class="timer">{{ formattedTime }}</span>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';

const props = defineProps({
  seconds: {
    type: Number,
    default: 180,
  },
});

const emit = defineEmits(['expired']);
const remainingSeconds = ref(props.seconds);
let timerId = null;

const formattedTime = computed(() => {
  const minutes = Math.floor(remainingSeconds.value / 60);
  const seconds = remainingSeconds.value % 60;
  return `${minutes}:${String(seconds).padStart(2, '0')}`;
});

// 타이머 시작
const startTimer = () => {
  clearInterval(timerId);
  remainingSeconds.value = props.seconds;

  timerId = setInterval(() => {
    if (remainingSeconds.value > 0) remainingSeconds.value -= 1;

    if (remainingSeconds.value === 0) {
      clearInterval(timerId);
      emit('expired');
    }
  }, 1000);
};

watch(() => props.seconds, startTimer);
onMounted(startTimer);
onBeforeUnmount(() => clearInterval(timerId));
</script>

<style scoped>
.timer {
  color: #e34b4b;
  font-size: 15px;
  font-weight: 600;
}
</style>