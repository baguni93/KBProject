package org.scoula.task.service;

import org.scoula.common.util.Enum;

public interface TaskEventService {

    void sendTaskEvent(
            int userId,
            Enum.TaskType taskType,
            String message,
            Integer targetId
    );
}