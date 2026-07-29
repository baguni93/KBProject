<template>
  <div class="container py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-4">
      <div>
        <h2 class="mb-1">포인트 지갑 API 테스트</h2>
        <p class="text-muted mb-0">
          현재 백엔드 컨트롤러 기준 임시 사용자 ID는 1번입니다.
        </p>
      </div>
      <button class="btn btn-dark" :disabled="loading.dashboard" @click="loadDashboard(false)">
        <span v-if="loading.dashboard" class="spinner-border spinner-border-sm me-2"></span>
        전체 새로고침
      </button>
    </div>

    <div class="row g-3 mb-4">
      <div class="col-md-4">
        <div class="card h-100">
          <div class="card-body">
            <div class="text-muted small">보유 포인트</div>
            <div class="fs-3 fw-bold">{{ formatNumber(wallet?.pointBalance) }} P</div>
            <div class="small text-muted">pointWalletId: {{ wallet?.pointWalletId ?? '-' }}</div>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card h-100">
          <div class="card-body">
            <div class="text-muted small">오늘 출석</div>
            <div class="fs-3 fw-bold">{{ attendanceStatus?.attendedToday ? '완료' : '미완료' }}</div>
            <div class="small text-muted">{{ attendanceStatus?.message ?? '조회 전' }}</div>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card h-100">
          <div class="card-body">
            <div class="text-muted small">미개봉 랜덤박스</div>
            <div class="fs-3 fw-bold">{{ randomBoxCount }}개</div>
            <div class="small text-muted">GET /api/random-boxes/unopened/count</div>
          </div>
        </div>
      </div>
    </div>

    <div class="row g-4">
      <div class="col-lg-6">
        <section class="card mb-4">
          <div class="card-header fw-bold">출석 체크</div>
          <div class="card-body">
            <p class="text-muted small">
              오늘 출석 여부를 조회하거나 출석을 처리합니다. 중복 출석은 백엔드 오류 응답으로 확인할 수 있습니다.
            </p>
            <div class="d-flex gap-2">
              <button class="btn btn-outline-dark" :disabled="loading.attendance" @click="loadAttendanceStatus">
                오늘 출석 조회
              </button>
              <button class="btn btn-warning" :disabled="loading.attendance" @click="submitAttendance">
                출석 체크
              </button>
            </div>
          </div>
        </section>

        <section class="card mb-4">
          <div class="card-header fw-bold">포인트 전환</div>
          <div class="card-body">
            <form class="row g-2" @submit.prevent="submitConversion">
              <div class="col-sm-8">
                <label class="form-label" for="conversionPoint">전환 포인트</label>
                <input
                    id="conversionPoint"
                    v-model.number="conversionPoint"
                    class="form-control"
                    type="number"
                    min="1"
                    placeholder="예: 1000"
                    required
                />
              </div>
              <div class="col-sm-4 d-flex align-items-end">
                <button class="btn btn-warning w-100" :disabled="loading.conversion">
                  전자지갑으로 전환
                </button>
              </div>
            </form>
          </div>
        </section>

        <section class="card mb-4">
          <div class="card-header fw-bold">포인트 거래내역</div>
          <div class="card-body">
            <div class="row g-2 mb-3">
              <div class="col-sm-7">
                <select v-model="transactionType" class="form-select">
                  <option value="">전체 유형</option>
                  <option value="EARN">EARN</option>
                  <option value="USE">USE</option>
                  <option value="EXPIRE">EXPIRE</option>
                  <option value="CANCEL">CANCEL</option>
                </select>
              </div>
              <div class="col-sm-5 d-grid gap-2 d-sm-flex">
                <button class="btn btn-outline-dark flex-fill" :disabled="loading.transactions" @click="loadTransactions">
                  조건 조회
                </button>
                <button class="btn btn-outline-secondary flex-fill" :disabled="loading.transactions" @click="loadRecentTransactions">
                  최근 5건
                </button>
              </div>
            </div>

            <div v-if="transactions.length" class="table-responsive">
              <table class="table table-sm align-middle mb-0">
                <thead>
                <tr>
                  <th>유형</th>
                  <th class="text-end">포인트</th>
                  <th>사유</th>
                  <th>일시</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="transaction in transactions" :key="transaction.pointTransactionId">
                  <td>{{ transaction.transactionType }}</td>
                  <td class="text-end">{{ formatNumber(transaction.pointAmount) }} P</td>
                  <td>{{ transaction.reasonType }}</td>
                  <td class="small">{{ transaction.createdAt }}</td>
                </tr>
                </tbody>
              </table>
            </div>
            <div v-else class="text-muted small">조회된 거래내역이 없습니다.</div>
          </div>
        </section>
      </div>

      <div class="col-lg-6">
        <section class="card mb-4">
          <div class="card-header d-flex justify-content-between align-items-center">
            <span class="fw-bold">랜덤박스</span>
            <span class="badge text-bg-dark">미개봉 {{ randomBoxes.length }}개</span>
          </div>
          <div class="card-body">
            <div class="d-flex flex-wrap gap-2 mb-3">
              <button class="btn btn-outline-dark" :disabled="loading.randomBox" @click="loadRandomBoxes">
                목록 조회
              </button>
              <button class="btn btn-warning" :disabled="loading.randomBox || !randomBoxes.length" @click="openAllRandomBoxes">
                모두 열기
              </button>
            </div>

            <div v-if="randomBoxes.length" class="list-group">
              <div
                  v-for="box in randomBoxes"
                  :key="box.userRandomBoxId"
                  class="list-group-item d-flex justify-content-between align-items-center gap-3"
              >
                <div>
                  <div class="fw-bold">랜덤박스 #{{ box.userRandomBoxId }}</div>
                  <div class="small text-muted">
                    {{ box.issueReason }} · {{ box.issuedAt }}
                  </div>
                </div>
                <button class="btn btn-sm btn-warning" :disabled="loading.randomBox" @click="openRandomBox(box.userRandomBoxId)">
                  1개 열기
                </button>
              </div>
            </div>
            <div v-else class="text-muted small">미개봉 랜덤박스가 없습니다.</div>
          </div>
        </section>

        <section class="card">
          <div class="card-header d-flex justify-content-between align-items-center">
            <span class="fw-bold">마지막 API 응답</span>
            <span v-if="lastResult" :class="['badge', lastResult.success ? 'text-bg-success' : 'text-bg-danger']">
              {{ lastResult.success ? 'SUCCESS' : 'ERROR' }}
            </span>
          </div>
          <div class="card-body">
            <div v-if="lastResult" class="mb-2 small">
              <code>{{ lastResult.label }}</code>
            </div>
            <pre class="api-result mb-0">{{ prettyLastResult }}</pre>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import pointWalletApi from '@/api/pointWalletApi';

const wallet = ref(null);
const attendanceStatus = ref(null);
const randomBoxCount = ref(0);
const randomBoxes = ref([]);
const transactions = ref([]);
const transactionType = ref('');
const conversionPoint = ref(500);
const lastResult = ref(null);

const loading = reactive({
  dashboard: false,
  attendance: false,
  conversion: false,
  transactions: false,
  randomBox: false,
});

const prettyLastResult = computed(() => {
  if (!lastResult.value) {
    return '아직 실행한 API가 없습니다.';
  }

  return JSON.stringify(lastResult.value.data, null, 2);
});

const formatNumber = (value) => Number(value ?? 0).toLocaleString('ko-KR');

const normalizeError = (error) => {
  if (error?.response) {
    return {
      status: error.response.status,
      statusText: error.response.statusText,
      data: error.response.data,
    };
  }

  return {
    message: error?.message ?? String(error),
  };
};

const runApi = async (label, request) => {
  try {
    const data = await request();
    lastResult.value = { success: true, label, data };
    return { success: true, data };
  } catch (error) {
    const data = normalizeError(error);
    lastResult.value = { success: false, label, data };
    return { success: false, data: null };
  }
};

const loadDashboard = async (preserveResult = false) => {
  loading.dashboard = true;

  const requests = await Promise.allSettled([
    pointWalletApi.getWallet(),
    pointWalletApi.getTodayAttendanceStatus(),
    pointWalletApi.getUnopenedRandomBoxCount(),
    pointWalletApi.getRecentTransactions(),
    pointWalletApi.getUnopenedRandomBoxes(),
  ]);

  const [walletResult, attendanceResult, countResult, transactionsResult, boxesResult] = requests;

  if (walletResult.status === 'fulfilled') wallet.value = walletResult.value;
  if (attendanceResult.status === 'fulfilled') attendanceStatus.value = attendanceResult.value;
  if (countResult.status === 'fulfilled') randomBoxCount.value = countResult.value.unopenedCount ?? 0;
  if (transactionsResult.status === 'fulfilled') transactions.value = transactionsResult.value;
  if (boxesResult.status === 'fulfilled') randomBoxes.value = boxesResult.value;

  if (!preserveResult) {
    const rejected = requests.find((result) => result.status === 'rejected');
    lastResult.value = rejected
        ? { success: false, label: '대시보드 API 일괄 조회', data: normalizeError(rejected.reason) }
        : {
          success: true,
          label: '대시보드 API 일괄 조회',
          data: {
            wallet: wallet.value,
            attendanceStatus: attendanceStatus.value,
            randomBoxCount: randomBoxCount.value,
            recentTransactions: transactions.value,
            randomBoxes: randomBoxes.value,
          },
        };
  }

  loading.dashboard = false;
};

const loadAttendanceStatus = async () => {
  loading.attendance = true;
  try {
    const result = await runApi(
        'GET /api/attendance/today',
        pointWalletApi.getTodayAttendanceStatus,
    );
    if (result.success) attendanceStatus.value = result.data;
  } finally {
    loading.attendance = false;
  }
};

const submitAttendance = async () => {
  loading.attendance = true;
  try {
    const result = await runApi('POST /api/attendance', pointWalletApi.attend);
    if (result.success) await loadDashboard(true);
  } finally {
    loading.attendance = false;
  }
};

const submitConversion = async () => {
  loading.conversion = true;
  try {
    const result = await runApi(
        'POST /api/point-conversions',
        () => pointWalletApi.convertPoints(conversionPoint.value),
    );
    if (result.success) await loadDashboard(true);
  } finally {
    loading.conversion = false;
  }
};

const loadTransactions = async () => {
  loading.transactions = true;
  try {
    const result = await runApi(
        `GET /api/point-transactions${transactionType.value ? `?type=${transactionType.value}` : ''}`,
        () => pointWalletApi.getTransactions(transactionType.value),
    );
    if (result.success) transactions.value = result.data;
  } finally {
    loading.transactions = false;
  }
};

const loadRecentTransactions = async () => {
  loading.transactions = true;
  try {
    const result = await runApi(
        'GET /api/point-transactions/recent',
        pointWalletApi.getRecentTransactions,
    );
    if (result.success) transactions.value = result.data;
  } finally {
    loading.transactions = false;
  }
};

const loadRandomBoxes = async () => {
  loading.randomBox = true;
  try {
    const result = await runApi(
        'GET /api/random-boxes/unopened',
        pointWalletApi.getUnopenedRandomBoxes,
    );
    if (result.success) {
      randomBoxes.value = result.data;
      randomBoxCount.value = result.data.length;
    }
  } finally {
    loading.randomBox = false;
  }
};

const openRandomBox = async (userRandomBoxId) => {
  loading.randomBox = true;
  try {
    const result = await runApi(
        `POST /api/random-boxes/${userRandomBoxId}/open`,
        () => pointWalletApi.openRandomBox(userRandomBoxId),
    );
    if (result.success) await loadDashboard(true);
  } finally {
    loading.randomBox = false;
  }
};

const openAllRandomBoxes = async () => {
  loading.randomBox = true;
  try {
    const result = await runApi(
        'POST /api/random-boxes/open-all',
        pointWalletApi.openAllRandomBoxes,
    );
    if (result.success) await loadDashboard(true);
  } finally {
    loading.randomBox = false;
  }
};

onMounted(() => loadDashboard(false));
</script>

<style scoped>
.api-result {
  min-height: 220px;
  max-height: 520px;
  overflow: auto;
  padding: 1rem;
  border-radius: 0.5rem;
  background: #f8f9fa;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>