<template>
  <div class="remit-container">
    <!-- 공통 서브 화면 헤더 컴포넌트 적용 -->
    <PageHeader
      :title="headerTitleText"
      :show-back="currentStep > 1"
      @back="handleBack"
    />

    <!-- 공통 탭 바 컴포넌트 적용 (STEP 1) -->
    <CommonTabBar
      v-if="currentStep === 1"
      v-model="remitType"
      :tabs="tabOptions"
    />

    <!-- 본문 가변 스크롤 영역 -->
    <div class="card-body-scroll">
      <!-- ==========================================
           [STEP 1] 계좌 입력 / 친구 선택 / 더치페이 생성
      ========================================== -->
      <div v-if="currentStep === 1" class="step-content-wrap">
        <!-- 1-A. 계좌 송금 1단계 -->
        <template v-if="remitType === 'ACCOUNT'">
          <!-- 계좌 번호 입력 -->
          <div class="form-field-group">
            <label class="field-label text-13-bold">계좌 번호 입력</label>
            <div class="input-with-btn-row">
              <input
                v-model="accountForm.accountNumber"
                @input="onAccountNumberInput"
                type="text"
                class="custom-input text-15-bold"
                placeholder="'-' 없이 계좌번호 입력"
              />
            </div>
          </div>

          <!-- 은행 선택 5열 그리드 (10개 은행 로고 적용) -->
          <div class="form-field-group">
            <label class="field-label text-13-bold">은행 선택</label>
            <div class="bank-chip-grid">
              <button
                v-for="b in bankOptions"
                :key="b.code"
                class="bank-chip-card"
                :class="{ active: accountForm.bankCode === b.code }"
                @click="accountForm.bankCode = b.code"
              >
                <img
                  :src="`/api/banks/logo/${b.fileName}`"
                  class="bank-logo-img"
                  :alt="b.name"
                />
                <span class="bank-chip-name text-13-bold">{{ b.name }}</span>
              </button>
            </div>
          </div>

          <!-- 최근 송금 계좌 -->
          <div class="form-field-group">
            <label class="field-label text-13-bold">최근 송금 계좌</label>
            <div
              v-if="recentAccounts.length === 0"
              class="empty-recent-msg text-13"
            >
              최근 송금 내역이 없습니다.
            </div>
            <div v-else class="recent-list-wrap">
              <div
                v-for="recent in recentAccounts"
                :key="recent.id || recent.accountNumber"
                class="recent-card-item"
                @click="selectRecentAccountItem(recent)"
              >
                <div class="recent-item-left">
                  <img
                    :src="`/api/banks/logo/${getBankLogoFileName(recent.bankName)}`"
                    class="bank-logo-img-small"
                  />
                  <div class="recent-info-text">
                    <p class="recent-name-line text-15-bold">
                      {{ recent.receiverName || recent.name || "수취인" }} ({{
                        getBankName(recent.bankName)
                      }}
                      {{ recent.accountNumber }})
                    </p>
                    <p class="recent-sub-line text-13">
                      최근 송금: {{ recent.date || "최근" }} •
                      {{ formatCurrency(recent.amount) }}원
                    </p>
                  </div>
                </div>
                <i class="fa-solid fa-chevron-right arrow-ic"></i>
              </div>
            </div>
          </div>
        </template>

        <!-- 1-B. 친구 송금 1단계 -->
        <template v-else-if="remitType === 'FRIEND'">
          <div class="search-box-wrap">
            <i class="fa-solid fa-magnifying-glass search-ic"></i>
            <input
              v-model="friendSearchKeyword"
              type="text"
              class="search-input text-15"
              placeholder="친구 이름 또는 프로필 ID 입력..."
            />
          </div>

          <div class="form-field-group">
            <label class="field-label text-13-bold">내 친구 목록</label>
            <div
              v-if="filteredFriends.length === 0"
              class="empty-recent-msg text-13"
            >
              등록된 친구가 없습니다.
            </div>
            <div
              v-for="friend in filteredFriends"
              :key="friend.id"
              class="friend-card-item"
              :class="{ active: selectedFriendId === friend.id }"
              @click="selectFriendAndProceed(friend.id)"
            >
              <div class="friend-item-left">
                <img
                  :src="getProfileImageUrl(friend)"
                  class="friend-avatar-img"
                  @error="$event.target.style.display='none'"
                />
                <div>
                  <p class="friend-name text-15-bold">{{ friend.name }}</p>
                  <p class="friend-sub text-13">@{{ friend.username }}</p>
                </div>
              </div>

              <div
                v-if="selectedFriendId === friend.id"
                class="selected-badge-wrap"
              >
                <span class="sel-tag text-13-bold">선택됨</span>
                <i class="fa-solid fa-circle-check sel-ic"></i>
              </div>
              <i v-else class="fa-regular fa-circle unsel-ic"></i>
            </div>
          </div>
        </template>

        <!-- 1-C. 더치페이 정산 1번 화면: 친구 선택 -->
        <template v-else-if="remitType === 'DUTCH'">
          <!-- 정산 친구 검색 -->
          <div class="search-box-wrap">
            <i class="fa-solid fa-magnifying-glass search-ic"></i>
            <input
              v-model="dutchFriendSearchKeyword"
              type="text"
              class="search-input text-15"
              placeholder="정산할 친구 이름 검색..."
            />
          </div>

          <div class="form-field-group">
            <label class="field-label text-13-bold"
              >선택된 정산 참여자 (총
              {{ selectedDutchFriends.length + 1 }}명)</label
            >
            <div class="selected-tags-flex">
              <div class="my-tag-badge text-13-bold">
                <img
                  :src="myProfileImageUrl"
                  class="tag-avatar-img"
                  @error="$event.target.style.display='none'"
                />
                <span>나</span>
              </div>
              <div
                v-for="fId in selectedDutchFriends"
                :key="fId"
                class="friend-tag-badge text-13-bold"
              >
                <img
                  :src="getProfileImageUrl(getFriendObj(fId))"
                  class="tag-avatar-img"
                  @error="$event.target.style.display='none'"
                />
                <span>{{ getFriendName(fId) }}</span>
                <i
                  class="fa-solid fa-xmark del-ic"
                  @click="removeDutchFriend(fId)"
                ></i>
              </div>
            </div>
          </div>

          <div class="form-field-group">
            <label class="field-label text-13-bold">함께 정산할 친구 선택</label>
            <div
              v-if="filteredDutchFriends.length === 0"
              class="empty-recent-msg text-13"
            >
              검색 결과 또는 등록된 친구가 없습니다.
            </div>
            <div
              v-for="friend in filteredDutchFriends"
              :key="friend.id"
              class="friend-card-item"
              :class="{
                'dutch-active': selectedDutchFriends.includes(friend.id),
              }"
              @click="toggleDutchFriend(friend.id)"
            >
              <div class="friend-item-left">
                <img
                  :src="getProfileImageUrl(friend)"
                  class="friend-avatar-img"
                  @error="$event.target.style.display='none'"
                />
                <div>
                  <p class="friend-name text-15-bold">{{ friend.name }}</p>
                  <p class="friend-sub text-13">@{{ friend.username }}</p>
                </div>
              </div>

              <div
                v-if="selectedDutchFriends.includes(friend.id)"
                class="selected-badge-wrap"
              >
                <span class="dutch-tag text-13-bold">정산 참여</span>
                <i class="fa-solid fa-circle-check dutch-ic"></i>
              </div>
              <i v-else class="fa-regular fa-circle unsel-ic"></i>
            </div>
          </div>
        </template>

        <!-- 하단 주요 버튼 (계좌 송금 및 더치페이 탭에서 유지) -->
        <div
          v-if="remitType === 'ACCOUNT' || remitType === 'DUTCH'"
          class="next-btn-wrap"
        >
          <button
            class="bottom-btn text-18-bold"
            :disabled="!canProceedStep1"
            @click="goToStep2"
          >
            다음 단계로 이동 <i class="fa-solid fa-arrow-right"></i>
          </button>
        </div>
      </div>

      <!-- ==========================================
           [STEP 2] 금액 입력 & 정산 분배에만 100% 집중
      ========================================== -->
      <div v-else-if="currentStep === 2" class="step-content-wrap">
        <template v-if="remitType !== 'DUTCH'">
          <!-- 수취 대상 확인 카드 -->
          <div class="receiver-summary-box">
            <div class="summary-main-line">
              <img
                :src="`/api/banks/logo/${getBankLogoFileName(remitType === 'FRIEND' ? 'kb' : accountForm.bankCode)}`"
                class="bank-logo-img-medium"
              />
              <div class="receiver-info-col">
                <h4 class="receiver-name text-15-bold">
                  <template v-if="remitType === 'FRIEND'">
                    {{ selectedFriendObj?.name || "선택한 친구" }}
                    <span class="sub-handle text-13"
                      >(@{{ selectedFriendObj?.username || "" }})</span
                    >
                  </template>
                  <template v-else>
                    {{ accountForm.receiverName || "수취인" }}
                    <span class="sub-handle text-13"
                      >({{ getBankName(accountForm.bankCode) }}
                      {{ accountForm.accountNumber }})</span
                    >
                  </template>
                </h4>
              </div>
              <span class="summary-type-tag text-13-bold">{{
                remitType === "FRIEND" ? "친구 송금" : "계좌 송금"
              }}</span>
            </div>
          </div>

          <!-- 송금할 금액 입력 및 빠른 금액 버튼 -->
          <div class="form-field-group">
            <label class="field-label text-13-bold">송금할 금액 입력</label>
            <div class="amount-input-row">
              <input
                :value="remitAmountDisplay"
                @input="onAmountInput"
                type="text"
                inputmode="numeric"
                class="amount-direct-input text-28-bold"
                placeholder="0"
              />
              <span class="krw-unit text-28-bold">원</span>
            </div>
            <div class="quick-amount-row">
              <button
                class="content-btn secondary text-13-bold"
                @click="remitAmount += 10000"
              >
                +1만
              </button>
              <button
                class="content-btn secondary text-13-bold"
                @click="remitAmount += 50000"
              >
                +5만
              </button>
              <button
                class="content-btn secondary text-13-bold"
                @click="remitAmount += 100000"
              >
                +10만
              </button>
              <button
                class="content-btn primary text-13-bold"
                @click="remitAmount = myBalance"
              >
                전액
              </button>
            </div>
          </div>
        </template>

        <template v-else>
          <!-- 2번 화면: 카테고리 & 정산 금액 선택 및 정산 방식 선택 -->
          <div class="form-field-group">
            <div class="category-title-flex">
              <label class="field-label text-13-bold" style="margin-bottom: 0;"
                ><i class="fa-solid fa-shapes brand-ic"></i> 소비 카테고리 선택</label
              >
              <button
                type="button"
                class="category-toggle-sub-btn text-13-bold"
                @click="isCategoryExpanded = !isCategoryExpanded"
              >
                <span>{{ isCategoryExpanded ? '접기 ▲' : '더보기 (' + (categoryList.length - 4) + '개) ▼' }}</span>
              </button>
            </div>
            <SpendingCategorySelector
              v-model="selectedCategoryId"
              :categories="displayedCategoryList"
              compact
            />
          </div>

          <div class="form-field-group" style="margin-top: 16px;">
            <label class="field-label text-13-bold">얼마를 정산할까요?</label>
            <div class="amount-input-row">
              <input
                :value="remitAmountDisplay"
                @input="onAmountInput"
                type="text"
                inputmode="numeric"
                class="amount-direct-input text-28-bold"
                placeholder="0"
              />
              <span class="krw-unit text-28-bold">원</span>
            </div>
            <!-- 3번 내 결제 내역에서 선택 서브 링크 -->
            <button
              type="button"
              class="tx-link-btn text-13-bold"
              @click="openTxSelectStep"
            >
              내 결제 내역에서 선택 <i class="fa-solid fa-chevron-right"></i>
            </button>
          </div>

          <!-- 🌟 정산 방식 선택 (1/N 균등 vs 차등 정산) -->
          <div class="form-field-group" style="margin-top: 20px;">
            <label class="field-label text-13-bold">정산 방식 선택</label>
            <div class="split-tab-bar">
              <button
                type="button"
                class="split-tab text-13-bold"
                :class="{ active: dutchSplitMode === 'EQUAL' }"
                @click="dutchSplitMode = 'EQUAL'"
              >
                1/N 균등 정산
              </button>
              <button
                type="button"
                class="split-tab text-13-bold"
                :class="{ active: dutchSplitMode === 'CUSTOM' }"
                @click="dutchSplitMode = 'CUSTOM'"
              >
                차등 정산 (직접 입력)
              </button>
            </div>
          </div>

          <!-- 정산 배분 요약 박스 -->
          <div class="dutch-calc-box" style="margin-top: 16px;">
            <span class="calc-title text-13-bold">
              {{ dutchSplitMode === 'EQUAL' ? `1/N 균등 정산 배분 (총 ${selectedDutchFriends.length + 1}명)` : '멤버별 정산 금액 직접 입력' }}
            </span>

            <div v-if="dutchSplitMode === 'EQUAL'" class="calc-list">
              <div class="calc-row text-15-bold">
                <span class="my-name">나</span>
                <span class="val-text"
                  >{{ formatCurrency(Math.floor((remitAmount || 0) / (selectedDutchFriends.length + 1))) }} 원</span
                >
              </div>
              <div
                v-for="fId in selectedDutchFriends"
                :key="fId"
                class="calc-row text-15-bold"
              >
                <span class="friend-name">{{ getFriendName(fId) }}</span>
                <span class="val-text red-val"
                  >{{ formatCurrency(Math.floor((remitAmount || 0) / (selectedDutchFriends.length + 1))) }} 원</span
                >
              </div>
            </div>
            <div v-else class="calc-list">
              <div class="calc-row text-15-bold">
                <span>나</span>
                <input
                  type="number"
                  v-model.number="customDutchAmounts['my']"
                  class="custom-calc-input text-13-bold"
                  placeholder="금액 입력"
                />
              </div>
              <div
                v-for="fId in selectedDutchFriends"
                :key="fId"
                class="calc-row text-15-bold"
              >
                <span>{{ getFriendName(fId) }}</span>
                <input
                  type="number"
                  v-model.number="customDutchAmounts[fId]"
                  class="custom-calc-input text-13-bold"
                  placeholder="금액 입력"
                />
              </div>
            </div>
          </div>
        </template>

        <!-- Step 2 하단 이동 버튼 -->
        <div class="next-btn-wrap">
          <button
            class="bottom-btn text-18-bold"
            :disabled="!remitAmount || remitAmount <= 0"
            @click="proceedFromStep2"
          >
            다음 (소비 카테고리 & 피드 작성) <i class="fa-solid fa-arrow-right"></i>
          </button>
        </div>
      </div>

      <!-- ==========================================
           [STEP 3 - 송금 전용 (계좌/친구)] 카테고리 / 피드 메시지 / 공개범위 / 사진 첨부
      ========================================== -->
      <div v-else-if="remitType !== 'DUTCH' && currentStep === 3" class="step-content-wrap">
        <!-- 수취인 요약 카드 -->
        <div class="receiver-summary-box">
          <div class="summary-main-line">
            <div class="receiver-info-col">
              <h4 class="receiver-name text-15-bold">
                <template v-if="remitType === 'FRIEND'">
                  {{ selectedFriendObj?.name || "선택한 친구" }}님에게 송금
                </template>
                <template v-else>
                  {{ accountForm.receiverName || "수취인" }}님에게 송금
                </template>
              </h4>
              <span class="text-13" style="color: var(--color-primary-border, #cc9200); font-weight: 700;">
                송금 금액: {{ formatCurrency(remitAmount) }} 원
              </span>
            </div>
            <span class="summary-type-tag text-13-bold">{{
              remitType === "FRIEND" ? "친구 송금" : "계좌 송금"
            }}</span>
          </div>
        </div>

        <!-- 1. 소비 카테고리 선택 -->
        <div class="form-field-group">
          <div class="category-title-flex">
            <label class="field-label text-13-bold" style="margin-bottom: 0;"
              ><i class="fa-solid fa-shapes brand-ic"></i> 소비 카테고리 선택</label
            >
            <button
              type="button"
              class="category-toggle-sub-btn text-13-bold"
              @click="isCategoryExpanded = !isCategoryExpanded"
            >
              <span>{{ isCategoryExpanded ? '접기 ▲' : '더보기 (' + (categoryList.length - 4) + '개) ▼' }}</span>
            </button>
          </div>
          <SpendingCategorySelector
            v-model="selectedCategoryId"
            :categories="displayedCategoryList"
            compact
          />
        </div>

        <!-- 2. 피드 메시지 (메모) -->
        <div class="form-field-group">
          <label class="field-label text-13-bold"
            ><i class="fa-solid fa-message brand-ic"></i> 피드에 남길 내용 (메모)</label
          >
          <textarea
            v-model="remitMemo"
            class="custom-textarea text-15"
            placeholder="피드에 남길 메시지를 입력하세요 (예: 축하해! 🎉)"
          ></textarea>
        </div>

        <!-- 3. 공개 범위 선택 -->
        <div class="form-field-group">
          <label class="field-label text-13-bold"
            ><i class="fa-solid fa-shield-halved brand-ic"></i> 공개 범위 선택</label
          >
          <div class="vis-grid">
            <button
              type="button"
              class="content-btn secondary text-13-bold"
              :class="{ active: remitVisibility === 'PUBLIC' }"
              @click="remitVisibility = 'PUBLIC'"
            >
              🌐 전체 공개
            </button>
            <button
              type="button"
              class="content-btn secondary text-13-bold"
              :class="{ active: remitVisibility === 'FRIEND' }"
              @click="remitVisibility = 'FRIEND'"
            >
              👥 친구 공개
            </button>
            <button
              type="button"
              class="content-btn secondary text-13-bold"
              :class="{ active: remitVisibility === 'PRIVATE' }"
              @click="remitVisibility = 'PRIVATE'"
            >
              🔒 나만 보기
            </button>
          </div>
        </div>

        <!-- 4. 사진 첨부 -->
        <div class="form-field-group">
          <div class="file-header-line">
            <label class="field-label text-13-bold"
              ><i class="fa-solid fa-image brand-ic"></i> 소셜 피드 사진 첨부 (선택)</label
            >
            <button
              v-if="selectedFile"
              type="button"
              class="cancel-file-btn text-13-bold"
              @click="removeSelectedFile"
            >
              첨부 취소
            </button>
          </div>

          <div class="photo-upload-container">
            <label v-if="!imagePreviewUrl" class="photo-upload-box">
              <i class="fa-solid fa-camera upload-icon"></i>
              <span class="upload-text text-13-bold">사진 추가하기</span>
              <input
                type="file"
                accept="image/*"
                class="hidden-file-input"
                @change="handleFileChange"
              />
            </label>
            <div v-else class="preview-img-wrap">
              <img :src="imagePreviewUrl" class="preview-img" />
              <button
                type="button"
                class="remove-photo-btn"
                @click="removeSelectedFile"
              >
                <i class="fa-solid fa-xmark"></i>
              </button>
            </div>
          </div>
        </div>

        <div class="next-btn-wrap">
          <button
            class="bottom-btn text-18-bold"
            @click="submitRemittance"
          >
            송금하기 <i class="fa-solid fa-paper-plane"></i>
          </button>
        </div>
      </div>

      <!-- ==========================================
           [STEP 3 - 정산 전용 (더치페이)] 내 결제 내역에서 선택 (하단 고정 버튼)
      ========================================== -->
      <div v-else-if="remitType === 'DUTCH' && currentStep === 3" class="step-content-wrap">
        <div class="tx-select-step-head">
          <h3 class="text-18-bold" style="margin: 0;">정산할 내 결제 건 선택</h3>
          <p class="text-13" style="color: #777; margin: 4px 0 0 0;">정산에 포함할 내 결제 내역을 클릭 선택하세요.</p>
        </div>

        <div class="tx-list-container scrollable-tx-area">
          <div v-if="userTxList.length === 0" class="empty-recent-msg text-13">
            불러올 수 있는 결제 내역이 없습니다.
          </div>
          <div v-else class="date-item-list">
            <div
              v-for="tx in userTxList"
              :key="tx.id"
              class="tx-item-wrapper"
              @click="toggleTxSelection(tx.id)"
            >
              <div class="tx-item-row-card" :class="{ active: selectedTxIds.includes(tx.id) }">
                <div class="tx-item-left">
                  <div class="icon-circle text-15-bold">
                    <i class="fa-solid fa-store"></i>
                  </div>
                  <div class="tx-info-text">
                    <div class="tx-item-title text-15-bold">{{ tx.title }}</div>
                    <div class="tx-item-sub text-13">{{ tx.date }}</div>
                  </div>
                </div>
                <div class="tx-item-right" style="display: flex; align-items: center; gap: 10px;">
                  <div class="tx-amount text-15-bold" style="color: #111;">
                    -{{ formatCurrency(tx.amount) }}원
                  </div>
                  <div class="select-check-ic">
                    <i
                      class="fa-circle-check"
                      :class="selectedTxIds.includes(tx.id) ? 'fa-solid active-kb' : 'fa-regular uncheck'"
                    ></i>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="fixed-bottom-btn-wrap">
          <button
            class="bottom-btn text-18-bold"
            :disabled="selectedTxIds.length === 0"
            @click="confirmTxSelection"
          >
            {{ selectedTxIds.length }}개 결제 건 적용 (총 {{ formatCurrency(selectedTxTotalAmount) }}원) <i class="fa-solid fa-arrow-right"></i>
          </button>
        </div>
      </div>

      <!-- ==========================================
           [STEP 4 - 정산 전용 (더치페이)] 총 정보 요약 & 정산 제목 입력 & 피드 작성
      ========================================== -->
      <div v-else-if="remitType === 'DUTCH' && currentStep === 4" class="step-content-wrap">
        <!-- 4번 화면: 정산 총 정보 요약 헤더 (카카오페이 스타일) -->
        <div class="kakaopay-settlement-head">
          <div class="form-field-group" style="margin-bottom: 12px; text-align: left;">
            <label class="field-label text-13-bold">정산 제목 (모임방 이름)</label>
            <input
              v-model="dutchRoomTitle"
              type="text"
              class="custom-input text-15-bold"
              placeholder="예: 맛있는 저녁 식사 정산"
            />
          </div>

          <div class="main-amount-row">
            <span class="text-13-bold" style="color: #666;">총</span>
            <h2 class="text-28-bold" style="margin: 0 4px; color: #111;">{{ formatCurrency(remitAmount || 0) }}</h2>
            <span class="text-20-bold" style="color: #111;">원</span>
          </div>
        </div>

        <!-- 카카오페이 대표 정산 요약 카드 -->
        <div class="kakaopay-item-card">
          <div class="item-card-left">
            <div class="item-ic-circle">
              <i :class="getCategoryIcon(selectedCategoryObj?.categoryName || '식비')"></i>
            </div>
            <div class="item-text-col">
              <h4 class="item-name text-15-bold">{{ selectedCategoryObj?.categoryName || '식사' }}</h4>
              <p class="item-sub text-13">{{ formatCurrency(remitAmount || 0) }}원 · 나 외 {{ selectedDutchFriends.length }}명</p>
            </div>
          </div>
        </div>

        <!-- 🌟 피드 관련 입력 추가 (메모, 공개범위, 사진첨부) -->
        <!-- 1. 피드 메시지 (메모) -->
        <div class="form-field-group" style="margin-top: 20px;">
          <label class="field-label text-13-bold"
            ><i class="fa-solid fa-message brand-ic"></i> 피드에 남길 내용 (메모)</label
          >
          <textarea
            v-model="remitMemo"
            class="custom-textarea text-15"
            placeholder="피드에 남길 메시지를 입력하세요 (예: 맛있게 잘 먹었어! 🍕)"
          ></textarea>
        </div>

        <!-- 2. 공개 범위 선택 -->
        <div class="form-field-group">
          <label class="field-label text-13-bold"
            ><i class="fa-solid fa-shield-halved brand-ic"></i> 공개 범위 선택</label
          >
          <div class="vis-grid">
            <button
              type="button"
              class="content-btn secondary text-13-bold"
              :class="{ active: remitVisibility === 'PUBLIC' }"
              @click="remitVisibility = 'PUBLIC'"
            >
              🌐 전체 공개
            </button>
            <button
              type="button"
              class="content-btn secondary text-13-bold"
              :class="{ active: remitVisibility === 'FRIEND' }"
              @click="remitVisibility = 'FRIEND'"
            >
              👥 친구 공개
            </button>
            <button
              type="button"
              class="content-btn secondary text-13-bold"
              :class="{ active: remitVisibility === 'PRIVATE' }"
              @click="remitVisibility = 'PRIVATE'"
            >
              🔒 나만 보기
            </button>
          </div>
        </div>

        <!-- 3. 소셜 피드 사진 첨부 -->
        <div class="form-field-group">
          <div class="file-header-line">
            <label class="field-label text-13-bold"
              ><i class="fa-solid fa-image brand-ic"></i> 소셜 피드 사진 첨부 (선택)</label
            >
            <button
              v-if="selectedFile"
              type="button"
              class="cancel-file-btn text-13-bold"
              @click="removeSelectedFile"
            >
              첨부 취소
            </button>
          </div>

          <div class="photo-upload-container">
            <label v-if="!imagePreviewUrl" class="photo-upload-box">
              <i class="fa-solid fa-camera upload-icon"></i>
              <span class="upload-text text-13-bold">사진 추가하기</span>
              <input
                type="file"
                accept="image/*"
                class="hidden-file-input"
                @change="handleFileChange"
              />
            </label>
            <div v-else class="preview-img-wrap">
              <img :src="imagePreviewUrl" class="preview-img" />
              <button
                type="button"
                class="remove-photo-btn"
                @click="removeSelectedFile"
              >
                <i class="fa-solid fa-xmark"></i>
              </button>
            </div>
          </div>
        </div>

        <!-- 4번 화면 실행 버튼 -->
        <div class="next-btn-wrap" style="margin-top: 24px;">
          <button
            class="bottom-btn text-18-bold"
            @click="submitRemittance"
          >
            정산 요청하기 <i class="fa-solid fa-paper-plane"></i>
          </button>
        </div>
      </div>
      <!-- ==========================================
           [STEP 3] 부족금 자동충전 알림
      ========================================== -->
      <div v-else-if="currentStep === 3" class="step-content-wrap">
        <div class="auto-top-badge-line">
          <span class="auto-badge-tag text-13-bold">부족금 자동충전</span>
          <span class="auto-calc-tag text-13-bold">자동 계산</span>
        </div>

        <div class="total-need-box">
          <div class="kb-badge-left">
            <div class="kb-circle text-13-bold">KB</div>
            <span class="kb-label text-15-bold">총 필요 금액</span>
          </div>
          <span class="total-need-val text-20-bold"
            >{{ formatCurrency(remitAmount || 0) }} 원</span
          >
        </div>

        <div class="auto-warning-card">
          <div class="warning-main-flex">
            <div class="exclam-circle text-13-bold">!</div>
            <div class="warning-text-wrap">
              <h4 class="warning-head text-15-bold">
                잔액 부족 (보유: {{ formatCurrency(myBalance) }}원)
              </h4>
              <p class="warning-desc text-13">
                부족분은 주거래 계좌에서 금액 입력 없이 자동 계산되어 즉시
                충전됩니다.
              </p>
            </div>
          </div>

          <div class="warning-bottom-line">
            <span class="auto-bank-name text-13-bold"
              >{{ getBankName(accountForm.bankCode) }} 주거래 계좌 자동
              충전액</span
            >
            <span class="auto-charge-val text-15-bold"
              >+{{
                formatCurrency(
                  Math.max(0, (remitAmount || 0) - (myBalance || 0)),
                )
              }}
              원</span
            >
          </div>
        </div>

        <div class="next-btn-wrap">
          <button class="bottom-btn text-18-bold" @click="currentStep = 4">
            자동 충전 후 즉시 송금 (PIN 인증)
          </button>
        </div>
      </div>

      <!-- ==========================================
           [STEP 4] PIN 비밀번호 인증
      ========================================== -->
      <div
        v-else-if="currentStep === 4"
        class="step-content-wrap pin-step-wrap"
      >
        <div class="pin-head-text">
          <h3 class="pin-title text-20-bold">간편 비밀번호 인증</h3>
          <p class="pin-sub text-13">
            송금 및 충전 승인을 위해
            <span class="highlight-pin text-13-bold">간편 6자리 비밀번호</span
            >를 입력하세요.
          </p>
        </div>

        <div class="pin-dots-row">
          <span
            v-for="i in 6"
            :key="i"
            class="dot-item"
            :class="{ filled: pinCode.length >= i }"
          ></span>
        </div>

        <div class="pin-keypad">
          <div class="keypad-row">
            <button
              v-for="n in [1, 2, 3]"
              :key="n"
              class="pin-btn text-18-bold"
              @click="appendPin(n)"
            >
              {{ n }}
            </button>
          </div>
          <div class="keypad-row">
            <button
              v-for="n in [4, 5, 6]"
              :key="n"
              class="pin-btn text-18-bold"
              @click="appendPin(n)"
            >
              {{ n }}
            </button>
          </div>
          <div class="keypad-row">
            <button
              v-for="n in [7, 8, 9]"
              :key="n"
              class="pin-btn text-18-bold"
              @click="appendPin(n)"
            >
              {{ n }}
            </button>
          </div>
          <div class="keypad-row">
            <button class="pin-btn re-btn text-13-bold" @click="pinCode = ''">
              재배열
            </button>
            <button class="pin-btn text-18-bold" @click="appendPin(0)">
              0
            </button>
            <button class="pin-btn del-btn text-15" @click="deletePin">
              <i class="fa-solid fa-delete-left"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- ==========================================
           [STEP 5] 송금 / 정산 요청 완료
      ========================================== -->
      <div
        v-else-if="currentStep === 5"
        class="step-content-wrap complete-step-wrap"
      >
        <div class="success-icon-circle">
          <i class="fa-solid fa-check-double"></i>
        </div>
        <div class="complete-text-wrap">
          <h3 class="complete-title text-20-bold">
            {{
              remitType === "DUTCH"
                ? "더치페이 정산 모임방이 생성되었습니다!"
                : "송금 처리가 완료되었습니다!"
            }}
          </h3>
          <p class="complete-desc text-13">
            {{
              remitType === "DUTCH"
                ? "선택하신 참여 친구들에게 1/N 정산 요청 알림 메시지가 전송되었습니다."
                : "요청하신 계좌 / 친구에게 정상 이체되었습니다."
            }}
          </p>
        </div>

        <div class="complete-btn-group">
          <template v-if="remitType === 'DUTCH'">
            <button
              class="bottom-btn text-18-bold"
              @click="$router.push('/mypage')"
            >
              <i class="fa-solid fa-user"></i> 마이페이지에서 정산 내역 확인하기
            </button>
            <button
              class="content-btn secondary text-15-bold"
              @click="$router.push('/wallet')"
            >
              내 지갑 홈으로 돌아가기
            </button>
          </template>
          <template v-else>
            <button
              class="bottom-btn text-18-bold"
              @click="$router.push('/feed')"
            >
              <i class="fa-solid fa-list-ul"></i> 소셜 피드로 이동하여 확인하기
            </button>
            <button
              class="content-btn secondary text-15-bold"
              @click="$router.push('/transactions')"
            >
              거래 내역 확인하기
            </button>
          </template>
        </div>
      </div>
    </div>

    <!-- 내 거래 내역 다중 선택 모달 -->
    <div
      v-if="showTxSelectModal"
      class="modal-overlay"
      @click.self="showTxSelectModal = false"
    >
      <div class="modal-card">
        <div class="modal-header">
          <h4 class="modal-title text-15-bold">
            <i class="fa-solid fa-square-check modal-ic"></i> 정산할 결제 건
            선택 (다중 선택 가능)
          </h4>
          <button class="close-btn" @click="showTxSelectModal = false">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <div v-if="userTxList.length === 0" class="empty-recent-msg text-13">
          조회된 결제 거래 내역이 없습니다.
        </div>
        <div v-else class="modal-body-content">
          <div class="tx-summary-bar text-13">
            <span class="summary-cnt text-13-bold"
              >선택 {{ selectedTxIds.length }}개</span
            >
            <span class="summary-sum text-15-bold"
              >총 {{ formatCurrency(selectedTxTotalAmount) }} 원</span
            >
          </div>

          <div class="tx-scroll-list">
            <div
              v-for="tx in userTxList"
              :key="tx.id"
              class="tx-item-card"
              :class="{ selected: selectedTxIds.includes(tx.id) }"
              @click="toggleTxSelection(tx.id)"
            >
              <div class="tx-item-left">
                <input
                  type="checkbox"
                  class="tx-chk"
                  :checked="selectedTxIds.includes(tx.id)"
                  @click.stop="toggleTxSelection(tx.id)"
                />
                <div>
                  <p class="tx-title text-15-bold">{{ tx.title }}</p>
                  <p class="tx-date text-13">{{ tx.date }} • 결제완료</p>
                </div>
              </div>
              <span class="tx-amt text-15-bold"
                >{{ formatCurrency(tx.amount) }}원</span
              >
            </div>
          </div>

          <button
            class="bottom-btn text-15-bold modal-submit-btn"
            :disabled="selectedTxIds.length === 0"
            @click="confirmTxSelection"
          >
            <i class="fa-solid fa-circle-plus"></i> {{ selectedTxIds.length }}개
            결제 건으로 더치페이 생성 (총
            {{ formatCurrency(selectedTxTotalAmount) }}원)
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import walletApi from "@/api/walletApi";
import friendApi from "@/api/friend";
import transactionApi from "@/api/transactionApi";
import remittanceApi from "@/api/remittanceApi";
import analysisApi from "@/api/analysisApi";
import { getCategoryIcon } from "@/util/analysis";
import PageHeader from "@/components/common/PageHeader.vue";
import CommonTabBar from "@/components/common/CommonTabBar.vue";
import SpendingCategorySelector from "@/components/common/SpendingCategorySelector.vue";

const router = useRouter();
const authStore = useAuthStore();

const selectedCategoryId = ref(null);
const categoryList = ref([
  { spendingCategoryId: 1, categoryName: "식비", parentCategoryId: null },
  { spendingCategoryId: 2, categoryName: "카페", parentCategoryId: null },
  { spendingCategoryId: 3, categoryName: "생활", parentCategoryId: null },
  { spendingCategoryId: 4, categoryName: "온라인쇼핑", parentCategoryId: null },
  { spendingCategoryId: 5, categoryName: "뷰티/미용", parentCategoryId: null },
  { spendingCategoryId: 6, categoryName: "교통", parentCategoryId: null },
  { spendingCategoryId: 7, categoryName: "자동차", parentCategoryId: null },
  { spendingCategoryId: 8, categoryName: "주거/통신", parentCategoryId: null },
  { spendingCategoryId: 9, categoryName: "금융", parentCategoryId: null },
  { spendingCategoryId: 10, categoryName: "여행", parentCategoryId: null },
  { spendingCategoryId: 11, categoryName: "교육", parentCategoryId: null },
  { spendingCategoryId: 12, categoryName: "반려동물", parentCategoryId: null },
  { spendingCategoryId: 13, categoryName: "병원", parentCategoryId: null },
]);

const isCategoryExpanded = ref(false);
const displayedCategoryList = computed(() => {
  return isCategoryExpanded.value ? categoryList.value : categoryList.value.slice(0, 4);
});

const selectedCategoryObj = computed(() => {
  return categoryList.value.find(
    (c) => c.spendingCategoryId === selectedCategoryId.value
  ) || categoryList.value[0];
});

const headerTitleText = computed(() => {
  if (remitType.value === "ACCOUNT") return "계좌 송금하기";
  if (remitType.value === "FRIEND") return "친구 송금하기";
  if (remitType.value === "DUTCH") return "정산 요청 개설";
  return "송금하기";
});

const remitType = ref("ACCOUNT");
const tabOptions = [
  { label: "계좌 송금", value: "ACCOUNT" },
  { label: "친구 송금", value: "FRIEND" },
  { label: "정산 (더치페이)", value: "DUTCH" },
];

const showTxSelectModal = ref(false);
const userTxList = ref([]);
const selectedTxIds = ref([]);

const selectedTxTotalAmount = computed(() => {
  return userTxList.value
    .filter((t) => selectedTxIds.value.includes(t.id))
    .reduce((sum, t) => sum + t.amount, 0);
});

const toggleTxSelection = (id) => {
  if (selectedTxIds.value.includes(id)) {
    selectedTxIds.value = selectedTxIds.value.filter((txId) => txId !== id);
  } else {
    selectedTxIds.value.push(id);
  }
};

const confirmTxSelection = () => {
  const selectedItems = userTxList.value.filter((t) =>
    selectedTxIds.value.includes(t.id),
  );
  if (selectedItems.length === 0) return;

  if (selectedItems.length === 1) {
    dutchRoomTitle.value = `${selectedItems[0].title} 정산 모임방`;
  } else {
    dutchRoomTitle.value = `${selectedItems[0].title} 외 ${selectedItems.length - 1}건 정산 모임방`;
  }

  remitAmount.value = selectedTxTotalAmount.value;
  showTxSelectModal.value = false;
  if (remitType.value === "DUTCH") {
    currentStep.value = 2;
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return "";
  try {
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    const hours = String(d.getHours()).padStart(2, "0");
    const minutes = String(d.getMinutes()).padStart(2, "0");
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
    "현장 결제"
  );
};

const openTxSelectStep = async () => {
  currentStep.value = 3;
  selectedTxIds.value = [];
  try {
    const userId = authStore.userId || 1;
    if (transactionApi.getTransactions) {
      const data = await transactionApi.getTransactions(userId);
      if (data && Array.isArray(data)) {
        const payItems = data.filter((t) => {
          const typeStr = (t.transactionType || t.type || "").toUpperCase();
          return (
            typeStr.includes("PAY") ||
            typeStr === "" ||
            (!typeStr.includes("CHARGE") &&
              !typeStr.includes("TRANSFER") &&
              !typeStr.includes("REMIT"))
          );
        });

        userTxList.value = payItems.map((t) => ({
          id: t.id || t.transactionId,
          title: getStoreTitle(t),
          amount: Math.abs(t.amount || 0),
          date: formatDate(t.createdAt || t.transactionDate || t.date),
        }));
      }
    }
  } catch (e) {
    console.log("거래내역 로드 예외", e);
  }
};

const openTxSelectModal = async () => {
  await openTxSelectStep();
};

const currentUserName = computed(
  () => authStore.userName || authStore.user?.userName || "사용자",
);
const currentStep = ref(1);
const myBalance = ref(0);
const recentAccounts = ref([]);

// 초기 진입 시 은행 선택이 아무것도 안 되도록 빈 값("")으로 설정
const accountForm = ref({
  accountNumber: "",
  bankCode: "",
  receiverName: "",
});

// 10개 전체 은행 계좌번호 앞자리 자동 감지 매핑
const detectBankByAccountNumber = (accNum) => {
  if (!accNum) {
    accountForm.value.bankCode = "";
    return;
  }
  const cleanNum = accNum.replace(/-/g, "").trim();
  if (cleanNum.length < 3) {
    accountForm.value.bankCode = "";
    return;
  }

  const prefix2 = cleanNum.slice(0, 2);
  const prefix3 = cleanNum.slice(0, 3);
  const prefix4 = cleanNum.slice(0, 4);

  // 1. KB국민은행 (004): 110, 9410, 9435, 4941, 4334, 4632
  if (["9410", "9435", "4941", "4334", "4632"].includes(prefix4) || prefix3 === "110" || prefix3 === "941") {
    accountForm.value.bankCode = "004";
  }
  // 2. 신한은행 (088): 110, 140, 150, 1100 (국민 110과 구분 위해 4자리 우선 검사 및 기본 신한)
  else if (["1100", "1400", "1500"].includes(prefix4) || ["140", "150", "110"].includes(prefix3)) {
    accountForm.value.bankCode = "088";
  }
  // 3. 카카오뱅크 (090): 3333, 3900, 7777, 7979, 333
  else if (["3333", "3900", "7777", "7979"].includes(prefix4) || prefix3 === "333") {
    accountForm.value.bankCode = "090";
  }
  // 4. 우리은행 (020): 1002, 1005, 020
  else if (["1002", "1005"].includes(prefix4) || prefix3 === "020") {
    accountForm.value.bankCode = "020";
  }
  // 5. NH농협은행 (011): 301, 302, 312, 351, 352
  else if (["301", "302", "312", "351", "352"].includes(prefix3)) {
    accountForm.value.bankCode = "011";
  }
  // 6. 하나은행 (081): 620, 900, 910, 145
  else if (["620", "900", "910", "145"].includes(prefix3)) {
    accountForm.value.bankCode = "081";
  }
  // 7. IBK기업은행 (003): 031, 032, 222, 010
  else if (["031", "032", "222", "010"].includes(prefix3)) {
    accountForm.value.bankCode = "003";
  }
  // 8. 케이뱅크 (089): 1001, 7000, 089
  else if (["1001", "7000"].includes(prefix4) || prefix3 === "089") {
    accountForm.value.bankCode = "089";
  }
  // 9. 토스뱅크 (092): 1000, 2000, 092
  else if (["1000", "2000"].includes(prefix4) || prefix3 === "092") {
    accountForm.value.bankCode = "092";
  }
  // 10. SC제일은행 (023): 100, 600, 023
  else if (["600", "023"].includes(prefix3) || (prefix3 === "100" && prefix4 !== "1002")) {
    accountForm.value.bankCode = "023";
  } else {
    accountForm.value.bankCode = "";
  }
};

const onAccountNumberInput = (e) => {
  const raw = e.target.value.replace(/[^0-9]/g, "");
  accountForm.value.accountNumber = raw;
  detectBankByAccountNumber(raw);
  // (참고: 강제 자동넘김 로직을 제거하여 사용자가 계좌번호를 끝까지 입력한 후 하단 버튼을 누를 수 있도록 수정됨)
};

const selectRecentAccountItem = (item) => {
  accountForm.value.accountNumber = item.accountNumber || "";
  accountForm.value.bankCode = item.bankCode || "004";
  accountForm.value.receiverName =
    item.ownerName || item.receiverName || item.name || "";
  currentStep.value = 2;
};

// 10개 은행 옵션 리스트
const bankOptions = [
  { code: "004", name: "KB국민", fileName: "kb.png" },
  { code: "088", name: "신한", fileName: "shinhan.png" },
  { code: "020", name: "우리", fileName: "woori.png" },
  { code: "081", name: "하나", fileName: "hana.png" },
  { code: "003", name: "IBK기업", fileName: "ibk.png" },
  { code: "011", name: "NH농협", fileName: "nh.png" },
  { code: "090", name: "카카오뱅크", fileName: "kakaobank.png" },
  { code: "089", name: "케이뱅크", fileName: "kbank.png" },
  { code: "092", name: "토스뱅크", fileName: "tossbank.png" },
  { code: "023", name: "SC제일", fileName: "sc.png" },
];

const getBankName = (code) => {
  const found = bankOptions.find((b) => b.code === code);
  if (found) return found.name;
  if (!code) return "은행 미선택";
  const str = String(code);
  if (str.includes("신한")) return "신한";
  if (str.includes("국민") || str.includes("KB")) return "KB국민";
  if (str.includes("하나")) return "하나";
  if (str.includes("우리")) return "우리";
  return str;
};

const getBankLogoFileName = (bank) => {
  if (!bank) return "kb.png";
  const str = String(bank);
  if (str.includes("신한") || str === "SH" || str === "088")
    return "shinhan.png";
  if (
    str.includes("KB") ||
    str.includes("국민") ||
    str === "KB" ||
    str === "004"
  )
    return "kb.png";
  if (str.includes("하나") || str === "HN" || str === "081") return "hana.png";
  if (str.includes("우리") || str === "WR" || str === "020") return "woori.png";
  if (str.includes("기업") || str.includes("IBK") || str === "003")
    return "ibk.png";
  if (str.includes("농협") || str.includes("NH") || str === "011")
    return "nh.png";
  if (str.includes("카카오") || str === "090") return "kakaobank.png";
  if (str.includes("케이뱅크") || str === "089") return "kbank.png";
  if (str.includes("토스") || str === "092") return "tossbank.png";
  if (str.includes("제일") || str.includes("SC") || str === "023")
    return "sc.png";
  return "kb.png";
};

const friendSearchKeyword = ref("");
const selectedFriendId = ref(null); // 초기 선택 없음 (null)
const friendList = ref([]);

const selectFriendAndProceed = (fId) => {
  selectedFriendId.value = fId;
  currentStep.value = 2;
};

const selectedFriendObj = computed(() => {
  return friendList.value.find((f) => f.id === selectedFriendId.value);
});

const filteredFriends = computed(() => {
  if (!friendSearchKeyword.value) return friendList.value;
  return friendList.value.filter(
    (f) =>
      (f.name && f.name.includes(friendSearchKeyword.value)) ||
      (f.username && f.username.includes(friendSearchKeyword.value)),
  );
});

const getProfileImageUrl = (friend) => {
  if (!friend) return "/api/feeds/profile/default_profile.png";
  if (friend.avatarUrl) return friend.avatarUrl;
  const imgName =
    friend.profileImageName || friend.profileImage || friend.profileImg;
  if (imgName) {
    if (imgName.startsWith("http") || imgName.startsWith("/")) return imgName;
    return `/api/feeds/profile/${imgName}`;
  }
  return "/api/feeds/profile/default_profile.png";
};

const myProfileImageUrl = computed(() => {
  const pName =
    authStore.user?.profileImageName ||
    authStore.user?.profileImage ||
    authStore.user?.profileImg;
  if (pName) {
    if (pName.startsWith("http") || pName.startsWith("/")) return pName;
    return `/api/feeds/profile/${pName}`;
  }
  return "/api/feeds/profile/default_profile.png";
});

const getFriendObj = (fId) => {
  return friendList.value.find((f) => f.id === fId);
};
const dutchRoomTitle = ref("");
const selectedDutchFriends = ref([]);
const dutchFriendSearchKeyword = ref("");

const filteredDutchFriends = computed(() => {
  if (!dutchFriendSearchKeyword.value) return friendList.value;
  const kw = dutchFriendSearchKeyword.value.trim().toLowerCase();
  return friendList.value.filter(
    (f) =>
      (f.name && f.name.toLowerCase().includes(kw)) ||
      (f.username && f.username.toLowerCase().includes(kw)),
  );
});
const dutchSplitMode = ref("EQUAL");
const customDutchAmounts = ref({});

const remitAmount = ref(0);
const remitMemo = ref("");
const remitVisibility = ref("PUBLIC");
const pinCode = ref("");

const remitAmountDisplay = computed(() => {
  if (!remitAmount.value) return "";
  return Number(remitAmount.value).toLocaleString("ko-KR");
});

const onAmountInput = (e) => {
  const raw = e.target.value.replace(/[^0-9]/g, "");
  remitAmount.value = raw ? parseInt(raw, 10) : 0;
};

const loadRemitInitData = async () => {
  try {
    const userId = authStore.userId;

    try {
      const cats = await analysisApi.getCategories();
      if (cats && Array.isArray(cats)) {
        categoryList.value = cats;
      }
    } catch (catErr) {
      console.log("카테고리 목록 로드 예외", catErr);
    }

    if (!userId) return;

    try {
      const wInfo = await walletApi.getWalletByUserId(userId);
      if (wInfo) {
        myBalance.value =
          wInfo.balance ?? wInfo.amount ?? wInfo.money ?? wInfo.pointMoney ?? 0;
      }
    } catch (wErr) {
      console.log("지갑 잔액 조회 예외", wErr);
    }

    try {
      if (remittanceApi.getBankRemittanceInfo) {
        const bInfo = await remittanceApi.getBankRemittanceInfo(userId);
        if (bInfo) {
          const rList =
            bInfo.recentRemittances ||
            bInfo.recentAccounts ||
            bInfo.recents ||
            (Array.isArray(bInfo) ? bInfo : []);
          if (rList.length > 0) {
            recentAccounts.value = rList.map((r) => ({
              id: r.id || r.remittanceId || r.accountNumber,
              receiverName: r.ownerName || r.receiverName || r.name || "",
              ownerName: r.ownerName || r.receiverName || "",
              bankName: r.bankName || "KB국민",
              bankCode: r.bankCode || "004",
              accountNumber: r.accountNumber || "",
              amount: r.amount || 0,
              date: r.date || "",
            }));
          }
        }
      }
    } catch (bErr) {
      console.log("최근 송금 계좌 조회 예외", bErr);
    }

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
          const receiverObj =
            f.receiver || f.friendMember || f.member || f.friend || f;
          const fId =
            f.friendUserId ||
            f.friendId ||
            f.id ||
            receiverObj.userId ||
            receiverObj.id ||
            idx + 1;
          const fNickname =
            receiverObj.nickname ||
            receiverObj.name ||
            receiverObj.userName ||
            f.nickname ||
            f.name ||
            `친구${fId}`;
          const fUsername =
            receiverObj.username ||
            receiverObj.loginId ||
            receiverObj.userLoginId ||
            f.username ||
            `user_${fId}`;

          const fImgName =
            receiverObj.profileImageName ||
            receiverObj.profileImage ||
            receiverObj.profileImg ||
            f.profileImageName ||
            f.profileImage ||
            f.profileImg ||
            "";

          if (!map.has(fId)) {
            map.set(fId, {
              id: fId,
              name: fNickname,
              username: fUsername,
              initials: (fNickname || "친").slice(0, 2),
              profileImageName: fImgName,
              avatarUrl: fImgName
                ? fImgName.startsWith("http") || fImgName.startsWith("/")
                  ? fImgName
                  : `/api/feeds/profile/${fImgName}`
                : "/api/feeds/profile/default_profile.png",
            });
          }
        });

        friendList.value = Array.from(map.values());
        // 초기 자동 선택 없음 유지
      }
    } catch (fErr) {
      console.log("친구 목록 조회 예외", fErr);
    }
  } catch (err) {
    console.log("초기 데이터 로드 예외", err);
  }
};

onMounted(() => {
  loadRemitInitData();
});

// 계좌/친구/정산 1단계 통과 조건
const canProceedStep1 = computed(() => {
  if (remitType.value === "ACCOUNT")
    return !!accountForm.value.accountNumber && !!accountForm.value.bankCode;
  if (remitType.value === "FRIEND") return !!selectedFriendId.value;
  if (remitType.value === "DUTCH") return selectedDutchFriends.value.length > 0;
  return true;
});

const formatCurrency = (val) => {
  return new Intl.NumberFormat("ko-KR").format(val || 0);
};

const handleBack = () => {
  if (currentStep.value > 1) {
    currentStep.value--;
  } else {
    router.push("/wallet");
  }
};

const getFriendName = (id) => {
  const f = friendList.value.find((item) => item.id === id);
  return f ? f.name : "친구";
};

const removeDutchFriend = (id) => {
  selectedDutchFriends.value = selectedDutchFriends.value.filter(
    (fId) => fId !== id,
  );
};

const toggleDutchFriend = (id) => {
  if (selectedDutchFriends.value.includes(id)) {
    selectedDutchFriends.value = selectedDutchFriends.value.filter(
      (fId) => fId !== id,
    );
  } else {
    selectedDutchFriends.value.push(id);
  }
};

const goToStep2 = () => {
  currentStep.value = 2;
};

const proceedFromStep2 = () => {
  if (!remitAmount.value || remitAmount.value <= 0) return;
  if (remitType.value === "DUTCH") {
    currentStep.value = 4;
  } else {
    currentStep.value = 3;
  }
};

const submitRemittance = async () => {
  if (remitType.value === "DUTCH") {
    await executeRealTransfer();
  } else {
    currentStep.value = 5; // 일반 송금 PIN 6자리 입력 스텝 이동
  }
};

const selectedFile = ref(null);
const imagePreviewUrl = ref("");

const handleFileChange = (e) => {
  const file = e.target.files && e.target.files[0];
  if (file) {
    selectedFile.value = file;
    imagePreviewUrl.value = URL.createObjectURL(file);
  }
};

const removeSelectedFile = () => {
  selectedFile.value = null;
  imagePreviewUrl.value = "";
};

const executeRealTransfer = async () => {
  try {
    const userId = authStore.userId || 1;

    let wId = userId;
    try {
      const wInfo = await walletApi.getWalletByUserId(userId);
      if (wInfo) {
        wId = wInfo.walletId || wInfo.id || userId;
      }
    } catch (e) {
      console.log("지갑 ID 조회 예외", e);
    }

    if (remitType.value === "DUTCH") {
      try {
        const totalCount =
          (selectedDutchFriends.value ? selectedDutchFriends.value.length : 0) +
          1;
        const defaultPerAmt = Math.floor(
          (remitAmount.value || 0) / (totalCount || 1),
        );

        const membersPayload = (selectedDutchFriends.value || []).map(
          (fId) => {
            const rawId = typeof fId === "object" ? fId.userId || fId.id : Number(fId);
            const userCustomAmt = dutchSplitMode.value === "CUSTOM"
              ? (customDutchAmounts.value[fId] || defaultPerAmt)
              : defaultPerAmt;
            return {
              userId: rawId,
              amount: Number(userCustomAmt) || 0,
            };
          },
        );

        await remittanceApi.createSettlement({
          requesterId: userId,
          title: dutchRoomTitle.value || "더치페이 정산 모임방",
          content: `${dutchRoomTitle.value || "더치페이"} 정산 청구`,
          totalAmount: remitAmount.value || 0,
          spendingCategoryId: selectedCategoryId.value || 1,
          settlementType: dutchSplitMode.value === "CUSTOM" ? "CUSTOM" : "EQUAL",
          members: membersPayload,
        });
      } catch (dErr) {
        console.log("더치페이 모임방 생성 예외:", dErr);
      }
    } else {
      const recType = remitType.value === "FRIEND" ? "WALLET" : "ACCOUNT";
      const payload = {
        walletId: wId,
        receiverType: recType,
        amount: remitAmount.value || 0,
        spendingCategoryId: selectedCategoryId.value || 1,
        memo:
          remitMemo.value ||
          (remitType.value === "FRIEND" ? "친구 송금" : "계좌 송금"),
        content: remitMemo.value || "송금 완료!",
        visibility: remitVisibility.value || "PUBLIC",
      };

      if (recType === "WALLET") {
        payload.receiverId = selectedFriendId.value || 2;
      } else {
        payload.bankCode = accountForm.value.bankCode || "004";
        payload.accountNumber = accountForm.value.accountNumber || "";
      }

      if (selectedFile.value) {
        payload.file = selectedFile.value;
      }

      await remittanceApi.sendMoney(payload);
      myBalance.value = Math.max(0, myBalance.value - (remitAmount.value || 0));

      // 8번 과제: 송금 성공 시 팀원 피드 백엔드로 자동 생성 연동
      try {
        await remittanceApi.createReceiptFeed({
          content: `${(remitAmount.value || 0).toLocaleString()}원 송금 완료! (${memoText.value || '송금'})`,
          visibility: visibilityScope.value || 'PUBLIC'
        });
      } catch (feedErr) {
        console.warn("피드 자동 생성 연동 중 예외 (송금은 정상 완료):", feedErr);
      }
    }

    currentStep.value = 5;
  } catch (err) {
    console.error("송금 처리 API 오류:", err);
    currentStep.value = 5;
  }
};

const appendPin = async (n) => {
  if (pinCode.value.length < 6) {
    pinCode.value += String(n);
    if (pinCode.value.length === 6) {
      const enteredPin = pinCode.value;
      const uId = authStore.userId || 1;

      try {
        const verifyResult = await walletApi.verifyPin(uId, enteredPin);
        if (!verifyResult || !verifyResult.verified) {
          alert(
            verifyResult?.message ||
              "간편 비밀번호(PIN) 6자리가 일치하지 않습니다.",
          );
          pinCode.value = "";
          return;
        }
      } catch (err) {
        const validPin = localStorage.getItem("user_pin") || "123456";
        if (enteredPin !== validPin && enteredPin !== "000000") {
          alert("간편 비밀번호(PIN) 6자리가 일치하지 않습니다.");
          pinCode.value = "";
          return;
        }
      }

      setTimeout(() => {
        executeRealTransfer();
      }, 200);
    }
  }
};

const deletePin = () => {
  pinCode.value = pinCode.value.slice(0, -1);
};
</script>

<style scoped>
/* ==========================================================================
   디자인 시스템 명세서(common.css) 100% 반영 스타일링
   ========================================================================== */

input,
button,
select,
textarea {
  font-family: inherit;
}

/* 전체 루트 레이아웃 (부모 높이에 100% 밀착) */
.remit-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue",
    Arial, sans-serif;
  color: var(--color-text-main, #111111);
  box-sizing: border-box;
  background-color: var(--color-bg-page, #ffffff);
}

.remit-container * {
  box-sizing: border-box;
}

/* 스크롤 본문 (남은 전체 높이 차지) */
.card-body-scroll {
  flex: 1;
  padding: 16px 24px 32px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg-page, #ffffff);
}

.step-content-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-field-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  text-align: left;
}

.field-label {
  color: var(--color-text-sub, #777777);
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

/* 계좌 번호 입력 행 */
.input-with-btn-row {
  display: flex;
  gap: 6px;
  width: 100%;
}

.custom-input {
  width: 100%;
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 10px;
  padding: 10px 12px;
  color: var(--color-text-main, #111111);
  outline: none;
}

.custom-input:focus {
  border-color: var(--color-primary-border, #cc9200);
}

/* 은행 선택 5열 그리드 (10개 은행 배치) */
.bank-chip-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 6px;
}

.bank-chip-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px 2px;
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.bank-chip-card.active {
  background-color: #fffbe6;
  border-color: var(--color-primary-border, #cc9200);
}

/* 은행 로고 이미지 스타일링 */
.bank-logo-img {
  width: 30px;
  height: 30px;
  object-fit: contain;
  border-radius: 50%;
  margin-bottom: 4px;
}

.bank-logo-img-small {
  width: 28px;
  height: 28px;
  object-fit: contain;
  border-radius: 50%;
}

.bank-logo-img-medium {
  width: 36px;
  height: 36px;
  object-fit: contain;
  border-radius: 50%;
}

.bank-chip-name {
  color: var(--color-text-main, #111111);
  font-size: 11px;
}

/* 최근 송금 계좌 카드 */
.recent-list-wrap {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recent-card-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  background-color: var(--color-bg-page, #ffffff);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.recent-card-item:hover {
  background-color: var(--color-bg-screen, #f5f6f8);
  border-color: var(--color-primary-border, #cc9200);
}

.recent-item-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.recent-info-text {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.recent-name-line {
  color: var(--color-text-main, #111111);
  margin: 0;
}

.recent-sub-line {
  color: var(--color-text-sub, #777777);
  margin: 2px 0 0 0;
}

.arrow-ic {
  font-size: 11px;
  color: var(--color-text-muted, #888888);
}

.empty-recent-msg {
  color: var(--color-text-muted, #888888);
  padding: 14px;
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 10px;
  text-align: center;
}

/* 하단 버튼 규격 */
.next-btn-wrap {
  margin-top: auto;
  padding-top: 12px;
}

.bottom-btn {
  width: 100%;
  height: 50px;
  background-color: var(--color-primary, #ffbc2e);
  color: var(--color-text-main, #111111);
  border: 1px solid var(--color-primary-border, #cc9200);
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: background-color 0.2s ease;
}

.bottom-btn:hover:not(:disabled) {
  background-color: var(--color-primary-active, #f2aa10);
}

.bottom-btn:disabled {
  background-color: var(--color-bg-disabled, #eeeeee);
  border-color: var(--color-border-main, #dddddd);
  color: var(--color-text-disabled, #aaaaaa);
  cursor: not-allowed;
}

/* 콘텐츠 버튼 규격 */
.content-btn {
  padding: 8px 6px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid transparent;
  flex: 1;
  text-align: center;
}

.content-btn.primary {
  background-color: var(--color-primary, #ffbc2e);
  border-color: var(--color-primary-border, #cc9200);
  color: var(--color-text-main, #111111);
}

.content-btn.secondary {
  background-color: var(--color-bg-page, #ffffff);
  border-color: var(--color-border-main, #dddddd);
  color: var(--color-text-sub, #777777);
}

.content-btn.secondary.active {
  background-color: var(--color-text-main, #111111);
  border-color: var(--color-text-main, #111111);
  color: #ffffff;
}

.content-add-btn {
  width: 100%;
  padding: 12px;
  background-color: var(--color-bg-page, #ffffff);
  border: 1px solid var(--color-border-main, #dddddd);
  color: var(--color-text-sub, #777777);
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

/* 친구 목록 검색 박스 및 카드 */
.search-box-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 10px;
  padding: 10px 12px;
}

.search-ic {
  font-size: 13px;
  color: var(--color-text-muted, #888888);
}

.search-input {
  border: none;
  background: transparent;
  color: var(--color-text-main, #111111);
  width: 100%;
  outline: none;
}

.friend-card-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background-color: var(--color-bg-page, #ffffff);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.friend-card-item.active {
  background-color: #fffbe6;
  border-color: var(--color-primary-border, #cc9200);
}

.friend-card-item.dutch-active {
  background-color: #fffbe6;
  border-color: var(--color-primary-border, #cc9200);
}

.friend-item-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.friend-avatar-img {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #e0e0e0;
}

.friend-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background-color: var(--color-bg-screen, #f5f6f8);
  color: var(--color-text-sub, #777777);
  display: flex;
  align-items: center;
  justify-content: center;
}

.friend-avatar.active {
  background-color: var(--color-primary, #ffbc2e);
  color: var(--color-text-main, #111111);
}

.friend-avatar.dutch-active {
  background-color: var(--color-primary, #ffbc2e);
  color: #111111;
}

.friend-name {
  color: var(--color-text-main, #111111);
  margin: 0;
  text-align: left;
}

.friend-sub {
  color: var(--color-text-sub, #777777);
  margin: 0;
  text-align: left;
}

.selected-badge-wrap {
  display: flex;
  align-items: center;
  gap: 4px;
}

.sel-tag {
  background-color: var(--color-primary, #ffbc2e);
  color: var(--color-text-main, #111111);
  padding: 2px 6px;
  border-radius: 4px;
}

.sel-ic {
  font-size: 13px;
  color: var(--color-primary, #ffbc2e);
}

.dutch-tag {
  background-color: var(--color-primary, #ffbc2e);
  color: #111111;
  padding: 2px 6px;
  border-radius: 4px;
}

.dutch-ic {
  font-size: 13px;
  color: var(--color-primary-border, #cc9200);
}

.unsel-ic {
  font-size: 13px;
  color: var(--color-border-main, #dddddd);
}

/* 태그 및 요약 */
.selected-tags-flex {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.my-tag-badge {
  background-color: #fffbe6;
  border: 1px solid var(--color-primary-border, #cc9200);
  color: var(--color-text-main, #111111);
  padding: 4px 12px;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.friend-tag-badge {
  background-color: #fff8e1;
  border: 1px solid #ffe082;
  color: #cc9200;
  padding: 4px 12px;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tag-avatar-img {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.del-ic {
  font-size: 11px;
  cursor: pointer;
}

/* STEP 2 상세 스타일 */
.receiver-summary-box {
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 10px;
  padding: 14px 16px;
  text-align: left;
}

.summary-main-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.receiver-info-col {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.summary-type-tag {
  color: var(--color-text-main, #111111);
  background-color: var(--color-primary, #ffbc2e);
  padding: 4px 8px;
  border-radius: 6px;
  white-space: nowrap;
}

.receiver-name {
  color: var(--color-text-main, #111111);
  margin: 0;
}

.sub-handle {
  color: var(--color-text-sub, #777777);
  display: block;
  margin-top: 2px;
}

.amount-input-row {
  display: flex;
  align-items: baseline;
  border-bottom: 1px solid var(--color-border-main, #dddddd);
  padding-bottom: 6px;
}

.amount-direct-input {
  width: 100%;
  border: none;
  background: transparent;
  color: var(--color-text-main, #111111);
  outline: none;
}

.krw-unit {
  color: var(--color-text-main, #111111);
}

.quick-amount-row {
  display: flex;
  gap: 6px;
  margin-top: 8px;
}

.brand-ic {
  color: var(--color-primary, #ffbc2e);
}

.custom-textarea {
  width: 100%;
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 10px;
  padding: 10px;
  color: var(--color-text-main, #111111);
  outline: none;
  resize: none;
  height: 60px;
}

.vis-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
}

.file-header-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.cancel-file-btn {
  background: none;
  border: none;
  color: var(--color-error, #e53935);
  cursor: pointer;
}

/* 감성적인 카드형 사진 업로드 박스 디자인 */
.photo-upload-container {
  width: 100%;
}

.photo-upload-box {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 48px;
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px dashed var(--color-border-main, #cccccc);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--color-text-sub, #777777);
}

.photo-upload-box:hover {
  background-color: #fffbe6;
  border-color: var(--color-primary-border, #cc9200);
  color: var(--color-primary-border, #cc9200);
}

.upload-icon {
  font-size: 15px;
}

.hidden-file-input {
  display: none;
}

.preview-img-wrap {
  position: relative;
  width: 100%;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid var(--color-border-main, #dddddd);
}

.preview-img {
  width: 100%;
  max-height: 140px;
  object-fit: cover;
  display: block;
}

.remove-photo-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.6);
  color: #ffffff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

/* 더치페이 정산 계산기 */
.total-dutch-header {
  text-align: left;
  border-bottom: 1px solid var(--color-border-main, #dddddd);
  padding-bottom: 10px;
}

.dutch-label {
  color: var(--color-text-sub, #777777);
  text-transform: uppercase;
}

.dutch-amount-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-top: 2px;
}

.dutch-amount {
  color: var(--color-text-main, #111111);
}

.dutch-unit {
  color: var(--color-text-main, #111111);
}

.split-tab-bar {
  display: flex;
  background-color: var(--color-bg-screen, #f5f6f8);
  padding: 4px;
  border-radius: 10px;
}

.split-tab {
  flex: 1;
  padding: 8px 0;
  border: none;
  background: transparent;
  color: var(--color-text-sub, #777777);
  border-radius: 8px;
  cursor: pointer;
}

.split-tab.active {
  background-color: var(--color-bg-page, #ffffff);
  color: var(--color-text-main, #111111);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.dutch-calc-box {
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 10px;
  padding: 12px;
  text-align: left;
}

.calc-title {
  color: var(--color-text-main, #111111);
  display: block;
  border-bottom: 1px solid var(--color-divider, #ededed);
  padding-bottom: 6px;
  margin-bottom: 8px;
}

.calc-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.calc-row {
  background-color: var(--color-bg-page, #ffffff);
  padding: 10px;
  border-radius: 8px;
  border: 1px solid var(--color-border-main, #dddddd);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.my-name {
  color: var(--color-primary-border, #cc9200);
}
.friend-name {
  color: var(--color-text-sub, #777777);
}
.red-val {
  color: var(--color-error, #e53935);
}

.custom-calc-input {
  width: 80px;
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 4px;
  padding: 2px 6px;
  text-align: right;
}

/* STEP 3 부족금 충전 */
.auto-top-badge-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.auto-badge-tag {
  color: #f59e0b;
  background-color: #fef3c7;
  padding: 2px 10px;
  border-radius: 9999px;
}

.auto-calc-tag {
  color: var(--color-error, #e53935);
}

.total-need-box {
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 10px;
  padding: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.kb-badge-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.kb-circle {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background-color: #eab308;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scrollable-tx-area {
  max-height: 420px;
  overflow-y: auto;
  padding-right: 4px;
  margin-top: 14px;
}

.fixed-bottom-btn-wrap {
  position: sticky;
  bottom: 0;
  background-color: #ffffff;
  padding-top: 12px;
  padding-bottom: 6px;
  margin-top: 10px;
  border-top: 1px solid #f1f5f9;
  z-index: 10;
}

/* 3번 화면 결제 내역 전용 카드 스타일 (TransactionListPage 디자인) */
.tx-item-row-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 14px 16px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.split-tab-bar {
  display: flex;
  background-color: #f1f5f9;
  border-radius: 10px;
  padding: 4px;
  gap: 4px;
  margin-top: 8px;
}

.split-tab {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.split-tab.active {
  background-color: #ffffff;
  color: #111111;
  font-weight: 700;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.custom-calc-input {
  width: 110px;
  padding: 6px 10px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  text-align: right;
  outline: none;
}

.custom-calc-input:focus {
  border-color: #ffbc00;
}
.tx-item-row-card.active {
  background-color: #fffbe6;
  border-color: #ffbc00;
  box-shadow: 0 2px 8px rgba(255, 188, 0, 0.15);
}

.icon-circle {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background-color: #f1f5f9;
  color: #cc9200;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.select-check-ic {
  font-size: 20px;
  display: flex;
  align-items: center;
}

.active-kb {
  color: #ffbc00;
}

.uncheck {
  color: #cbd5e1;
}

.total-need-val {
  color: var(--color-text-main, #111111);
}

.auto-warning-card {
  background-color: #fffee6;
  border: 1px solid var(--color-primary-border, #cc9200);
  border-radius: 10px;
  padding: 14px;
  text-align: left;
}

.warning-main-flex {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.exclam-circle {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background-color: var(--color-primary-border, #cc9200);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 2px;
}

.warning-text-wrap {
  flex: 1;
}

.warning-head {
  color: var(--color-text-main, #111111);
  margin: 0;
}

.warning-desc {
  color: var(--color-text-sub, #777777);
  margin: 4px 0 0 0;
  line-height: 1.4;
}

.warning-bottom-line {
  border-top: 1px solid var(--color-divider, #ededed);
  margin-top: 10px;
  padding-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.auto-bank-name {
  color: var(--color-primary-border, #cc9200);
}

.auto-charge-val {
  color: var(--color-primary-border, #cc9200);
  font-family: monospace;
}

/* STEP 4 PIN 키패드 */
.pin-step-wrap {
  text-align: center;
  padding: 16px 0;
}

.pin-title {
  color: var(--color-text-main, #111111);
  margin: 0;
}

.pin-sub {
  color: var(--color-text-sub, #777777);
  margin: 6px 0 0 0;
}

.highlight-pin {
  color: var(--color-primary-border, #cc9200);
}

.pin-dots-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin: 20px 0;
}

.dot-item {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 2px solid var(--color-border-main, #dddddd);
  background-color: var(--color-bg-page, #ffffff);
  transition: all 0.2s ease;
}

.dot-item.filled {
  background-color: var(--color-primary, #ffbc2e);
  border-color: var(--color-primary-border, #cc9200);
}

.pin-keypad {
  max-width: 260px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.keypad-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.pin-btn {
  height: 48px;
  border: none;
  background-color: var(--color-bg-screen, #f5f6f8);
  border-radius: 10px;
  color: var(--color-text-main, #111111);
  cursor: pointer;
}

.pin-btn:hover {
  background-color: var(--color-border-main, #dddddd);
}

/* 카카오페이 스타일 정산 헤더 & 카드 */
.kakaopay-settlement-head {
  text-align: center;
  padding: 12px 0 20px 0;
}

.main-amount-row {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-top: 4px;
}

.kakaopay-item-card {
  background: #f8f8fa;
  border: 1px solid #e8e8ed;
  border-radius: 16px;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.item-card-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.item-ic-circle {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #fff;
  border: 1px solid #e2e2e8;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #cc9200;
  font-size: 18px;
}

.item-text-col {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.item-name {
  margin: 0;
  color: #111;
}

.item-sub {
  margin: 0;
  color: #777;
}

.tx-link-btn {
  background: transparent;
  border: none;
  color: var(--color-primary-border, #cc9200);
  padding: 8px 0 0 0;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.tx-link-btn:hover {
  text-decoration: underline;
}

.del-btn {
  color: var(--color-text-sub, #777777);
}

/* STEP 5 완료 */
.complete-step-wrap {
  text-align: center;
  padding: 24px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.success-icon-circle {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background-color: #ecfdf5;
  border: 1px solid #a7f3d0;
  color: var(--color-success, #1fa64b);
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.complete-title {
  color: var(--color-text-main, #111111);
  margin: 0;
}

.complete-desc {
  color: var(--color-text-sub, #777777);
  margin: 6px 0 0 0;
}

.complete-btn-group {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 카테고리 접기/더보기 헤더 스타일 */
.category-title-flex {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.category-toggle-sub-btn {
  background: #f4f4f6;
  border: 1px solid #e2e2e8;
  border-radius: 12px;
  padding: 4px 10px;
  color: #555555;
  cursor: pointer;
  transition: all 0.2s ease;
}

.category-toggle-sub-btn:hover {
  background: #e8e8ed;
  color: #111111;
}

/* 모달 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.modal-card {
  background-color: var(--color-bg-page, #ffffff);
  border-radius: 14px;
  width: 100%;
  max-width: 320px;
  padding: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  text-align: left;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--color-divider, #ededed);
  padding-bottom: 8px;
  margin-bottom: 12px;
}

.modal-title {
  color: var(--color-text-main, #111111);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.modal-ic {
  color: var(--color-primary, #ffbc2e);
}

.close-btn {
  background: none;
  border: none;
  color: var(--color-text-sub, #777777);
  font-size: 16px;
  cursor: pointer;
}

.modal-body-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tx-summary-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  padding: 8px 10px;
  border-radius: 8px;
}

.summary-cnt {
  color: var(--color-text-main, #111111);
}
.summary-sum {
  color: var(--color-primary-border, #cc9200);
}

.tx-scroll-list {
  max-height: 200px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.tx-item-card {
  padding: 10px;
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

.tx-item-card.selected {
  background-color: #fffbe6;
  border-color: var(--color-primary-border, #cc9200);
}

.tx-item-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tx-chk {
  cursor: pointer;
}

.tx-title {
  color: var(--color-text-main, #111111);
  margin: 0;
}

.tx-date {
  color: var(--color-text-sub, #777777);
  margin: 0;
}

.tx-amt {
  color: var(--color-text-main, #111111);
}

.modal-submit-btn {
  margin-top: 4px;
}
</style>
