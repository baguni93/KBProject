import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useTaskStore = defineStore('task', () => {
  const taskQueue = ref([]);

  const addTaskEvent = (task, callback = null) => {
    taskQueue.value.push({
      ...task,
      callback,
    });
  };

  const removeTaskEvent = () => {
    taskQueue.value.shift();
  };

  const clearTaskEvents = () => {
    taskQueue.value = [];
  };

  return {
    taskQueue,
    addTaskEvent,
    removeTaskEvent,
    clearTaskEvents,
  };
});
