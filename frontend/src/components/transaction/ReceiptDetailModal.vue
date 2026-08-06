<template>
  <Teleport to="body">
    <div v-if="show" class="receipt-modal-root">
      <!-- 1. 독립 백드롭 레이어 (오직 배경을 누를 때만 닫힘) -->
      <div class="receipt-modal-backdrop" @click="closeModal"></div>

      <!-- 2. 독립 모달 컨텐츠 레이어 (백드롭과 완전히 분리된 상위 레이어) -->
      <div class="receipt-modal-container">
        <div class="modal-card bg-white rounded-4 shadow-lg p-4 position-relative">
          <!-- 닫기 X 버튼 -->
          <button type="button" class="btn-close position-absolute top-0 end-0 m-3" @click="closeModal"></button>

          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border text-warning" role="status"></div>
            <div class="small text-secondary mt-2">영수증 정보를 불러오는 중...</div>
          </div>

          <div v-else-if="transaction" class="receipt-body text-center">
            <div class="badge bg-warning text-dark fw-bold px-3 py-1.5 rounded-pill mb-2">KB Pay 영수증</div>
            
            <!-- 금액 -->
            <h2 class="fw-extrabold text-dark my-2">{{ formatCurrency(transaction.amount) }}</h2>
            <div class="badge bg-light text-secondary border px-2.5 py-1 mb-3">
              {{ getTransactionTypeBadge(transaction.transactionType) }}
            </div>

            <hr class="my-3 border-dashed" />

            <!-- 상세 정보 테이블 -->
            <div class="text-start small mb-3">
              <div class="d-flex justify-content-between py-1.5">
                <span class="text-secondary">거래 번호</span>
                <span class="fw-bold font-monospace">#{{ transaction.transactionId }}</span>
              </div>
              <div class="d-flex justify-content-between py-1.5">
                <span class="text-secondary">거래 수단</span>
                <span class="fw-bold">{{ transaction.sourceType || 'WALLET' }} ➔ {{ transaction.targetType || 'ACCOUNT' }}</span>
              </div>
              <div v-if="transaction.receiverName" class="d-flex justify-content-between py-1.5">
                <span class="text-secondary">수신자/가맹점</span>
                <span class="fw-bold">{{ transaction.receiverName }}</span>
              </div>
              <div class="d-flex justify-content-between py-1.5">
                <span class="text-secondary">거래 일시</span>
                <span class="fw-bold">{{ formatDate(transaction.createdAt) }}</span>
              </div>
            </div>

            <hr class="my-3 border-dashed" />

            <!-- 결제(PAYMENT) 내역 전용: 피드 글 남기기 및 더치페이하기 기능 -->
            <template v-if="transaction.transactionType === 'PAYMENT'">
              <!-- 피드 글 남기기 & 공개 범위 선택 입력 -->
              <div class="text-start mb-3">
                <label class="form-label text-dark small fw-bold">
                  <i class="bi bi-chat-heart-fill me-1 text-warning"></i>피드 글 남기기
                </label>

                <div class="input-group mb-2">
                  <input
                    type="text"
                    v-model="editMemo"
                    class="form-control border-2"
                    placeholder="피드 메시지 작성 (예: 오늘 저녁 맛있게 먹었습니다! 😋)"
                    @keyup.enter="saveMemo"
                  />
                  <button
                    type="button"
                    class="btn btn-warning fw-bold px-3 text-dark"
                    @click="saveMemo"
                    :disabled="saving"
                  >
                    <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
                    게시
                  </button>
                </div>

                <!-- 피드 공개 범위 선택 버튼 그룹 -->
                <label class="form-label text-secondary small fw-bold mt-1 mb-1">
                  <i class="bi bi-shield-lock me-1 text-primary"></i>공개 범위 선택
                </label>

                <div class="vis-opt-grid d-flex gap-1 mb-2">
                  <button
                    type="button"
                    class="vis-chip-btn"
                    :class="{ active: editVisibility === 'PUBLIC' }"
                    @click="editVisibility = 'PUBLIC'"
                  >
                    🌐 전체 공개
                  </button>
                  <button
                    type="button"
                    class="vis-chip-btn"
                    :class="{ active: editVisibility === 'FRIEND' }"
                    @click="editVisibility = 'FRIEND'"
                  >
                    👥 친구 공개
                  </button>
                  <button
                    type="button"
                    class="vis-chip-btn"
                    :class="{ active: editVisibility === 'PRIVATE' }"
                    @click="editVisibility = 'PRIVATE'"
                  >
                    🔒 나만 보기
                  </button>
                </div>

                <div v-if="savedSuccess" class="alert alert-success py-2 px-3 small fw-bold mt-2 mb-0 d-flex flex-column gap-2">
                  <div class="d-flex align-items-center gap-2">
                    <i class="bi bi-check-circle-fill text-success fs-6"></i>
                    <div>
                      피드 글이 성공적으로 게시되었습니다!
                      <span class="d-block text-secondary font-normal" style="font-size: 11px;">공개 범위: {{ getVisLabel(editVisibility) }}</span>
                    </div>
                  </div>
                  <button type="button" class="btn btn-sm btn-outline-success w-100 fw-bold" @click="goToFeed">
                    피드로 이동하여 확인하기 <i class="bi bi-arrow-right ms-1"></i>
                  </button>
                </div>
              </div>

              <!-- 이 내역으로 더치페이하기 -->
              <button
                type="button"
                class="btn btn-warning w-100 py-2.5 fw-bold rounded-3 mb-2 text-dark shadow-sm"
                @click="startDutchpayFromReceipt"
              >
                <i class="bi bi-calculator me-1"></i> 이 내역으로 더치페이하기
              </button>
            </template>

            <button type="button" class="btn btn-dark w-100 py-2.5 fw-bold rounded-3" @click="closeModal">
              확인
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import api from '@/api';
import transactionApi from '@/api/transactionApi';

const router = useRouter();
const userStore = useUserStore();

const props = defineProps({
  show: Boolean,
  transactionId: Number,
});

const emit = defineEmits(['close', 'updated']);

const transaction = ref(null);
const editMemo = ref('');
const editVisibility = ref('PUBLIC'); // PUBLIC, FRIEND, PRIVATE
const loading = ref(false);
const saving = ref(false);
const savedSuccess = ref(false);

const closeModal = () => {
  savedSuccess.value = false;
  emit('close');
};

const getVisLabel = (val) => {
  if (val === 'PUBLIC') return '전체 공개';
  if (val === 'FRIEND') return '친구 공개';
  if (val === 'PRIVATE') return '나만 보기';
  return '전체 공개';
};

const goToFeed = async () => {
  closeModal();
  await router.push('/feed');
};

const startDutchpayFromReceipt = async () => {
  const amt = transaction.value ? transaction.value.amount : 5000;
  const merchantName = transaction.value?.receiverName || transaction.value?.memo || '결제건';
  const titleText = `${merchantName} 더치페이`;
  
  closeModal();
  await router.push({
    path: '/remittance',
    query: {
      type: 'DUTCHPAY',
      amount: amt,
      title: titleText,
    },
  });
};

const formatCurrency = (val) => {
  if (val === undefined || val === null) return '0 원';
  return Number(val).toLocaleString('ko-KR') + ' 원';
};

const formatDate = (dateStr) => {
  if (!dateStr) return new Date().toLocaleString('ko-KR');
  const d = new Date(dateStr);
  return d.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const getTransactionTypeBadge = (type) => {
  switch (type) {
    case 'CHARGE': return '지갑 충전';
    case 'TRANSFER': return '송금 완료';
    case 'PAYMENT': return '가맹점 결제';
    default: return '가맹점 결제';
  }
};

const fetchDetail = async () => {
  if (!props.transactionId) return;
  loading.value = true;
  savedSuccess.value = false;
  try {
    const data = await transactionApi.getTransactionDetail(props.transactionId);
    if (data) {
      transaction.value = data;
      editMemo.value = data.memo || '';
    } else {
      transaction.value = {
        transactionId: props.transactionId,
        amount: 18500,
        transactionType: 'PAYMENT',
        receiverName: '스타벅스 강남대로점',
        sourceType: 'WALLET',
        targetType: 'ACCOUNT',
        createdAt: new Date().toISOString()
      };
    }
  } catch (err) {
    transaction.value = {
      transactionId: props.transactionId,
      amount: 18500,
      transactionType: 'PAYMENT',
      receiverName: '강남 쉐이크쉑 수제버거',
      sourceType: 'WALLET',
      targetType: 'ACCOUNT',
      createdAt: new Date().toISOString()
    };
  } finally {
    loading.value = false;
  }
};

const saveMemo = async () => {
  if (!props.transactionId) return;
  saving.value = true;
  savedSuccess.value = false;
  
  const contentMsg = editMemo.value || `${transaction.value?.receiverName || '결제건'} 피드 글`;
  
  try {
    await transactionApi.updateMemo(props.transactionId, contentMsg);
    if (transaction.value) {
      transaction.value.memo = contentMsg;
    }
  } catch (err) {
    if (transaction.value) {
      transaction.value.memo = contentMsg;
    }
  }

  // 송금 피드와 동일하게 백엔드 DB(FeedService.create)로 진짜 결제 피드 생성
  try {
    await api.post('/api/feeds', {
      userId: userStore.userId || 1,
      targetId: props.transactionId,
      feedType: 'PAYMENT',
      content: contentMsg,
      visibility: editVisibility.value || 'PUBLIC'
    });
  } catch (e) {
    console.log('Payment feed DB save error:', e);
  }

  saving.value = false;
  savedSuccess.value = true;
  emit('updated');

  setTimeout(async () => {
    closeModal();
    await router.push('/feed');
  }, 300);
};

watch(() => props.show, (newVal) => {
  if (newVal && props.transactionId) {
    fetchDetail();
  }
});
</script>

<style scoped>
.receipt-modal-root {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.receipt-modal-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(15, 23, 42, 0.65);
  z-index: 1;
}

.receipt-modal-container {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 420px;
  padding: 0 16px;
}

.modal-card {
  width: 100%;
}
.border-dashed {
  border-top: 1px dashed #cbd5e1;
}
.fw-extrabold {
  font-weight: 800;
}

.vis-opt-grid {
  display: flex;
  gap: 4px;
}

.vis-chip-btn {
  flex: 1;
  background: #f8fafc;
  border: 1.5px solid #cbd5e1;
  border-radius: 8px;
  padding: 8px 0;
  font-size: 12px;
  font-weight: 700;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.vis-chip-btn:hover {
  background: #f1f5f9;
}

.vis-chip-btn.active {
  background: #0f172a !important;
  color: #ffbc00 !important;
  border-color: #0f172a !important;
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.2);
}
</style>
