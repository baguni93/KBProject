package org.scoula.settlement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.Enum;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.feed.dto.FeedCreateRequestDTO;
import org.scoula.feed.service.FeedService;
import org.scoula.notification.dto.NotificationRequestDTO;
import org.scoula.notification.service.NotificationService;
import org.scoula.settlement.domain.SettlementMemberVO;
import org.scoula.settlement.domain.SettlementVO;
import org.scoula.settlement.dto.SettlementCreateRequestDTO;
import org.scoula.settlement.dto.SettlementMemberRequestDTO;
import org.scoula.settlement.dto.SettlementResponseDTO;
import org.scoula.settlement.mapper.SettlementMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class SettlementServiceImpl implements SettlementService{

    private final SettlementMapper settlementMapper;
    private final FeedService feedService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    @Override
    public SettlementResponseDTO create(SettlementCreateRequestDTO request) {

        SettlementVO settlementVO = request.toVo();

        if (settlementVO.getSettlementType() == null) {
            settlementVO.setSettlementType(Enum.SettlementType.EQUAL);
        }
        if (settlementVO.getSpendingCategoryId() <= 0) {
            settlementVO.setSpendingCategoryId(1);
        }
        if (settlementVO.getTitle() == null || settlementVO.getTitle().isEmpty()) {
            settlementVO.setTitle("더치페이 정산");
        }
        if (settlementVO.getTitle().length() > 20) {
            settlementVO.setTitle(settlementVO.getTitle().substring(0, 20));
        }
        if (settlementVO.getContent() != null && settlementVO.getContent().length() > 20) {
            settlementVO.setContent(settlementVO.getContent().substring(0, 20));
        }

        settlementMapper.create(settlementVO);

        List<SettlementMemberRequestDTO> members = request.getMembers();

        if(members == null || members.isEmpty()) {
            throw new CustomException(ErrorCode.SETTLEMENT_CAN_NOT_CREATE);
        }

        log.info("정산 생성 완료 - ID: {}", settlementVO.getSettlementId());

        createMember(settlementVO, members);

        var settlementResponseDTO = get(settlementVO.getSettlementId());

        for(var member : settlementResponseDTO.getMembers()){

            messagingTemplate.convertAndSendToUser(
                    String.valueOf(member.getUserId()),
                    "/queue/settlements",
                    settlementResponseDTO
            );

        }

        return settlementResponseDTO;
    }

    private void createMember(SettlementVO settlementVO, List<SettlementMemberRequestDTO> members) {

        for (var member : members) {
            SettlementMemberVO settlementMemberVO = SettlementMemberVO.of(settlementVO.getSettlementId(), member);
            settlementMapper.createMember(settlementMemberVO);

            notificationService.createSettlementNotification(
                    settlementVO.getRequesterId(),
                    member.getUserId(),
                    settlementVO.getSettlementId(),
                    Enum.NotificationType.SETTLEMENT_REQUEST
            );
            try {
                notificationService.create(
                        NotificationRequestDTO.builder()
                                .receiverId(member.getUserId())
                                .senderId(settlementVO.getRequesterId())
                                .notificationType(Enum.NotificationType.SETTLEMENT_REQUEST)
                                .targetId(settlementVO.getSettlementId())
                                .build());
            } catch (Exception e) {
                log.warn("알림 생성 중 오류 발생 (무시하고 정산 진행): {}", e.getMessage());
            }
        }
    }


    @Transactional
    @Override
    public SettlementResponseDTO get(int settlementId) {

       SettlementVO settlementVO = settlementMapper.get(settlementId);

       if(settlementVO == null){
           throw new CustomException(ErrorCode.SETTLEMENT_NOT_FOUND);
       }

       return SettlementResponseDTO.of(settlementVO);
    }

    @Transactional
    @Override
    public SettlementResponseDTO payment(int settlementId, int userId) {

        SettlementVO settlementVO =  settlementMapper.get(settlementId);

        if(settlementVO ==null){
            throw new CustomException(ErrorCode.SETTLEMENT_NOT_FOUND);
        }


        boolean isRequest = settlementVO.getMembers().stream()
                .anyMatch(member ->
                        member.getUserId() == userId &&
                                member.getStatus() == Enum.SettlementStatus.REQUEST
                );


        //이미 지불 -> 예외처리
        if(!isRequest)
        {
            throw  new CustomException(ErrorCode.SETTLEMENT_ALREADY_PAYMENT);
        }

        //송금 api 호출


        //송금 후 정산 완료 처리
        settlementMapper.completeMemberSettlement(settlementId , userId);

        //모든 참여자가 COMPLETE인지 확인
        boolean completed = settlementMapper.isAllMemberCompleted(settlementId);

        if (completed) {

            //settlement_tbl 상태 COMPLETE
            settlementMapper.completeSettlement(settlementId);

            notificationService.createSettlementNotification(
                    settlementVO.getRequesterId(),
                    settlementVO.getRequesterId(),
                    settlementVO.getSettlementId(),
                    Enum.NotificationType.SETTLEMENT_COMPLETE
            );


            for(var member :settlementVO.getMembers()) {

                notificationService.createSettlementNotification(
                        settlementVO.getRequesterId(),
                        member.getUserId(),
                        settlementVO.getSettlementId(),
                        Enum.NotificationType.SETTLEMENT_COMPLETE
                        );
            }


            //피드 생성
            var feedCreateRequestDTO = FeedCreateRequestDTO.builder()
                    .userId(settlementVO.getRequesterId())
                    .targetId(settlementVO.getSettlementId())
                    .feedType(Enum.FeedType.SETTLEMENT)
                    .content(settlementVO.getContent())
                    .visibility(Enum.VisibilityType.FRIEND)
                    .build();

            feedService.create(feedCreateRequestDTO);
        }
        else{

            //지불한 부분을 요청자에게 알림
            notificationService.createSettlementNotification(
                    userId,
                    settlementVO.getRequesterId(),
                    settlementVO.getSettlementId(),
                    Enum.NotificationType.SETTLEMENT_PAYMENT
            );


        }


        var settlementResponseDTO = get(settlementId);

        messagingTemplate.convertAndSendToUser(
                String.valueOf(settlementResponseDTO.getRequesterId()),
                "/queue/settlements",
                settlementResponseDTO
        );


        if(completed){

            for(var member : settlementResponseDTO.getMembers()){

                messagingTemplate.convertAndSendToUser(
                        String.valueOf(member.getUserId()),
                        "/queue/settlements",
                        settlementResponseDTO
                );

            }
        }
        else{

            for(var member : settlementResponseDTO.getMembers()){

                if(member.getUserId() == userId)
                    continue;

                messagingTemplate.convertAndSendToUser(
                        String.valueOf(member.getUserId()),
                        "/queue/settlements",
                        settlementResponseDTO
                );
            }
        }

        return settlementResponseDTO;
    }

    @Transactional
    @Override
    public boolean cancel(int settlementId, int userId) {

        SettlementVO settlementVO =  settlementMapper.get(settlementId);

        if(settlementVO ==null){
            throw new CustomException(ErrorCode.SETTLEMENT_NOT_FOUND);
        }

        //0.해당 유저가 정산을 요청 한 유저인지 확인 및 이미 취소된 상태인지 확인
        boolean isCanceled = settlementMapper.canCancelSettlement(settlementId, userId);

        if(!isCanceled){
            throw  new CustomException(ErrorCode.SETTLEMENT_CAN_NOT_CANCEL);
        }

        //1.먼저 맴버들이 지불 여부를 확인한다.
        boolean completed = settlementMapper.isAllMemberCompleted(settlementId);

        //2.맴버들 전부 지불 완료
        if(completed){
            return false;
        }

        //3.맴버들 전부 지불 미완료
        //3-2. 지불한 맴버들에게 환불


        //settlement 테이블 , settlement_member 테이블 전부 cancel 처리
        settlementMapper.cancelSettlement(settlementId);
        settlementMapper.cancelMemberSettlement(settlementId);

        for(var member :settlementVO.getMembers()) {

            if(member.getStatus() == Enum.SettlementStatus.COMPLETE){

                //지불한 요청자에게 환불 알림
                notificationService.createSettlementNotification(
                        settlementVO.getRequesterId(),
                        member.getUserId(),
                        settlementVO.getSettlementId(),
                        Enum.NotificationType.SETTLEMENT_CANCEL
                );
            }
            else
            {
                notificationService.createSettlementNotification(
                        settlementVO.getRequesterId(),
                        member.getUserId(),
                        settlementVO.getSettlementId(),
                        Enum.NotificationType.SETTLEMENT_CANCEL
                );
            }
        }

        return true;
    }

    @Override
    public boolean remine(int settlementId, int userId) {

        SettlementVO settlementVO =  settlementMapper.get(settlementId);

        if(settlementVO ==null){
            throw new CustomException(ErrorCode.SETTLEMENT_NOT_FOUND);
        }

//        if (settlementVO.getLastReminderDate() != null) {
//            long diff = System.currentTimeMillis() - settlementVO.getLastReminderDate().getTime();
//
//            // 6시간 = 6 * 60 * 60 * 1000
//            if (diff < 6 * 60 * 60 * 1000L) {
//                // 아직 6시간이 지나지 않음
//                throw new CustomException(ErrorCode.SETTLEMENT_CAN_NOT_REMINE);
//            }
//        }

        var remineMemberList = settlementVO.getMembers().stream()
                .filter(member ->
                                member.getStatus() == Enum.SettlementStatus.REQUEST
                ).toList();

        settlementMapper.updateLastReminderDate(settlementId);

        for(var member :remineMemberList){

            if(member.getStatus() != Enum.SettlementStatus.COMPLETE) {

                //지불안한 유저에게 리마인드
                notificationService.createSettlementNotification(
                        settlementVO.getRequesterId(),
                        member.getUserId(),
                        settlementVO.getSettlementId(),
                        Enum.NotificationType.SETTLEMENT_REMIND
                );
            }

        }

        return true;
    }


    @Override
    public List<SettlementResponseDTO> getMyList(int userId) {

       List<SettlementVO> list = settlementMapper.getMyList(userId);

        if(list == null){
            log.error("settlementMapper.getMyList() returned null");
            throw new CustomException(ErrorCode.DATA_ACCESS_ERROR);
        }

        return list.stream().map(SettlementResponseDTO::of).toList();
    }
}
