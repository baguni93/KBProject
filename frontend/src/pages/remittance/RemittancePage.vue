<template>
  <div class="kb-container py-3">
    <!-- 헤더 -->
    <div class="d-flex align-items-center mb-3 px-1">
      <router-link to="/wallet" class="btn btn-link text-dark p-0 me-2">
        <i class="bi bi-chevron-left fs-4"></i>
      </router-link>
      <h5 class="fw-bold mb-0 text-dark">송금 & 정산하기</h5>
    </div>

    <!-- 송금 / 정산 완료 요약 화면 (isSuccess가 true일 때) -->
    <div v-if="isSuccess" class="card border-0 shadow-sm rounded-4 p-4 bg-white text-center mb-4 fade-in">
      <div class="my-3">
        <div class="success-icon-wrap mx-auto mb-3 text-dark rounded-circle d-flex align-items-center justify-content-center shadow-sm">
          <i class="bi bi-check-lg display-4 fw-bold"></i>
        </div>
        <span class="badge bg-secondary-subtle text-dark px-3 py-1.5 rounded-pill mb-2 fw-semibold">
          {{ form.receiverType === 'DUTCHPAY' ? '정산 요청 성공' : '송금 이체 성공' }}
        </span>
        <h4 class="fw-bold text-dark mb-1">
          {{ form.receiverType === 'DUTCHPAY' ? '정산 요청이 완료되었습니다' : '송금이 완료되었습니다' }}
        </h4>
        <p class="text-secondary small mb-0">
          {{ statusMessage }}
        </p>
      </div>

      <!-- 이체 / 정산 금액 대형 표시 -->
      <div class="bg-light rounded-4 p-3 my-3 border border-light-subtle">
        <span class="text-muted small d-block mb-1">
          {{ form.receiverType === 'DUTCHPAY' ? '총 정산 요청 금액' : '보낸 금액' }}
        </span>
        <h2 class="fw-bold text-dark mb-0 font-monospace">
          {{ formatCurrency(form.amount) }}
        </h2>
      </div>

      <!-- 상세 영수증 / 요약 정보 -->
      <div class="text-start bg-white p-2 my-2">
        <div class="d-flex justify-content-between py-2.5 border-bottom align-items-center">
          <span class="text-secondary small fw-bold">
            {{ form.receiverType === 'DUTCHPAY' ? '정산 제목' : '받는 사람 / 계좌' }}
          </span>
          <span class="fw-bold text-dark">
            <template v-if="form.receiverType === 'WALLET'">
              친구 #{{ form.receiverId }}
            </template>
            <template v-else-if="form.receiverType === 'ACCOUNT'">
              {{ getSelectedBankName() }} ({{ form.accountNumber }})
            </template>
            <template v-else-if="form.receiverType === 'DUTCHPAY'">
              {{ dutchForm.title }}
            </template>
          </span>
        </div>

        <div v-if="form.receiverType === 'DUTCHPAY'" class="d-flex justify-content-between py-2.5 border-bottom align-items-center">
          <span class="text-secondary small fw-bold">정산 참여 인원</span>
          <span class="fw-bold text-dark">{{ parseMemberIds().length + 1 }}명 (본인 포함)</span>
        </div>

        <div class="d-flex justify-content-between py-2.5 border-bottom align-items-center">
          <span class="text-secondary small fw-bold">출금 지갑</span>
          <span class="fw-bold text-dark">KB Pay 지갑 (#{{ form.walletId }})</span>
        </div>

        <div v-if="form.memo" class="d-flex justify-content-between py-2.5 border-bottom align-items-center">
          <span class="text-secondary small fw-bold">메모 (피드 내용)</span>
          <span class="text-dark font-semibold">{{ form.memo }}</span>
        </div>

        <div v-if="imagePreviews.length > 0" class="d-flex justify-content-between py-2.5 border-bottom align-items-center">
          <span class="text-secondary small fw-bold">첨부 사진</span>
          <div class="d-flex gap-1.5 overflow-hidden">
            <img
              v-for="(imgSrc, idx) in imagePreviews"
              :key="idx"
              :src="imgSrc"
              class="rounded-2 border object-fit-cover shadow-sm"
              style="width: 36px; height: 36px;"
              alt="첨부 사진"
            />
          </div>
        </div>

        <div class="d-flex justify-content-between py-2.5 border-bottom align-items-center">
          <span class="text-secondary small fw-bold">피드 공개 범위</span>
          <span class="badge bg-light text-dark border">
            {{ form.visibility === 'PUBLIC' ? '전체 공개' : form.visibility === 'FRIENDS' ? '친구 공개' : '나만 보기' }}
          </span>
        </div>

        <div v-if="lastTransactionId" class="d-flex justify-content-between py-2.5 border-bottom align-items-center">
          <span class="text-secondary small fw-bold">거래 번호</span>
          <span class="font-monospace text-muted small">#{{ lastTransactionId }}</span>
        </div>

        <div class="d-flex justify-content-between py-2.5 align-items-center">
          <span class="text-secondary small fw-bold">완료 일시</span>
          <span class="text-muted small">{{ formatDate(new Date()) }}</span>
        </div>
      </div>

      <!-- 액션 버튼 -->
      <div class="d-flex gap-2 mt-3">
        <button type="button" class="btn btn-outline-dark py-2.5 flex-fill fw-bold rounded-3" @click="resetForm">
          <i class="bi bi-arrow-counterclockwise me-1"></i> 추가 송금하기
        </button>
        <router-link to="/wallet" class="btn btn-warning py-2.5 flex-fill fw-bold rounded-3 text-dark shadow-sm">
          <i class="bi bi-wallet2 me-1"></i> 지갑 확인하기
        </router-link>
      </div>
    </div>

    <!-- 송금 폼 카드 (isSuccess가 false일 때만 표시) -->
    <div v-else class="card border-0 shadow-sm rounded-4 p-4 bg-white mb-4">
      <form @submit.prevent="confirmAction">
        <!-- 1. 보낼 출금 정보 (내 지갑) -->
        <div class="mb-4 p-3 bg-light rounded-3 d-flex justify-content-between align-items-center">
          <div>
            <span class="text-secondary small d-block">출금 지갑</span>
            <strong class="text-dark">KB Pay 지갑 (#{{ form.walletId }})</strong>
          </div>
          <div class="text-end">
            <span class="text-secondary small d-block">잔액</span>
            <span class="fw-bold text-dark">{{ formatCurrency(myBalance) }}</span>
          </div>
        </div>

        <!-- 2. 송금 / 정산 방식 선택 (친구 송금 vs 계좌 송금 vs 정산하기) -->
        <div class="mb-4">
          <label class="form-label text-secondary small fw-bold">송금 / 정산 방식</label>
          <div class="btn-group w-100" role="group">
            <input
              type="radio"
              class="btn-check"
              name="receiverType"
              id="typeFriend"
              value="WALLET"
              v-model="form.receiverType"
            />
            <label class="btn btn-outline-dark py-2.5 fw-bold" for="typeFriend">
              <i class="bi bi-person-heart me-1"></i> 친구 송금
            </label>

            <input
              type="radio"
              class="btn-check"
              name="receiverType"
              id="typeAccount"
              value="ACCOUNT"
              v-model="form.receiverType"
            />
            <label class="btn btn-outline-dark py-2.5 fw-bold" for="typeAccount">
              <i class="bi bi-bank me-1"></i> 계좌 송금
            </label>

            <input
              type="radio"
              class="btn-check"
              name="receiverType"
              id="typeDutch"
              value="DUTCHPAY"
              v-model="form.receiverType"
            />
            <label class="btn btn-outline-dark py-2.5 fw-bold" for="typeDutch">
              <i class="bi bi-calculator me-1"></i> 정산하기
            </label>
          </div>
        </div>

        <!-- 3. 받으실 분 / 정산 세부 설정 (유형별 분기) -->
        <!-- 3-A. 친구 송금 -->
        <div v-if="form.receiverType === 'WALLET'" class="mb-4">
          <label class="form-label text-secondary small fw-bold">받는 친구 (회원 ID)</label>
          <input
            type="number"
            v-model.number="form.receiverId"
            class="form-control form-control-lg fs-6 border-2"
            placeholder="친구 회원 번호 입력 (예: 2)"
            required
          />
        </div>

        <!-- 3-B. 계좌 송금 & 최근 송금 계좌 -->
        <div v-else-if="form.receiverType === 'ACCOUNT'" class="mb-4">
          <!-- 최근 송금 계좌 리스트 -->
          <div class="mb-3">
            <div class="mb-2">
              <span class="text-secondary small fw-bold">최근 송금 계좌</span>
            </div>
            <div class="d-flex flex-column gap-2">
              <div
                v-for="(acc, idx) in recentAccounts"
                :key="idx"
                class="p-2.5 rounded-3 border bg-light d-flex align-items-center justify-content-between cursor-pointer recent-acc-item"
                :class="{ 'border-warning bg-warning-subtle shadow-sm': form.accountNumber === acc.accountNumber }"
                @click="selectRecentAccount(acc)"
              >
                <div class="d-flex align-items-center gap-2.5">
                  <div class="bank-badge bg-warning text-dark fw-bold rounded-circle d-flex align-items-center justify-content-center" style="width: 36px; height: 36px; font-size: 0.75rem;">
                    {{ acc.bankName?.substring(0, 2) || '은행' }}
                  </div>
                  <div>
                    <div class="fw-bold text-dark small mb-0">{{ acc.ownerName }} ({{ acc.bankName }})</div>
                    <div class="text-secondary font-monospace" style="font-size: 0.8rem;">{{ acc.accountNumber }}</div>
                  </div>
                </div>
                <i class="bi bi-chevron-right text-muted small"></i>
              </div>
            </div>
          </div>

          <!-- 입금 은행 및 계좌번호 직접 입력 -->
          <label class="form-label text-secondary small fw-bold mt-2">입금 은행 및 계좌번호</label>
          <div class="row g-2 mb-2">
            <div class="col-5">
              <select v-model="form.bankCode" class="form-select form-select-lg fs-6 border-2">
                <option v-for="b in bankList" :key="b.bankCode" :value="b.bankCode">
                  {{ b.bankName }}
                </option>
              </select>
            </div>
            <div class="col-7">
              <input
                type="text"
                v-model="form.accountNumber"
                class="form-control form-control-lg fs-6 border-2"
                placeholder="계좌번호 (- 없이 입력)"
                required
              />
            </div>
          </div>
        </div>

        <!-- 3-C. 정산하기 (더치페이 폼) -->
        <div v-else-if="form.receiverType === 'DUTCHPAY'" class="mb-4 p-3 border rounded-3 bg-light">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="fw-bold text-dark mb-0"><i class="bi bi-pie-chart-fill me-1"></i> 더치페이 정산방 생성</h6>
            <!-- 최근 결제 내역 불러오기 버튼 -->
            <button type="button" class="btn btn-sm btn-outline-dark rounded-pill fw-bold" @click="toggleHistoryPicker">
              <i class="bi bi-receipt me-1"></i> 결제 내역에서 불러오기
            </button>
          </div>

          <!-- 결제 내역 선택 아코디언/목록 -->
          <div v-if="showHistoryPicker" class="mb-3 p-2 bg-white rounded-3 border">
            <div class="small fw-bold text-secondary mb-2 px-1">최근 내 결제/이체 내역 선택</div>
            <div v-if="historyLoading" class="text-center py-2 text-muted small">내역을 불러오는 중...</div>
            <div v-else-if="historyList.length === 0" class="text-center py-2 text-muted small">최근 내역이 없습니다.</div>
            <div v-else class="d-flex flex-column gap-1.5 style-scroll" style="max-height: 180px; overflow-y: auto;">
              <div
                v-for="tx in historyList"
                :key="tx.transactionId"
                class="p-2 border rounded-2 bg-light hover-bg d-flex align-items-center justify-content-between cursor-pointer"
                @click="applyTransactionToDutch(tx)"
              >
                <div>
                  <div class="fw-bold small text-dark">{{ tx.memo || tx.merchantName || '결제건 #' + tx.transactionId }}</div>
                  <div class="text-secondary small" style="font-size: 0.75rem;">{{ formatDate(tx.createdAt) }}</div>
                </div>
                <div class="fw-bold text-dark small">{{ formatCurrency(tx.amount) }}</div>
              </div>
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label text-secondary small fw-bold">정산 제목</label>
            <input
              type="text"
              v-model="dutchForm.title"
              class="form-control border-2"
              placeholder="예: 회식비 더치페이, 펜션 이용료"
              required
            />
          </div>

          <div class="mb-3">
            <label class="form-label text-secondary small fw-bold">분배 방식</label>
            <div class="d-flex gap-2">
              <button
                type="button"
                class="btn flex-fill py-2 fw-bold"
                :class="dutchForm.settlementType === 'EQUAL' ? 'btn-dark' : 'btn-outline-secondary'"
                @click="dutchForm.settlementType = 'EQUAL'"
              >
                1/N 균등 분배
              </button>
              <button
                type="button"
                class="btn flex-fill py-2 fw-bold"
                :class="dutchForm.settlementType === 'UNEQUAL' ? 'btn-dark' : 'btn-outline-secondary'"
                @click="dutchForm.settlementType = 'UNEQUAL'"
              >
                개별 직접 입력
              </button>
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label text-secondary small fw-bold">정산 참여 친구 ID (쉼표 구분)</label>
            <input
              type="text"
              v-model="dutchForm.memberIdsStr"
              class="form-control border-2"
              placeholder="예: 2, 3"
              required
            />
            <span class="form-text text-muted small">정산금을 요청할 친구 회원번호를 입력해주세요.</span>
          </div>

          <!-- 개별 직접 입력 방식(UNEQUAL) 선택 시 개별 금액 입력 칸 -->
          <div v-if="dutchForm.settlementType === 'UNEQUAL' && parseMemberIds().length > 0" class="mb-3 p-3 bg-white border rounded-3">
            <label class="form-label text-dark small fw-bold mb-2">참여 멤버별 요청 금액 직접 입력</label>
            <div v-for="mId in parseMemberIds()" :key="mId" class="mb-2 row align-items-center">
              <div class="col-4">
                <span class="fw-bold small text-secondary">친구 #{{ mId }}</span>
              </div>
              <div class="col-8">
                <div class="input-group input-group-sm">
                  <input
                    type="number"
                    v-model.number="dutchForm.customAmounts[mId]"
                    class="form-control border-2 text-end fw-bold"
                    placeholder="0"
                    min="0"
                    required
                  />
                  <span class="input-group-text bg-light small">원</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 4. 금액 입력 (공통) -->
        <div class="mb-4">
          <label class="form-label text-secondary small fw-bold">
            {{ form.receiverType === 'DUTCHPAY' ? '총 정산 요청 금액' : '보낼 금액' }}
          </label>
          <div class="input-group input-group-lg">
            <input
              type="number"
              v-model.number="form.amount"
              class="form-control fw-bold border-2"
              placeholder="0"
              min="1"
              required
            />
            <span class="input-group-text bg-light fw-bold">원</span>
          </div>

          <!-- 퀵 금액 버튼 -->
          <div class="d-flex gap-1.5 mt-2">
            <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill flex-fill" @click="addAmount(10000)">+1만</button>
            <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill flex-fill" @click="addAmount(30000)">+3만</button>
            <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill flex-fill" @click="addAmount(50000)">+5만</button>
            <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill flex-fill" @click="addAmount(100000)">+10만</button>
          </div>
        </div>

        <!-- 5. 메모 및 사진 첨부 (피드 공유) -->
        <div class="mb-3">
          <label class="form-label text-secondary small fw-bold">메모 (피드 내용)</label>
          <input
            type="text"
            v-model="form.memo"
            class="form-control border-2 mb-2"
            placeholder="예: 축의금, 점심값 송금 완료!"
          />

          <!-- 사진 첨부 버튼 & 서브 텍스트 -->
          <div class="d-flex align-items-center gap-2 mt-2">
            <button
              type="button"
              class="btn btn-sm btn-outline-secondary rounded-pill px-3 py-1.5 fw-semibold d-inline-flex align-items-center gap-1.5"
              @click="triggerFileInput"
            >
              <i class="bi bi-camera-fill text-warning"></i>
              <span>사진 첨부</span>
              <span v-if="attachedFiles.length > 0" class="badge bg-warning text-dark ms-1">{{ attachedFiles.length }}</span>
            </button>
            <span class="text-muted small" style="font-size: 0.75rem;" v-if="attachedFiles.length === 0">
              송금 피드에 추억 사진을 함께 남겨보세요 (선택)
            </span>
            <input
              type="file"
              ref="fileInputRef"
              accept="image/*"
              multiple
              class="d-none"
              @change="handleFileChange"
            />
          </div>

          <!-- 첨부 사진 미리보기 (가로 스크롤) -->
          <div v-if="imagePreviews.length > 0" class="d-flex gap-2 mt-2.5 overflow-auto pb-1 style-scroll">
            <div
              v-for="(imgSrc, idx) in imagePreviews"
              :key="idx"
              class="position-relative flex-shrink-0"
              style="width: 72px; height: 72px;"
            >
              <img
                :src="imgSrc"
                class="w-100 h-100 rounded-3 object-fit-cover border shadow-sm"
                alt="첨부 이미지"
              />
              <button
                type="button"
                class="btn btn-dark btn-sm rounded-circle position-absolute top-0 end-0 translate-middle-y p-0 d-flex align-items-center justify-content-center shadow"
                style="width: 20px; height: 20px; font-size: 0.65rem;"
                @click="removeImage(idx)"
              >
                <i class="bi bi-x"></i>
              </button>
            </div>
          </div>
        </div>

        <!-- 6. 피드 공개 설정 (visibility) -->
        <div class="mb-4">
          <label class="form-label text-secondary small fw-bold">피드 공유 공개 범위 (visibility)</label>
          <select v-model="form.visibility" class="form-select border-2">
            <option value="PUBLIC">전체 공개 (PUBLIC)</option>
            <option value="FRIENDS">친구 공개 (FRIENDS)</option>
            <option value="PRIVATE">나만 보기 (PRIVATE)</option>
          </select>
        </div>

        <!-- 실행 버튼 -->
        <button
          type="submit"
          class="btn btn-warning w-100 py-3 fw-bold rounded-3 fs-6 shadow-sm text-dark"
          :disabled="loading"
        >
          <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
          {{ form.receiverType === 'DUTCHPAY' ? '더치페이 정산 요청하기' : '송금하기' }}
        </button>
      </form>
    </div>

    <!-- 처리 실패 시 Toast/Alert (isSuccess가 false일 때만) -->
    <div v-if="statusMessage && !isSuccess" class="alert alert-danger rounded-3 shadow-sm border-0 mb-3 d-flex align-items-center justify-content-between">
      <div>
        <h6 class="fw-bold mb-1">처리 실패</h6>
        <div class="small">{{ statusMessage }}</div>
      </div>
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

const showHistoryPicker = ref(false);
const historyList = ref([]);
const historyLoading = ref(false);

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
  return dutchForm.memberIdsStr
    .split(',')
    .map(s => Number(s.trim()))
    .filter(n => !isNaN(n) && n > 0);
};

const formatCurrency = (val) => {
  if (val === undefined || val === null) return '0 원';
  return Number(val).toLocaleString('ko-KR') + ' 원';
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

const triggerFileInput = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click();
  }
};

const handleFileChange = (e) => {
  const files = Array.from(e.target.files || []);
  if (!files.length) return;

  files.forEach((file) => {
    attachedFiles.value.push(file);
    const reader = new FileReader();
    reader.onload = (event) => {
      imagePreviews.value.push(event.target.result);
    };
    reader.readAsDataURL(file);
  });

  if (fileInputRef.value) {
    fileInputRef.value.value = '';
  }
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

const addAmount = (val) => {
  form.amount = (form.amount || 0) + val;
};

const fetchMyBalance = async () => {
  try {
    const data = await walletApi.getWalletByUserId(form.walletId);
    if (data) {
      myBalance.value = data.balance;
    }
  } catch (e) {
    console.error('Balance fetch error:', e);
  }
};

const fetchRecentBankInfo = async () => {
  try {
    const data = await remittanceApi.getBankRemittanceInfo(form.walletId);
    if (data) {
      if (data.banks && data.banks.length > 0) {
        bankList.value = data.banks;
      }
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
      if (data) {
        historyList.value = data;
      }
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
  if (form.receiverType === 'DUTCHPAY') {
    await submitDutchpay();
  } else {
    await confirmTransfer();
  }
};

const submitDutchpay = async () => {
  const memberIds = parseMemberIds();
  if (memberIds.length === 0) {
    alert('정산 참여 친구 회원 ID를 입력해 주세요.');
    return;
  }

  loading.value = true;
  statusMessage.value = '';
  isSuccess.value = false;

  const totalMembers = memberIds.length + 1; // 본인 포함
  const defaultPerAmount = Math.floor(form.amount / totalMembers);

  let membersPayload = [];
  if (dutchForm.settlementType === 'UNEQUAL') {
    membersPayload = memberIds.map(mId => ({
      userId: mId,
      amount: dutchForm.customAmounts[mId] || defaultPerAmount,
    }));
  } else {
    membersPayload = memberIds.map(mId => ({
      userId: mId,
      amount: defaultPerAmount,
    }));
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
    statusMessage.value = `'${dutchForm.title}' 정산방이 성공적으로 생성되었습니다! (참여 인원: ${totalMembers}명)`;

    if (res && attachedFiles.value.length > 0) {
      try {
        const formData = new FormData();
        formData.append('userId', form.walletId);
        formData.append('targetId', res.settlementId || 1);
        formData.append('feedType', 'SETTLEMENT');
        formData.append('content', dutchForm.title || '정산 요청');
        formData.append('visibility', form.visibility);
        attachedFiles.value.forEach(file => {
          formData.append('files', file);
        });

        await api.post('/api/feeds', formData);
      } catch (imgErr) {
        console.warn('정산 피드 이미지 업로드 API 전송 예외:', imgErr);
      }
    }

    await fetchMyBalance();
  } catch (err) {
    console.error('Dutchpay Error:', err);
    isSuccess.value = false;
    statusMessage.value = err.response?.data?.message || '더치페이 정산방 생성 실패: 입력 정보를 확인해 주세요.';
  } finally {
    loading.value = false;
  }
};

const confirmTransfer = async () => {
  if (form.amount > myBalance.value) {
    if (!confirm('지갑 잔액이 부족합니다. 계속 진행하시겠습니까?')) {
      return;
    }
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

    if (form.receiverType === 'WALLET') {
      formData.append('receiverId', form.receiverId);
    } else {
      formData.append('bankCode', form.bankCode);
      formData.append('accountNumber', form.accountNumber);
    }

    attachedFiles.value.forEach(file => {
      formData.append('files', file);
    });
    sendData = formData;
  } else {
    sendData = {
      walletId: form.walletId,
      receiverType: form.receiverType,
      amount: form.amount,
      memo: form.memo,
      feedType: 'TRANSFER',
      content: form.memo || '송금 완료!',
      visibility: form.visibility,
    };
    if (form.receiverType === 'WALLET') {
      sendData.receiverId = form.receiverId;
    } else {
      sendData.bankCode = form.bankCode;
      sendData.accountNumber = form.accountNumber;
    }
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
    statusMessage.value = `${targetName}님에게 ${Number(form.amount).toLocaleString('ko-KR')}원을 송금했습니다.`;

    if (res && attachedFiles.value.length > 0) {
      try {
        const formData = new FormData();
        formData.append('userId', form.walletId);
        formData.append('targetId', res.transactionId || 1);
        formData.append('feedType', 'TRANSFER');
        formData.append('content', form.memo || '송금 완료!');
        formData.append('visibility', form.visibility);
        attachedFiles.value.forEach(file => {
          formData.append('files', file);
        });

        await api.post('/api/feeds', formData);
      } catch (imgErr) {
        console.warn('DB 피드 이미지 업로드 API 전송 예외:', imgErr);
      }
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
  if (newVal === 'ACCOUNT') {
    fetchRecentBankInfo();
  }
});

onMounted(() => {
  if (route.query.walletId) {
    form.walletId = Number(route.query.walletId);
  }
  if (route.query.receiverType) {
    form.receiverType = route.query.receiverType;
  }
  if (route.query.amount) {
    form.amount = Number(route.query.amount);
  }
  if (route.query.title) {
    dutchForm.title = route.query.title;
  }
  fetchMyBalance();
  fetchRecentBankInfo();
});
</script>

<style scoped>
.kb-container {
  max-width: 500px;
  margin: 0 auto;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, sans-serif;
}
.success-icon-wrap {
  width: 72px;
  height: 72px;
  background-color: #FFBC00 !important;
}
.fade-in {
  animation: fadeIn 0.3s ease-in-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.recent-acc-item {
  transition: all 0.15s ease;
}
.recent-acc-item:hover {
  background-color: #f1f5f9 !important;
  transform: translateY(-1px);
}
.hover-bg:hover {
  background-color: #e2e8f0 !important;
}
.cursor-pointer {
  cursor: pointer;
}
.style-scroll::-webkit-scrollbar {
  width: 4px;
}
.style-scroll::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}
</style>
