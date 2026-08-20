package org.scoula.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEventDTO {

    private  Enum.TaskType taskType;
    private String status;
    private Integer taskId;
    private String message;
    private Integer targetId;
}