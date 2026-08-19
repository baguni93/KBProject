import { defineStore } from 'pinia';
import { ref } from 'vue';
import customCardApi from '@/api/customCard.Api';

export const useCustomCardStore = defineStore('customcard', () => {
  const isAgreementAgree = ref(false);
  const agreements = ref([]);

  const code = ref('');
  const id = ref('');

  const checkAgreementAgree = async (userId) => {
    try {
      const data = await customCardApi.checkAgreementAgree(userId);
      isAgreementAgree.value = data;
      console.log(isAgreementAgree.value);
    } catch (e) {
      console.log(e);
    }
  };

  const getAgreements = async () => {
    try {
      const data = await customCardApi.getAgreements();
      agreements.value = data.map((agreement) => {
        const existing = agreements.value.find(
          (item) => item.agreementId === agreement.agreementId,
        );

        return {
          ...agreement,
          checked: existing?.checked ?? false,
        };
      });
    } catch (e) {
      console.log(e);
    }
  };

  const setAgreementAgree = async (userId) => {
    try {
      const data = await customCardApi.setAgreementAgree(userId);
      isAgreementAgree.value = data;
      console.log(isAgreementAgree.value);
    } catch (e) {
      console.log(e);
    }
  };

  const load = async (userId) => {
    try {
      await checkAgreementAgree(userId);
      await getAgreements();

      console.log(agreements);
    } catch (e) {
      console.log(e);
    }
  };

  return {
    isAgreementAgree,
    agreements,
    code,
    id,
    load,
    setAgreementAgree,
  };
});
