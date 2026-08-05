<template>
  <nav class="bottom-nav">
    <RouterLink
      v-for="menu in config.menus"
      :key="menu.title"
      :to="menu.url"
      class="nav-item"
      :class="{ active: isActive(menu.url) }"
    >
      <div class="icon-box" :class="{ center: menu.title === '결제' }">
        <i :class="menu.icon"></i>
      </div>

      <span>{{ menu.title }}</span>
    </RouterLink>
  </nav>
</template>
<!-- //주석처리 -->
<script setup>
import { useRoute } from 'vue-router';
import config from '@/config';

const route = useRoute();

const isActive = (url) => {
  // 홈은 정확히 일치
  if (url === '/') {
    return route.path === '/';
  }

  // 하위 경로까지 활성화
  return route.path.startsWith(url);
};
</script>

<style scoped>
.bottom-nav {
  width: 100%;
  height: 75px;

  background: #fff;

  display: flex;
  justify-content: space-around;
  align-items: center;

  border-top: 1px solid #eee;

  flex-shrink: 0;
}

.nav-item {
  flex: 1;
  height: 100%;

  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 5px;

  text-decoration: none;
  color: #666;

  transition: color 0.2s ease;
}

.icon-box {
  width: 35px;
  height: 35px;

  display: flex;
  justify-content: center;
  align-items: center;
}

.icon-box i {
  font-size: 24px;
  transition: all 0.2s ease;
}

.nav-item span {
  font-size: 12px;
  font-weight: 500;
}

/* 활성화 */
.nav-item.active {
  color: #ffbf00;
}

.nav-item.active .icon-box:not(.center) i {
  color: #ffbf00;
}

/* 가운데 결제 버튼 */
.icon-box.center {
  width: 58px;
  height: 58px;

  margin-top: -28px;

  background: #ffbf00;
  color: white;

  border-radius: 20px;

  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}

.icon-box.center i {
  font-size: 30px;
}

/* 가운데 버튼은 항상 흰색 유지 */
.nav-item.active .icon-box.center {
  background: #ffbf00;
  color: white;
}
</style>
