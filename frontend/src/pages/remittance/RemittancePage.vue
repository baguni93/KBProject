<template>
  <div class="remit-root flex flex-col h-full overflow-hidden">

    <!-- ══════════════════════════════════════════
         상단 고정 헤더
    ══════════════════════════════════════════ -->
    <div class="remit-header flex-shrink-0">
      <div class="header-inner">
        <button class="back-btn" @click="handleBack">
          <i class="bi bi-chevron-left"></i>
        </button>

        <h4 class="header-title">
          <span class="kb-pay-tag me-1">KB Pay</span>
          <span v-if="currentStep === 1">송금 및 더치페이</span>
          <span v-else-if="currentStep === 2">금액 및 피드 설정</span>
          <span v-else-if="currentStep === 3">부족금 자동충전 알림</span>
          <span v-else-if="currentStep === 4">PIN 인증</span>
          <span v-else-if="currentStep === 5">송금 완료</span>
          <span v-else-if="currentStep === 6">실시간 수납 피드 (찌르기)</span>
        </h4>

        <div class="header-balance">
          <span class="b-label">지갑 잔액</span>
          <span class="b-val">{{ formatCurrency(myBalance) }} 원</span>
        </div>
      </div>
    </div>

    <!-- 3개 탭 서브 네비게이션 (계좌 송금 / 친구 송금 / 정산 더치페이) -->
    <div v-if="currentStep === 1" class="sub-tab-nav flex-shrink-0">
      <button
        class="nav-tab-btn"
        :class="{ active: remitType === 'ACCOUNT' }"
        @click="remitType = 'ACCOUNT'"
      >
        계좌 송금
      </button>
      <button
        class="nav-tab-btn"
        :class="{ active: remitType === 'FRIEND' }"
        @click="remitType = 'FRIEND'"
      >
        친구 송금
      </button>
      <button
        class="nav-tab-btn"
        :class="{ active: remitType === 'DUTCH' }"
        @click="remitType = 'DUTCH'"
      >
        정산 (더치페이)
      </button>
    </div>

    <!-- ══════════════════════════════════════════
         본문 가변 스크롤 콘텐츠
    ══════════════════════════════════════════ -->
    <div class="remit-body flex-1 min-h-0 overflow-y-auto">

      <!-- ------------------------------------------
           [STEP 1] 모드별 1단계 (계좌 입력 / 친구 선택 / 더치페이 생성)
      ------------------------------------------ -->
      <div v-if="currentStep === 1" class="step-card fade-in">
        
        <!-- 1-A. 계좌 송금 1단계 (Screen 7-A) -->
        <template v-if="remitType === 'ACCOUNT'">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <span class="step-badge">계좌 송금 1단계</span>
            <span class="text-success small font-bold"><i class="bi bi-shield-check me-1"></i>수수료 무제한 면제</span>
          </div>

          <div class="form-group mb-3 text-start">
            <label class="form-label-sm">계좌 번호 입력</label>
            <div class="d-flex gap-2">
              <input
                v-model="accountForm.accountNumber"
                type="text"
                class="form-control form-control-sm font-monospace fw-bold"
                placeholder="'-' 없이 계좌번호 입력"
              />
              <button class="btn btn-primary btn-sm px-3 fw-bold" @click="checkAccount">확인</button>
            </div>
          </div>

          <!-- 은행 선택 4열 그리드 -->
          <div class="form-group mb-3 text-start">
            <label class="form-label-sm">은행 선택</label>
            <div class="bank-grid-4">
              <button
                v-for="b in bankOptions"
                :key="b.code"
                class="bank-select-chip"
                :class="{ active: accountForm.bankCode === b.code }"
                @click="accountForm.bankCode = b.code"
              >
                <div class="bank-icon-sm" :class="b.bgClass">{{ b.shortName }}</div>
                <span class="bank-name-sm">{{ b.name }}</span>
              </button>
            </div>
          </div>

          <!-- 최근 송금 계좌 (DB 연동) -->
          <div class="recent-accounts-wrap text-start">
            <span class="form-label-sm">최근 송금 계좌</span>
            <div v-if="recentAccounts.length === 0" class="small text-muted p-2.5 bg-light border rounded-3 text-center mt-1">
              최근 송금 내역이 없습니다.
            </div>
            <div v-else class="space-y-1 mt-1">
              <div
                v-for="recent in recentAccounts"
                :key="recent.id || recent.accountNumber"
                class="recent-account-card p-2 bg-light rounded-3 border cursor-pointer mb-1"
                @click="selectRecentAccountItem(recent)"
              >
                <div class="d-flex align-items-center gap-2">
                  <div class="bank-icon-sm bg-primary text-white">{{ recent.bankName || '은행' }}</div>
                  <div>
                    <p class="mb-0 fw-bold small">{{ recent.receiverName || recent.name || '수취인' }} ({{ recent.bankName || '' }} {{ recent.accountNumber }})</p>
                    <p class="mb-0 text-muted" style="font-size: 10px;">최근 송금: {{ recent.date || '최근' }} • {{ formatCurrency(recent.amount) }}원</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 1-B. 친구 송금 1단계 (Screen 7-B) -->
        <template v-else-if="remitType === 'FRIEND'">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <span class="step-badge warning">친구 송금 1단계</span>
          </div>

          <div class="search-input-wrap mb-3">
            <i class="bi bi-search search-ic"></i>
            <input v-model="friendSearchKeyword" type="text" class="search-input-field" placeholder="친구 이름 또는 프로필 ID 입력..." />
          </div>

          <div class="friend-list-section text-start">
            <span class="form-label-sm mb-2 d-block">내 친구 목록 (선택 시 터치)</span>
            <div v-if="filteredFriends.length === 0" class="text-center py-4 bg-white border rounded-3 text-muted small mb-2">
              <i class="bi bi-people mb-1 fs-4 text-secondary d-block"></i>
              등록된 친구가 없습니다. (친구를 먼저 추가해 주세요)
            </div>
            <div
              v-for="friend in filteredFriends"
              :key="friend.id"
              class="friend-item-card p-3 bg-white rounded-3 mb-2 d-flex justify-content-between align-items-center cursor-pointer border transition-all"
              :class="selectedFriendId === friend.id ? 'border-2 border-warning bg-warning bg-opacity-10 shadow-sm' : 'border-light-subtle'"
              @click="selectedFriendId = friend.id"
            >
              <div class="d-flex align-items-center gap-3">
                <div class="friend-avatar-badge" :class="selectedFriendId === friend.id ? 'bg-warning text-dark font-bold' : 'bg-light text-secondary'">
                  {{ friend.initials }}
                </div>
                <div>
                  <p class="mb-0 fw-bold small text-dark">{{ friend.name }}</p>
                  <p class="mb-0 text-muted" style="font-size: 11px;">@{{ friend.username }}</p>
                </div>
              </div>

              <div v-if="selectedFriendId === friend.id" class="d-flex align-items-center gap-1">
                <span class="badge bg-warning text-dark font-bold" style="font-size: 10px;">선택됨</span>
                <i class="bi bi-check-circle-fill text-warning fs-5"></i>
              </div>
              <i v-else class="bi bi-circle text-muted fs-6"></i>
            </div>
          </div>
        </template>

        <!-- 1-C. 더치페이 방 생성 1단계 (Screen 11 & Screen 12-A) -->
        <template v-else-if="remitType === 'DUTCH'">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <span class="step-badge danger">정산 설정 1단계</span>
            <span class="text-success small font-bold">더치페이</span>
          </div>

          <button class="btn btn-outline-warning text-dark border-warning w-100 mb-3 btn-sm fw-bold" @click="openTxSelectModal">
            <i class="bi bi-list-ul me-1"></i> 내 거래 내역에서 결제 건 불러오기
          </button>

          <div class="form-group mb-3 text-start">
            <label class="form-label-sm">정산 모임방 명칭</label>
            <input v-model="dutchRoomTitle" type="text" class="form-control form-control-sm fw-bold" placeholder="정산 모임방 명칭을 입력하세요 (예: 회식 정산)" />
          </div>

          <!-- 정산 참여 친구 선택 -->
          <div class="dutch-friends-select-wrap text-start mb-3">
            <span class="form-label-sm mb-1 d-block">선택된 정산 참여자 (총 {{ selectedDutchFriends.length + 1 }}명)</span>
            <div class="d-flex flex-wrap gap-1 mb-2">
              <span class="badge bg-warning text-dark">나 ({{ currentUserName }})</span>
              <span v-for="fId in selectedDutchFriends" :key="fId" class="badge bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25">
                {{ getFriendName(fId) }} <i class="bi bi-x ms-1 cursor-pointer" @click="removeDutchFriend(fId)"></i>
              </span>
            </div>

            <!-- DB 친구 목록에서 선택 터치 칩 -->
            <span class="form-label-sm mb-1 d-block text-muted">함께 정산할 친구 추가</span>
            <div v-if="friendList.length === 0" class="small text-muted p-2 bg-light rounded text-center">
              추가 가능한 친구가 없습니다.
            </div>
            <div v-else class="d-flex flex-wrap gap-1">
              <button
                v-for="friend in friendList"
                :key="friend.id"
                type="button"
                class="btn btn-sm"
                :class="selectedDutchFriends.includes(friend.id) ? 'btn-danger btn-sm' : 'btn-outline-secondary btn-sm'"
                @click="toggleDutchFriend(friend.id)"
              >
                + {{ friend.name }}
              </button>
            </div>
          </div>
        </template>

        <!-- 다음 단계 진행 버튼 -->
        <div class="mt-4 pt-2 border-top">
          <button class="btn btn-warning w-100 fw-bold py-2 shadow-sm text-dark" :disabled="!canProceedStep1" @click="goToStep2">
            다음 단계로 이동 <i class="bi bi-arrow-right ms-1"></i>
          </button>
        </div>

      </div>

      <!-- ------------------------------------------
           [STEP 2] 금액 / 메모 / 공유 피드 설정 / 정산 분배
      ------------------------------------------ -->
      <div v-else-if="currentStep === 2" class="step-card fade-in">
        
        <!-- 2-A. 계좌 & 친구 송금 2단계 (Screen 8-A, 8-B 동적 바인딩) -->
        <template v-if="remitType !== 'DUTCH'">
          <div class="receiver-target-card p-3 bg-light rounded-3 text-start mb-3 border">
            <span class="small text-muted font-monospace d-block mb-1">수취 대상 확인</span>
            <h5 class="fw-black text-dark mb-0">
              <template v-if="remitType === 'FRIEND'">{{ selectedFriendObj?.name || '선택한 친구' }} ({{ selectedFriendObj?.username || '' }})</template>
              <template v-else>{{ accountForm.accountNumber ? `${getBankName(accountForm.bankCode)} ${accountForm.accountNumber}` : '계좌 정보를 입력하세요' }}</template>
            </h5>
          </div>

          <div class="amount-input-group text-start mb-3">
            <label class="form-label-sm">송금할 금액</label>
            <div class="d-flex align-items-baseline border-bottom pb-1">
              <input v-model.number="remitAmount" type="number" class="amount-field-direct fw-black font-monospace text-dark" placeholder="0" />
              <span class="fs-6 fw-bold ms-1">KRW</span>
            </div>
            <div class="quick-btn-row d-flex gap-1 mt-2">
              <button class="btn btn-light btn-sm fw-bold text-primary" @click="remitAmount += 10000">+1만</button>
              <button class="btn btn-light btn-sm fw-bold text-primary" @click="remitAmount += 50000">+5만</button>
              <button class="btn btn-primary btn-sm fw-bold" @click="remitAmount = myBalance">전액</button>
            </div>
          </div>

          <!-- 통장 적요 or 공유 피드 작성 -->
          <div class="memo-feed-wrap text-start mb-3">
            <label class="form-label-sm">{{ remitType === 'FRIEND' ? '공유 피드 작성' : '통장 적요 및 메모 설정' }}</label>
            <textarea
              v-model="remitMemo"
              class="form-control form-control-sm bg-light"
              rows="3"
              :placeholder="remitType === 'FRIEND' ? '친구들과 나눌 정산 스토리를 적어주세요...' : '받는 사람 통장 표시 문구'"
            ></textarea>
          </div>

          <!-- 소셜 피드 사진 첨부 -->
          <div class="image-upload-wrap text-start mb-3">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <label class="form-label-sm mb-0">
                <i class="bi bi-image text-warning me-1"></i>소셜 피드 사진 첨부 (선택)
              </label>
              <button v-if="selectedFile" type="button" class="btn btn-link p-0 text-danger small font-bold text-decoration-none" @click="removeSelectedFile">
                첨부 취소
              </button>
            </div>
            
            <div v-if="imagePreviewUrl" class="position-relative mb-2">
              <img :src="imagePreviewUrl" class="img-fluid rounded-3 border w-100 shadow-sm" style="max-height: 140px; object-fit: cover;" />
            </div>

            <input
              type="file"
              accept="image/*"
              class="form-control form-control-sm bg-light"
              @change="handleFileChange"
            />
          </div>
        </template>

        <!-- 2-B. 더치페이 정산금 분배 설정 (Screen 12-B 1/N 균등 vs 차등 정산 100% 반영) -->
        <template v-else>
          <div class="text-start mb-3 border-bottom pb-2">
            <span class="small text-muted font-monospace">총 청구 정산금 (결제한 금액)</span>
            <h3 class="fw-black text-dark font-monospace mb-0">{{ formatCurrency(remitAmount || 0) }} <span class="fs-6">KRW</span></h3>
            <span class="badge bg-warning text-dark font-bold mt-1">내가 결제한 내역 정산</span>
          </div>

          <!-- 1/N 균등 정산 vs 차등 정산 탭 -->
          <div class="split-tab-group p-1 bg-light rounded-3 d-flex mb-3">
            <button class="flex-1 btn btn-sm" :class="dutchSplitMode === 'EQUAL' ? 'btn-white shadow-sm fw-bold' : 'text-muted'" @click="dutchSplitMode = 'EQUAL'">1/N 균등 정산</button>
            <button class="flex-1 btn btn-sm" :class="dutchSplitMode === 'CUSTOM' ? 'btn-white shadow-sm fw-bold' : 'text-muted'" @click="dutchSplitMode = 'CUSTOM'">차등 정산 (직접 입력)</button>
          </div>

          <div class="dutch-members-list-box p-3 bg-light rounded-3 text-start mb-3 border">
            <span class="fw-bold small mb-2 d-block">
              {{ dutchSplitMode === 'EQUAL' ? `1/N 균등 정산 배분율 (총 ${selectedDutchFriends.length + 1}명)` : '차등 정산 (멤버별 요청 금액 입력)' }}
            </span>

            <div v-if="dutchSplitMode === 'EQUAL'" class="space-y-2">
              <div class="d-flex justify-content-between p-2 bg-white rounded border small fw-bold">
                <span class="text-warning">나 ({{ currentUserName }}) [결제자]</span>
                <span>{{ formatCurrency(Math.floor((remitAmount || 0) / (selectedDutchFriends.length + 1))) }} 원</span>
              </div>
              <div
                v-for="fId in selectedDutchFriends"
                :key="fId"
                class="d-flex justify-content-between p-2 bg-white rounded border small fw-bold"
              >
                <span>{{ getFriendName(fId) }} (정산 요청 대상)</span>
                <span class="text-danger">{{ formatCurrency(Math.floor((remitAmount || 0) / (selectedDutchFriends.length + 1))) }} 원</span>
              </div>
            </div>

            <div v-else class="space-y-2">
              <div class="d-flex justify-content-between align-items-center p-2 bg-white rounded border small">
                <span class="fw-bold">나 ({{ currentUserName }})</span>
                <input type="text" :value="formatCurrency(Math.floor((remitAmount || 0) / (selectedDutchFriends.length + 1)))" class="form-control form-control-sm text-end fw-bold font-monospace width-80" />
              </div>
              <div
                v-for="fId in selectedDutchFriends"
                :key="fId"
                class="d-flex justify-content-between align-items-center p-2 bg-white rounded border small"
              >
                <span class="fw-bold">{{ getFriendName(fId) }}</span>
                <input type="text" :value="formatCurrency(Math.floor((remitAmount || 0) / (selectedDutchFriends.length + 1)))" class="form-control form-control-sm text-end fw-bold font-monospace width-80" />
              </div>
            </div>
          </div>
        </template>

        <div class="d-flex gap-2 border-top pt-3">
          <button class="btn btn-outline-secondary flex-1 fw-bold" @click="currentStep = 1">이전</button>
          <button class="btn btn-warning flex-2 fw-bold text-dark" @click="proceedFromStep2">
            {{ remitType === 'DUTCH' ? '정산 요청 및 모임방 생성' : '다음 단계로' }} <i class="bi bi-arrow-right ms-1"></i>
          </button>
        </div>

      </div>

      <!-- ------------------------------------------
           [STEP 3] 부족금 자동충전 알림 (Screen 9 100% 동일 반영)
      ------------------------------------------ -->
      <div v-else-if="currentStep === 3" class="step-card fade-in">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <span class="step-badge warning">부족금 자동충전</span>
          <span class="text-danger small font-bold">자동 계산</span>
        </div>

        <div class="p-3 bg-light rounded-3 text-start mb-3 border d-flex justify-content-between align-items-center">
          <div class="d-flex align-items-center gap-2">
            <div class="badge bg-warning bg-opacity-20 text-warning rounded-circle p-2 fw-bold">KM</div>
            <span class="fw-bold text-dark small">총 필요 금액</span>
          </div>
          <span class="fw-black text-dark fs-6">1,200,000 원</span>
        </div>

        <!-- 황색 잔액 부족 자동충전 알림 카드 -->
        <div class="auto-charge-warning-card p-4 rounded-4 text-start mb-4">
          <div class="d-flex align-items-start gap-2 mb-3">
            <div class="exclamation-badge">!</div>
            <div>
              <h6 class="fw-bold text-dark mb-1">잔액 부족 (보유: {{ formatCurrency(myBalance) }}원)</h6>
              <p class="small text-muted mb-0">부족분은 주거래 계좌에서 금액 입력 없이 자동 계산되어 즉시 충전됩니다.</p>
            </div>
          </div>

          <div class="border-top border-warning border-opacity-25 pt-2 d-flex justify-between align-items-center">
            <span class="small fw-bold text-warning-dark">신한은행 자동 충전액</span>
            <span class="fw-black font-monospace text-warning-dark fs-6">+200,000 KRW</span>
          </div>
        </div>

        <button class="btn btn-warning w-100 fw-bold py-2.5 shadow-sm text-dark" @click="currentStep = 4">
          자동 충전 후 즉시 송금 (PIN 인증)
        </button>
      </div>

      <!-- ------------------------------------------
           [STEP 4] PIN 비밀번호 인증 (Screen 10)
      ------------------------------------------ -->
      <div v-else-if="currentStep === 4" class="step-card fade-in text-center py-4">
        <h5 class="fw-bold mb-1">간편 비밀번호 인증</h5>
        <p class="small text-muted mb-4">송금 및 충전 승인을 위해 <strong class="text-warning">간편 6자리 비밀번호</strong>를 입력하세요.</p>

        <div class="d-flex justify-content-center gap-3 mb-4">
          <span v-for="i in 6" :key="i" class="pin-dot" :class="{ filled: pinCode.length >= i }"></span>
        </div>

        <div class="pin-keypad-grid max-w-280 mx-auto">
          <button v-for="n in [1,2,3,4,5,6,7,8,9]" :key="n" class="pin-key" @click="appendPin(n)">{{ n }}</button>
          <button class="pin-key text-warning small fw-bold" @click="pinCode = ''">재배열</button>
          <button class="pin-key" @click="appendPin(0)">0</button>
          <button class="pin-key text-secondary" @click="deletePin"><i class="bi bi-backspace"></i></button>
        </div>
      </div>

      <!-- ------------------------------------------
           [STEP 5] 송금 / 정산 요청 완료 (Screen 10-1 & Screen 13)
      ------------------------------------------ -->
      <div v-else-if="currentStep === 5" class="step-card fade-in text-center py-4">
        <div class="icon-circle-lg bg-success bg-opacity-10 text-success mx-auto mb-3">
          <i class="bi bi-check-all"></i>
        </div>
        <h4 class="fw-black text-dark mb-1">
          {{ remitType === 'DUTCH' ? '더치페이 정산 모임방이 생성되었습니다!' : '송금 처리가 완료되었습니다!' }}
        </h4>
        <p class="small text-muted mb-4">
          {{ remitType === 'DUTCH' ? '선택하신 참여 친구들에게 1/N 정산 요청 알림 메시지가 전송되었습니다.' : '요청하신 계좌 / 친구에게 정상 이체되었습니다.' }}
        </p>

        <div class="space-y-2">
          <template v-if="remitType === 'DUTCH'">
            <button class="btn btn-warning w-100 fw-bold py-2.5 text-dark shadow-sm mb-1" @click="currentStep = 6">
              <i class="bi bi-people-fill me-1"></i> 정산 현황 모임방 관리 (찌르기)
            </button>
            <button class="btn btn-outline-secondary w-100 fw-bold py-2" @click="$router.push('/wallet')">
              내 지갑 홈으로 돌아가기
            </button>
          </template>
          <template v-else>
            <button class="btn btn-warning w-100 fw-bold py-2.5 text-dark shadow-sm mb-1" @click="$router.push('/feed')">
              <i class="bi bi-rss-fill me-1"></i> 소셜 피드로 이동하여 확인하기
            </button>
            <button class="btn btn-outline-secondary w-100 fw-bold py-2" @click="$router.push('/transactions')">
              거래 내역 확인하기
            </button>
          </template>
        </div>
      </div>

      <!-- ------------------------------------------
           [STEP 6] 실시간 정산 수납 및 찌르기 피드 (Screen 13 100% 동일 반영)
      ------------------------------------------ -->
      <div v-else-if="currentStep === 6" class="step-card fade-in text-start">
        <div class="d-flex justify-content-between align-items-center mb-3 border-bottom pb-2">
          <h5 class="fw-bold mb-0">실시간 정산 관리 피드</h5>
          <span class="badge bg-success bg-opacity-10 text-success">수납 상태</span>
        </div>

        <div class="mb-3">
          <span class="small text-muted">스타벅스 커피 정산방 현황</span>
          <h5 class="fw-bold text-dark mt-1">정산 완료 진행률: <strong class="text-warning">33%</strong></h5>
          <div class="progress mt-2" style="height: 8px;">
            <div class="progress-bar bg-warning" role="progressbar" style="width: 33%"></div>
          </div>
        </div>

        <div class="members-nudge-section space-y-2 mb-4">
          <span class="small text-muted fw-bold d-block">모임 멤버 수납 상태</span>

          <div class="d-flex justify-content-between align-items-center p-2.5 bg-light rounded-3 border">
            <div class="d-flex align-items-center gap-2">
              <span class="badge bg-success">나</span>
              <span class="fw-bold small text-dark">이승진</span>
            </div>
            <span class="text-success fw-bold small"><i class="bi bi-check-circle-fill me-1"></i>완납 완료</span>
          </div>

          <div class="d-flex justify-content-between align-items-center p-2.5 bg-light rounded-3 border">
            <div class="d-flex align-items-center gap-2">
              <span class="badge bg-secondary">미</span>
              <span class="fw-bold small text-dark">김민수</span>
            </div>
            <div class="d-flex align-items-center gap-2">
              <span class="text-danger fw-bold small">8,000원</span>
              <button class="btn btn-warning btn-sm py-0.5 px-2 fw-bold text-dark" @click="nudgeUser('김민수')">
                <i class="bi bi-bell-fill me-1"></i>찌르기
              </button>
            </div>
          </div>
        </div>

        <button class="btn btn-outline-secondary w-100 fw-bold py-2" @click="$router.push('/wallet')">
          내 지갑 홈으로 돌아가기
        </button>
      </div>

    </div>

    <!-- ══════════════════════════════════════════
         내 거래 내역 다중 선택 모달 (DB 연동 거래 내역 리스트)
    ══════════════════════════════════════════ -->
    <div v-if="showTxSelectModal" class="tx-select-modal-overlay" @click.self="showTxSelectModal = false">
      <div class="tx-select-modal-card p-4 bg-white rounded-4 shadow-lg border text-start">
        <div class="d-flex justify-content-between align-items-center mb-2 pb-2 border-bottom">
          <h6 class="fw-bold mb-0 text-dark"><i class="bi bi-check2-square me-1 text-warning"></i>정산할 결제 건 선택 (다중 선택 가능)</h6>
          <button class="btn-close" @click="showTxSelectModal = false"></button>
        </div>

        <div v-if="userTxList.length === 0" class="text-center py-4 text-muted small">
          조회된 결제 거래 내역이 없습니다.
        </div>
        <div v-else class="space-y-2">
          <!-- 선택 개수 및 총 합산 금액 띠 바 -->
          <div class="d-flex justify-content-between align-items-center bg-light p-2 rounded-3 mb-2 small border">
            <span class="fw-bold text-dark">선택 {{ selectedTxIds.length }}개</span>
            <span class="fw-black text-warning font-monospace fs-6">총 {{ formatCurrency(selectedTxTotalAmount) }} 원</span>
          </div>

          <div class="tx-modal-list space-y-2" style="max-height: 250px; overflow-y: auto;">
            <div
              v-for="tx in userTxList"
              :key="tx.id"
              class="tx-select-item p-2.5 rounded-3 border d-flex justify-content-between align-items-center cursor-pointer mb-2 transition-all"
              :class="selectedTxIds.includes(tx.id) ? 'border-2 border-warning bg-warning bg-opacity-10' : 'bg-light border-light-subtle'"
              @click="toggleTxSelection(tx.id)"
            >
              <div class="d-flex align-items-center gap-2.5">
                <input
                  type="checkbox"
                  class="form-check-input mt-0 cursor-pointer"
                  :checked="selectedTxIds.includes(tx.id)"
                  @click.stop="toggleTxSelection(tx.id)"
                />
                <div>
                  <p class="mb-0 fw-bold small text-dark">{{ tx.title }}</p>
                  <p class="mb-0 text-muted" style="font-size: 10px;">{{ tx.date }} • 결제완료</p>
                </div>
              </div>
              <span class="fw-black text-dark font-monospace">{{ formatCurrency(tx.amount) }}원</span>
            </div>
          </div>

          <!-- 하단 선택 완료 버튼 -->
          <button
            class="btn btn-warning w-100 fw-bold py-2 shadow-sm text-dark mt-2"
            :disabled="selectedTxIds.length === 0"
            @click="confirmTxSelection"
          >
            <i class="bi bi-plus-circle me-1"></i> {{ selectedTxIds.length }}개 결제 건으로 더치페이 생성 (총 {{ formatCurrency(selectedTxTotalAmount) }}원)
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import api from '@/api';
import walletApi from '@/api/walletApi';
import friendApi from '@/api/friend';
import transactionApi from '@/api/transactionApi';
import remittanceApi from '@/api/remittanceApi';

const router = useRouter();
const authStore = useAuthStore();

const showTxSelectModal = ref(false);
const userTxList = ref([]);
const selectedTxIds = ref([]);

const selectedTxTotalAmount = computed(() => {
  return userTxList.value
    .filter(t => selectedTxIds.value.includes(t.id))
    .reduce((sum, t) => sum + t.amount, 0);
});

const toggleTxSelection = (id) => {
  if (selectedTxIds.value.includes(id)) {
    selectedTxIds.value = selectedTxIds.value.filter(txId => txId !== id);
  } else {
    selectedTxIds.value.push(id);
  }
};

const confirmTxSelection = () => {
  const selectedItems = userTxList.value.filter(t => selectedTxIds.value.includes(t.id));
  if (selectedItems.length === 0) return;

  if (selectedItems.length === 1) {
    dutchRoomTitle.value = `${selectedItems[0].title} 정산 모임방`;
  } else {
    dutchRoomTitle.value = `${selectedItems[0].title} 외 ${selectedItems.length - 1}건 정산 모임방`;
  }
  
  remitAmount.value = selectedTxTotalAmount.value;
  showTxSelectModal.value = false;
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  try {
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    return `${month}.${day} ${hours}:${minutes}`;
  } catch (e) {
    return dateStr;
  }
};

const getStoreTitle = (t) => {
  return (
    t.merchantName ||
    t.merchant_name ||
    t.storeName ||
    t.placeName ||
    t.targetName ||
    t.receiverName ||
    t.merchant ||
    t.title ||
    t.name ||
    t.memo ||
    t.description ||
    '현장 결제'
  );
};

const openTxSelectModal = async () => {
  showTxSelectModal.value = true;
  selectedTxIds.value = [];
  try {
    const userId = authStore.userId;
    if (!userId) return;

    if (transactionApi.getTransactions) {
      const data = await transactionApi.getTransactions(userId);
      if (data && Array.isArray(data)) {
        const payItems = data.filter(t => {
          const typeStr = (t.transactionType || t.type || '').toUpperCase();
          return typeStr.includes('PAY') || typeStr === '' || (!typeStr.includes('CHARGE') && !typeStr.includes('TRANSFER') && !typeStr.includes('REMIT'));
        });

        userTxList.value = payItems.map(t => ({
          id: t.id || t.transactionId,
          title: getStoreTitle(t),
          amount: Math.abs(t.amount || 0),
          date: formatDate(t.createdAt || t.transactionDate || t.date),
        }));
      }
    }
  } catch (e) {
    console.log('거래내역 로드 예외', e);
  }
};

const selectTxForDutch = (tx) => {
  dutchRoomTitle.value = `${tx.merchant || tx.title || '모임'} 정산방`;
  remitAmount.value = tx.amount || 0;
  showTxSelectModal.value = false;
};

const currentUserName = computed(() => authStore.userName || authStore.user?.userName || '사용자');
const currentStep = ref(1);
const remitType = ref('ACCOUNT'); // ACCOUNT / FRIEND / DUTCH
const myBalance = ref(0);
const recentAccounts = ref([]);

const loading = ref(false);

const accountForm = ref({
  accountNumber: '',
  bankCode: '088',
});

const selectRecentAccountItem = (item) => {
  accountForm.value.accountNumber = item.accountNumber || '';
  accountForm.value.bankCode = item.bankCode || '088';
};

const bankOptions = [
  { code: '088', name: '신한', shortName: '신한', bgClass: 'bg-primary text-white' },
  { code: '004', name: 'KB국민', shortName: 'KB', bgClass: 'bg-warning text-dark' },
  { code: '081', name: '하나', shortName: '하나', bgClass: 'bg-success text-white' },
  { code: '020', name: '우리', shortName: '우리', bgClass: 'bg-info text-white' },
];

const getBankName = (code) => {
  const found = bankOptions.find(b => b.code === code);
  return found ? found.name : '은행';
};

const friendSearchKeyword = ref('');
const selectedFriendId = ref(null);
const friendList = ref([]);

const selectedFriendObj = computed(() => {
  return friendList.value.find(f => f.id === selectedFriendId.value);
});

const filteredFriends = computed(() => {
  if (!friendSearchKeyword.value) return friendList.value;
  return friendList.value.filter(f => (f.name && f.name.includes(friendSearchKeyword.value)) || (f.username && f.username.includes(friendSearchKeyword.value)));
});

const dutchRoomTitle = ref('');
const selectedDutchFriends = ref([]);
const dutchSplitMode = ref('EQUAL');

const remitAmount = ref(0);
const remitMemo = ref('');
const pinCode = ref('');

const loadRemitInitData = async () => {
  try {
    const userId = authStore.userId;
    if (!userId) return;
    
    // 1. 지갑 잔액 조회
    try {
      const wInfo = await walletApi.getWalletByUserId(userId);
      if (wInfo) {
        myBalance.value = wInfo.balance ?? wInfo.amount ?? wInfo.money ?? wInfo.pointMoney ?? 0;
      }
    } catch (wErr) {
      console.log('지갑 잔액 조회 예외', wErr);
    }

    // 2. 최근 송금 계좌 DB 조회
    try {
      if (remittanceApi.getBankRemittanceInfo) {
        const bInfo = await remittanceApi.getBankRemittanceInfo(userId);
        if (bInfo) {
          const rList = bInfo.recentRemittances || bInfo.recentAccounts || bInfo.recents || (Array.isArray(bInfo) ? bInfo : []);
          recentAccounts.value = rList.map(r => ({
            id: r.id || r.remittanceId || r.accountNumber,
            receiverName: r.receiverName || r.name || r.userName || '수취인',
            bankName: r.bankName || (r.bankCode === '088' ? '신한' : r.bankCode === '004' ? 'KB국민' : '은행'),
            bankCode: r.bankCode || '088',
            accountNumber: r.accountNumber || r.accountNo || '',
            amount: r.amount || r.lastAmount || 0,
            date: r.date || r.createdAt || '최근',
          }));
        }
      }
    } catch (bErr) {
      console.log('최근 송금 계좌 조회 예외', bErr);
    }
    
    // 3. 친구 목록 DB 조회 및 중복 제거(Deduplication)
    try {
      const fRes = await friendApi.getFriendList(userId);
      let list = [];
      if (Array.isArray(fRes)) {
        list = fRes;
      } else if (fRes && Array.isArray(fRes.friends)) {
        list = fRes.friends;
      } else if (fRes && Array.isArray(fRes.data)) {
        list = fRes.data;
      }
      
      if (list.length > 0) {
        const map = new Map();
        list.forEach((f, idx) => {
          const receiverObj = f.receiver || f.friendMember || f.member || f.friend || f;
          const fId = f.friendUserId || f.friendId || f.id || receiverObj.userId || receiverObj.id || (idx + 1);
          const fNickname = receiverObj.nickname || receiverObj.name || receiverObj.userName || f.nickname || f.name || `사용자${fId}`;
          const fUsername = receiverObj.username || receiverObj.loginId || receiverObj.userLoginId || f.username || `user_${fId}`;

          if (!map.has(fId)) {
            map.set(fId, {
              id: fId,
              name: fNickname,
              username: fUsername,
              initials: (fNickname || '친').slice(0, 2),
              profileImage: receiverObj.profileImageName || 'default.png',
            });
          }
        });

        friendList.value = Array.from(map.values());
        if (friendList.value.length > 0 && !selectedFriendId.value) {
          selectedFriendId.value = friendList.value[0].id;
        }
      }
    } catch (fErr) {
      console.log('친구 목록 조회 예외', fErr);
    }
  } catch (err) {
    console.log('초기 데이터 로드 예외', err);
  }
};

onMounted(() => {
  loadRemitInitData();
});

const canProceedStep1 = computed(() => {
  if (remitType.value === 'ACCOUNT') return !!accountForm.value.accountNumber;
  if (remitType.value === 'FRIEND') return !!selectedFriendId.value;
  if (remitType.value === 'DUTCH') return !!dutchRoomTitle.value;
  return true;
});

const formatCurrency = (val) => {
  return new Intl.NumberFormat('ko-KR').format(val || 0);
};

const handleBack = () => {
  if (currentStep.value > 1) {
    currentStep.value--;
  } else {
    router.push('/wallet');
  }
};

const checkAccount = () => {
  alert('수취인 계좌 실명이 확인되었습니다: 홍길동');
};

const selectRecentAccount = () => {
  accountForm.value.accountNumber = '110-382-918231';
  accountForm.value.bankCode = '088';
};

const loadLastTransaction = () => {
  alert('이전 결제 내역을 불러왔습니다: 스타벅스 강남점 (24,000원)');
};

const getFriendName = (id) => {
  const f = friendList.value.find(item => item.id === id);
  return f ? f.name : '친구';
};

const removeDutchFriend = (id) => {
  selectedDutchFriends.value = selectedDutchFriends.value.filter(fId => fId !== id);
};

const toggleDutchFriend = (id) => {
  if (selectedDutchFriends.value.includes(id)) {
    selectedDutchFriends.value = selectedDutchFriends.value.filter(fId => fId !== id);
  } else {
    selectedDutchFriends.value.push(id);
  }
};

const goToStep2 = () => {
  currentStep.value = 2;
};

const proceedFromStep2 = () => {
  if (remitType.value === 'DUTCH') {
    // 더치페이는 이미 결제한 건을 타인에게 청구하는 것이므로 잔액 부족 체크 없이 PIN 인증 후 모임방 생성
    currentStep.value = 4;
  } else if (remitAmount.value > myBalance.value) {
    currentStep.value = 3; // 부족금 자동충전 알림 뷰 (Screen 9)
  } else {
    currentStep.value = 4; // PIN 인증 (Screen 10)
  }
};

const selectedFile = ref(null);
const imagePreviewUrl = ref('');

const handleFileChange = (e) => {
  const file = e.target.files && e.target.files[0];
  if (file) {
    selectedFile.value = file;
    imagePreviewUrl.value = URL.createObjectURL(file);
  }
};

const removeSelectedFile = () => {
  selectedFile.value = null;
  imagePreviewUrl.value = '';
};

const executeRealTransfer = async () => {
  loading.value = true;
  try {
    const userId = authStore.userId || 1;

    // 지갑 정보 조회
    let wId = userId;
    try {
      const wInfo = await walletApi.getWalletByUserId(userId);
      if (wInfo) {
        wId = wInfo.walletId || wInfo.id || userId;
      }
    } catch (e) {
      console.log('지갑 ID 조회 예외', e);
    }

    if (remitType.value === 'DUTCH') {
      // 더치페이 모임방 생성 API (POST /api/settlements)
      try {
        await remittanceApi.createSettlement({
          requesterId: userId,
          title: dutchRoomTitle.value || '더치페이 정산 모임방',
          content: `${dutchRoomTitle.value} 정산 청구`,
          totalAmount: remitAmount.value || 0,
          memberUserIds: selectedDutchFriends.value,
        });
      } catch (dErr) {
        console.log('더치페이 모임방 생성 예외:', dErr);
      }
    } else {
      // UI/UX 수정 전 원본 백엔드 송금 통신 규격 100% 복원
      const recType = remitType.value === 'FRIEND' ? 'WALLET' : 'ACCOUNT';
      const payload = {
        walletId: wId,
        receiverType: recType,
        amount: remitAmount.value || 0,
        memo: remitMemo.value || (remitType.value === 'FRIEND' ? '친구 송금' : '계좌 송금'),
        content: remitMemo.value || '송금 완료!',
        visibility: 'PUBLIC',
      };

      if (recType === 'WALLET') {
        payload.receiverId = selectedFriendId.value || 2;
      } else {
        payload.bankCode = accountForm.value.bankCode || '088';
        payload.accountNumber = accountForm.value.accountNumber || '';
      }

      if (selectedFile.value) {
        payload.file = selectedFile.value;
      }

      const res = await remittanceApi.sendMoney(payload);
      console.log('원본 송금 API 성공 결과:', res);

      // 지갑 잔액 차감 반영
      myBalance.value = Math.max(0, myBalance.value - (remitAmount.value || 0));

      // 피드 목록에 즉시 반영 (FeedImageSlider.vue 및 TransferFeedBody.vue 연동)
      const imgList = imagePreviewUrl.value ? [{ imageId: Date.now(), url: imagePreviewUrl.value }] : [];
      const newFeedObj = {
        feedId: res?.feedId || res?.transactionId || Date.now(),
        userId: userId,
        feedType: 'TRANSFER',
        userName: authStore.userName || '사용자',
        content: payload.content,
        createdAt: new Date().toISOString(),
        visibility: 'PUBLIC',
        images: imgList,
        likeCount: 0,
        commentCount: 0,
        liked: false,
        sender: {
          nickname: authStore.userName || '사용자',
          profileImageName: 'default.png'
        }
      };
      const existingFeeds = JSON.parse(localStorage.getItem('user_created_feeds') || '[]');
      existingFeeds.unshift(newFeedObj);
      localStorage.setItem('user_created_feeds', JSON.stringify(existingFeeds));
    }

    currentStep.value = 5; // 완료 화면
  } catch (err) {
    console.error('송금 처리 원본 API 오류:', err);
    alert('송금 처리 실패: 출금 잔액 및 입력 정보를 확인해 주세요.');
  } finally {
    loading.value = false;
  }
};

const appendPin = (n) => {
  if (pinCode.value.length < 6) {
    pinCode.value += String(n);
    if (pinCode.value.length === 6) {
      setTimeout(() => {
        executeRealTransfer();
      }, 200);
    }
  }
};

const deletePin = () => {
  pinCode.value = pinCode.value.slice(0, -1);
};

const nudgeUser = (name) => {
  alert(`${name} 님에게 찌르기 푸시 알림을 발송했습니다.`);
};
</script>

<style scoped>
.remit-root {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  overflow: hidden;
  background: #F8F9FB;
  color: #1F2024;
}

.remit-header {
  flex-shrink: 0;
  height: 48px;
  background: #ffffff;
  border-bottom: 1px solid #F0F1F4;
  padding: 0 16px;
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.back-btn {
  background: none;
  border: none;
  font-size: 18px;
  color: #1F2024;
  cursor: pointer;
}

.header-title {
  margin: 0;
  font-size: 14px;
  font-weight: 800;
}

.kb-pay-tag {
  background: #FFB300;
  color: #ffffff;
  font-size: 10px;
  font-weight: 900;
  padding: 2px 6px;
  border-radius: 4px;
}

.header-balance {
  font-size: 11px;
  text-align: right;
}

.b-label {
  color: #8F92A1;
  display: block;
  font-size: 9px;
}

.b-val {
  font-weight: 800;
  color: #1F2024;
}

.sub-tab-nav {
  display: flex;
  background: #E2E8F0;
  padding: 3px;
}

.nav-tab-btn {
  flex: 1;
  padding: 7px 0;
  border: none;
  background: transparent;
  font-size: 11px;
  font-weight: 800;
  color: #64748B;
  border-radius: 6px;
  cursor: pointer;
}

.nav-tab-btn.active {
  background: #ffffff;
  color: #1F2024;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.remit-body {
  flex: 1;
  min-height: 0;
  padding: 16px;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.remit-body::-webkit-scrollbar {
  display: none;
}

.step-card {
  background: #ffffff;
  border: 1px solid #E2E8F0;
  border-radius: 20px;
  padding: 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.03);
}

.step-badge {
  background: #EFF6FF;
  color: #2563EB;
  font-size: 10px;
  font-weight: 800;
  padding: 3px 8px;
  border-radius: 999px;
}

.step-badge.warning { background: #FFFBE6; color: #FFA000; }
.step-badge.danger { background: #FDF2F8; color: #DB2777; }

.form-label-sm {
  font-size: 11px;
  font-weight: 800;
  color: #64748B;
  display: block;
  margin-bottom: 6px;
}

.bank-grid-4 {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
}

.bank-select-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 4px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  cursor: pointer;
}

.bank-select-chip.active {
  border-color: #2563EB;
  background: #EFF6FF;
}

.bank-icon-sm {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 9px;
  font-weight: 800;
  margin-bottom: 4px;
}

.bank-name-sm {
  font-size: 10px;
  font-weight: 800;
}

.search-input-wrap {
  position: relative;
}

.search-ic {
  position: absolute;
  left: 10px;
  top: 8px;
  color: #94A3B8;
  font-size: 12px;
}

.search-input-field {
  width: 100%;
  padding: 6px 10px 6px 30px;
  background: #F1F5F9;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 700;
  outline: none;
}

.friend-avatar-badge {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #FEF3C7;
  color: #D97706;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 800;
}

.friend-item-card.selected {
  border-color: #FFB300;
  background: #FFFBE6;
}

.amount-field-direct {
  width: 100%;
  border: none;
  font-size: 24px;
  outline: none;
  background: transparent;
}

.auto-charge-warning-card {
  background: #FFFEE6;
  border: 1px solid #FFD54F;
}

.exclamation-badge {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #FFA000;
  color: #ffffff;
  font-size: 11px;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
}

.text-warning-dark { color: #B45309; }

.pin-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #CBD5E1;
  background: #ffffff;
}

.pin-dot.filled {
  background: #FFB300;
  border-color: #FFB300;
}

.pin-keypad-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.pin-key {
  height: 48px;
  border: none;
  background: #F8FAFC;
  border-radius: 12px;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
}

.icon-circle-lg {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.width-80 { width: 80px; }
.flex-2 { flex: 2; }
.btn-white { background: #ffffff; }

.tx-select-modal-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.tx-select-modal-card {
  width: 100%;
  max-width: 360px;
}</style>
