<script setup lang="ts">
import { useAlbumStore } from "@/stores/album";
import { ref } from "vue";

const props = defineProps([
  "internalId",
  "externalId",
  "description",
  "creationDate",
  "albumId",
]);

const description = ref(props.description);

const userStore = useAlbumStore();

const descriptionChanged = () => {
  userStore.setAlbumDescription(props.internalId, description.value);
};
const addSection = () => {
  userStore.addSection(props.internalId);
};

const deleteThisElement = () => {
  userStore.deleteElement(props.albumId, props.internalId);
};
</script>

<template>
  <div class="section-edit-container">
    <div class="action-buttons">
      <button
        class="edit-btn section-btn"
        @click="addSection"
        title="Add Section"
      >
        <font-awesome-icon :icon="['fas', 'heading']" />
      </button>
      <button
        class="edit-btn delete-btn"
        @click="deleteThisElement"
        title="Delete"
      >
        <font-awesome-icon :icon="['fas', 'trash']" />
      </button>
    </div>
    <div class="content-area">
      <div class="content-layout">
        <div class="section-icon-wrapper">
          <font-awesome-icon :icon="['fas', 'heading']" class="section-icon" />
        </div>
        <div class="text-content">
          <div class="description-area">
            <textarea
              v-model="description"
              @blur="descriptionChanged"
              placeholder="Add a section title or description..."
              class="description-input"
            ></textarea>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.section-edit-container {
  display: flex;
  width: 100%;
  background-color: #fafafa;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eaeaea;
  transition: box-shadow 0.2s ease;
  min-height: 80px;
  position: relative;
  margin-bottom: 10px;
}

.section-edit-container:hover {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.content-area {
  flex: 1;
  padding: 0;
}

.content-layout {
  display: flex;
  gap: 0;
}

.section-icon-wrapper {
  flex: 1;
  height: 250px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f4ff;
  border-radius: 0;
  margin-top: 0;
  overflow: hidden;
}

.section-icon {
  font-size: 4rem;
  color: #4263eb;
}

.text-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 10px;
}

.description-area {
  margin-bottom: 10px;
}

.description-input {
  width: 100%;
  min-height: 120px;
  max-height: 120px;
  padding: 8px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-family: inherit;
  font-size: 0.95rem;
  resize: vertical;
  transition: border-color 0.2s ease;
  background-color: white;
}

.description-input:focus {
  border-color: #a8a8d8;
  outline: none;
}

.meta-info {
  display: flex;
  justify-content: flex-start;
  font-size: 0.8rem;
  color: #888;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  padding: 12px 0;
  gap: 8px;
  background-color: #f5f5f5;
  border-right: 1px solid #eaeaea;
}

.edit-btn {
  background-color: white;
  border: 1px solid #dedede;
  border-radius: 4px;
  padding: 4px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  transition: all 0.2s ease;
  color: #555;
  margin: 0 8px;
}

.edit-btn:hover {
  background-color: #f0f0f0;
  transform: translateX(1px);
}

.section-btn {
  color: #0d6efd;
}

.delete-btn {
  color: #dc3545;
}

@media (max-width: 768px) {
  .section-edit-container {
    flex-direction: column-reverse;
  }

  .action-buttons {
    flex-direction: row;
    justify-content: flex-end;
    padding: 8px;
    border-right: none;
    border-top: 1px solid #eaeaea;
  }

  .content-layout {
    flex-direction: column;
    align-items: center;
  }

  .section-icon-wrapper {
    margin-bottom: 10px;
  }

  .text-content {
    width: 100%;
  }
}
</style>
