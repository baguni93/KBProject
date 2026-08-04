<template>
  <section class="profile-header">
    <img :src="profile.url" class="profile-image" />

    <div class="profile-info">
      <h2>
        {{ profile.nickname }}
      </h2>

      <p>
        {{ profile.introduction }}
      </p>

      <slot />
    </div>
  </section>
</template>

<script setup>
defineProps({
  profile: {
    type: Object,
    required: true,
  },
});
</script>

<style scoped>
.profile-header {
  display: flex;
  flex-direction: column;

  align-items: center;

  padding: 24px 16px;
}

.profile-image {
  width: 90px;
  height: 90px;

  border-radius: 50%;

  object-fit: cover;

  margin-bottom: 12px;
}

.profile-info {
  display: flex;

  flex-direction: column;

  align-items: center;

  gap: 8px;
}

.profile-info h2 {
  margin: 0;

  font-size: 22px;
  font-weight: 700;
}

.profile-info p {
  margin: 0;

  color: #000000;

  font-size: 14px;

  text-align: center;
}
</style>
