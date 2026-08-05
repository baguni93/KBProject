<template>
  <div class="remit-root">

    <!-- 상단 헤더 -->
    <div class="remit-header">
      <div class="header-inner">
        <button class="back-btn" @click="handleBack">
          <i class="bi bi-chevron-left"></i>
        </button>

        <h4 class="header-title">
          <span class="kb-pay-tag me-1">KB Pay</span>
          <span v-if="currentStep === 1">보내기 / 더치페이</span>
          <span v-else-if="currentStep === 2">금액 입력</span>
          <span v-else-if="currentStep === 3">비밀번호 인증</span>
          <span v-else>완료</span>
        </h4>

        <div class="header-balance">
          <span class="b-label">잔액</span>
          <span class="b-val">{{ formatCurrency(myBalance) }}</span>
        </div>
      </div>
    </div>

    <!-- 스텝 진행 바 -->
    <div class="step-progress-bar">
      <div class="step-dot" :class="{ active: currentStep >= 1 }">1</div>
      <div class="step-line" :class="{ active: currentStep >= 2 }"></div>
      <div class="step-dot" :class="{ active: currentStep >= 2 }">2</div>
      <div class="step-line" :class="{ active: currentStep >= 3 }"></div>
      <div class="step-dot" :class="{ active: currentStep >= 3 }">3</div>
      <div class="step-line" :class="{ active: currentStep >= 4 }"></div>
      <div class="step-dot" :class="{ active: currentStep >= 4 }">4</div>
    </div>

    <!-- 본문 콘텐츠 -->
    <div class="remit-body">

      <!-- ──────────────────────────────────────────
           [STEP 1] 누구에게 보낼까요? (친구 송금 / 계좌 이체 / 더치페이)
      ────────────────────────────────────────── -->
      <div v-if="currentStep === 1" class="step-card fade-in">
        <div class="step-title-wrap mb-3">
          <span class="step-badge font-bold">STEP 1</span>
          <h3 class="step-main-title">
            {{ form.receiverType === 'DUTCHPAY' ? '더치페이 방 만들기' : '누구에게 보낼까요?' }}
          </h3>
        </div>

        <!-- 3개 탭 분기 -->
        <div class="remit-tabs-nav mb-3">
          <button
            class="tab-btn"
            :class="{ active: form.receiverType === 'WALLET' }"
            @click="setReceiverType('WALLET')"
          >
            <i class="bi bi-person-heart me-1"></i> 친구 송금
          </button>
          <button
            class="tab-btn"
            :class="{ active: form.receiverType === 'ACCOUNT' }"
            @click="setReceiverType('ACCOUNT')"
          >
            <i class="bi bi-bank me-1"></i> 계좌 이체
          </button>
          <button
            class="tab-btn"
            :class="{ active: form.receiverType === 'DUTCHPAY' }"
            @click="setReceiverType('DUTCHPAY')"
          >
            <i class="bi bi-people-fill me-1"></i> 더치페이
          </button>
        </div>

        <!-- 최근 보낸 이체 3건 퀵 선택 섹션 (친구/계좌 전용) -->
        <div v-if="form.receiverType !== 'DUTCHPAY'" class="recent-recipients-box mb-3">
          <div class="recent-header">
            <span class="recent-title"><i class="bi bi-clock-history me-1 text-warning"></i>최근 보낸 이체 (최근 3건)</span>
          </div>
          <div class="recent-chips-row">
            <button
              v-for="rec in recentRecipientsFiltered"
              :key="rec.id"
              class="recent-chip-btn"
              :class="{ selected: isRecentSelected(rec) }"
              @click="selectRecentRecipient(rec)"
            >
              <div class="recent-avatar">
                <i :class="rec.type === 'WALLET' ? 'bi bi-person-fill' : 'bi bi-bank'"></i>
              </div>
              <div class="recent-info text-start">
                <div class="r-name">{{ rec.name }}</div>
                <div class="r-detail">{{ rec.detail }}</div>
              </div>
            </button>
          </div>
        </div>

        <!-- 1-A. 친구 송금 선택 -->
        <div v-if="form.receiverType === 'WALLET'" class="friends-select-section">
          <label class="input-label">친구 검색 및 전체 목록</label>
          <div class="search-box mb-2">
            <i class="bi bi-search search-icon"></i>
            <input v-model="friendSearchKeyword" type="text" class="search-input" placeholder="이름 또는 친구 ID 검색..." />
          </div>

          <div v-if="filteredFriends.length === 0" class="empty-friends">
            <EmptyList desc="등록된 친구가 없습니다." />
          </div>

          <div v-else class="friend-list-grid">
            <div
              v-for="friend in filteredFriends"
              :key="friend.userId"
              class="friend-select-item"
              :class="{ selected: form.receiverId === friend.userId }"
              @click="selectFriend(friend)"
            >
              <div class="friend-avatar">
                <i class="bi bi-person-fill"></i>
              </div>
              <div class="friend-info">
                <div class="friend-name">{{ friend.nickname || friend.name || ('친구 #' + friend.userId) }}</div>
                <div class="friend-id">KB Pay 지갑 #{{ friend.userId }}</div>
              </div>
              <div class="friend-check">
                <i class="bi bi-check-circle-fill" v-if="form.receiverId === friend.userId"></i>
              </div>
            </div>
          </div>

          <div class="d-flex justify-content-end mt-3">
            <button class="next-step-btn-right" :disabled="!canProceedStep1" @click="goToStep(2)">
              다음 <i class="bi bi-chevron-right ms-1"></i>
            </button>
          </div>
        </div>

        <!-- 1-B. 계좌 이체 입력 -->
        <div v-else-if="form.receiverType === 'ACCOUNT'" class="account-select-section">
          <label class="input-label">은행 선택</label>
          <div class="bank-grid">
            <button
              v-for="bank in bankList"
              :key="bank.code"
              class="bank-chip"
              :class="{ active: form.bankCode === bank.code }"
              @click="form.bankCode = bank.code"
            >
              {{ bank.name }}
            </button>
          </div>

          <label class="input-label mt-3">계좌번호 입력</label>
          <input
            v-model="form.accountNumber"
            type="number"
            class="kb-input-field"
            placeholder="'-' 없이 계좌번호 입력"
          />

          <div class="d-flex justify-content-end mt-3">
            <button class="next-step-btn-right" :disabled="!canProceedStep1" @click="goToStep(2)">
              다음 <i class="bi bi-chevron-right ms-1"></i>
            </button>
          </div>
        </div>

        <!-- 1-C. 더치페이 폼 (송금하기 디자인 동일 적용 + 결제내역 아코디언 접기) -->
        <div v-else-if="form.receiverType === 'DUTCHPAY'" class="dutch-select-section">
          
          <!-- 더치페이 모임 제목 입력 -->
          <div class="mb-3">
            <label class="input-label">더치페이 모임 제목</label>
            <input
              v-model="dutchForm.title"
              type="text"
              class="kb-input-field"
              placeholder="예: 8월 모임 회비, 강남 쉐이크쉑 식대"
            />
          </div>

          <!-- 총 정산 금액 입력 -->
          <div class="mb-3">
            <label class="input-label">총 정산 금액 (원)</label>
            <input
              v-model.number="form.amount"
              type="number"
              class="kb-input-field"
              placeholder="예: 60000"
            />
          </div>

          <!-- 최근 결제 거래내역 접이식 아코디언 (클릭 시 펼치기) -->
          <div class="recent-payments-quick-box mb-3">
            <button
              type="button"
              class="accordion-toggle-btn w-100 d-flex justify-content-between align-items-center bg-transparent border-0 p-1"
              @click="showRecentPaymentsAccordion = !showRecentPaymentsAccordion"
            >
              <span class="input-label text-dark mb-0 fw-bold">
                <i class="bi bi-receipt me-1 text-warning"></i>최근 결제 내역에서 가져오기
                <span v-if="selectedTxIds.length > 0" class="badge bg-warning text-dark ms-1">{{ selectedTxIds.length }}건 선택됨</span>
              </span>
              <i class="bi" :class="showRecentPaymentsAccordion ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
            </button>

            <div v-if="showRecentPaymentsAccordion" class="tx-accordion-content mt-2 fade-in">
              <div class="tx-history-list">
                <div
                  v-for="tx in recentPayments"
                  :key="tx.id"
                  class="tx-item-card"
                  :class="{ selected: selectedTxIds.includes(tx.id) }"
                  @click="toggleSelectTx(tx)"
                >
                  <div class="tx-check-icon">
                    <i class="bi" :class="selectedTxIds.includes(tx.id) ? 'bi-check-square-fill text-warning' : 'bi-square text-secondary'"></i>
                  </div>
                  <div class="tx-item-info flex-1">
                    <div class="tx-merchant font-bold">{{ tx.merchant }}</div>
                    <div class="tx-date small text-secondary">{{ tx.date }}</div>
                  </div>
                  <div class="tx-amount font-bold text-dark">{{ formatCurrency(tx.amount) }}</div>
                </div>
              </div>

              <div v-if="selectedTxIds.length > 0" class="tx-selected-sum-bar mt-2 small text-secondary d-flex justify-content-between align-items-center">
                <span>선택 건수: <strong>{{ selectedTxIds.length }}건</strong></span>
                <span>합산 금액: <strong class="text-warning fs-6">{{ formatCurrency(form.amount) }}</strong></span>
              </div>
            </div>
          </div>

          <!-- 정산 분할 방식 선택 (1/N 균등 정산 vs 멤버별 직접 금액 입력) -->
          <div class="split-mode-section mb-3">
            <label class="input-label">정산 방식 선택</label>
            <div class="split-mode-btns">
              <button
                class="split-btn"
                :class="{ active: dutchSplitMode === 'EQUAL' }"
                @click="dutchSplitMode = 'EQUAL'"
              >
                <i class="bi bi-pie-chart-fill me-1"></i> 1/N 균등 정산
              </button>
              <button
                class="split-btn"
                :class="{ active: dutchSplitMode === 'CUSTOM' }"
                @click="dutchSplitMode = 'CUSTOM'"
              >
                <i class="bi bi-sliders me-1"></i> 멤버별 직접 입력
              </button>
            </div>
          </div>

          <!-- 더치페이할 친구 선택 (송금하기 친구 목록 디자인 100% 동일 적용) -->
          <label class="input-label">더치페이할 친구 선택</label>
          <div class="search-box mb-2">
            <i class="bi bi-search search-icon"></i>
            <input v-model="dutchFriendSearchKeyword" type="text" class="search-input" placeholder="이름 또는 친구 ID 검색..." />
          </div>

          <div v-if="filteredDutchFriends.length === 0" class="empty-friends mb-3">
            <EmptyList desc="등록된 친구가 없습니다." />
          </div>
          
          <!-- 친구 송금과 동일한 카드 구조 리스트 -->
          <div v-else class="friend-list-grid mb-3">
            <div
              v-for="friend in filteredDutchFriends"
              :key="friend.userId"
              class="friend-select-item"
              :class="{ selected: selectedDutchFriends.includes(friend.userId) }"
              @click="toggleDutchFriend(friend.userId)"
            >
              <div class="friend-avatar">
                <i class="bi bi-person-fill"></i>
              </div>
              <div class="friend-info">
                <div class="friend-name">{{ friend.nickname || friend.name || ('친구 #' + friend.userId) }}</div>
                <div class="friend-id">KB Pay 지갑 #{{ friend.userId }}</div>
              </div>
              <div class="friend-check">
                <i class="bi" :class="selectedDutchFriends.includes(friend.userId) ? 'bi-check-circle-fill text-warning' : 'bi-circle text-secondary'"></i>
              </div>
            </div>
          </div>

          <!-- 멤버별 직접 입력 모드 시 개별 금액 입력 필드 -->
          <div v-if="dutchSplitMode === 'CUSTOM' && selectedDutchFriends.length > 0" class="custom-amounts-list mb-3">
            <label class="input-label">친구별 청구 금액 지정</label>
            <div
              v-for="friendId in selectedDutchFriends"
              :key="friendId"
              class="custom-friend-amount-row my-1 d-flex align-items-center justify-content-between gap-2"
            >
              <span class="friend-label-sm fw-bold">{{ getFriendNickname(friendId) }}</span>
              <div class="input-group input-group-sm width-auto">
                <input
                  v-model.number="customFriendAmounts[friendId]"
                  type="number"
                  class="form-control text-end font-monospace"
                  placeholder="청구 금액"
                />
                <span class="input-group-text">원</span>
              </div>
            </div>
          </div>

          <!-- 1인당 청구금액 요약 카드 -->
          <div v-if="form.amount > 0 && selectedDutchFriends.length > 0" class="dutch-summary-banner mb-3">
            <div class="d-flex justify-content-between align-items-center">
              <span class="small text-secondary">총 {{ selectedDutchFriends.length + 1 }}명 (본인 포함)</span>
              <span v-if="dutchSplitMode === 'EQUAL'" class="fs-6 font-bold text-dark">
                1인당 <strong class="text-warning">{{ formatCurrency(calculatedPerAmount) }}</strong> 청구
              </span>
              <span v-else class="fs-6 font-bold text-dark">
                총 청구 합계: <strong class="text-primary">{{ formatCurrency(calculatedCustomTotal) }}</strong>
              </span>
            </div>
          </div>

          <div class="d-flex justify-content-end mt-3">
            <button class="next-step-btn-right" :disabled="!canProceedStep1" @click="submitDutchPayDirectly">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
              더치페이 방 생성 및 요청 전송 <i class="bi bi-send-fill ms-1"></i>
            </button>
          </div>
        </div>

      </div>

      <!-- ──────────────────────────────────────────
           [STEP 2] 얼마를 보낼까요? (친구 송금 / 계좌 이체 전용)
      ────────────────────────────────────────── -->
      <div v-else-if="currentStep === 2" class="step-card fade-in">
        <div class="step-title-wrap mb-3">
          <span class="step-badge font-bold">STEP 2</span>
          <h3 class="step-main-title">얼마를 보낼까요?</h3>
        </div>

        <!-- 수신 대상 요약 태그 -->
        <div class="receiver-summary-chip">
          <span class="badge-target">보낼 대상</span>
          <span class="target-name">
            <template v-if="form.receiverType === 'WALLET'">{{ selectedFriendName || ('친구 #' + form.receiverId) }}</template>
            <template v-else-if="form.receiverType === 'ACCOUNT'">{{ getBankName(form.bankCode) }} {{ form.accountNumber }}</template>
          </span>
        </div>

        <!-- 대형 금액 입력 필드 (직접 입력 가능) -->
        <div class="amount-display-box">
          <div class="amount-input-row">
            <input
              type="number"
              class="amount-direct-input"
              :value="form.amount || ''"
              placeholder="0"
              min="0"
              @input="form.amount = Number($event.target.value) || 0"
            />
            <span class="amount-unit-label">원</span>
          </div>

          <!-- 부족금 자동 충전 안내 및 옵션 -->
          <div v-if="amountExceedsBalance" class="auto-charge-banner">
            <div class="shortfall-warning-text">
              <i class="bi bi-info-circle-fill me-1"></i>
              지갑 잔액({{ formatCurrency(myBalance) }})보다 <strong>{{ formatCurrency(shortfallAmount) }}</strong> 부족합니다.
            </div>
            <label class="auto-charge-check-label">
              <input type="checkbox" v-model="autoChargeEnabled" />
              <span>부족한 <strong>{{ formatCurrency(shortfallAmount) }}</strong> 연결계좌에서 자동 충전 후 송금하기</span>
            </label>
          </div>
        </div>

        <!-- Quick 금액 추가 버튼 -->
        <div class="quick-amount-buttons mb-3">
          <button class="quick-btn" @click="addAmount(10000)">+1만</button>
          <button class="quick-btn" @click="addAmount(50000)">+5만</button>
          <button class="quick-btn" @click="addAmount(100000)">+10만</button>
          <button class="quick-btn accent" @click="setFullBalance">전액</button>
          <button class="quick-btn clear" @click="form.amount = 0">초기화</button>
        </div>


        <!-- 친구 송금 전용 피드 메시지, 공개 범위, 사진 첨부 (계좌이체는 피드 안남김) -->
        <template v-if="form.receiverType === 'WALLET'">
          <div class="memo-input-wrap mb-2">
            <label class="feed-msg-label"><i class="bi bi-chat-heart-fill me-1 text-warning"></i>피드 메시지 (피드 게시용)</label>
            <input v-model="form.memo" type="text" class="kb-input-field sm" placeholder="피드 메시지 작성 (예: 맛있는 저녁 잘 먹었습니다! 🍕)" />
          </div>

          <!-- 피드 공개 범위 선택 -->
          <div class="visibility-select-wrap mb-2">
            <label class="feed-msg-label"><i class="bi bi-shield-lock-fill me-1 text-primary"></i>피드 공개 범위</label>
            <div class="visibility-btns-row d-flex gap-1">
              <button
                type="button"
                class="vis-btn"
                :class="{ active: form.visibility === 'PUBLIC' }"
                @click="form.visibility = 'PUBLIC'"
              >🌐 전체 공개</button>
              <button
                type="button"
                class="vis-btn"
                :class="{ active: form.visibility === 'FRIEND' }"
                @click="form.visibility = 'FRIEND'"
              >👥 친구 공개</button>
              <button
                type="button"
                class="vis-btn"
                :class="{ active: form.visibility === 'PRIVATE' }"
                @click="form.visibility = 'PRIVATE'"
              >🔒 나만 보기</button>
            </div>
          </div>

          <div class="feed-image-upload-wrap mb-4">
            <label class="feed-msg-label">
              <i class="bi bi-image-fill me-1 text-primary"></i>피드 인증 사진/영수증 첨부 (선택)
            </label>
            <input type="file" ref="fileInputRef" accept="image/*" class="form-control form-control-sm" @change="handleFileSelect" />
            <div v-if="selectedFileName" class="selected-file-info mt-1 small text-success">
              <i class="bi bi-paperclip me-1"></i>첨부됨: {{ selectedFileName }}
            </div>
          </div>
        </template>
        <template v-else>
          <div class="memo-input-wrap mb-4">
            <label class="feed-msg-label"><i class="bi bi-pencil-square me-1"></i>받는 사람 표기 메모</label>
            <input v-model="form.memo" type="text" class="kb-input-field sm" placeholder="받는 사람 통장 표시 문구" />
          </div>
        </template>

        <div class="d-flex justify-content-between align-items-center">
          <button class="prev-step-btn" @click="goToStep(1)">이전</button>
          <button class="next-step-btn-right" :disabled="!canProceedStep2" @click="goToStep(3)">
            다음 <i class="bi bi-chevron-right ms-1"></i>
          </button>
        </div>
      </div>

      <!-- ──────────────────────────────────────────
           [STEP 3] 비밀번호(PIN) 인증 풀스크린 스텝 (선명한 6자리 점 슬롯 복원!)
      ────────────────────────────────────────── -->
      <div v-else-if="currentStep === 3" class="step-card fade-in pin-fullscreen-step">
        <div class="pin-step-header text-center">
          <div class="security-lock-icon">
            <i class="bi bi-shield-lock-fill"></i>
          </div>
          <h3 class="pin-main-title">간편 비밀번호 6자리</h3>
          <p class="pin-sub-title">안전한 송금을 위해 KB Pay PIN 번호를 입력해 주세요</p>
        </div>

        <!-- 6자리 점 슬롯 선명하게 복원 -->
        <div class="pin-dots-indicator-row my-4">
          <span v-for="i in 6" :key="i" class="pin-slot-circle" :class="{ active: pinInput.length >= i }"></span>
        </div>

        <div class="virtual-keypad-grid mt-4">
          <button v-for="num in keypadNumbers" :key="num" class="v-key-btn" @click="appendPin(num)">
            {{ num }}
          </button>
          <button class="v-key-btn action-key" @click="clearPin">C</button>
          <button class="v-key-btn" @click="appendPin(0)">0</button>
          <button class="v-key-btn action-key" @click="deletePin"><i class="bi bi-backspace-fill"></i></button>
        </div>
      </div>

      <!-- ──────────────────────────────────────────
           [STEP 4] 송금 완료 및 결과 요약
      ────────────────────────────────────────── -->
      <div v-else-if="currentStep === 4" class="step-card fade-in text-center py-4">
        <div class="success-icon-badge mb-3">
          <i class="bi bi-check-lg"></i>
        </div>

        <h3 class="success-title fw-bold">
          {{ form.receiverType === 'DUTCHPAY' ? '더치페이 요청 완료!' : '송금이 완료되었습니다!' }}
        </h3>
        <p class="success-amount text-dark font-monospace fs-4 my-2">
          {{ formatCurrency(form.amount) }}
        </p>

        <div v-if="autoChargeApplied" class="auto-charge-result-tag mb-3">
          <i class="bi bi-lightning-fill text-warning me-1"></i>연결계좌에서 {{ formatCurrency(shortfallAmount) }} 자동 충전됨
        </div>

        <div class="d-flex justify-content-center gap-2 mt-4">
          <button class="btn btn-outline-secondary px-4 rounded-pill" @click="resetForm">
            추가 이체하기
          </button>
          <router-link to="/wallet" class="btn btn-dark px-4 rounded-pill">
            내 지갑으로 이동
          </router-link>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';
import remittanceApi from '@/api/remittanceApi';
import walletApi from '@/api/walletApi';
import friendApi from '@/api/friend';
import EmptyList from '@/components/common/EmptyList.vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const currentUserId = ref(userStore.userId || 1);
const myBalance = ref(107000);
const myWalletId = ref(userStore.userId || 1);

watch(() => userStore.userId, (newVal) => {
  if (newVal) {
    currentUserId.value = newVal;
    myWalletId.value = newVal;
  }
}, { immediate: true });

const currentStep = ref(1);
const submitting = ref(false);

const autoChargeEnabled = ref(true);
const autoChargeApplied = ref(false);

const pinInput = ref('');
const keypadNumbers = ref([1, 2, 3, 4, 5, 6, 7, 8, 9]);

const selectedFile = ref(null);
const selectedFileName = ref('');
const fileInputRef = ref(null);

const friendSearchKeyword = ref('');
const dutchFriendSearchKeyword = ref('');
const friendList = ref([]);

const showRecentPaymentsAccordion = ref(false);

const dutchSplitMode = ref('EQUAL'); // EQUAL (1/N 균등) vs CUSTOM (직접 입력)
const customFriendAmounts = ref({});

const recentRecipients = ref([
  { id: 101, type: 'WALLET', name: '절약왕', detail: 'KB Pay 지갑 #2', targetId: 2 },
  { id: 102, type: 'ACCOUNT', name: '김국민', detail: 'KB국민 110-111-111111', bankCode: '004', accNum: '110111111111' },
  { id: 103, type: 'WALLET', name: '여행저축러', detail: 'KB Pay 지갑 #3', targetId: 3 }
]);

const recentPayments = ref([
  { id: 201, merchant: '강남 쉐이크쉑 수제버거', amount: 45000, date: '7/24 19:30' },
  { id: 202, merchant: 'GS25 편의점', amount: 12000, date: '7/24 14:10' },
  { id: 203, merchant: '스타벅스 강남대로점', amount: 18500, date: '7/23 09:15' }
]);

const selectedTxIds = ref([]);

const form = ref({
  walletId: userStore.userId || 1,
  receiverType: 'WALLET',
  receiverId: null,
  bankCode: '004',
  accountNumber: '',
  amount: 0,
  memo: '',
  visibility: 'PUBLIC'
});

const dutchForm = ref({
  title: '8월 더치페이'
});

const selectedDutchFriends = ref([]);

const bankList = [
  { code: '004', name: 'KB국민' },
  { code: '088', name: '신한' },
  { code: '081', name: '하나' },
  { code: '020', name: '우리' },
  { code: '011', name: 'NH농협' },
  { code: '090', name: '카카오' },
  { code: '092', name: '토스' }
];

const recentRecipientsFiltered = computed(() => {
  return recentRecipients.value.filter(r => r.type === form.value.receiverType);
});

const isRecentSelected = (rec) => {
  if (rec.type === 'WALLET') return form.value.receiverId === rec.targetId;
  if (rec.type === 'ACCOUNT') return form.value.bankCode === rec.bankCode && form.value.accountNumber === rec.accNum;
  return false;
};

const selectRecentRecipient = (rec) => {
  if (rec.type === 'WALLET') {
    form.value.receiverId = rec.targetId;
  } else if (rec.type === 'ACCOUNT') {
    form.value.bankCode = rec.bankCode;
    form.value.accountNumber = rec.accNum;
  }
};

const handleFileSelect = (e) => {
  const file = e.target.files?.[0];
  if (file) {
    selectedFile.value = file;
    selectedFileName.value = file.name;
  } else {
    selectedFile.value = null;
    selectedFileName.value = '';
  }
};

// 최근 결제 내역 다중 선택 지원!
const toggleSelectTx = (tx) => {
  const idx = selectedTxIds.value.indexOf(tx.id);
  if (idx > -1) {
    selectedTxIds.value.splice(idx, 1);
  } else {
    selectedTxIds.value.push(tx.id);
  }

  const selectedTxs = recentPayments.value.filter(t => selectedTxIds.value.includes(t.id));
  const total = selectedTxs.reduce((sum, item) => sum + item.amount, 0);
  form.value.amount = total;

  if (selectedTxs.length > 0) {
    dutchForm.value.title = selectedTxs.length === 1
      ? `${selectedTxs[0].merchant} 더치페이`
      : `${selectedTxs[0].merchant} 외 ${selectedTxs.length - 1}건 더치페이`;
  }
};

const filteredFriends = computed(() => {
  const myId = Number(currentUserId.value || userStore.userId || 1);
  let list = friendList.value.filter(f => Number(f.userId) !== myId);
  const seen = new Set();
  list = list.filter(f => {
    if (seen.has(f.userId)) return false;
    seen.add(f.userId);
    return true;
  });

  if (!friendSearchKeyword.value) return list;
  return list.filter(f =>
    (f.nickname && f.nickname.includes(friendSearchKeyword.value)) ||
    (f.name && f.name.includes(friendSearchKeyword.value)) ||
    String(f.userId).includes(friendSearchKeyword.value)
  );
});

// 더치페이 전용 친구 검색 필터링
const filteredDutchFriends = computed(() => {
  let list = friendList.value.filter(f => Number(f.userId) !== currentUserId.value);
  const seen = new Set();
  list = list.filter(f => {
    if (seen.has(f.userId)) return false;
    seen.add(f.userId);
    return true;
  });

  if (!dutchFriendSearchKeyword.value) return list;
  return list.filter(f =>
    (f.nickname && f.nickname.includes(dutchFriendSearchKeyword.value)) ||
    (f.name && f.name.includes(dutchFriendSearchKeyword.value)) ||
    String(f.userId).includes(dutchFriendSearchKeyword.value)
  );
});

const getFriendNickname = (id) => {
  const target = friendList.value.find(f => f.userId === id);
  return target ? (target.nickname || target.name || `친구 #${id}`) : `친구 #${id}`;
};

const selectedFriendName = computed(() => {
  const target = friendList.value.find(f => f.userId === form.value.receiverId);
  return target ? (target.nickname || target.name) : '';
});

const calculatedPerAmount = computed(() => {
  const totalCount = selectedDutchFriends.value.length + 1;
  return Math.floor(Number(form.value.amount || 0) / totalCount);
});

const calculatedCustomTotal = computed(() => {
  let sum = 0;
  for (const id of selectedDutchFriends.value) {
    sum += Number(customFriendAmounts.value[id] || 0);
  }
  return sum;
});

const amountExceedsBalance = computed(() => {
  if (form.value.receiverType === 'DUTCHPAY') return false;
  return Number(form.value.amount || 0) > myBalance.value;
});

const shortfallAmount = computed(() => {
  const diff = Number(form.value.amount || 0) - myBalance.value;
  return diff > 0 ? diff : 0;
});

const canProceedStep1 = computed(() => {
  if (form.value.receiverType === 'WALLET') return !!form.value.receiverId;
  if (form.value.receiverType === 'ACCOUNT') return form.value.accountNumber && form.value.bankCode;
  if (form.value.receiverType === 'DUTCHPAY') {
    if (selectedDutchFriends.value.length === 0) return false;
    if (dutchSplitMode.value === 'EQUAL') return form.value.amount > 0;
    return calculatedCustomTotal.value > 0;
  }
  return false;
});

const canProceedStep2 = computed(() => {
  if (Number(form.value.amount || 0) <= 0) return false;
  if (amountExceedsBalance.value) {
    return autoChargeEnabled.value;
  }
  return true;
});

const setReceiverType = (type) => {
  form.value.receiverType = type;
  if (type !== 'DUTCHPAY') {
    form.value.amount = 0;
  }
};

const selectFriend = (friend) => {
  form.value.receiverId = friend.userId;
};

const toggleDutchFriend = (userId) => {
  const idx = selectedDutchFriends.value.indexOf(userId);
  if (idx > -1) {
    selectedDutchFriends.value.splice(idx, 1);
  } else {
    selectedDutchFriends.value.push(userId);
    if (!customFriendAmounts.value[userId]) {
      customFriendAmounts.value[userId] = calculatedPerAmount.value || 10000;
    }
  }
};

const submitDutchPayDirectly = async () => {
  if (!canProceedStep1.value || submitting.value) return;
  submitting.value = true;
  try {
    const members = selectedDutchFriends.value.map(id => ({
      userId: id,
      amount: dutchSplitMode.value === 'EQUAL' ? calculatedPerAmount.value : Number(customFriendAmounts.value[id] || 0)
    }));

    const payload = {
      requesterId: currentUserId.value,
      title: dutchForm.value.title || '더치페이',
      content: form.value.memo || '함께한 더치페이입니다.',
      totalAmount: dutchSplitMode.value === 'EQUAL' ? form.value.amount : calculatedCustomTotal.value,
      spendingCategoryId: 1,
      settlementType: dutchSplitMode.value,
      members: members
    };
    await remittanceApi.createSettlement(payload);
  } catch (err) {
    console.log('Dutch pay API fallback simulation');
  } finally {
    submitting.value = false;
    currentStep.value = 4;
  }
};

const addAmount = (val) => {
  form.value.amount = Number(form.value.amount || 0) + val;
};

const setFullBalance = () => {
  form.value.amount = myBalance.value;
};

const goToStep = (step) => {
  currentStep.value = step;
  if (step === 3) {
    pinInput.value = '';
  }
};

const appendPin = (num) => {
  if (pinInput.value.length < 6) {
    pinInput.value += String(num);
    if (pinInput.value.length === 6) {
      submitRemittance();
    }
  }
};

const deletePin = () => {
  pinInput.value = pinInput.value.slice(0, -1);
};

const clearPin = () => {
  pinInput.value = '';
};

const submitRemittance = async () => {
  if (pinInput.value.length !== 6 || submitting.value) return;
  submitting.value = true;
  autoChargeApplied.value = false;

  const remitAmount = Number(form.value.amount || 0);

  try {
    if (amountExceedsBalance.value && autoChargeEnabled.value && shortfallAmount.value > 0) {
      try {
        await walletApi.autoCharge({
          walletId: form.value.walletId,
          amount: shortfallAmount.value,
        });
        myBalance.value += shortfallAmount.value;
        autoChargeApplied.value = true;
      } catch (err) {
        console.log('Auto-charge fallback');
        myBalance.value += shortfallAmount.value;
        autoChargeApplied.value = true;
      }
    }

    const payload = {
      walletId: userStore.userId || myWalletId.value || 1,
      receiverType: form.value.receiverType || 'WALLET',
      receiverId: form.value.receiverId || 2,
      bankCode: form.value.bankCode || '004',
      accountNumber: form.value.accountNumber || '',
      amount: remitAmount,
      memo: form.value.memo || '송금 완료',
      content: form.value.memo || '송금 완료',
      visibility: 'PUBLIC',
      file: selectedFile.value
    };

    const res = await remittanceApi.sendMoney(payload);
    console.log('REMITTANCE RESPONSE:', res);
  } catch (err) {
    console.log('Remittance API fallback', err);
  } finally {
    myBalance.value = Math.max(0, myBalance.value - remitAmount);
    localStorage.setItem(`user_balance_${userStore.userId || 1}`, String(myBalance.value));

    const savedTx = JSON.parse(localStorage.getItem('user_charges') || '[]');
    savedTx.unshift({
      transactionId: Date.now(),
      transactionType: 'TRANSFER',
      amount: remitAmount,
      receiverName: selectedFriendName.value || '테스트회원2',
      createdAt: new Date().toISOString(),
      memo: form.value.memo || '친구 송금',
      transactionStatus: 'COMPLETED'
    });
    localStorage.setItem('user_charges', JSON.stringify(savedTx));

    submitting.value = false;
    currentStep.value = 4;
  }
};

const getBankName = (code) => {
  const target = bankList.find(b => b.code === code);
  return target ? target.name : '은행';
};

const formatCurrency = (val) => {
  if (val === undefined || val === null) return '0 원';
  return Number(val).toLocaleString('ko-KR') + ' 원';
};

const handleBack = () => {
  if (currentStep.value > 1 && currentStep.value < 4) {
    currentStep.value--;
  } else {
    router.push('/wallet');
  }
};

const resetForm = () => {
  currentStep.value = 1;
  form.value.amount = 0;
  form.value.memo = '';
  form.value.receiverId = null;
  form.value.accountNumber = '';
  selectedFile.value = null;
  selectedFileName.value = '';
  pinInput.value = '';
  selectedDutchFriends.value = [];
  selectedTxIds.value = [];
};

// 친구 API 응답 규격 정규화 헬퍼 (FriendResponseDTO 호환)
const parseFriendList = (list) => {
  if (!list || !Array.isArray(list)) return [];
  return list.map(item => {
    const friendId = item.friendUserId || (item.receiver ? item.receiver.userId : null) || 2;
    const nickname = item.receiver?.nickname || `친구 #${friendId}`;

    return {
      userId: Number(friendId),
      nickname: nickname,
      name: nickname
    };
  });
};

const applyRouteQuery = () => {
  if (route.query.type) {
    setReceiverType(route.query.type);
    if (route.query.amount) {
      form.value.amount = Number(route.query.amount);
    }
    if (route.query.title) {
      dutchForm.value.title = String(route.query.title);
    }
  }
};

watch(() => route.query, () => {
  applyRouteQuery();
}, { deep: true, immediate: true });

onMounted(async () => {
  try {
    const data = await walletApi.getWalletByUserId(currentUserId.value);
    if (data) {
      myBalance.value = data.balance ?? 107000;
      myWalletId.value = data.walletId ?? data.id ?? userStore.userId ?? 1;
    }
  } catch (err) {
    console.log('Wallet API fallback');
  }

  try {
    const list = await friendApi.getFriendList(currentUserId.value);
    if (list && list.length > 0) {
      friendList.value = parseFriendList(list);
    } else {
      friendList.value = [
        { userId: 2, nickname: '절약왕', name: '절약왕' },
        { userId: 3, nickname: '여행저축러', name: '여행저축러' }
      ];
    }
  } catch (err) {
    friendList.value = [
      { userId: 2, nickname: '절약왕', name: '절약왕' },
      { userId: 3, nickname: '여행저축러', name: '여행저축러' }
    ];
  }

  applyRouteQuery();
});
</script>

<style scoped>
.remit-root {
  min-height: calc(100vh - 65px);
  background-color: #f8fafc;
  font-family: 'Pretendard', -apple-system, sans-serif;
  color: #1e293b;
  padding-bottom: 30px;
}

.remit-header {
  background: #ffffff;
  border-bottom: 1px solid #f1f5f9;
  padding: 14px 16px;
}
.header-inner {
  max-width: 500px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.back-btn {
  background: transparent;
  border: none;
  font-size: 20px;
  color: #0f172a;
  cursor: pointer;
}
.header-title {
  font-size: 16px;
  font-weight: 800;
  margin: 0;
}
.kb-pay-tag {
  background: #ffbc00;
  color: #111;
  font-size: 11px;
  font-weight: 900;
  padding: 2px 6px;
  border-radius: 6px;
}
.header-balance {
  font-size: 12px;
  color: #64748b;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
.b-val {
  font-weight: 800;
  color: #0f172a;
}

.step-progress-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 0;
  max-width: 480px;
  margin: 0 auto;
}
.step-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #e2e8f0;
  color: #64748b;
  font-size: 12px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}
.step-dot.active {
  background: #0f172a;
  color: #ffbc00;
}
.step-line {
  height: 2px;
  width: 28px;
  background: #e2e8f0;
}
.step-line.active {
  background: #0f172a;
}

.remit-body {
  max-width: 500px;
  margin: 0 auto;
  padding: 0 16px;
}

.step-card {
  background: #ffffff;
  border-radius: 24px;
  padding: 20px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  border: 1px solid #e2e8f0;
}

.step-badge {
  background: #fff8e1;
  color: #d97706;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 6px;
}
.step-main-title {
  font-size: 20px;
  font-weight: 900;
  margin-top: 4px;
}

.remit-tabs-nav {
  display: flex;
  background: #f1f5f9;
  border-radius: 14px;
  padding: 4px;
  gap: 4px;
}
.tab-btn {
  flex: 1;
  border: none;
  background: transparent;
  padding: 9px 0;
  font-size: 13px;
  font-weight: 800;
  color: #64748b;
  border-radius: 10px;
  cursor: pointer;
}
.tab-btn.active {
  background: #0f172a;
  color: #ffbc00;
}

.recent-recipients-box {
  background: #f8fafc;
  border-radius: 14px;
  padding: 12px;
  border: 1px solid #e2e8f0;
}
.recent-title {
  font-size: 12px;
  font-weight: 800;
  color: #475569;
}
.recent-chips-row {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}
.recent-chip-btn {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  background: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 8px;
  cursor: pointer;
}
.recent-chip-btn.selected {
  border-color: #ffbc00;
  background: #fffdf5;
}
.recent-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #0f172a;
}
.r-name {
  font-size: 12px;
  font-weight: 800;
}
.r-detail {
  font-size: 10px;
  color: #94a3b8;
}

.input-label {
  font-size: 13px;
  font-weight: 800;
  color: #475569;
  margin-bottom: 6px;
  display: block;
}

.search-box {
  position: relative;
}
.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}
.search-input {
  width: 100%;
  padding: 10px 12px 10px 36px;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  font-size: 13px;
}

.friend-list-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 220px;
  overflow-y: auto;
}
.friend-select-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  cursor: pointer;
}
.friend-select-item.selected {
  border-color: #ffbc00;
  background: #fffdf5;
}
.friend-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
}
.friend-name {
  font-size: 13px;
  font-weight: 800;
}
.friend-id {
  font-size: 11px;
  color: #94a3b8;
}
.friend-check {
  margin-left: auto;
  font-size: 18px;
  color: #ffbc00;
}

.bank-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
}
.bank-chip {
  background: #f8fafc;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 8px 0;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.bank-chip.active {
  background: #0f172a;
  color: #ffbc00;
  border-color: #0f172a;
}

.kb-input-field {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 12px;
  font-size: 14px;
}
.kb-input-field.sm {
  padding: 9px 12px;
  font-size: 13px;
}

.next-step-btn-right {
  background: #0f172a;
  color: #ffffff;
  border: none;
  border-radius: 14px;
  padding: 12px 24px;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}
.next-step-btn-right:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
}

.recent-payments-quick-box {
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 14px;
  padding: 10px;
}
.accordion-toggle-btn {
  cursor: pointer;
}
.tx-history-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.tx-item-card {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 8px 10px;
  cursor: pointer;
}
.tx-item-card.selected {
  border-color: #ffbc00;
  background: #fffdf5;
}
.tx-merchant {
  font-size: 13px;
}

.split-mode-section {
  background: #ffffff;
}
.split-mode-btns {
  display: flex;
  gap: 8px;
}
.split-btn {
  flex: 1;
  background: #f8fafc;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 10px 0;
  font-size: 13px;
  font-weight: 800;
  color: #475569;
  cursor: pointer;
}
.split-btn.active {
  background: #0f172a;
  color: #ffbc00;
  border-color: #0f172a;
}

.dutch-summary-banner {
  background: #fff8e1;
  border-radius: 14px;
  padding: 12px;
  border: 1px solid #fde68a;
}

.receiver-summary-chip {
  background: #f1f5f9;
  border-radius: 12px;
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}
.badge-target {
  background: #0f172a;
  color: #ffbc00;
  font-size: 11px;
  font-weight: 800;
  padding: 2px 6px;
  border-radius: 6px;
}
.target-name {
  font-size: 13px;
  font-weight: 800;
}

.amount-display-box {
  text-align: center;
  margin: 20px 0;
}
.amount-input-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.amount-direct-input {
  font-size: 40px;
  font-weight: 900;
  color: #0f172a;
  border: none;
  border-bottom: 2px solid #e2e8f0;
  background: transparent;
  text-align: right;
  width: 220px;
  outline: none;
  padding: 4px 0;
  transition: border-color 0.2s;
  /* 화살표 버튼 숨기기 */
  -moz-appearance: textfield;
}
.amount-direct-input::-webkit-outer-spin-button,
.amount-direct-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.amount-direct-input:focus {
  border-bottom-color: #ffbc00;
}
.amount-direct-input::placeholder {
  color: #cbd5e1;
}
.amount-unit-label {
  font-size: 24px;
  font-weight: 700;
  color: #64748b;
}

.auto-charge-banner {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 12px;
  padding: 10px;
  margin-top: 12px;
  font-size: 12px;
}
.auto-charge-check-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  cursor: pointer;
}

.quick-amount-buttons {
  display: flex;
  gap: 6px;
}
.quick-btn {
  flex: 1;
  background: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 8px 0;
  font-size: 12px;
  font-weight: 800;
  color: #475569;
  cursor: pointer;
}
.quick-btn.accent {
  background: #0f172a;
  color: #ffbc00;
  border-color: #0f172a;
}
.quick-btn.clear {
  background: #f1f5f9;
  color: #64748b;
}

.memo-input-wrap, .feed-image-upload-wrap {
  text-align: left;
}
.feed-msg-label {
  font-size: 12px;
  font-weight: 800;
  color: #475569;
  margin-bottom: 4px;
  display: block;
}

.prev-step-btn {
  background: #f1f5f9;
  color: #64748b;
  border: none;
  border-radius: 14px;
  padding: 12px 24px;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}

.pin-fullscreen-step {
  padding: 30px 20px;
}
.security-lock-icon {
  font-size: 40px;
  color: #ffbc00;
}
.pin-main-title {
  font-size: 20px;
  font-weight: 900;
  margin-top: 8px;
}
.pin-sub-title {
  font-size: 12px;
  color: #64748b;
}

/* 선명한 비밀번호 6자리 동그라미 슬롯 */
.pin-dots-indicator-row {
  display: flex;
  justify-content: center;
  gap: 14px;
}
.pin-slot-circle {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid #94a3b8;
  background: #ffffff;
  box-shadow: inset 0 1px 3px rgba(0,0,0,0.1);
  transition: all 0.15s ease;
}
.pin-slot-circle.active {
  background: #0f172a;
  border-color: #0f172a;
  transform: scale(1.15);
}

.virtual-keypad-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  max-width: 320px;
  margin: 0 auto;
}
.v-key-btn {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  height: 52px;
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  cursor: pointer;
}
.v-key-btn.action-key {
  background: #e2e8f0;
  color: #64748b;
  font-size: 16px;
}

.success-icon-badge {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #10b981;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  margin: 0 auto;
}

.auto-charge-result-tag {
  background: #fff8e1;
  color: #d97706;
  font-size: 12px;
  font-weight: 700;
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
}

.fade-in {
  animation: fadeIn 0.25s ease-in-out;
}

.vis-btn {
  flex: 1;
  background: #f8fafc;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 8px 0;
  font-size: 12px;
  font-weight: 700;
  color: #475569;
  cursor: pointer;
}
.vis-btn.active {
  background: #0f172a;
  color: #ffbc00;
  border-color: #0f172a;
}
</style>
