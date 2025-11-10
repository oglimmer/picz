<script setup lang="ts">
import { computed } from "vue";
import { useLoginStore } from "@/stores/login";
import config from "@/Config";
import { useTosContent } from "@/composables/useTosContent";

const loginStore = useLoginStore();
const { tosContent, lastUpdated } = useTosContent();

const showModal = computed(() => {
  return (
    loginStore.accessToken &&
    loginStore.userCapacity &&
    loginStore.userCapacity.acceptedTos === false
  );
});

const acceptTos = async () => {
  await fetch(`${config.apiServer}/api/v1/user/accept-tos`, {
    method: "POST",
    headers: { Authorization: `Bearer ${loginStore.accessToken}` },
  });
  if (loginStore.userCapacity) {
    loginStore.userCapacity.acceptedTos = true;
  }
};

const deleteAccount = async () => {
  await fetch(`${config.apiServer}/api/v1/user`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${loginStore.accessToken}` },
  });
  window.location.href = "/";
};
</script>

<template>
  <div v-if="showModal" class="modal-overlay">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h2 class="modal-title">Terms of Service</h2>
        </div>
        <div class="modal-body">
          <p class="tos-intro">
            Please review and accept our Terms and Conditions to continue using
            PicZ.
          </p>
          <p class="tos-updated">Last Updated: {{ lastUpdated }}</p>
          <div class="tos-content" v-html="tosContent"></div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-danger" @click="deleteAccount">
            Delete this account
          </button>
          <button type="button" class="btn btn-primary" @click="acceptTos">
            Accept TOS
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1050;
}

.modal-dialog {
  max-width: 800px;
  width: 90%;
  margin: 1rem auto;
  max-height: 90vh;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-height: 90vh;
}

.modal-header {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #dee2e6;
  background-color: #f8f9fa;
}

.modal-title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 500;
  color: #212529;
}

.modal-body {
  padding: 1.5rem;
  overflow-y: auto;
  flex: 1;
}

.modal-body p {
  margin: 0;
  line-height: 1.5;
  color: #6c757d;
}

.modal-body a {
  color: #007bff;
  text-decoration: underline;
}

.modal-body a:hover {
  color: #0056b3;
}

.modal-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid #dee2e6;
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
}

.btn {
  padding: 0.375rem 0.75rem;
  border: 1px solid transparent;
  border-radius: 4px;
  font-size: 1rem;
  line-height: 1.5;
  cursor: pointer;
  transition: all 0.15s ease-in-out;
}

.btn-primary {
  background-color: #007bff;
  border-color: #007bff;
  color: white;
}

.btn-primary:hover {
  background-color: #0056b3;
  border-color: #004085;
}

.btn-danger {
  background-color: #dc3545;
  border-color: #dc3545;
  color: white;
}

.btn-danger:hover {
  background-color: #c82333;
  border-color: #bd2130;
}

.tos-intro {
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem !important;
}

.tos-updated {
  font-size: 0.9rem;
  color: #777;
  margin-bottom: 1.5rem !important;
  font-style: italic;
}

.tos-content {
  line-height: 1.6;
}

.tos-content .intro-text {
  font-size: 1rem;
  color: #555;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 6px;
  border-left: 3px solid #007bff;
}

.tos-content h2 {
  color: #333;
  font-size: 1.2rem;
  font-weight: 600;
  margin: 1.5rem 0 0.75rem 0;
  padding-bottom: 0.25rem;
  border-bottom: 2px solid #007bff;
}

.tos-content p {
  margin-bottom: 0.75rem !important;
  color: #555 !important;
  line-height: 1.6 !important;
}

.tos-content a {
  color: #007bff !important;
  text-decoration: underline !important;
}

.tos-content a:hover {
  color: #0056b3 !important;
}
</style>
