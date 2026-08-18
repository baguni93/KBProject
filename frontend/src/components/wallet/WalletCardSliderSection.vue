<template>
  <div>
    <template v-if="!hasRepresentativeCard">
      <div class="center-graphic-section">
        <div class="outer-dashed-ring">
          <div class="inner-dashed-ring"></div>
          <div
            class="tilted-dashed-card"
            @click="$router.push('/wallet/card/add')"
          >
            <div class="plus-icon-circle text-15-bold">+</div>
            <span class="dashed-card-label text-13-bold"
              >대표 카드 지정 필요</span
            >
          </div>
        </div>
      </div>

      <div class="bottom-no-card-area">
        <div class="no-card-notice-card">
          <p class="notice-main-text text-15-bold">
            결제를 하려면 먼저 실물 카드를 등록해 주세요
          </p>
          <p class="notice-sub-text text-13-bold">
            대표 카드가 지정되지 않았습니다.
          </p>
        </div>
      </div>
    </template>

    <template v-else>
      <div
        class="spay-carousel-deck"
        @touchstart="$emit('touchStart', $event)"
        @touchmove="$emit('touchMove', $event)"
        @touchend="$emit('touchEnd')"
      >
        <div class="carousel-track">
          <div
            v-for="(card, index) in registeredCards"
            :key="index"
            class="carousel-card-item"
            :style="getCard3DStyle(index)"
            @click="$emit('cardClick', index)"
          >
            <img
              v-if="getCardImg(card)"
              :src="getCardImg(card)"
              class="card-plate-bg-img"
              alt="card plate"
              @error="(e) => (e.target.style.display = 'none')"
            />
            <template v-if="!getCardImg(card)">
              <div class="card-plate-overlay"></div>

              <div class="card-plate-top">
                <div class="chip-ic-sm"></div>
                <span v-if="index === 0" class="rep-badge text-13-bold"
                  >대표카드</span
                >
                <span v-else class="kb-badge-sm text-13-bold"
                  ><i class="fa-solid fa-shield-halved"></i>
                  KB국민카드</span
                >
              </div>

              <div class="card-plate-bottom-info">
                <div class="card-brand-label text-15-bold">
                  {{ card.cardAlias || card.cardName || "KB국민카드" }}
                </div>
                <div class="card-number-label text-15-bold">
                  {{ formatMaskedCardNum(card.cardNum) }}
                </div>
              </div>
            </template>
            <template v-else>
              <span
                v-if="index === 0"
                class="rep-badge text-13-bold"
                style="position: absolute; top: 12px; right: 12px; z-index: 10;"
                >대표카드</span
              >
            </template>
          </div>

          <div
            class="carousel-card-item card-add-deck-item"
            :style="getCard3DStyle(registeredCards.length)"
            @click="$emit('addCardClick')"
          >
            <div class="add-deck-content">
              <div class="add-icon-circle">
                <i class="fa-solid fa-plus brand-ic"></i>
              </div>
              <span class="text-18-bold">새 카드 등록하기</span>
              <span class="text-13">터치하여 신규 카드 추가</span>
            </div>
          </div>
        </div>

        <div class="indicator-dots">
          <span
            v-for="(_, idx) in registeredCards.length + 1"
            :key="idx"
            class="dot"
            :class="{ active: currentCardIdx === idx }"
            @click="$emit('selectDot', idx)"
          ></span>
        </div>

        <div class="hint-text-line text-13">
          <i class="fa-solid fa-hand-pointer brand-ic"></i> 좌우로
          스와이프하거나 카드를 터치하면 결제가 활성화됩니다.
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
defineProps({
  hasRepresentativeCard: {
    type: Boolean,
    default: false,
  },
  registeredCards: {
    type: Array,
    default: () => [],
  },
  currentCardIdx: {
    type: Number,
    default: 0,
  },
  getCard3DStyle: {
    type: Function,
    required: true,
  },
  getCardImg: {
    type: Function,
    required: true,
  },
  formatMaskedCardNum: {
    type: Function,
    required: true,
  },
});

defineEmits([
  "touchStart",
  "touchMove",
  "touchEnd",
  "cardClick",
  "addCardClick",
  "selectDot",
]);
</script>

<style scoped>
.spay-carousel-deck {
  position: relative;
  width: 100%;
  margin: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 14px 0 10px;
  touch-action: pan-y;
  overflow: visible;
}

.carousel-track {
  position: relative;
  width: 100%;
  height: 240px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.carousel-card-item {
  position: absolute;
  width: 340px;
  height: 214px;
  border-radius: 20px;
  box-shadow: 0 22px 45px rgba(0, 0, 0, 0.22), 0 0 26px rgba(255, 188, 46, 0.25);
  transition:
    transform 0.35s cubic-bezier(0.25, 1, 0.5, 1),
    opacity 0.35s ease,
    filter 0.35s ease;
  overflow: hidden;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  cursor: pointer;
  user-select: none;
}

.card-plate-bg-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 1;
}

.card-plate-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(0, 0, 0, 0.1) 0%,
    rgba(0, 0, 0, 0.5) 100%
  );
  z-index: 2;
}

.card-plate-top {
  position: relative;
  z-index: 3;
  padding: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chip-ic-sm {
  width: 32px;
  height: 22px;
  border-radius: 4px;
  background: #ffe58f;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.rep-badge {
  background-color: var(--color-primary, #ffbc2e);
  color: var(--color-text-main, #111111);
  padding: 2px 8px;
  border-radius: 6px;
}

.kb-badge-sm {
  color: #ffffff;
}

.card-plate-bottom-info {
  position: absolute;
  bottom: 14px;
  left: 14px;
  right: 14px;
  z-index: 3;
  text-align: left;
}

.card-brand-label {
  color: #ffffff;
}

.card-number-label {
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 1px;
}

.card-add-deck-item {
  background: #ffffff !important;
  border: 2.5px dashed #cbd5e1;
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.08);
}

.add-deck-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  gap: 6px;
}

.add-deck-content .text-18-bold {
  color: #0f172a !important;
}

.add-deck-content .text-13 {
  color: #64748b !important;
}

.add-icon-circle {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background-color: #fff8e5;
  border: 1.5px solid #ffbc2e;
  color: #d97706;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  margin-bottom: 2px;
}

.indicator-dots {
  display: flex;
  gap: 4px;
  margin-top: 12px;
}

.indicator-dots .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--color-border-main, #dddddd);
  cursor: pointer;
  transition: all 0.2s ease;
}

.indicator-dots .dot.active {
  background-color: var(--color-primary, #ffbc2e);
  width: 16px;
  border-radius: 10px;
}

.hint-text-line {
  color: var(--color-text-sub, #777777);
  text-align: center;
  margin-top: 20px;
  margin-bottom: 12px;
  background: transparent;
}

.center-graphic-section {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.outer-dashed-ring {
  width: 180px;
  height: 180px;
  border: 2px dashed #e0e0e0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.inner-dashed-ring {
  width: 140px;
  height: 140px;
  border: 1px dashed #ededed;
  border-radius: 50%;
  position: absolute;
}

.tilted-dashed-card {
  width: 150px;
  height: 95px;
  border: 2px dashed #ffbc00;
  border-radius: 14px;
  background-color: rgba(255, 188, 0, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
  z-index: 2;
  transform: rotate(-6deg);
  transition: transform 0.2s ease;
}

.tilted-dashed-card:hover {
  transform: rotate(0deg) scale(1.05);
}

.plus-icon-circle {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background-color: #ffbc00;
  color: #111111;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dashed-card-label {
  color: #ffbc00;
}

.bottom-no-card-area {
  width: 100%;
}

.no-card-notice-card {
  background-color: #f8f9fa;
  padding: 14px;
  border-radius: 10px;
  text-align: center;
}

.notice-main-text {
  margin: 0 0 4px;
  color: #111111;
}

.notice-sub-text {
  margin: 0;
  color: #e53935;
}
</style>
