<template>
  <div
    class="image-slider"
    @pointerdown="touchStart"
    @pointermove="touchMove"
    @pointerup="touchEnd"
    @pointercancel="touchEnd"
  >
    <div
      class="slider-track"
      :style="{
        transform: `translateX(-${currentIndex * 100}%)`,
      }"
    >
      <div v-for="image in images" :key="image.imageId" class="slide">
        <img :src="image.url" class="feed-image" />
      </div>
    </div>

    <div v-if="images.length > 1" class="indicator">
      <span
        v-for="(image, index) in images"
        :key="image.imageId"
        class="dot"
        :class="{ active: index === currentIndex }"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  images: {
    type: Array,
    required: true,
  },
});

const currentIndex = ref(0);

const nextImage = () => {
  if (currentIndex.value < props.images.length - 1) {
    currentIndex.value++;
  }
};

const prevImage = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
  }
};

let startX = 0;
let endX = 0;

const touchStart = (e) => {
  startX = e.clientX;
  e.currentTarget.setPointerCapture(e.pointerId);
};

const touchMove = (e) => {
  endX = e.clientX;
};

const touchEnd = () => {
  const diff = startX - endX;

  if (diff > 50) nextImage();
  if (diff < -50) prevImage();

  startX = 0;
  endX = 0;
};
</script>

<style scoped>
.image-slider {
  position: relative;
  margin-top: 0px;
  width: 100%;
  aspect-ratio: 1/1;
  overflow: hidden;
  border-radius: 16px;
  background: #eee;
  touch-action: pan-y;
  user-select: none;
}

.slider-track {
  display: flex;
  height: 100%;
  transition: transform 0.3s ease;
}

.slide {
  min-width: 100%;
  height: 100%;
}

.feed-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  user-select: none;
  -webkit-user-drag: none;
}

.indicator {
  position: absolute;
  left: 50%;
  bottom: 12px;
  transform: translateX(-50%);
  display: flex;
  gap: 6px;
}

.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
}

.dot.active {
  background: white;
}
</style>
