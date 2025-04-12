package com.munetmo.lingetic.lib.tasks;

public interface TaskQueue {
    /**
     * Submit a task for asynchronous processing.
     *
     * @param taskId    A unique identifier for the task. Used for tracking and
     *                  avoiding duplicates.
     * @param payload   The payload to be processed. Must be serializable.
     * @param queueName The name of the queue to submit the task to.
     */
    <T> void submitTask(String taskId, T payload, String queueName);
}
