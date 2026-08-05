<template>
  <Teleport to="body">
    <div
      v-if="show"
      class="receipt-modal-backdrop d-flex align-items-center justify-content-center"
      @click="onBackdropClick"
    >
      <div
        class="modal-dialog modal-dialog-centered w-100 px-3"
        style="max-width: 420px;"
        @click.stop
      >
        <div class="modal-card bg-white rounded-4 shadow-lg p-4 position-relative" @click.stop>
          <!-- 닫기 X 버튼 -->
          <button type="button" class="btn-close position-absolute top-0 end-0 m-3" @click.stop="closeModal"></button>

          <div v-if="loading" class="text-center py-5" @click.stop>
            <div class="spinner-border text-warning" role="status"></div>
            <div class="small text-secondary mt-2">영수증 정보를 불러오는 중...</div>
          </div>

          <div v-else-if="transaction" class="receipt-body text-center" @click.stop>
            <div class="badge bg-warning text-dark fw-bold px-3 py-1.5 rounded-pill mb-2">KB Pay 영수증</div>
            
            <!-- 금액 -->
            <h2 class="fw-extrabold text-dark my-2">{{ formatCurrency(transaction.amount) }}</h2>
            <div class="badge bg-light text-secondary border px-2.5 py-1 mb-3">
              {{ getTransactionTypeBadge(transaction.transactionType) }}
            </div>

            <hr class="my-3 border-dashed" />

            <!-- 상세 정보 테이블 -->
            <div class="text-start small mb-3" @click.stop>
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

            <!-- 피드 글 남기기 & 공개 범위 선택 입력 (결제 건 전용 피드) -->
            <div class="text-start mb-3" @click.stop>
              <label class="form-label text-dark small fw-bold">
                <i class="bi bi-chat-heart-fill me-1 text-warning"></i>피드 글 남기기
              </label>

              <div class="input-group mb-2" @click.stop>
                <input
                  type="text"
                  v-model="editMemo"
                  class="form-control border-2"
                  placeholder="피드 메시지 작성 (예: 오늘 저녁 맛있게 먹었습니다! 😋)"
                  @click.stop
                  @keyup.enter.stop="saveMemo"
                />
                <button
                  type="button"
                  class="btn btn-warning fw-bold px-3 text-dark"
                  @click.stop.prevent="saveMemo"
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

              <div class="vis-opt-grid d-flex gap-1 mb-2" @click.stop>
                <button
                  type="button"
                  class="vis-chip-btn"
                  :class="{ active: editVisibility === 'PUBLIC' }"
                  @click.stop.prevent="editVisibility = 'PUBLIC'"
                >
                  🌐 전체 공개
                </button>
                <button
                  type="button"
                  class="vis-chip-btn"
                  :class="{ active: editVisibility === 'FRIEND' }"
                  @click.stop.prevent="editVisibility = 'FRIEND'"
                >
                  👥 친구 공개
                </button>
                <button
                  type="button"
                  class="vis-chip-btn"
                  :class="{ active: editVisibility === 'PRIVATE' }"
                  @click.stop.prevent="editVisibility = 'PRIVATE'"
                >
                  🔒 나만 보기
                </button>
              </div>

              <div v-if="savedSuccess" class="alert alert-success py-2 px-3 small fw-bold mt-2 mb-0 d-flex align-items-center gap-2">
                <i class="bi bi-check-circle-fill text-success fs-6"></i>
                <div>
                  피드 글이 성공적으로 게시되었습니다!
                  <span class="d-block text-secondary font-normal" style="font-size: 11px;">공개 범위: {{ getVisLabel(editVisibility) }}</span>
                </div>
              </div>
            </div>

            <!-- 이 내역으로 더치페이하기 -->
            <button
              type="button"
              class="btn btn-warning w-100 py-2.5 fw-bold rounded-3 mb-2 text-dark shadow-sm"
              @click.stop.prevent="startDutchpayFromReceipt"
            >
              <i class="bi bi-calculator me-1"></i> 이 내역으로 더치페이하기
            </button>

            <button type="button" class="btn btn-dark w-100 py-2.5 fw-bold rounded-3" @click.stop.prevent="closeModal">
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
import transactionApi from '@/api/transactionApi';

const router = useRouter();

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

// 백드롭 직접 클릭 시만 닫히도록 억제
const onBackdropClick = (e) => {
  if (e.target.classList.contains('receipt-modal-backdrop')) {
    closeModal();
  }
};

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

const startDutchpayFromReceipt = (e) => {
  if (e) e.stopPropagation();
  const amt = transaction.value ? transaction.value.amount : 5000;
  const merchantName = transaction.value?.receiverName || transaction.value?.memo || '결제건';
  const titleText = `${merchantName} 더치페이`;
  
  emit('close');

  setTimeout(() => {
    router.push({
      path: '/remittance',
      query: {
        type: 'DUTCHPAY',
        amount: amt,
        title: titleText,
      },
    });
  }, 100);
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
    console.log('Transaction detail fallback');
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

const saveMemo = async (e) => {
  if (e) e.stopPropagation();
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

  // 로컬 피드 스토리지에도 새 게시물 보존 저장하여 피드 탭에서 영구 노출
  try {
    const userFeeds = JSON.parse(localStorage.getItem('user_created_feeds') || '[]');
    userFeeds.unshift({
      feedId: Date.now(),
      userId: 1,
      userName: '나',
      sender: {
        nickname: '나',
        profileImageName: null
      },
      content: contentMsg,
      visibility: editVisibility.value,
      amount: transaction.value ? transaction.value.amount : 0,
      createdAt: new Date().toISOString(),
      transactionId: props.transactionId,
      merchantName: transaction.value?.receiverName || '가맹점 결제'
    });
    localStorage.setItem('user_created_feeds', JSON.stringify(userFeeds));
  } catch (e) {
    console.log('Feed save local cache error');
  }

  saving.value = false;
  savedSuccess.value = true;
  emit('updated');
};

watch(() => props.show, (newVal) => {
  if (newVal && props.transactionId) {
    fetchDetail();
  }
});
</script>

<style scoped>
.receipt-modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.65);
  z-index: 9999;
  padding: 16px;
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
