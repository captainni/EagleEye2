<template>
  <teleport to="body">
    <transition name="modal-fade">
      <div v-if="isOpen" class="modal fixed inset-0 z-100 flex items-center justify-center overflow-auto bg-black bg-opacity-40" @click.self="closeModal">
        <div class="modal-content bg-white m-4 sm:m-10 p-6 rounded-lg shadow-xl w-full max-w-xl max-h-[90vh] overflow-y-auto">
          <!-- Modal Header -->
          <div v-if="title" class="modal-header flex justify-between items-center border-b border-gray-200 pb-4 mb-4">
            <h4 class="modal-title text-lg font-semibold text-gray-800">{{ title }}</h4>
            <button class="modal-close text-gray-500 hover:text-gray-700 text-2xl font-bold" @click="closeModal">&times;</button>
          </div>
          <!-- Modal Body -->
          <div class="modal-body pb-4">
            <slot></slot> <!-- Default slot for the main content -->
          </div>
          <!-- Modal Footer (Optional via slot) -->
          <div v-if="hasFooterSlot" class="modal-footer border-t border-gray-200 pt-4 flex justify-end gap-2">
             <slot name="footer"></slot>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup lang="ts">
import { defineProps, defineEmits, watch, computed, useSlots } from 'vue';

const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true,
  },
  title: {
    type: String,
    default: '',
  },
  // Optional: Prevent closing by clicking backdrop
  persistent: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['close']);

const slots = useSlots();
const hasFooterSlot = computed(() => !!slots.footer);

const closeModal = () => {
  if (!props.persistent) {
    emit('close');
  }
};

// Optional: Handle Escape key to close modal
const handleEsc = (event: KeyboardEvent) => {
  if (event.key === 'Escape' && props.isOpen) {
    closeModal();
  }
};

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    document.addEventListener('keydown', handleEsc);
    document.body.style.overflow = 'hidden'; // Prevent background scroll
  } else {
    document.removeEventListener('keydown', handleEsc);
    document.body.style.overflow = ''; // Restore background scroll
  }
});

// Cleanup listener on unmount
import { onUnmounted } from 'vue';
onUnmounted(() => {
  document.removeEventListener('keydown', handleEsc);
  // Ensure body scroll is restored if component is unmounted while open
  if (props.isOpen) {
      document.body.style.overflow = '';
  }
});

</script>

<style scoped>
.modal {
  /* Tailwind handles positioning and background */
}
.modal-content {
  /* Tailwind handles background, padding, rounded, shadow */
}
.modal-header,
.modal-body,
.modal-footer {
  /* Tailwind handles borders, padding, flex etc. */
}

/* Transition styles */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-active .modal-content,
.modal-fade-leave-active .modal-content {
  transition: all 0.3s ease;
}

.modal-fade-enter-from .modal-content,
.modal-fade-leave-to .modal-content {
  transform: translateY(-20px);
  opacity: 0;
}

.modal-fade-enter-to .modal-content,
.modal-fade-leave-from .modal-content {
    transform: translateY(0);
    opacity: 1;
}
</style> 