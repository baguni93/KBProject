package org.scoula.task.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.Enum;
import org.scoula.task.dto.TaskEventDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class TaskEventServiceImpl implements TaskEventService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendTaskEvent(
            int userId,
            Enum.TaskType taskType,
            String message,
            Integer targetId
    ) {

        TaskEventDTO event = TaskEventDTO.builder()
                .taskType(taskType)
                .message(message)
                .targetId(targetId)
                .build();

        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/tasks",
                event
        );

        log.info(
                "작업 이벤트 전송 - userId: {}, taskType: {}",
                userId,
                taskType
        );
    }
}