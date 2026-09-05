<script setup lang="ts">
defineProps<{ show: boolean; title: string; message: string }>();
const emit = defineEmits<{ confirm: []; cancel: [] }>();
</script>

<template>
  <div v-if="show" class="modal-overlay" @click.self="emit('cancel')">
    <div class="modal-box">
      <h3>{{ title }}</h3>
      <p>{{ message }}</p>
      <div class="modal-actions">
        <button class="btn" @click="emit('cancel')">Annuler</button>
        <button class="btn btn-danger-solid" @click="emit('confirm')">
          Confirmer
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(17, 17, 17, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 300;
}
.modal-box {
  background: white;
  padding: 1.75rem;
  border-radius: 10px;
  max-width: 380px;
  width: 90%;
}
.modal-box h3 {
  margin: 0 0 0.5rem;
  font-size: 1.05rem;
}
.modal-box p {
  color: var(--ink-soft);
  font-size: 0.88rem;
  margin-bottom: 1.3rem;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.6rem;
}
.btn-danger-solid {
  background: #b3261e;
  color: white;
  border: none;
  padding: 0.5rem 0.9rem;
  border-radius: 6px;
  font-size: 0.82rem;
  cursor: pointer;
}
</style>
