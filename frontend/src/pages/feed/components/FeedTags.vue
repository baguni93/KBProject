<template>
  <div class="feed-tags">
    <span
      v-for="(tag, index) in tags"
      :key="index"
      class="tag"
      :class="{ clickable: tag.route && !tag.icon }"
      @click="handleClick(tag)"
    >
      <i v-if="tag.icon" :class="tag.icon"></i>

      {{ tag.value }}
    </span>
  </div>
</template>

<script setup>
defineProps({
  tags: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['tag-click']);

const handleClick = (tag) => {
  // 아이콘이 있는 태그는 클릭하지 않음
  if (!tag.route || tag.icon) return;

  emit('tag-click', tag);
};
</script>

<style scoped>
.feed-tags {
  display: flex;
  gap: 6px;
  margin-top: 6px;
  flex-wrap: wrap;
}

.tag {
  font-size: 11px;
  font-weight: 600;
  color: #0066cc;
}

.tag.clickable {
  cursor: pointer;
}

.tag.clickable:hover {
  text-decoration: underline;
}

.tag i {
  margin-right: -1px;
  vertical-align: -1px;
}
</style>
