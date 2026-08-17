<template>
  <div class="step-content-wrap">
    <RemitStep3FeedForm
      remit-type="ACCOUNT"
      :account-form="remittanceStore.accountForm"
      :remit-amount="remittanceStore.remitAmount"
      :remit-memo="remittanceStore.remitMemo"
      :remit-visibility="remittanceStore.remitVisibility"
      :selected-category-id="remittanceStore.selectedCategoryId"
      :category-list="remittanceStore.categoryList"
      :displayed-category-list="remittanceStore.categoryList"
      :selected-file="remittanceStore.selectedFile"
      :image-preview-url="remittanceStore.imagePreviewUrl"
      :image-preview-urls="remittanceStore.imagePreviewUrls"
      :format-currency="remittanceStore.formatCurrency"
      @update:remit-memo="(val) => remittanceStore.remitMemo = val"
      @update:remit-visibility="(val) => remittanceStore.remitVisibility = val"
      @update:selected-category-id="(id) => remittanceStore.selectedCategoryId = id"
      @file-change="remittanceStore.handleFileChange"
      @remove-file="remittanceStore.removeSelectedFile"
      @submit="submitRemittance"
    />
  </div>
</template>

<script setup>
import { useRemittanceStore } from "@/stores/remittance";
import RemitStep3FeedForm from "@/components/remittance/RemitStep3FeedForm.vue";

const remittanceStore = useRemittanceStore();

const submitRemittance = () => {
  remittanceStore.showConfirmModal = true;
};
</script>

<style scoped>
.step-content-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
}
</style>
