<template>
  <div class="remit-root">

    <!-- ══════════════════════════════════════════
         상단 헤더
    ══════════════════════════════════════════ -->
    <div class="remit-header">
      <div class="remit-header-inner">
        <router-link to="/wallet" class="back-btn">
          <i class="bi bi-chevron-left"></i>
        </router-link>
        <div class="header-title-wrap">
          <span class="header-badge">KB Pay</span>
          <h4 class="header-title">{{ form.receiverType === 'DUTCHPAY' ? '정산하기' : '송금하기' }}</h4>
        </div>
        <div class="header-balance-chip">
          <i class="bi bi-wallet2 me-1"></i>
          {{ formatCurrency(myBalance) }}
        </div>
      </div>
    </div>

    <!-- ══════════════════════════════════════════
         성공 영수증 화면
    ══════════════════════════════════════════ -->
    <transition name="slide-up">
      <div v-if="isSuccess" class="page-body">
        <div class="receipt-wrapper">
          <!-- 성공 아이콘 영역 -->
          <div class="receipt-hero">
            <div class="receipt-check-ring">
              <div class="receipt-check-icon">
                <i class="bi bi-check-lg"></i>
              </div>
            </div>
            <div class="receipt-type-badge">
              {{ form.receiverType === 'DUTCHPAY' ? 'SETTLEMENT' : 'TRANSFER' }}
            </div>
            <h2 class="receipt-main-title">
              {{ form.receiverType === 'DUTCHPAY' ? '정산 요청 완료' : '송금 이체 완료' }}
            </h2>
            <p class="receipt-sub">{{ statusMessage }}</p>
          </div>

          <!-- 금액 강조 -->
          <div class="receipt-amount-box">
            <p class="receipt-amount-label">{{ form.receiverType === 'DUTCHPAY' ? '총 정산 금액' : '이체 금액' }}</p>
            <div class="receipt-amount">{{ formatCurrency(form.amount) }}</div>
          </div>

          <!-- 영수증 디테일 -->
          <div class="receipt-detail-card">
            <div class="receipt-row">
              <span class="receipt-label">{{ form.receiverType === 'DUTCHPAY' ? '정산 제목' : '수신 대상' }}</span>
              <span class="receipt-value">
                <template v-if="form.receiverType === 'WALLET'">친구 #{{ form.receiverId }}</template>
                <template v-else-if="form.receiverType === 'ACCOUNT'">{{ getSelectedBankName() }} {{ form.accountNumber }}</template>
                <template v-else-if="form.receiverType === 'DUTCHPAY'">{{ dutchForm.title }}</template>
              </span>
            </div>
            <div v-if="form.receiverType === 'DUTCHPAY'" class="receipt-row">
              <span class="receipt-label">참여 인원</span>
              <span class="receipt-value">{{ parseMemberIds().length + 1 }}명 (본인 포함)</span>
            </div>
            <div class="receipt-row">
              <span class="receipt-label">출금 지갑</span>
              <span class="receipt-value">KB Pay 지갑 #{{ form.walletId }}</span>
            </div>
            <div v-if="form.memo" class="receipt-row">
              <span class="receipt-label">메모</span>
              <span class="receipt-value">{{ form.memo }}</span>
            </div>
            <div v-if="lastTransactionId" class="receipt-row">
              <span class="receipt-label">승인 번호</span>
              <span class="receipt-value mono">#{{ lastTransactionId }}</span>
            </div>
            <div class="receipt-row last">
              <span class="receipt-label">완료 시간</span>
              <span class="receipt-value">{{ formatDate(new Date()) }}</span>
            </div>
          </div>

          <!-- 하단 액션 -->
          <div class="receipt-actions">
            <button class="receipt-btn outline" @click="resetForm">
              <i class="bi bi-arrow-counterclockwise me-1"></i>
              추가 송금
            </button>
            <router-link to="/wallet" class="receipt-btn primary">
              <i class="bi bi-wallet2 me-1"></i>
              지갑 확인
            </router-link>
          </div>
        </div>
      </div>
    </transition>

    <!-- ══════════════════════════════════════════
         송금 폼 화면
    ══════════════════════════════════════════ -->
    <div v-if="!isSuccess" class="page-body">

      <!-- 송금 방식 선택 탭 -->
      <div class="type-tab-group">
        <button
          class="type-tab"
          :class="{ active: form.receiverType === 'WALLET' }"
          @click="form.receiverType = 'WALLET'"
        >
          <i class="bi bi-person-heart"></i>
          <span>친구 송금</span>
        </button>
        <button
          class="type-tab"
          :class="{ active: form.receiverType === 'ACCOUNT' }"
          @click="form.receiverType = 'ACCOUNT'"
        >
          <i class="bi bi-bank"></i>
          <span>계좌 송금</span>
        </button>
        <button
          class="type-tab"
          :class="{ active: form.receiverType === 'DUTCHPAY' }"
          @click="form.receiverType = 'DUTCHPAY'"
        >
          <i class="bi bi-pie-chart-fill"></i>
          <span>정산하기</span>
        </button>
      </div>

      <form @submit.prevent="confirmAction" class="form-sections">

        <!-- ──── STEP 1: 받는 사람 ──── -->
        <div class="form-card">
          <div class="form-card-label">
            <span class="step-num">1</span>
            <span>{{ form.receiverType === 'DUTCHPAY' ? '정산 설정' : '받는 사람' }}</span>
          </div>

          <!-- 친구 송금 -->
          <div v-if="form.receiverType === 'WALLET'">
            <!-- 친구 아바타 리스트 -->
            <div v-if="friendsList.length > 0" class="friend-scroll">
              <div
                v-for="f in friendsList"
                :key="f.friendUserId || f.friendId"
                class="friend-chip"
                :class="{ selected: form.receiverId === (f.friendUserId || f.friendId) }"
                @click="form.receiverId = f.friendUserId || f.friendId"
              >
                <div class="friend-avatar">
                  <i class="bi bi-person-fill"></i>
                  <span v-if="form.receiverId === (f.friendUserId || f.friendId)" class="friend-check">
                    <i class="bi bi-check"></i>
                  </span>
                </div>
                <span class="friend-name">{{ f.nickname || f.friendName || '친구' }}</span>
                <span class="friend-id">#{{ f.friendUserId || f.friendId }}</span>
              </div>
            </div>
            <div class="form-field-group">
              <label class="field-label">친구 회원 번호</label>
              <div class="input-with-icon">
                <i class="bi bi-person-fill field-icon"></i>
                <input
                  type="number"
                  v-model.number="form.receiverId"
                  class="field-input"
                  placeholder="직접 입력 (예: 2)"
                  required
                />
              </div>
            </div>
          </div>

          <!-- 계좌 송금 -->
          <div v-else-if="form.receiverType === 'ACCOUNT'">
            <!-- 최근 계좌 -->
            <div class="recent-accs">
              <p class="recent-accs-label">최근 계좌</p>
              <div
                v-for="(acc, idx) in recentAccounts"
                :key="idx"
                class="recent-acc-item"
                :class="{ selected: form.accountNumber === acc.accountNumber }"
                @click="selectRecentAccount(acc)"
              >
                <div class="acc-bank-badge">{{ acc.bankName?.substring(0, 2) || 'KB' }}</div>
                <div class="acc-info">
                  <strong>{{ acc.ownerName }}</strong>
                  <span>{{ acc.bankName }} · {{ acc.accountNumber }}</span>
                </div>
                <i class="bi bi-chevron-right acc-arrow"></i>
              </div>
            </div>

            <!-- 직접 입력 -->
            <div class="form-field-group">
              <label class="field-label">은행 선택</label>
              <select v-model="form.bankCode" class="field-select">
                <option v-for="b in bankList" :key="b.bankCode" :value="b.bankCode">{{ b.bankName }}</option>
              </select>
            </div>
            <div class="form-field-group">
              <label class="field-label">계좌번호</label>
              <div class="input-with-icon">
                <i class="bi bi-hash field-icon"></i>
                <input
                  type="text"
                  v-model="form.accountNumber"
                  class="field-input"
                  placeholder="계좌번호 (- 없이 입력)"
                  required
                />
              </div>
            </div>
          </div>

          <!-- 정산(더치페이) -->
          <div v-else-if="form.receiverType === 'DUTCHPAY'">
            <div class="form-field-group">
              <div class="dutch-header">
                <label class="field-label">정산 제목</label>
                <button type="button" class="history-btn" @click="toggleHistoryPicker">
                  <i class="bi bi-receipt me-1"></i>결제 내역 불러오기
                </button>
              </div>
              <!-- 결제 내역 피커 -->
              <div v-if="showHistoryPicker" class="history-picker">
                <div v-if="historyLoading" class="history-loading">
                  <span class="spinner-border spinner-border-sm me-2"></span> 로딩 중...
                </div>
                <div v-else-if="historyList.length === 0" class="history-empty">내역이 없습니다</div>
                <div v-else class="history-list">
                  <div
                    v-for="tx in historyList"
                    :key="tx.transactionId"
                    class="history-item"
                    @click="applyTransactionToDutch(tx)"
                  >
                    <div class="history-item-info">
                      <strong>{{ tx.memo || tx.merchantName || '결제건 #' + tx.transactionId }}</strong>
                      <span>{{ formatDate(tx.createdAt) }}</span>
                    </div>
                    <span class="history-item-amount">{{ formatCurrency(tx.amount) }}</span>
                  </div>
                </div>
              </div>
              <input type="text" v-model="dutchForm.title" class="field-input" placeholder="예: 회식비 더치페이" required />
            </div>

            <div class="form-field-group">
              <label class="field-label">분배 방식</label>
              <div class="split-type-group">
                <button
                  type="button"
                  class="split-btn"
                  :class="{ active: dutchForm.settlementType === 'EQUAL' }"
                  @click="dutchForm.settlementType = 'EQUAL'"
                >
                  <i class="bi bi-distribute-vertical me-1"></i> 1/N 균등
                </button>
                <button
                  type="button"
                  class="split-btn"
                  :class="{ active: dutchForm.settlementType === 'UNEQUAL' }"
                  @click="dutchForm.settlementType = 'UNEQUAL'"
                >
                  <i class="bi bi-sliders me-1"></i> 개별 직접 입력
                </button>
              </div>
            </div>

            <div class="form-field-group">
              <label class="field-label">참여 친구 ID <span class="field-hint">쉼표로 구분 (예: 2, 3)</span></label>
              <div class="input-with-icon">
                <i class="bi bi-people-fill field-icon"></i>
                <input type="text" v-model="dutchForm.memberIdsStr" class="field-input" placeholder="2, 3" required />
              </div>
            </div>

            <!-- UNEQUAL 개별 금액 입력 -->
            <div v-if="dutchForm.settlementType === 'UNEQUAL' && parseMemberIds().length > 0" class="unequal-box">
              <p class="unequal-title">멤버별 요청 금액</p>
              <div v-for="mId in parseMemberIds()" :key="mId" class="unequal-row">
                <span class="unequal-id">친구 #{{ mId }}</span>
                <div class="unequal-input-wrap">
                  <input
                    type="number"
                    v-model.number="dutchForm.customAmounts[mId]"
                    class="unequal-input"
                    placeholder="0"
                    min="0"
                    required
                  />
                  <span class="unequal-unit">원</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ──── STEP 2: 금액 ──── -->
        <div class="form-card">
          <div class="form-card-label">
            <span class="step-num">2</span>
            <span>{{ form.receiverType === 'DUTCHPAY' ? '총 정산 금액' : '보낼 금액' }}</span>
          </div>

          <div class="big-amount-input-wrap">
            <span class="big-amount-currency">₩</span>
            <input
              type="number"
              v-model.number="form.amount"
              class="big-amount-input"
              placeholder="0"
              min="1"
              required
            />
          </div>

          <div class="quick-amounts">
            <button type="button" class="q-btn" @click="addAmount(10000)">+1만</button>
            <button type="button" class="q-btn" @click="addAmount(30000)">+3만</button>
            <button type="button" class="q-btn" @click="addAmount(50000)">+5만</button>
            <button type="button" class="q-btn" @click="addAmount(100000)">+10만</button>
          </div>

          <!-- 잔액 대비 -->
          <div v-if="form.amount > 0" class="balance-compare">
            <div class="balance-compare-row">
              <span>현재 잔액</span>
              <span class="compare-val">{{ formatCurrency(myBalance) }}</span>
            </div>
            <div class="balance-compare-row">
              <span>송금 후 잔액</span>
              <span class="compare-val" :class="{ warn: (myBalance - form.amount) < 0 }">
                {{ formatCurrency(Math.max(0, myBalance - form.amount)) }}
              </span>
            </div>
          </div>
        </div>

        <!-- ──── STEP 3: 메모 & 피드 ──── -->
        <div class="form-card">
          <div class="form-card-label">
            <span class="step-num">3</span>
            <span>메모 & 피드</span>
          </div>

          <div class="form-field-group">
            <label class="field-label">메모 <span class="field-hint">선택 · 피드에 표시됩니다</span></label>
            <div class="input-with-icon">
              <i class="bi bi-pencil-fill field-icon"></i>
              <input
                type="text"
                v-model="form.memo"
                class="field-input"
                placeholder="예: 축의금, 점심값"
              />
            </div>
          </div>

          <!-- 사진 첨부 -->
          <div class="photo-attach-row">
            <button type="button" class="photo-btn" @click="triggerFileInput">
              <i class="bi bi-camera-fill me-1"></i>
              사진 첨부
              <span v-if="attachedFiles.length > 0" class="photo-count">{{ attachedFiles.length }}</span>
            </button>
            <span v-if="attachedFiles.length === 0" class="photo-hint">추억 사진을 함께 남겨보세요</span>
            <input type="file" ref="fileInputRef" accept="image/*" multiple class="d-none" @change="handleFileChange" />
          </div>

          <!-- 사진 미리보기 -->
          <div v-if="imagePreviews.length > 0" class="photo-previews">
            <div v-for="(src, idx) in imagePreviews" :key="idx" class="photo-thumb">
              <img :src="src" alt="첨부" />
              <button type="button" class="photo-remove" @click="removeImage(idx)">
                <i class="bi bi-x"></i>
              </button>
            </div>
          </div>

          <!-- 공개 범위 -->
          <div class="form-field-group">
            <label class="field-label">피드 공개 범위</label>
            <div class="visibility-group">
              <button
                type="button"
                class="visibility-btn"
                :class="{ active: form.visibility === 'PUBLIC' }"
                @click="form.visibility = 'PUBLIC'"
              >
                <i class="bi bi-globe2 me-1"></i> 전체
              </button>
              <button
                type="button"
                class="visibility-btn"
                :class="{ active: form.visibility === 'FRIENDS' }"
                @click="form.visibility = 'FRIENDS'"
              >
                <i class="bi bi-people-fill me-1"></i> 친구
              </button>
              <button
                type="button"
                class="visibility-btn"
                :class="{ active: form.visibility === 'PRIVATE' }"
                @click="form.visibility = 'PRIVATE'"
              >
                <i class="bi bi-lock-fill me-1"></i> 나만
              </button>
            </div>
          </div>
        </div>

        <!-- 실패 메시지 -->
        <div v-if="statusMessage && !isSuccess" class="error-msg-box">
          <i class="bi bi-exclamation-triangle-fill me-2"></i>
          {{ statusMessage }}
        </div>

        <!-- 제출 버튼 -->
        <button type="submit" class="submit-main-btn" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
          <template v-else>
            <i :class="['bi me-2', form.receiverType === 'DUTCHPAY' ? 'bi-pie-chart-fill' : 'bi-send-fill']"></i>
          </template>
          {{ form.receiverType === 'DUTCHPAY' ? '정산 요청하기' : '송금하기' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import remittanceApi from '@/api/remittanceApi';
import walletApi from '@/api/walletApi';
import dutchpayApi from '@/api/dutchpayApi';
import transactionApi from '@/api/transactionApi';
import api from '@/api';

const route = useRoute();
const myBalance = ref(0);
const loading = ref(false);
const statusMessage = ref('');
const isSuccess = ref(false);
const lastTransactionId = ref(null);
const lastFeedType = ref('REMITTANCE');
const lastVisibility = ref('PUBLIC');

const attachedFiles = ref([]);
const imagePreviews = ref([]);
const fileInputRef = ref(null);

const friendsList = ref([]);
const showHistoryPicker = ref(false);
const historyList = ref([]);
const historyLoading = ref(false);

const fetchFriends = async () => {
  try {
    const data = await remittanceApi.getFriends(form.walletId);
    if (data && Array.isArray(data)) friendsList.value = data;
  } catch (e) {
    console.error('Fetch friends list error:', e);
  }
};

const defaultAccounts = [
  { bankCode: '088', bankName: '신한은행', accountNumber: '222-002-000001', ownerName: '이KB' },
  { bankCode: '004', bankName: 'KB국민은행', accountNumber: '110-111-111111', ownerName: '김국민' },
  { bankCode: '020', bankName: '우리은행', accountNumber: '1002-345-6789', ownerName: '박스타' },
];

const recentAccounts = ref([...defaultAccounts]);
const bankList = ref([
  { bankCode: '004', bankName: 'KB국민' },
  { bankCode: '088', bankName: '신한' },
  { bankCode: '020', bankName: '우리' },
  { bankCode: '011', bankName: 'NH농협' },
  { bankCode: '090', bankName: '카카오뱅크' },
]);

const form = reactive({
  walletId: 1,
  receiverType: 'WALLET',
  receiverId: 2,
  bankCode: '088',
  accountNumber: '222-002-000001',
  amount: 30000,
  memo: '',
  visibility: 'PUBLIC',
});

const dutchForm = reactive({
  title: '금요일 모임 더치페이',
  settlementType: 'EQUAL',
  memberIdsStr: '2, 3',
  customAmounts: {},
});

const parseMemberIds = () => {
  if (!dutchForm.memberIdsStr) return [];
  return dutchForm.memberIdsStr.split(',').map(s => Number(s.trim())).filter(n => !isNaN(n) && n > 0);
};

const formatCurrency = (val) => {
  if (val === undefined || val === null) return '₩0';
  return '₩' + Number(val).toLocaleString('ko-KR');
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`;
};

const getSelectedBankName = () => {
  const found = bankList.value.find(b => b.bankCode === form.bankCode);
  return found ? found.bankName : '은행';
};

const triggerFileInput = () => { if (fileInputRef.value) fileInputRef.value.click(); };

const handleFileChange = (e) => {
  const files = Array.from(e.target.files || []);
  if (!files.length) return;
  files.forEach((file) => {
    attachedFiles.value.push(file);
    const reader = new FileReader();
    reader.onload = (event) => { imagePreviews.value.push(event.target.result); };
    reader.readAsDataURL(file);
  });
  if (fileInputRef.value) fileInputRef.value.value = '';
};

const removeImage = (index) => {
  attachedFiles.value.splice(index, 1);
  imagePreviews.value.splice(index, 1);
};

const resetForm = () => {
  isSuccess.value = false;
  statusMessage.value = '';
  lastTransactionId.value = null;
  form.amount = 30000;
  form.memo = '';
  attachedFiles.value = [];
  imagePreviews.value = [];
};

const addAmount = (val) => { form.amount = (form.amount || 0) + val; };

const fetchMyBalance = async () => {
  try {
    const data = await walletApi.getWalletByUserId(form.walletId);
    if (data) myBalance.value = data.balance;
  } catch (e) {
    console.error('Balance fetch error:', e);
  }
};

const fetchRecentBankInfo = async () => {
  try {
    const data = await remittanceApi.getBankRemittanceInfo(form.walletId);
    if (data) {
      if (data.banks && data.banks.length > 0) bankList.value = data.banks;
      if (data.recentAccounts && data.recentAccounts.length > 0) {
        recentAccounts.value = data.recentAccounts.slice(0, 3);
      } else {
        recentAccounts.value = [...defaultAccounts];
      }
    }
  } catch (e) {
    console.error('Fetch recent bank info error:', e);
    recentAccounts.value = [...defaultAccounts];
  }
};

const toggleHistoryPicker = async () => {
  showHistoryPicker.value = !showHistoryPicker.value;
  if (showHistoryPicker.value && historyList.value.length === 0) {
    historyLoading.value = true;
    try {
      const data = await transactionApi.getTransactions(form.walletId);
      if (data) historyList.value = data;
    } catch (err) {
      console.error('History fetch error:', err);
    } finally {
      historyLoading.value = false;
    }
  }
};

const applyTransactionToDutch = (tx) => {
  dutchForm.title = tx.memo || tx.merchantName || `결제건 #${tx.transactionId} 더치페이`;
  form.amount = tx.amount;
  form.memo = tx.memo || `${dutchForm.title} 정산 요청`;
  showHistoryPicker.value = false;
};

const selectRecentAccount = (acc) => {
  form.bankCode = acc.bankCode;
  form.accountNumber = acc.accountNumber;
};

const confirmAction = async () => {
  if (form.receiverType === 'DUTCHPAY') await submitDutchpay();
  else await confirmTransfer();
};

const submitDutchpay = async () => {
  const memberIds = parseMemberIds();
  if (memberIds.length === 0) { alert('정산 참여 친구 회원 ID를 입력해 주세요.'); return; }
  loading.value = true;
  statusMessage.value = '';
  isSuccess.value = false;
  const totalMembers = memberIds.length + 1;
  const defaultPerAmount = Math.floor(form.amount / totalMembers);
  let membersPayload = [];
  if (dutchForm.settlementType === 'UNEQUAL') {
    membersPayload = memberIds.map(mId => ({ userId: mId, amount: dutchForm.customAmounts[mId] || defaultPerAmount }));
  } else {
    membersPayload = memberIds.map(mId => ({ userId: mId, amount: defaultPerAmount }));
  }
  const payload = {
    requesterId: form.walletId,
    title: dutchForm.title,
    content: form.memo || `${dutchForm.title} 더치페이 정산`,
    totalAmount: form.amount,
    spendingCategoryId: 1,
    settlementType: dutchForm.settlementType,
    members: membersPayload,
  };
  try {
    const res = await dutchpayApi.createDutchpay(payload);
    isSuccess.value = true;
    statusMessage.value = `'${dutchForm.title}' 정산방 생성 완료! (참여 인원: ${totalMembers}명)`;
    if (res && attachedFiles.value.length > 0) {
      try {
        const formData = new FormData();
        formData.append('userId', form.walletId);
        formData.append('targetId', res.settlementId || 1);
        formData.append('feedType', 'SETTLEMENT');
        formData.append('content', dutchForm.title || '정산 요청');
        formData.append('visibility', form.visibility);
        attachedFiles.value.forEach(file => formData.append('files', file));
        await api.post('/api/feeds', formData);
      } catch (imgErr) { console.warn('정산 피드 이미지 업로드 예외:', imgErr); }
    }
    await fetchMyBalance();
  } catch (err) {
    console.error('Dutchpay Error:', err);
    isSuccess.value = false;
    statusMessage.value = err.response?.data?.message || '더치페이 정산방 생성 실패';
  } finally {
    loading.value = false;
  }
};

const confirmTransfer = async () => {
  if (form.amount > myBalance.value) {
    if (!confirm('지갑 잔액이 부족합니다. 계속 진행하시겠습니까?')) return;
  }
  loading.value = true;
  statusMessage.value = '';
  isSuccess.value = false;
  lastTransactionId.value = null;
  let sendData;
  if (attachedFiles.value.length > 0) {
    const formData = new FormData();
    formData.append('walletId', form.walletId);
    formData.append('receiverType', form.receiverType);
    formData.append('amount', form.amount);
    if (form.memo) formData.append('memo', form.memo);
    formData.append('feedType', 'TRANSFER');
    formData.append('content', form.memo || '송금 완료!');
    formData.append('visibility', form.visibility);
    if (form.receiverType === 'WALLET') formData.append('receiverId', form.receiverId);
    else { formData.append('bankCode', form.bankCode); formData.append('accountNumber', form.accountNumber); }
    attachedFiles.value.forEach(file => formData.append('files', file));
    sendData = formData;
  } else {
    sendData = {
      walletId: form.walletId, receiverType: form.receiverType,
      amount: form.amount, memo: form.memo,
      feedType: 'TRANSFER', content: form.memo || '송금 완료!',
      visibility: form.visibility,
    };
    if (form.receiverType === 'WALLET') sendData.receiverId = form.receiverId;
    else { sendData.bankCode = form.bankCode; sendData.accountNumber = form.accountNumber; }
  }
  try {
    const res = await remittanceApi.sendMoney(sendData);
    isSuccess.value = true;
    if (res) {
      lastTransactionId.value = res.transactionId;
      lastFeedType.value = res.feedType || 'REMITTANCE';
      lastVisibility.value = res.visibility || form.visibility;
    }
    const targetName = form.receiverType === 'WALLET' ? `친구 #${form.receiverId}` : `계좌(${form.accountNumber})`;
    statusMessage.value = `${targetName}에게 ${Number(form.amount).toLocaleString('ko-KR')}원 송금 완료`;
    if (res && attachedFiles.value.length > 0) {
      try {
        const formData = new FormData();
        formData.append('userId', form.walletId);
        formData.append('targetId', res.transactionId || 1);
        formData.append('feedType', 'TRANSFER');
        formData.append('content', form.memo || '송금 완료!');
        formData.append('visibility', form.visibility);
        attachedFiles.value.forEach(file => formData.append('files', file));
        await api.post('/api/feeds', formData);
      } catch (imgErr) { console.warn('DB 피드 이미지 업로드 예외:', imgErr); }
    }
    await fetchMyBalance();
    await fetchRecentBankInfo();
  } catch (err) {
    console.error('Remittance Error:', err);
    isSuccess.value = false;
    statusMessage.value = err.response?.data?.message || '송금 실패: 출금 잔액 및 입력 정보를 확인해 주세요.';
  } finally {
    loading.value = false;
  }
};

watch(() => form.receiverType, (newVal) => {
  if (newVal === 'ACCOUNT') fetchRecentBankInfo();
});

onMounted(() => {
  if (route.query.walletId) form.walletId = Number(route.query.walletId);
  if (route.query.receiverType) form.receiverType = route.query.receiverType;
  if (route.query.amount) form.amount = Number(route.query.amount);
  if (route.query.title) dutchForm.title = route.query.title;
  fetchMyBalance();
  fetchRecentBankInfo();
  fetchFriends();
});
</script>

<style scoped>
/* ═══════════════════════════════════════
   루트
═══════════════════════════════════════ */
.remit-root {
  min-height: 100vh;
  background: #F4F6FA;
  padding-bottom: 80px;
}

/* ═══════════════════════════════════════
   헤더
═══════════════════════════════════════ */
.remit-header {
  background: linear-gradient(150deg, #1A1A2E 0%, #16213E 60%, #0F3460 100%);
  padding: 52px 16px 20px;
  position: sticky;
  top: 0;
  z-index: 100;
}
.remit-header-inner {
  max-width: 480px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 12px;
}
.back-btn {
  width: 38px; height: 38px;
  background: rgba(255,255,255,0.1);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #fff;
  text-decoration: none;
  font-size: 1.1rem;
  flex-shrink: 0;
  transition: background 0.2s ease;
}
.back-btn:hover { background: rgba(255,255,255,0.18); color: #FFBC00; }
.header-title-wrap { flex: 1; display: flex; align-items: center; gap: 8px; }
.header-badge {
  background: rgba(255,188,0,0.2);
  color: #FFBC00;
  border: 1px solid rgba(255,188,0,0.3);
  border-radius: 20px;
  padding: 2px 10px;
  font-size: 0.7rem;
  font-weight: 800;
  white-space: nowrap;
}
.header-title { color: #fff; font-size: 1.15rem; font-weight: 800; margin: 0; }
.header-balance-chip {
  background: rgba(255,255,255,0.1);
  color: rgba(255,255,255,0.85);
  border-radius: 20px;
  padding: 5px 12px;
  font-size: 0.78rem;
  font-weight: 700;
  white-space: nowrap;
  border: 1px solid rgba(255,255,255,0.15);
}

/* ═══════════════════════════════════════
   본문
═══════════════════════════════════════ */
.page-body {
  max-width: 480px;
  margin: 0 auto;
  padding: 20px 16px;
}

/* ═══════════════════════════════════════
   타입 탭 (송금 방식)
═══════════════════════════════════════ */
.type-tab-group {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.type-tab {
  flex: 1;
  background: #fff;
  border: 2px solid #E2E8F0;
  border-radius: 14px;
  padding: 12px 4px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  font-size: 0.78rem;
  font-weight: 700;
  color: #94A3B8;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.type-tab i { font-size: 1.2rem; }
.type-tab.active {
  background: #1A1A2E;
  border-color: #1A1A2E;
  color: #FFBC00;
  box-shadow: 0 6px 20px rgba(26,26,46,0.25);
  transform: translateY(-2px);
}

/* ═══════════════════════════════════════
   폼 카드들
═══════════════════════════════════════ */
.form-sections { display: flex; flex-direction: column; gap: 12px; }

.form-card {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.form-card-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.9rem;
  font-weight: 800;
  color: #1A1A2E;
  margin-bottom: 16px;
}
.step-num {
  width: 24px; height: 24px;
  background: #FFBC00;
  color: #1A1A2E;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.78rem;
  font-weight: 900;
  flex-shrink: 0;
}

/* ═══════════════════════════════════════
   폼 필드
═══════════════════════════════════════ */
.form-field-group { display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px; }
.form-field-group:last-child { margin-bottom: 0; }

.field-label {
  font-size: 0.75rem;
  font-weight: 700;
  color: #64748B;
}
.field-hint { font-weight: 500; color: #94A3B8; margin-left: 4px; }

.input-with-icon { position: relative; }
.field-icon {
  position: absolute; left: 14px; top: 50%;
  transform: translateY(-50%);
  color: #94A3B8; font-size: 0.9rem;
}
.field-input {
  width: 100%;
  border: 1.5px solid #E2E8F0;
  border-radius: 12px;
  padding: 13px 16px 13px 38px;
  font-size: 0.95rem;
  font-weight: 600;
  color: #1A1A2E;
  outline: none;
  background: #FAFAFA;
  transition: all 0.2s ease;
}
.field-input:focus {
  border-color: #FFBC00;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(255,188,0,0.12);
}
.field-select {
  width: 100%;
  border: 1.5px solid #E2E8F0;
  border-radius: 12px;
  padding: 13px 16px;
  font-size: 0.9rem;
  font-weight: 600;
  color: #1A1A2E;
  outline: none;
  background: #FAFAFA;
  cursor: pointer;
  transition: all 0.2s ease;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24'%3E%3Cpath fill='%2394A3B8' d='M7 10l5 5 5-5z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
}
.field-select:focus { border-color: #FFBC00; box-shadow: 0 0 0 3px rgba(255,188,0,0.12); }

/* 친구 스크롤 */
.friend-scroll {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 12px;
  margin-bottom: 12px;
  scrollbar-width: none;
}
.friend-scroll::-webkit-scrollbar { display: none; }

.friend-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  flex-shrink: 0;
}
.friend-avatar {
  width: 54px; height: 54px;
  border-radius: 16px;
  background: #F1F5F9;
  border: 2px solid #E2E8F0;
  display: flex; align-items: center; justify-content: center;
  font-size: 1.4rem;
  color: #94A3B8;
  position: relative;
  transition: all 0.2s ease;
}
.friend-chip.selected .friend-avatar {
  background: #1A1A2E;
  border-color: #FFBC00;
  color: #FFBC00;
}
.friend-check {
  position: absolute; bottom: -4px; right: -4px;
  width: 18px; height: 18px;
  background: #FFBC00;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.65rem;
  color: #1A1A2E;
  font-weight: 900;
  border: 1.5px solid #fff;
}
.friend-name { font-size: 0.72rem; font-weight: 700; color: #1A1A2E; max-width: 60px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.friend-id { font-size: 0.65rem; color: #94A3B8; }

/* 최근 계좌 */
.recent-accs { margin-bottom: 16px; }
.recent-accs-label { font-size: 0.72rem; font-weight: 700; color: #94A3B8; margin-bottom: 8px; }
.recent-acc-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1.5px solid #F1F5F9;
  background: #FAFAFA;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.recent-acc-item:hover, .recent-acc-item.selected {
  border-color: #FFBC00;
  background: #FFFBEB;
}
.acc-bank-badge {
  width: 40px; height: 40px;
  background: #1A1A2E;
  color: #FFBC00;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.72rem;
  font-weight: 900;
  flex-shrink: 0;
}
.acc-info { flex: 1; }
.acc-info strong { display: block; font-size: 0.85rem; font-weight: 800; color: #1A1A2E; }
.acc-info span { font-size: 0.72rem; color: #94A3B8; }
.acc-arrow { color: #CBD5E1; }

/* 더치페이 */
.dutch-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.history-btn {
  background: #F1F5F9;
  border: none;
  border-radius: 20px;
  padding: 5px 12px;
  font-size: 0.72rem;
  font-weight: 700;
  color: #475569;
  cursor: pointer;
  transition: all 0.15s ease;
}
.history-btn:hover { background: #E2E8F0; }

.history-picker {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  margin-bottom: 8px;
  overflow: hidden;
}
.history-loading, .history-empty {
  padding: 16px;
  text-align: center;
  font-size: 0.82rem;
  color: #94A3B8;
}
.history-list { max-height: 180px; overflow-y: auto; }
.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  border-bottom: 1px solid #F1F5F9;
  cursor: pointer;
  transition: background 0.15s ease;
}
.history-item:hover { background: #FFF8E1; }
.history-item:last-child { border-bottom: none; }
.history-item-info strong { display: block; font-size: 0.82rem; font-weight: 700; color: #1A1A2E; }
.history-item-info span { font-size: 0.72rem; color: #94A3B8; }
.history-item-amount { font-size: 0.85rem; font-weight: 800; color: #1A1A2E; }

.split-type-group { display: flex; gap: 8px; }
.split-btn {
  flex: 1;
  background: #F8FAFC;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  padding: 10px 0;
  font-size: 0.8rem;
  font-weight: 700;
  color: #64748B;
  cursor: pointer;
  transition: all 0.2s ease;
}
.split-btn.active {
  background: #1A1A2E;
  border-color: #1A1A2E;
  color: #FFBC00;
}

.unequal-box {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 14px;
  margin-top: 8px;
}
.unequal-title { font-size: 0.75rem; font-weight: 700; color: #64748B; margin-bottom: 10px; }
.unequal-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.unequal-row:last-child { margin-bottom: 0; }
.unequal-id { font-size: 0.78rem; font-weight: 700; color: #1A1A2E; width: 64px; flex-shrink: 0; }
.unequal-input-wrap { position: relative; flex: 1; }
.unequal-input {
  width: 100%;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  padding: 9px 36px 9px 12px;
  font-size: 0.9rem;
  font-weight: 700;
  color: #1A1A2E;
  outline: none;
  background: #fff;
  text-align: right;
}
.unequal-input:focus { border-color: #FFBC00; box-shadow: 0 0 0 3px rgba(255,188,0,0.1); }
.unequal-unit { position: absolute; right: 10px; top: 50%; transform: translateY(-50%); font-size: 0.75rem; color: #94A3B8; font-weight: 600; }

/* ═══════════════════════════════════════
   금액 입력 (STEP 2)
═══════════════════════════════════════ */
.big-amount-input-wrap {
  display: flex;
  align-items: center;
  border: 2px solid #E2E8F0;
  border-radius: 16px;
  padding: 4px 20px;
  background: #FAFAFA;
  transition: all 0.2s ease;
  margin-bottom: 12px;
}
.big-amount-input-wrap:focus-within {
  border-color: #FFBC00;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(255,188,0,0.1);
}
.big-amount-currency { font-size: 1.8rem; font-weight: 900; color: #CBD5E1; margin-right: 8px; }
.big-amount-input {
  flex: 1;
  border: none;
  background: none;
  outline: none;
  font-size: 2rem;
  font-weight: 900;
  color: #1A1A2E;
  padding: 12px 0;
  min-width: 0;
}
.big-amount-input::placeholder { color: #E2E8F0; }

.quick-amounts { display: flex; gap: 6px; margin-bottom: 16px; }
.q-btn {
  flex: 1;
  background: #F8FAFC;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  padding: 9px 0;
  font-size: 0.78rem;
  font-weight: 700;
  color: #475569;
  cursor: pointer;
  transition: all 0.15s ease;
}
.q-btn:hover { background: #FFF8E1; border-color: #FFBC00; color: #1A1A2E; }
.q-btn:active { transform: scale(0.95); }

.balance-compare {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.balance-compare-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.78rem;
}
.balance-compare-row span:first-child { color: #64748B; font-weight: 600; }
.compare-val { font-weight: 800; color: #1A1A2E; }
.compare-val.warn { color: #EF4444; }

/* ═══════════════════════════════════════
   메모 & 피드 (STEP 3)
═══════════════════════════════════════ */
.photo-attach-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.photo-btn {
  background: #F1F5F9;
  border: none;
  border-radius: 20px;
  padding: 7px 16px;
  font-size: 0.8rem;
  font-weight: 700;
  color: #475569;
  cursor: pointer;
  display: flex; align-items: center;
  transition: all 0.15s ease;
  position: relative;
}
.photo-btn:hover { background: #FFF8E1; color: #1A1A2E; }
.photo-count {
  background: #FFBC00;
  color: #1A1A2E;
  border-radius: 20px;
  padding: 0 6px;
  font-size: 0.68rem;
  font-weight: 900;
  margin-left: 6px;
}
.photo-hint { font-size: 0.73rem; color: #94A3B8; }

.photo-previews { display: flex; gap: 8px; overflow-x: auto; padding-bottom: 4px; margin-bottom: 12px; }
.photo-thumb {
  position: relative;
  width: 72px; height: 72px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
}
.photo-thumb img { width: 100%; height: 100%; object-fit: cover; }
.photo-remove {
  position: absolute;
  top: 4px; right: 4px;
  width: 20px; height: 20px;
  background: rgba(0,0,0,0.6);
  border: none;
  border-radius: 50%;
  color: #fff;
  font-size: 0.7rem;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
}

.visibility-group { display: flex; gap: 8px; }
.visibility-btn {
  flex: 1;
  background: #F8FAFC;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  padding: 9px 0;
  font-size: 0.78rem;
  font-weight: 700;
  color: #64748B;
  cursor: pointer;
  transition: all 0.2s ease;
}
.visibility-btn.active { background: #1A1A2E; border-color: #1A1A2E; color: #FFBC00; }

/* ═══════════════════════════════════════
   에러 메시지
═══════════════════════════════════════ */
.error-msg-box {
  background: #FEF2F2;
  border: 1px solid #FCA5A5;
  border-radius: 12px;
  padding: 14px 16px;
  color: #DC2626;
  font-size: 0.85rem;
  font-weight: 600;
  display: flex;
  align-items: center;
}

/* ═══════════════════════════════════════
   제출 버튼
═══════════════════════════════════════ */
.submit-main-btn {
  width: 100%;
  background: linear-gradient(135deg, #FFBC00 0%, #FF9900 100%);
  color: #1A1A2E;
  border: none;
  border-radius: 18px;
  padding: 19px;
  font-size: 1.05rem;
  font-weight: 900;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(255,188,0,0.4);
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  letter-spacing: -0.3px;
}
.submit-main-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 16px 40px rgba(255,188,0,0.5);
}
.submit-main-btn:active { transform: translateY(0); }
.submit-main-btn:disabled { opacity: 0.6; cursor: not-allowed; transform: none; box-shadow: none; }

/* ═══════════════════════════════════════
   영수증 화면
═══════════════════════════════════════ */
.receipt-wrapper { display: flex; flex-direction: column; gap: 16px; }

.receipt-hero {
  text-align: center;
  padding: 24px 0 8px;
}
.receipt-check-ring {
  width: 90px; height: 90px;
  border-radius: 50%;
  background: rgba(255,188,0,0.12);
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 16px;
  animation: ring-pulse 2s ease-in-out infinite;
}
@keyframes ring-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255,188,0,0.3); }
  50% { box-shadow: 0 0 0 16px rgba(255,188,0,0); }
}
.receipt-check-icon {
  width: 64px; height: 64px;
  background: linear-gradient(135deg, #FFBC00 0%, #FF9900 100%);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 1.8rem;
  color: #1A1A2E;
  box-shadow: 0 8px 24px rgba(255,188,0,0.4);
}
.receipt-type-badge {
  display: inline-block;
  background: #1A1A2E;
  color: #FFBC00;
  border-radius: 20px;
  padding: 4px 16px;
  font-size: 0.7rem;
  font-weight: 900;
  letter-spacing: 1px;
  margin-bottom: 10px;
}
.receipt-main-title { font-size: 1.5rem; font-weight: 900; color: #1A1A2E; margin-bottom: 6px; letter-spacing: -0.5px; }
.receipt-sub { font-size: 0.82rem; color: #64748B; margin: 0; line-height: 1.5; }

.receipt-amount-box {
  background: linear-gradient(135deg, #1A1A2E 0%, #0F3460 100%);
  border-radius: 20px;
  padding: 20px 24px;
  text-align: center;
}
.receipt-amount-label { color: rgba(255,255,255,0.5); font-size: 0.75rem; font-weight: 600; margin-bottom: 6px; }
.receipt-amount { color: #FFBC00; font-size: 2.2rem; font-weight: 900; letter-spacing: -1px; line-height: 1; }

.receipt-detail-card {
  background: #fff;
  border-radius: 20px;
  padding: 4px 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.receipt-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #F1F5F9;
}
.receipt-row.last { border-bottom: none; }
.receipt-label { font-size: 0.78rem; font-weight: 600; color: #94A3B8; }
.receipt-value { font-size: 0.88rem; font-weight: 800; color: #1A1A2E; text-align: right; max-width: 60%; word-break: break-all; }
.receipt-value.mono { font-family: 'Courier New', monospace; color: #64748B; }

.receipt-actions { display: flex; gap: 10px; }
.receipt-btn {
  flex: 1;
  border: none;
  border-radius: 16px;
  padding: 16px;
  font-size: 0.9rem;
  font-weight: 800;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  transition: all 0.2s ease;
}
.receipt-btn.outline {
  background: #fff;
  border: 2px solid #E2E8F0;
  color: #475569;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.receipt-btn.outline:hover { border-color: #1A1A2E; color: #1A1A2E; }
.receipt-btn.primary {
  background: linear-gradient(135deg, #FFBC00 0%, #FF9900 100%);
  color: #1A1A2E;
  box-shadow: 0 8px 24px rgba(255,188,0,0.35);
}
.receipt-btn.primary:hover { transform: translateY(-2px); box-shadow: 0 12px 32px rgba(255,188,0,0.45); }

/* ═══════════════════════════════════════
   트랜지션
═══════════════════════════════════════ */
.slide-up-enter-active { transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
.slide-up-enter-from { opacity: 0; transform: translateY(20px); }
</style>
