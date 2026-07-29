package org.scoula.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.notification.domain.NotificationVO;
import org.scoula.notification.dto.NotificationRequestDTO;
import org.scoula.notification.dto.NotificationResponseDTO;
import org.scoula.notification.mapper.NotificationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class NotificationServiceImpl implements NotificationService{

   private final NotificationMapper notificationMapper;


    @Override
    public NotificationResponseDTO create(NotificationRequestDTO request) {

        NotificationVO notificationVO = request.toVo();

        notificationMapper.create(notificationVO);

        return get(notificationVO.getNotificationId());
    }


    @Override
    public NotificationResponseDTO get(int notificationId) {

       NotificationVO notificationVO = notificationMapper.get(notificationId);

        return NotificationResponseDTO.of(notificationVO);
    }

    @Override
    public List<NotificationResponseDTO> getList(int userId) {

       List<NotificationVO> list = notificationMapper.getList(userId);

        return list.stream().map(NotificationResponseDTO::of).toList();
    }

    @Override
    public NotificationResponseDTO read(int notificationId) {

        NotificationResponseDTO responseDTO = get(notificationId);

        notificationMapper.read(notificationId);

        return responseDTO;
    }

    @Override
    public List<NotificationResponseDTO> readAll(int userId) {

        List<NotificationResponseDTO> list = getList(userId);

        notificationMapper.readAll(userId);

        return list;
    }
}
