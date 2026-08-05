<template>
  <div class="page app-page">
    <header class="header">앱 화면</header>

    <main class="body">내용 영역</main>
  </div>
</template>

<style scoped>
.page {
  width: 100%;

  min-height: 100%;

  display: flex;

  flex-direction: column;

  box-sizing: border-box;
}

.app-page {
  background: #f5f5f5;
}

.header {
  padding: 20px;

  background: white;

  font-weight: bold;
}

.body {
  flex: 1;

  padding: 20px;
}
</style>
