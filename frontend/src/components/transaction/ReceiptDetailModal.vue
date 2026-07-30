<template>
  <div v-if="show" class="modal-backdrop d-flex align-items-center justify-content-center" @click.self="$emit('close')">
    <div class="modal-dialog modal-dialog-centered w-100 px-3" style="max-width: 400px;">
      <div class="modal-card bg-white rounded-4 shadow-lg p-4 position-relative">
        <!-- 닫기 버튼 -->
        <button type="button" class="btn-close position-absolute top-0 end-0 m-3" @click="$emit('close')"></button>

        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-warning" role="status"></div>
          <div class="small text-secondary mt-2">영수증 정보를 불러오는 중...</div>
        </div>

        <div v-else-if="transaction" class="receipt-body text-center">
          <div class="badge bg-warning text-dark fw-bold px-3 py-1.5 rounded-pill mb-2">KB Pay 영수증</div>
          
          <!-- 금액 -->
          <h2 class="fw-extrabold text-dark my-2">{{ formatCurrency(transaction.amount) }}</h2>
          <div class="badge bg-light text-secondary border px-2.5 py-1 mb-4">
            {{ getTransactionTypeBadge(transaction.transactionType) }} · {{ transaction.transactionStatus }}
          </div>

          <hr class="my-3 border-dashed" />

          <!-- 상세 정보 테이블 -->
          <div class="text-start small">
            <div class="d-flex justify-content-between py-1.5">
              <span class="text-secondary">거래 번호</span>
              <span class="fw-bold font-monospace">#{{ transaction.transactionId }}</span>
            </div>
            <div class="d-flex justify-content-between py-1.5">
              <span class="text-secondary">거래 수단</span>
              <span class="fw-bold">{{ transaction.sourceType }} ➔ {{ transaction.targetType }}</span>
            </div>
            <div v-if="transaction.receiverName" class="d-flex justify-content-between py-1.5">
              <span class="text-secondary">수신자</span>
              <span class="fw-bold">{{ transaction.receiverName }}</span>
            </div>
            <div class="d-flex justify-content-between py-1.5">
              <span class="text-secondary">거래 일시</span>
              <span class="fw-bold">{{ formatDate(transaction.createdAt) }}</span>
            </div>
          </div>

          <hr class="my-3 border-dashed" />

          <!-- 메모 입력/수정 -->
          <div class="text-start mb-3">
            <label class="form-label text-secondary small fw-bold">영수증 메모</label>
            <div class="input-group">
              <input
                type="text"
                v-model="editMemo"
                class="form-control border-2"
                placeholder="영수증 메모를 입력하세요"
              />
              <button class="btn btn-warning fw-bold px-3" @click="saveMemo" :disabled="saving">
                <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
                저장
              </button>
            </div>
            <span v-if="savedSuccess" class="text-success small fw-bold mt-1 d-block">
              <i class="bi bi-check-circle-fill me-1"></i> 메모가 저장되었습니다.
            </span>
          </div>

          <button type="button" class="btn btn-dark w-100 py-2.5 fw-bold rounded-3" @click="$emit('close')">
            확인
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import transactionApi from '@/api/transactionApi';

const props = defineProps({
  show: Boolean,
  transactionId: Number,
});

const emit = defineEmits(['close', 'updated']);

const transaction = ref(null);
const editMemo = ref('');
const loading = ref(false);
const saving = ref(false);
const savedSuccess = ref(false);

const formatCurrency = (val) => {
  if (val === undefined || val === null) return '0 원';
  return Number(val).toLocaleString('ko-KR') + ' 원';
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
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
    default: return type;
  }
};

const fetchDetail = async () => {
  if (!props.transactionId) return;
  loading.value = true;
  savedSuccess.value = false;
  try {
    const data = await transactionApi.getTransactionDetail(props.transactionId);
    transaction.value = data;
    editMemo.value = data.memo || '';
  } catch (err) {
    console.error('Fetch transaction detail error:', err);
  } finally {
    loading.value = false;
  }
};

const saveMemo = async () => {
  if (!props.transactionId) return;
  saving.value = true;
  savedSuccess.value = false;
  try {
    const updated = await transactionApi.updateMemo(props.transactionId, editMemo.value);
    if (updated) {
      transaction.value = updated;
      savedSuccess.value = true;
      emit('updated');
    }
  } catch (err) {
    console.error('Save memo error:', err);
  } finally {
    saving.value = false;
  }
};

watch(() => props.show, (newVal) => {
  if (newVal && props.transactionId) {
    fetchDetail();
  }
});
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.6);
  z-index: 1060;
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
</style>
