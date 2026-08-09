import api from './index';
import { useAuthStore } from '@/stores/auth';

// 프로필 조회
export const getProfile = async () => {
    const authStore = useAuthStore();

    const { data } = await api.get(
        `/api/users/${authStore.userId}/profile`,
    );

    return data;
};

// 프로필 수정
export const updateProfile = async (profile) => {
    const authStore = useAuthStore();

    const { data } = await api.patch(
        `/api/users/${authStore.userId}/profile`,
        profile,
    );

    return data;
};

// 프로필 이미지 등록 및 변경
export const updateProfileImage = async (image) => {
    const authStore = useAuthStore();
    const formData = new FormData();

    formData.append('image', image);

    const { data } = await api.put(
        `/api/users/${authStore.userId}/profile/image`,
        formData,
        {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        },
    );

    return data;
};

// 프로필 이미지 삭제
export const deleteProfileImage = async () => {
    const authStore = useAuthStore();

    const { data } = await api.delete(
        `/api/users/${authStore.userId}/profile/image`,
    );

    return data;
};

// 프로필 이미지 주소
export const getProfileImageUrl = () => {
    const authStore = useAuthStore();

    return `/api/users/${authStore.userId}/profile/image`;
};