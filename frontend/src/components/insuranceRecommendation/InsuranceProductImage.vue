<template>
  <img
    :src="src"
    :alt="alt"
    @load="removeBackground"
    @error="emit('error', $event)"
  />
</template>

<script setup>
import { onBeforeUnmount } from 'vue';

const props = defineProps({
  src: {
    type: String,
    required: true,
  },
  alt: {
    type: String,
    default: '',
  },
});

const emit = defineEmits(['error']);

const MAX_IMAGE_PROCESSING_SIDE = 1200;
const BACKGROUND_MIN_CHANNEL = 218;
const BACKGROUND_MAX_CHANNEL_GAP = 24;
const processedImageUrls = new Set();

/* 이미지 테두리와 연결된 흰색·연회색 배경만 투명하게 만든다. */
const removeBackground = (event) => {
  const image = event.currentTarget;
  if (
    !image ||
    image.dataset.backgroundSource === props.src ||
    image.dataset.backgroundProcessing === 'true'
  ) {
    return;
  }

  image.dataset.backgroundProcessing = 'true';

  try {
    const naturalWidth = image.naturalWidth;
    const naturalHeight = image.naturalHeight;
    if (!naturalWidth || !naturalHeight) {
      delete image.dataset.backgroundProcessing;
      return;
    }

    const scale = Math.min(
      1,
      MAX_IMAGE_PROCESSING_SIDE / Math.max(naturalWidth, naturalHeight),
    );
    const width = Math.max(1, Math.round(naturalWidth * scale));
    const height = Math.max(1, Math.round(naturalHeight * scale));
    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d', { willReadFrequently: true });

    if (!context) {
      delete image.dataset.backgroundProcessing;
      return;
    }

    canvas.width = width;
    canvas.height = height;
    context.drawImage(image, 0, 0, width, height);

    const imageData = context.getImageData(0, 0, width, height);
    const { data } = imageData;
    const pixelCount = width * height;
    const visited = new Uint8Array(pixelCount);
    const queue = new Int32Array(pixelCount);
    let queueHead = 0;
    let queueTail = 0;

    const isBackgroundPixel = (pixelIndex) => {
      const offset = pixelIndex * 4;
      if (data[offset + 3] === 0) return true;

      const red = data[offset];
      const green = data[offset + 1];
      const blue = data[offset + 2];
      const minimum = Math.min(red, green, blue);
      const maximum = Math.max(red, green, blue);

      return (
        minimum >= BACKGROUND_MIN_CHANNEL &&
        maximum - minimum <= BACKGROUND_MAX_CHANNEL_GAP
      );
    };

    const enqueue = (pixelIndex) => {
      if (
        pixelIndex < 0 ||
        pixelIndex >= pixelCount ||
        visited[pixelIndex] ||
        !isBackgroundPixel(pixelIndex)
      ) {
        return;
      }

      visited[pixelIndex] = 1;
      queue[queueTail] = pixelIndex;
      queueTail += 1;
    };

    for (let x = 0; x < width; x += 1) {
      enqueue(x);
      enqueue((height - 1) * width + x);
    }
    for (let y = 0; y < height; y += 1) {
      enqueue(y * width);
      enqueue(y * width + width - 1);
    }

    while (queueHead < queueTail) {
      const pixelIndex = queue[queueHead];
      queueHead += 1;
      data[pixelIndex * 4 + 3] = 0;

      const x = pixelIndex % width;
      const y = Math.floor(pixelIndex / width);
      if (x > 0) enqueue(pixelIndex - 1);
      if (x + 1 < width) enqueue(pixelIndex + 1);
      if (y > 0) enqueue(pixelIndex - width);
      if (y + 1 < height) enqueue(pixelIndex + width);
    }

    context.putImageData(imageData, 0, 0);
    canvas.toBlob((blob) => {
      delete image.dataset.backgroundProcessing;
      if (!blob || !image.isConnected) return;

      const processedUrl = URL.createObjectURL(blob);
      processedImageUrls.add(processedUrl);
      image.dataset.backgroundSource = props.src;
      image.src = processedUrl;
    }, 'image/png');
  } catch (error) {
    /* 외부 이미지의 CORS 제한이 있으면 가공하지 않고 원본을 유지한다. */
    delete image.dataset.backgroundProcessing;
    image.dataset.backgroundSource = props.src;
  }
};

onBeforeUnmount(() => {
  processedImageUrls.forEach((url) => URL.revokeObjectURL(url));
  processedImageUrls.clear();
});
</script>
