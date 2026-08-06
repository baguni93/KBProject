<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import {
  deleteProfileImage,
  getProfile,
  getProfileImageUrl,
  updateProfile,
  updateProfileImage,
} from '@/api/profileApi';

const router = useRouter();

const profile = reactive({ nickname: '', introduction: '' });
const originalProfile = reactive({ nickname: '', introduction: '' });

const imageInput = ref(null);
const profileImage = ref('');
const loading = ref(false);
const saving = ref(false);
const imageUploading = ref(false);
const imageDeleting = ref(false);
const imageActionSheetOpen = ref(false);
const nicknameError = ref('');
const imageError = ref('');
const errorMessage = ref('');

// 이미지 작업 중 여부
const imageProcessing = computed(() => imageUploading.value || imageDeleting.value);

// 저장 가능 여부
const canSave = computed(() => profile.nickname.length > 0 && !nicknameError.value);

// 프로필 이미지 URL 갱신
const refreshProfileImage = () => {
  profileImage.value = `${getProfileImageUrl()}?t=${Date.now()}`;
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

    refreshProfileImage();
  } catch (error) {
    console.error(error);

    imageError.value = error.response?.data?.message || '프로필 이미지 변경에 실패했습니다.';
  } finally {
    imageUploading.value = false;

    if (imageInput.value) imageInput.value.value = '';
  }
};

// 프로필 이미지 삭제
const removeProfileImage = async () => {
  if (imageProcessing.value) return;

  imageActionSheetOpen.value = false;

  const confirmed = window.confirm('등록된 프로필 사진을 삭제하고 기본 사진으로 변경할까요?');

  if (!confirmed) return;

  try {
    imageDeleting.value = true;
    imageError.value = '';
    errorMessage.value = '';

    await deleteProfileImage();

    refreshProfileImage();
  } catch (error) {
    console.error(error);

    imageError.value = error.response?.data?.message || '프로필 이미지 삭제에 실패했습니다.';
  } finally {
    imageDeleting.value = false;
  }
};

// ESC 키로 이미지 메뉴 닫기
const handleEscapeKey = (event) => {
  if (event.key !== 'Escape' || !imageActionSheetOpen.value) return;

  closeImageActionSheet();
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

    refreshProfileImage();
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

    const profileData = {
      nickname: profile.nickname.trim(),
      introduction: profile.introduction.trim(),
    };

    await updateProfile(profileData);

    originalProfile.nickname = profile.nickname;
    originalProfile.introduction = profile.introduction;

    window.alert('프로필이 수정되었습니다.');

    await router.replace('/setting');
  } catch (error) {
    console.error(error);

    errorMessage.value = error.response?.data?.message || '프로필 수정에 실패했습니다.';
  } finally {
    saving.value = false;
  }
};

// 수정 여부 확인
const isProfileChanged = () =>
    profile.nickname !== originalProfile.nickname
    || profile.introduction !== originalProfile.introduction;

// 수정 취소
const cancel = async () => {
  if (isProfileChanged()) {
    const confirmed = window.confirm('수정한 내용을 저장하지 않고 나갈까요?');

    if (!confirmed) return;
  }

  await router.back();
};

// 이전 화면
const goBack = () => {
  cancel();
};

onMounted(() => {
  loadProfile();
  window.addEventListener('keydown', handleEscapeKey);
});

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleEscapeKey);
});
</script>

<template>
  <div class="profile-page">
    <main class="profile-container">
      <header class="page-header">
        <button class="back-button" type="button" @click="goBack">
          &lt;
        </button>

        <h1>프로필 관리</h1>

        <div class="header-empty"></div>
      </header>

      <section class="title-section">
        <h2>
          내 정보를 확인하고<br />
          수정해 주세요
        </h2>

        <p>프로필 사진과 소개는 자유롭게 변경할 수 있어요.</p>
      </section>

      <section class="profile-image-section">
        <div class="profile-image-wrap">
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
              @click="openImageActionSheet"
          >
            <span v-if="imageProcessing" class="mini-spinner"></span>
            <span v-else class="edit-icon"></span>
          </button>
        </div>

        <input
            ref="imageInput"
            accept="image/jpeg,image/png,image/gif,image/webp"
            class="hidden-image-input"
            type="file"
            @change="changeImage"
        />

        <p v-if="imageError" class="image-error">
          {{ imageError }}
        </p>
      </section>

      <form class="profile-form" @submit.prevent="saveProfile">
        <section class="input-card">
          <label for="nickname">닉네임</label>

          <p class="input-guide">
            다른 사용자에게 표시되는 이름이에요.
          </p>

          <div :class="{ error: !!nicknameError }" class="input-area">
            <input
                id="nickname"
                v-model.trim="profile.nickname"
                maxlength="15"
                placeholder="닉네임을 입력해 주세요"
                type="text"
                @input="clearNicknameError"
            />

            <span>{{ profile.nickname.length }}/15</span>
          </div>

          <p v-if="nicknameError" class="field-error">
            {{ nicknameError }}
          </p>
        </section>

        <section class="input-card introduction-card">
          <label for="introduction">소개</label>

          <p class="input-guide">
            나를 간단히 소개해 보세요!
          </p>

          <div class="textarea-area">
            <textarea
                id="introduction"
                v-model="profile.introduction"
                maxlength="100"
                placeholder="소개를 입력해 주세요"
            ></textarea>

            <span>{{ profile.introduction.length }}/100</span>
          </div>
        </section>

        <p v-if="errorMessage" class="page-error">
          {{ errorMessage }}
        </p>

        <div class="button-area">
          <button
              class="cancel-button"
              :disabled="saving || imageProcessing"
              type="button"
              @click="cancel"
          >
            취소
          </button>

          <button
              class="save-button"
              :disabled="loading || saving || imageProcessing || !canSave"
              type="submit"
          >
            {{ saving ? '저장 중...' : '저장하기' }}
          </button>
        </div>
      </form>

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
              <h3>프로필 사진 수정</h3>
            </div>

            <div class="action-sheet-menu">
              <button
                  class="action-sheet-button"
                  :disabled="imageProcessing"
                  type="button"
                  @click="selectImage"
              >
                <span class="menu-icon image-icon">
                  <span class="image-mountain"></span>
                  <span class="image-sun"></span>
                </span>

                <span class="menu-text">
                  <strong>사진 선택</strong>
                  <small>라이브러리에서 사진 가져오기</small>
                </span>

                <span class="menu-arrow">›</span>
              </button>

              <button
                  class="action-sheet-button delete-action"
                  :disabled="imageProcessing"
                  type="button"
                  @click="removeProfileImage"
              >
                <span class="menu-icon trash-icon">
                  <span class="trash-lid"></span>
                  <span class="trash-body"></span>
                </span>

                <span class="menu-text">
                  <strong>프로필 사진 삭제</strong>
                  <small>기본 프로필 이미지로 변경</small>
                </span>

                <span class="menu-arrow">›</span>
              </button>
            </div>

            <button
                class="action-sheet-cancel"
                :disabled="imageProcessing"
                type="button"
                @click="closeImageActionSheet"
            >
              취소
            </button>
          </section>
        </div>
      </transition>

      <div v-if="loading" class="loading-overlay">
        <div class="loading-spinner"></div>
        <span>프로필 정보를 불러오고 있어요.</span>
      </div>
    </main>
  </div>
</template>

<style scoped>
.profile-page {
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.profile-container {
  position: relative;
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  padding: 10px 28px 140px;
  background: #ffffff;
  box-sizing: border-box;
}

.page-header {
  display: grid;
  grid-template-columns: 38px 1fr 38px;
  min-height: 44px;
  align-items: center;
}

.page-header h1 {
  margin: 0;
  color: #222222;
  font-size: 17px;
  font-weight: 700;
  text-align: center;
}

.back-button {
  justify-self: start;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 27px;
  line-height: 1;
  cursor: pointer;
}

.header-empty {
  width: 38px;
}

.title-section {
  margin-top: 38px;
}

.title-section h2 {
  margin: 0;
  color: #111111;
  font-size: 25px;
  font-weight: 800;
  line-height: 1.35;
  letter-spacing: -0.8px;
}

.title-section p {
  margin: 14px 0 0;
  color: #888888;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
}

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
}

.profile-image {
  display: block;
  width: 104px;
  height: 104px;
  border: 1px solid #eeeeee;
  border-radius: 50%;
  background: #dbe5ff;
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
  border: 2px solid #ffffff;
  border-radius: 50%;
  background: #222222;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.18);
  cursor: pointer;
  transition:
      transform 0.15s ease,
      background 0.15s ease;
}

.image-edit-button:hover:not(:disabled) {
  background: #333333;
  transform: scale(1.05);
}

.image-edit-button:active:not(:disabled) {
  transform: scale(0.95);
}

.image-edit-button:disabled {
  cursor: wait;
  opacity: 0.75;
}

.edit-icon {
  position: relative;
  display: block;
  width: 15px;
  height: 15px;
  transform: rotate(-45deg);
}

.edit-icon::before {
  position: absolute;
  top: 5px;
  left: 1px;
  width: 11px;
  height: 5px;
  border-radius: 1px;
  background: #ffffff;
  content: '';
}

.edit-icon::after {
  position: absolute;
  top: 5px;
  right: 0;
  width: 0;
  height: 0;
  border-top: 2.5px solid transparent;
  border-bottom: 2.5px solid transparent;
  border-left: 4px solid #ffffff;
  content: '';
}

.mini-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.45);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

.hidden-image-input {
  display: none;
}

.image-error {
  margin: 8px 0 0;
  color: #e53935;
  font-size: 12px;
  line-height: 1.4;
  text-align: center;
}

.profile-form {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
  margin-top: 28px;
}

.input-card {
  padding: 18px;
  border: 1px solid #e8e8e8;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
}

.introduction-card {
  margin-top: 16px;
}

.input-card label {
  display: block;
  color: #222222;
  font-size: 15px;
  font-weight: 800;
}

.input-guide {
  margin: 7px 0 12px;
  color: #999999;
  font-size: 12px;
  line-height: 1.5;
}

.input-area {
  display: flex;
  height: 54px;
  align-items: center;
  padding: 0 14px;
  border: 1px solid #dddddd;
  border-radius: 10px;
  background: #ffffff;
  box-sizing: border-box;
}

.input-area:focus-within {
  border-color: #ffbc2e;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.input-area.error {
  border-color: #e53935;
}

.input-area input {
  min-width: 0;
  flex: 1;
  border: 0;
  background: transparent;
  color: #222222;
  font-size: 16px;
  outline: none;
}

.input-area input::placeholder {
  color: #aaaaaa;
}

.input-area span {
  flex: none;
  margin-left: 8px;
  color: #999999;
  font-size: 12px;
}

.textarea-area {
  position: relative;
  height: 108px;
  padding: 14px 14px 30px;
  border: 1px solid #dddddd;
  border-radius: 10px;
  background: #ffffff;
  box-sizing: border-box;
}

.textarea-area:focus-within {
  border-color: #ffbc2e;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.textarea-area textarea {
  width: 100%;
  height: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  color: #222222;
  font-family: inherit;
  font-size: 15px;
  line-height: 1.5;
  outline: none;
  resize: none;
}

.textarea-area textarea::placeholder {
  color: #aaaaaa;
}

.textarea-area span {
  position: absolute;
  right: 14px;
  bottom: 10px;
  color: #999999;
  font-size: 12px;
}

.field-error {
  margin: 8px 0 0;
  color: #e53935;
  font-size: 12px;
  line-height: 1.4;
}

.page-error {
  margin: 12px 0 0;
  color: #e53935;
  font-size: 13px;
  line-height: 1.5;
  text-align: center;
}

.button-area {
  position: absolute;
  right: 28px;
  bottom: 58px;
  left: 28px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.cancel-button,
.save-button {
  width: 100%;
  height: 58px;
  border-radius: 10px;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
}

.cancel-button {
  border: 1px solid #bbbbbb;
  background: #ffffff;
  color: #222222;
}

.save-button {
  border: 1px solid #cc9200;
  background: #ffbc2e;
  color: #111111;
}

.cancel-button:active:not(:disabled) {
  background: #f5f5f5;
}

.save-button:active:not(:disabled) {
  background: #f2aa10;
}

.cancel-button:disabled,
.save-button:disabled {
  cursor: not-allowed;
}

.cancel-button:disabled {
  border-color: #dddddd;
  color: #aaaaaa;
}

.save-button:disabled {
  border-color: #dddddd;
  background: #eeeeee;
  color: #aaaaaa;
}

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
  background: #f7f7f7;
  box-shadow: 0 -10px 35px rgba(0, 0, 0, 0.18);
  box-sizing: border-box;
}

.action-sheet-handle {
  width: 38px;
  height: 4px;
  margin: 0 auto 14px;
  border-radius: 999px;
  background: #cccccc;
}

.action-sheet-header {
  padding: 0 8px 14px;
}

.action-sheet-header h3 {
  margin: 0;
  color: #222222;
  font-size: 17px;
  font-weight: 800;
}

.action-sheet-menu {
  overflow: hidden;
  border: 1px solid #e7e7e7;
  border-radius: 16px;
  background: #ffffff;
}

.action-sheet-button {
  display: flex;
  width: 100%;
  min-height: 72px;
  align-items: center;
  padding: 12px 16px;
  border: 0;
  background: #ffffff;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s ease;
}

.action-sheet-button + .action-sheet-button {
  border-top: 1px solid #eeeeee;
}

.action-sheet-button:hover:not(:disabled) {
  background: #f8f8f8;
}

.action-sheet-button:active:not(:disabled) {
  background: #f0f0f0;
}

.action-sheet-button:disabled {
  cursor: wait;
  opacity: 0.5;
}

.menu-icon {
  position: relative;
  display: block;
  width: 26px;
  height: 26px;
  flex: none;
  margin-right: 13px;
}

.image-icon {
  border: 2px solid #444444;
  border-radius: 6px;
}

.image-mountain {
  position: absolute;
  right: 3px;
  bottom: 3px;
  width: 14px;
  height: 9px;
  border-right: 2px solid #444444;
  border-bottom: 2px solid #444444;
  transform: rotate(-45deg);
}

.image-sun {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #444444;
}

.trash-icon {
  color: #e5484d;
}

.trash-lid {
  position: absolute;
  top: 4px;
  left: 4px;
  width: 18px;
  height: 2px;
  border-radius: 2px;
  background: currentColor;
}

.trash-lid::before {
  position: absolute;
  top: -4px;
  left: 5px;
  width: 8px;
  height: 4px;
  border: 2px solid currentColor;
  border-bottom: 0;
  border-radius: 3px 3px 0 0;
  content: '';
}

.trash-body {
  position: absolute;
  top: 8px;
  left: 6px;
  width: 14px;
  height: 15px;
  border: 2px solid currentColor;
  border-top: 0;
  border-radius: 0 0 4px 4px;
}

.menu-text {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 4px;
}

.menu-text strong {
  color: #222222;
  font-size: 15px;
  font-weight: 700;
}

.menu-text small {
  color: #999999;
  font-size: 11px;
}

.delete-action .menu-text strong,
.delete-action .menu-arrow {
  color: #e5484d;
}

.menu-arrow {
  flex: none;
  margin-left: 8px;
  color: #aaaaaa;
  font-size: 25px;
  font-weight: 300;
  line-height: 1;
}

.action-sheet-cancel {
  width: 100%;
  height: 54px;
  margin-top: 10px;
  border: 1px solid #e5e5e5;
  border-radius: 16px;
  background: #ffffff;
  color: #222222;
  font-size: 16px;
  font-weight: 800;
  cursor: pointer;
}

.action-sheet-cancel:hover:not(:disabled) {
  background: #f7f7f7;
}

.action-sheet-cancel:disabled {
  cursor: wait;
  opacity: 0.5;
}

.action-sheet-enter-active,
.action-sheet-leave-active {
  transition: opacity 0.2s ease;
}

.action-sheet-enter-active .action-sheet,
.action-sheet-leave-active .action-sheet {
  transition: transform 0.25s ease;
}

.action-sheet-enter-from,
.action-sheet-leave-to {
  opacity: 0;
}

.action-sheet-enter-from .action-sheet,
.action-sheet-leave-to .action-sheet {
  transform: translateY(100%);
}

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
  color: #444444;
  font-size: 14px;
  font-weight: 700;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 4px solid #eeeeee;
  border-top-color: #ffbc2e;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 360px) {
  .profile-container {
    padding-right: 20px;
    padding-left: 20px;
  }

  .button-area {
    right: 20px;
    left: 20px;
  }
}
</style>