<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import { deleteProfileImage, getProfile, getProfileImage, updateProfile, updateProfileImage } from '@/api/profileApi';

const router = useRouter();

const profile = reactive(/** @type {{ nickname: string, introduction: string }} */ ({ nickname: '', introduction: '' }));

const originalProfile = reactive(/** @type {{ nickname: string, introduction: string }} */ ({ nickname: '', introduction: '' }));

const imageInput = ref(/** @type {HTMLInputElement | null} */ (null));
const nicknameInput = ref(/** @type {HTMLInputElement | null} */ (null));
const profileImage = ref('');

const loading = ref(false);
const saving = ref(false);

const imageUploading = ref(false);
const imageDeleting = ref(false);
const imageActionSheetOpen = ref(false);

const nicknameError = ref('');
const imageError = ref('');
const errorMessage = ref('');

// 저장 성공 모달
const successModalOpen = ref(false);

const confirmModal = reactive({
  open: false,
  type: '',
  title: '',
  message: '',
  confirmText: '확인',
  cancelText: '취소',
  danger: false,
});

const openConfirmModal = ({
                            type,
                            title,
                            message,
                            confirmText = '확인',
                            cancelText = '취소',
                            danger = false,
                          }) => {
  confirmModal.type = type;
  confirmModal.title = title;
  confirmModal.message = message;
  confirmModal.confirmText = confirmText;
  confirmModal.cancelText = cancelText;
  confirmModal.danger = danger;
  confirmModal.open = true;
};

const closeConfirmModal = () => {
  if (imageProcessing.value) return;

  confirmModal.open = false;
  confirmModal.type = '';
};

const handleConfirmModal = async () => {
  if (confirmModal.type === 'DELETE_IMAGE') {
    await confirmRemoveProfileImage();
    return;
  }

  if (confirmModal.type === 'CANCEL_EDIT') {
    confirmModal.open = false;
    confirmModal.type = '';

    await router.push('/setting');
  }
};

// 이미지 작업 중 여부
const imageProcessing = computed(() => imageUploading.value || imageDeleting.value);

// 저장 가능 여부
const canSave = computed(() => profile.nickname.length > 0 && !nicknameError.value);

// 프로필 이미지 URL 갱신
const refreshProfileImage = async () => {
  profileImage.value = await getProfileImage();
};

// 이미지 메뉴 열기
const openImageActionSheet = () => {
  if (imageProcessing.value) return;

  imageActionSheetOpen.value = true;
};

// 이미지 메뉴 닫기
const closeImageActionSheet = () => {
  if (imageProcessing.value) return;

  imageActionSheetOpen.value = false;
};

// 이미지 선택창 열기
const selectImage = () => {
  if (imageProcessing.value) return;

  imageActionSheetOpen.value = false;
  imageInput.value?.click();
};

// 이미지 파일 검증
const validateImage = (file) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];

  const maxImageSize = 10 * 1024 * 1024;

  if (!allowedTypes.includes(file.type)) {
    imageError.value = 'JPG, PNG, GIF, WEBP 이미지만 등록할 수 있어요.';

    return false;
  }

  if (file.size > maxImageSize) {
    imageError.value = '프로필 이미지는 10MB 이하만 등록할 수 있어요.';

    return false;
  }

  imageError.value = '';

  return true;
};

// 프로필 이미지 등록 및 변경
const changeImage = async (event) => {
  const file = event.target.files?.[0];

  if (!file) return;

  if (!validateImage(file)) {
    event.target.value = '';
    return;
  }

  try {
    imageUploading.value = true;
    imageError.value = '';
    errorMessage.value = '';

    await updateProfileImage(file);
    await refreshProfileImage();
  } catch (error) {
    console.error(error);

    imageError.value = error.response?.data?.message || '프로필 이미지 변경에 실패했습니다.';
  } finally {
    imageUploading.value = false;

    if (imageInput.value) {
      imageInput.value.value = '';
    }
  }
};

// 프로필 이미지 삭제
const removeProfileImage = () => {
  if (imageProcessing.value) return;

  imageActionSheetOpen.value = false;

  openConfirmModal({
    type: 'DELETE_IMAGE',
    title: '프로필 사진 삭제',
    message: '등록된 프로필 사진을 삭제하고 기본 사진으로 변경할까요?',
    confirmText: '삭제',
    cancelText: '취소',
    danger: true,
  });
};

const confirmRemoveProfileImage = async () => {
  if (imageProcessing.value) return;

  try {
    imageDeleting.value = true;
    imageError.value = '';
    errorMessage.value = '';

    await deleteProfileImage();
    await refreshProfileImage();

    confirmModal.open = false;
    confirmModal.type = '';
  } catch (error) {
    console.error(error);

    imageError.value =
        error.response?.data?.message ||
        '프로필 이미지 삭제에 실패했습니다.';
  } finally {
    imageDeleting.value = false;
  }
};

// 닉네임 검증
const validateNickname = () => {
  const nicknamePattern = /^[가-힣a-z0-9_]{1,15}$/;

  if (!profile.nickname) {
    nicknameError.value = '닉네임을 입력해 주세요.';

    return false;
  }

  if (!nicknamePattern.test(profile.nickname)) {
    nicknameError.value = '한글, 영문 소문자, 숫자, 밑줄만 사용할 수 있어요.';

    return false;
  }

  nicknameError.value = '';

  return true;
};

// 닉네임 오류 초기화
const clearNicknameError = () => {
  nicknameError.value = '';
  errorMessage.value = '';
};

// 프로필 조회
const loadProfile = async () => {
  try {
    loading.value = true;
    errorMessage.value = '';

    const data = await getProfile();

    profile.nickname = data.nickname || '';

    profile.introduction = data.introduction || '';

    originalProfile.nickname = profile.nickname;

    originalProfile.introduction = profile.introduction;

    await refreshProfileImage();
  } catch (error) {
    console.error(error);

    errorMessage.value = error.response?.data?.message || '프로필 정보를 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

// 프로필 저장
const saveProfile = async () => {
  if (!validateNickname()) return;

  try {
    saving.value = true;
    errorMessage.value = '';

    const profileData = { nickname: profile.nickname.trim(), introduction: profile.introduction.trim() };

    await updateProfile(profileData);

    originalProfile.nickname = profileData.nickname;

    originalProfile.introduction = profileData.introduction;

    // 브라우저 alert 대신 성공 모달 표시
    successModalOpen.value = true;
  } catch (error) {
    console.error(error);

    const message = error.response?.data?.message || '프로필 수정에 실패했습니다.';

    if (message === '이미 사용 중인 닉네임입니다.') {
      nicknameError.value = message;
      nicknameInput.value?.setSelectionRange(profile.nickname.length, profile.nickname.length);
    } else errorMessage.value = message;
  } finally {
    saving.value = false;
  }
};

// 성공 모달 확인
const closeSuccessModal = async () => {
  successModalOpen.value = false;

  await router.replace('/setting');
};

// 수정 여부 확인
const isProfileChanged = () => profile.nickname !== originalProfile.nickname || profile.introduction !== originalProfile.introduction;

// 수정 취소
const cancel = async () => {
  if (isProfileChanged()) {
    openConfirmModal({
      type: 'CANCEL_EDIT',
      title: '수정을 취소할까요?',
      message: '수정한 내용은 저장되지 않아요.',
      confirmText: '나가기',
      cancelText: '계속 수정',
      danger: false,
    });

    return;
  }

  await router.push('/setting');
};

// 설정 메인 화면
const goBack = () => {
  if (isProfileChanged()) {
    openConfirmModal({
      type: 'CANCEL_EDIT',
      title: '수정을 취소할까요?',
      message: '수정한 내용은 저장되지 않아요.',
      confirmText: '나가기',
      cancelText: '계속 수정',
      danger: false,
    });

    return;
  }

  router.push('/setting');
};

onMounted(() => {
  loadProfile();
});
</script>

<template>
  <main class="page-layout profile-page">
    <PageHeader
        title="프로필 관리"
        custom-back
        @back="goBack"
    />

    <div class="page-content profile-content">
      <section class="title-section">
        <h2 class="text-26-bold">
          내 정보를 확인해 주세요
        </h2>

        <p class="text-15">
          프로필 사진과 소개는 자유롭게 변경할 수 있어요.
        </p>
      </section>

      <!-- 프로필 이미지 -->
      <section class="profile-image-section">
        <div
            class="profile-image-wrap"
            :class="{ disabled: imageProcessing }"
            role="button"
            aria-label="프로필 사진 수정 메뉴 열기"
            @click="openImageActionSheet"
        >
          <img
              :key="profileImage"
              :src="profileImage"
              alt="프로필 이미지"
              class="profile-image"
          />

          <button
              aria-label="프로필 사진 수정 메뉴 열기"
              class="image-edit-button"
              :disabled="imageProcessing"
              type="button"
              @click.stop="openImageActionSheet"
          >
            <span
                v-if="imageProcessing"
                class="mini-spinner"
            ></span>

            <i
                v-else
                class="fa-solid fa-camera"
            ></i>
          </button>
        </div>

        <input
            ref="imageInput"
            accept="image/jpeg,image/png,image/gif,image/webp"
            class="hidden-image-input"
            type="file"
            @change="changeImage"
        />

        <p
            v-if="imageError"
            class="image-error text-13"
        >
          {{ imageError }}
        </p>
      </section>

      <!-- 프로필 입력 폼 -->
      <form
          class="profile-form"
          @submit.prevent="saveProfile"
      >
        <!-- 닉네임 -->
        <section class="input-card">
          <label
              for="nickname"
              class="text-15-bold"
          >
            닉네임
          </label>

          <p class="input-guide text-13">
            다른 사용자에게 표시되는 이름이에요.
          </p>

          <div
              :class="{
                error: !!nicknameError,
              }"
              class="input-area"
          >
            <input
                id="nickname"
                ref="nicknameInput"
                v-model.trim="profile.nickname"
                maxlength="15"
                placeholder="닉네임을 입력해 주세요"
                type="text"
                @input="clearNicknameError"
            />

            <span class="text-13">
              {{ profile.nickname.length }}/15
            </span>
          </div>

          <p
              v-if="nicknameError"
              class="field-error text-13"
          >
            {{ nicknameError }}
          </p>
        </section>

        <!-- 소개 -->
        <section class="input-card introduction-card">
          <label
              for="introduction"
              class="text-15-bold"
          >
            소개
          </label>

          <p class="input-guide text-13">
            나를 간단히 소개해 보세요!
          </p>

          <div class="textarea-area">
            <textarea
                id="introduction"
                v-model="profile.introduction"
                maxlength="100"
                placeholder="소개를 입력해 주세요"
                class="text-15"
            ></textarea>

            <span class="text-13">
              {{ profile.introduction.length }}/100
            </span>
          </div>
        </section>

        <p
            v-if="errorMessage"
            class="page-error text-13"
        >
          {{ errorMessage }}
        </p>

        <!-- 하단 버튼 -->
        <div class="bottom-btn-area double button-area">
          <button
              class="bottom-btn cancel-button"
              :disabled="
                saving ||
                imageProcessing
              "
              type="button"
              @click="cancel"
          >
            취소
          </button>

          <button
              class="bottom-btn save-button"
              :disabled="
                loading ||
                saving ||
                imageProcessing ||
                !canSave
              "
              type="submit"
          >
            {{
              saving
                  ? '저장 중...'
                  : '저장하기'
            }}
          </button>
        </div>
      </form>
    </div>

    <!-- 프로필 이미지 액션시트 -->
    <transition name="action-sheet">
      <div
          v-if="imageActionSheetOpen"
          class="action-sheet-overlay"
          role="presentation"
          @click.self="closeImageActionSheet"
      >
        <section
            aria-label="프로필 사진 수정 메뉴"
            aria-modal="true"
            class="action-sheet"
            role="dialog"
        >
          <div class="action-sheet-handle"></div>

          <div class="action-sheet-header">
            <h3 class="text-18-bold">
              프로필 사진 수정
            </h3>
          </div>

          <div class="action-sheet-menu">
            <button
                class="action-sheet-button"
                :disabled="imageProcessing"
                type="button"
                @click="selectImage"
            >
              <span class="menu-icon">
                <i class="fa-regular fa-image"></i>
              </span>

              <span class="menu-text">
                <strong class="text-15-bold">
                  사진 선택
                </strong>

                <small class="text-13">
                  라이브러리에서 사진 가져오기
                </small>
              </span>

              <i
                  class="fa-solid fa-chevron-right menu-arrow"
              ></i>
            </button>

            <button
                class="action-sheet-button delete-action"
                :disabled="imageProcessing"
                type="button"
                @click="removeProfileImage"
            >
              <span class="menu-icon">
                <i class="fa-regular fa-trash-can"></i>
              </span>

              <span class="menu-text">
                <strong class="text-15-bold">
                  프로필 사진 삭제
                </strong>

                <small class="text-13">
                  기본 프로필 이미지로 변경
                </small>
              </span>

              <i
                  class="fa-solid fa-chevron-right menu-arrow"
              ></i>
            </button>
          </div>

          <button
              class="action-sheet-cancel text-15-bold"
              :disabled="imageProcessing"
              type="button"
              @click="closeImageActionSheet"
          >
            취소
          </button>
        </section>
      </div>
    </transition>

    <!-- 공통 확인 모달 -->
    <transition name="confirm-modal">
      <div
          v-if="confirmModal.open"
          class="confirm-modal-overlay"
          @click.self="closeConfirmModal"
      >
        <section
            aria-modal="true"
            class="confirm-modal"
            role="dialog"
        >
          <div class="confirm-icon-wrap">
            <div
                class="confirm-icon"
                :class="{ danger: confirmModal.danger }"
            >
              <i
                  v-if="confirmModal.type === 'DELETE_IMAGE'"
                  class="fa-regular fa-trash-can"
              ></i>

              <i
                  v-else
                  class="fa-solid fa-exclamation"
              ></i>
            </div>
          </div>

          <div class="confirm-content">
            <h3 class="text-20-bold">
              {{ confirmModal.title }}
            </h3>

            <p class="text-14">
              {{ confirmModal.message }}
            </p>
          </div>

          <div class="confirm-buttons">
            <button
                class="confirm-cancel-button text-15-bold"
                :disabled="imageProcessing"
                type="button"
                @click="closeConfirmModal"
            >
              {{ confirmModal.cancelText }}
            </button>

            <button
                class="confirm-action-button text-15-bold"
                :class="{ danger: confirmModal.danger }"
                :disabled="imageProcessing"
                type="button"
                @click="handleConfirmModal"
            >
              {{
                imageDeleting &&
                confirmModal.type === 'DELETE_IMAGE'
                    ? '삭제 중...'
                    : confirmModal.confirmText
              }}
            </button>
          </div>
        </section>
      </div>
    </transition>

    <!-- 프로필 수정 성공 모달 -->
    <transition name="success-modal">
      <div
          v-if="successModalOpen"
          class="success-modal-overlay"
      >
        <section
            aria-labelledby="profile-success-title"
            aria-modal="true"
            class="success-modal"
            role="dialog"
        >
          <!-- 성공 아이콘 -->
          <div class="success-icon-wrap">
            <div class="success-icon">
              <i class="fa-solid fa-check"></i>
            </div>
          </div>

          <!-- 성공 메시지 -->
          <div class="success-content">
            <h3
                id="profile-success-title"
                class="text-20-bold"
            >
              프로필 수정 완료
            </h3>

            <p class="text-14">
              프로필이 수정되었어요.
            </p>
          </div>

          <!-- 확인 -->
          <button
              class="success-confirm-button text-15-bold"
              type="button"
              @click="closeSuccessModal"
          >
            확인
          </button>
        </section>
      </div>
    </transition>

    <!-- 로딩 -->
    <div
        v-if="loading"
        class="loading-overlay"
    >
      <div class="loading-spinner"></div>

      <span class="text-15-bold">
        프로필 정보를 불러오고 있어요.
      </span>
    </div>
  </main>
</template>

<style scoped>
.profile-page {
  position: relative;
  background: var(--color-bg-page);
}

.profile-content {
  padding-bottom: 84px;
}

/* 제목 */
.title-section {
  margin-top: 38px;
}

.title-section h2 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
  letter-spacing: -0.8px;
}

.title-section p {
  margin: 14px 0 0;
  color: var(--color-text-muted);
  line-height: 1.5;
}

/* 프로필 이미지 */
.profile-image-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 30px;
}

.profile-image-wrap {
  position: relative;
  width: 104px;
  height: 104px;
  border-radius: 50%;
  cursor: pointer;
}

.profile-image-wrap.disabled {
  cursor: wait;
}

.profile-image {
  display: block;
  width: 104px;
  height: 104px;
  border: 1px solid var(--color-divider);
  border-radius: 50%;
  background: var(--color-bg-screen);
  object-fit: cover;
}

.image-edit-button {
  position: absolute;
  right: -2px;
  bottom: 1px;
  display: flex;
  width: 34px;
  height: 34px;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 2px solid var(--color-bg-page);
  border-radius: 50%;
  background: var(--color-text-main);
  color: var(--color-text-white);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.18);
  cursor: pointer;
  transition: transform 0.15s ease;
}

.image-edit-button:hover:not(:disabled) {
  transform: scale(1.05);
}

.image-edit-button:active:not(:disabled) {
  transform: scale(0.95);
}

.image-edit-button:disabled {
  cursor: wait;
  opacity: 0.75;
}

.image-edit-button i {
  font-size: 14px;
}

.mini-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.45);
  border-top-color: var(--color-text-white);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

.hidden-image-input {
  display: none;
}

.image-error {
  margin: 8px 0 0;
  color: var(--color-error);
  line-height: 1.4;
  text-align: center;
}

/* 프로필 폼 */
.profile-form {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
  margin-top: 28px;
}

/* 입력 카드 */
.input-card {
  padding: 18px;
  border: 1px solid var(--color-divider);
  border-radius: 16px;
  background: var(--color-bg-page);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
}

.introduction-card {
  margin-top: 16px;
}

.input-card label {
  display: block;
  color: var(--color-text-main);
}

.input-guide {
  margin: 7px 0 12px;
  color: var(--color-text-muted);
  line-height: 1.5;
}

/* 닉네임 */
.input-area {
  display: flex;
  height: 54px;
  align-items: center;
  padding: 0 14px;
  border: 1px solid var(--color-border-main);
  border-radius: 10px;
  background: var(--color-bg-page);
  box-sizing: border-box;
}

.input-area:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.input-area.error {
  border-color: var(--color-error);
}

.input-area.error:focus-within {
  border-color: var(--color-error);
  box-shadow: none;
}

.input-area input {
  min-width: 0;
  flex: 1;
  border: 0;
  background: transparent;
  color: var(--color-text-main);
  font-family: inherit;
  font-size: 15px;
  font-weight: 500;
  outline: none;
}

.input-area input::placeholder {
  color: var(--color-text-disabled);
}

.input-area span {
  flex: none;
  margin-left: 8px;
  color: var(--color-text-muted);
}

/* 소개 */
.textarea-area {
  position: relative;
  height: 108px;
  padding: 14px 14px 30px;
  border: 1px solid var(--color-border-main);
  border-radius: 10px;
  background: var(--color-bg-page);
  box-sizing: border-box;
}

.textarea-area:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.textarea-area textarea {
  width: 100%;
  height: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--color-text-main);
  font-family: inherit;
  line-height: 1.5;
  outline: none;
  resize: none;
}

.textarea-area textarea::placeholder {
  color: var(--color-text-disabled);
}

.textarea-area span {
  position: absolute;
  right: 14px;
  bottom: 10px;
  color: var(--color-text-muted);
}

/* 오류 */
.field-error {
  margin: 8px 0 0;
  color: var(--color-error);
  line-height: 1.4;
}

.page-error {
  margin: 12px 0 0;
  color: var(--color-error);
  line-height: 1.5;
  text-align: center;
}

/* 하단 버튼 */
.button-area {
  position: absolute;
  right: 24px;
  bottom: 32px;
  left: 24px;
}

.cancel-button {
  border: 1px solid var(--color-border-main);
  background: var(--color-bg-page);
  color: var(--color-text-main);
}

.save-button {
  border: 1px solid var(--color-primary-border);
  background: var(--color-primary);
  color: var(--color-text-main);
}

.cancel-button:active:not(:disabled) {
  background: var(--color-bg-screen);
}

.save-button:active:not(:disabled) {
  background: var(--color-primary-active);
}

.cancel-button:disabled,
.save-button:disabled {
  border-color: var(--color-border-main);
  background: var(--color-bg-disabled);
  color: var(--color-text-disabled);
  cursor: not-allowed;
}

/* 이미지 액션시트 */
.action-sheet-overlay {
  position: absolute;
  z-index: 100;
  inset: 0;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 14px;
  background: rgba(15, 15, 15, 0.48);
  backdrop-filter: blur(3px);
  box-sizing: border-box;
}

.action-sheet {
  width: 100%;
  padding: 10px 12px 12px;
  border-radius: 22px;
  background: var(--color-bg-screen);
  box-shadow: 0 -10px 35px rgba(0, 0, 0, 0.18);
  box-sizing: border-box;
}

.action-sheet-handle {
  width: 38px;
  height: 4px;
  margin: 0 auto 14px;
  border-radius: 999px;
  background: var(--color-border-main);
}

.action-sheet-header {
  padding: 0 8px 14px;
}

.action-sheet-header h3 {
  margin: 0;
  color: var(--color-text-main);
}

.action-sheet-menu {
  overflow: hidden;
  border: 1px solid var(--color-divider);
  border-radius: 16px;
  background: var(--color-bg-page);
}

.action-sheet-button {
  display: flex;
  width: 100%;
  min-height: 72px;
  align-items: center;
  padding: 12px 16px;
  border: 0;
  background: var(--color-bg-page);
  text-align: left;
  cursor: pointer;
  transition: background 0.15s ease;
}

.action-sheet-button + .action-sheet-button {
  border-top: 1px solid var(--color-divider);
}

.action-sheet-button:hover:not(:disabled),
.action-sheet-button:active:not(:disabled) {
  background: var(--color-bg-screen);
}

.action-sheet-button:disabled {
  cursor: wait;
  opacity: 0.5;
}

.menu-icon {
  display: flex;
  flex: none;
  width: 28px;
  align-items: center;
  justify-content: center;
  margin-right: 13px;
  color: var(--color-text-main);
  font-size: 20px;
}

.menu-text {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 4px;
}

.menu-text strong {
  color: var(--color-text-main);
}

.menu-text small {
  color: var(--color-text-muted);
}

.delete-action .menu-icon,
.delete-action .menu-text strong,
.delete-action .menu-arrow {
  color: var(--color-error);
}

.menu-arrow {
  flex: none;
  margin-left: 8px;
  color: var(--color-text-disabled);
  font-size: 13px;
}

.action-sheet-cancel {
  width: 100%;
  height: 54px;
  margin-top: 10px;
  border: 1px solid var(--color-border-main);
  border-radius: 16px;
  background: var(--color-bg-page);
  color: var(--color-text-main);
  cursor: pointer;
}

.action-sheet-cancel:active:not(:disabled) {
  background: var(--color-bg-screen);
}

.action-sheet-cancel:disabled {
  cursor: wait;
  opacity: 0.5;
}

/* 액션시트 애니메이션 */
/*noinspection CssUnusedSymbol*/
.action-sheet-enter-active,
.action-sheet-leave-active {
  transition: opacity 0.2s ease;
}

.action-sheet-enter-active .action-sheet,
.action-sheet-leave-active .action-sheet {
  transition: transform 0.25s ease;
}

/*noinspection CssUnusedSymbol*/
.action-sheet-enter-from,
.action-sheet-leave-to {
  opacity: 0;
}

.action-sheet-enter-from .action-sheet,
.action-sheet-leave-to .action-sheet {
  transform: translateY(100%);
}

/* ========================================
   프로필 저장 성공 모달
======================================== */
.success-modal-overlay {
  position: absolute;
  z-index: 300;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 15, 15, 0.42);
  backdrop-filter: blur(3px);
  -webkit-backdrop-filter: blur(3px);
  box-sizing: border-box;
}

.success-modal {
  width: 100%;
  max-width: 320px;
  padding: 28px 22px 20px;
  border-radius: 22px;
  background: var(--color-bg-page);
  box-shadow: 0 20px 55px rgba(0, 0, 0, 0.2);
  box-sizing: border-box;
  text-align: center;
}

/* 성공 아이콘 */
.success-icon-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 18px;
}

.success-icon {
  display: flex;
  width: 62px;
  height: 62px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 188, 46, 0.16);
  color: var(--color-primary-border);
  font-size: 25px;
  animation: success-icon-pop 0.4s
  cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* 성공 문구 */
.success-content h3 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.4;
}

.success-content p {
  margin: 9px 0 0;
  color: var(--color-text-sub);
  font-weight: 400;
  line-height: 1.5;
}

/* 확인 버튼 */
.success-confirm-button {
  width: 100%;
  height: 50px;
  margin-top: 24px;
  border: 0;
  border-radius: 14px;
  background: var(--color-primary);
  color: var(--color-text-main);
  cursor: pointer;
  transition:
      background 0.15s ease,
      transform 0.15s ease;
}

.success-confirm-button:active {
  background: var(--color-primary-active);
  transform: scale(0.98);
}

/* 성공 모달 애니메이션 */
/*noinspection CssUnusedSymbol*/
.success-modal-enter-active,
.success-modal-leave-active {
  transition: opacity 0.2s ease;
}

.success-modal-enter-active .success-modal,
.success-modal-leave-active .success-modal {
  transition:
      opacity 0.22s ease,
      transform 0.22s ease;
}

/*noinspection CssUnusedSymbol*/
.success-modal-enter-from,
.success-modal-leave-to {
  opacity: 0;
}

.success-modal-enter-from .success-modal {
  opacity: 0;
  transform: translateY(12px) scale(0.96);
}

.success-modal-leave-to .success-modal {
  opacity: 0;
  transform: translateY(6px) scale(0.98);
}

@keyframes success-icon-pop {
  0% {
    opacity: 0;
    transform: scale(0.6);
  }

  70% {
    opacity: 1;
    transform: scale(1.1);
  }

  100% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 로딩 */
.loading-overlay {
  position: absolute;
  z-index: 200;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  background: rgba(255, 255, 255, 0.88);
  color: var(--color-text-main);
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 4px solid var(--color-bg-disabled);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
/* 공통 확인 모달 */
.confirm-modal-overlay {
  position: absolute;
  z-index: 300;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 15, 15, 0.42);
  backdrop-filter: blur(3px);
  -webkit-backdrop-filter: blur(3px);
  box-sizing: border-box;
}

.confirm-modal {
  width: 100%;
  max-width: 320px;
  padding: 28px 22px 20px;
  border-radius: 22px;
  background: var(--color-bg-page);
  box-shadow: 0 20px 55px rgba(0, 0, 0, 0.2);
  box-sizing: border-box;
  text-align: center;
}

.confirm-icon-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 18px;
}

.confirm-icon {
  display: flex;
  width: 62px;
  height: 62px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 188, 46, 0.16);
  color: var(--color-primary-border);
  font-size: 25px;
}

.confirm-icon.danger {
  background: rgba(211, 47, 47, 0.1);
  color: var(--color-error);
}

.confirm-content h3 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.4;
}

.confirm-content p {
  margin: 9px 0 0;
  color: var(--color-text-sub);
  font-weight: 400;
  line-height: 1.5;
}

.confirm-buttons {
  display: flex;
  gap: 10px;
  margin-top: 24px;
}

.confirm-cancel-button,
.confirm-action-button {
  height: 50px;
  flex: 1;
  border-radius: 14px;
  font-family: inherit;
  cursor: pointer;
}

.confirm-cancel-button {
  border: 1px solid var(--color-border-main);
  background: var(--color-bg-page);
  color: var(--color-text-main);
}

.confirm-action-button {
  border: 1px solid var(--color-primary-border);
  background: var(--color-primary);
  color: var(--color-text-main);
}

.confirm-action-button.danger {
  border-color: var(--color-error);
  background: var(--color-error);
  color: var(--color-text-white);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (prefers-reduced-motion: reduce) {
  .success-icon {
    animation: none;
  }

  /*noinspection CssUnusedSymbol*/
  .success-modal-enter-active,
  .success-modal-leave-active,
  .success-modal-enter-active .success-modal,
  .success-modal-leave-active .success-modal {
    transition: none;
  }
}
</style>