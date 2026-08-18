package org.scoula.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.comment.domain.CommentVO;
import org.scoula.comment.dto.CommentRequestDTO;
import org.scoula.comment.dto.CommentResponseDTO;
import org.scoula.comment.mapper.CommentMapper;
import org.scoula.common.util.Enum;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.feed.dto.FeedResponseDTO;
import org.scoula.feed.service.FeedService;
import org.scoula.notification.service.NotificationGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final FeedService feedService;
    private final NotificationGroupService notificationGroupService;

    /* =========================
     * 댓글 생성
     * ========================= */

    @Transactional
    @Override
    public CommentResponseDTO create(
            CommentRequestDTO commentRequestDTO
    ) {

        CommentVO vo = commentRequestDTO.toVo();

        FeedResponseDTO feedResponseDTO =
                feedService.get(
                        vo.getFeedId(),
                        vo.getUserId()
                );

        // 댓글 생성
        commentMapper.create(vo);

        // 피드 작성자
        int receiverId = feedResponseDTO.getUserId();

        // 댓글 알림 그룹에 참여
        notificationGroupService.addActor(
                vo.getFeedId(),
                vo.getUserId(),
                receiverId,
                Enum.NotificationType.COMMENT
        );

        return get(
                vo.getCommentId(),
                vo.getUserId()
        );
    }

    /* =========================
     * 댓글 조회
     * ========================= */

    @Transactional
    @Override
    public CommentResponseDTO get(
            int commentId,
            int userId
    ) {

        CommentVO vo =
                commentMapper.get(commentId);

        return CommentResponseDTO.of(
                vo,
                userId
        );
    }

    /* =========================
     * 댓글 목록
     * ========================= */

    @Override
    public List<CommentResponseDTO> getList(
            int feedId
    ) {

        List<CommentVO> list =
                commentMapper.getList(feedId);

        return list.stream()
                .map(
                        x -> CommentResponseDTO.of(
                                x,
                                x.getUserId()
                        )
                )
                .toList();
    }

    /* =========================
     * 댓글 삭제
     * ========================= */

    @Transactional
    @Override
    public void delete(int commentId) {

        CommentVO commentVO =
                commentMapper.get(commentId);

        if (commentVO == null) {
            throw new CustomException(
                    ErrorCode.COMMENT_NOT_FOUND
            );
        }

        // 피드 작성자 조회
        FeedResponseDTO feedResponseDTO =
                feedService.get(
                        commentVO.getFeedId(),
                        commentVO.getUserId()
                );

        // 댓글 알림 그룹에서 제거
        // 이미 COMPLETED 된 그룹은
        // NotificationGroupService에서
        // WAITING 그룹만 찾기 때문에 건드리지 않음
        notificationGroupService.removeActor(
                commentVO.getFeedId(),
                commentVO.getUserId(),
                feedResponseDTO.getUserId(),
                Enum.NotificationType.COMMENT
        );

        // 댓글 삭제
        commentMapper.delete(commentId);
    }

    /* =========================
     * 댓글 수정
     * ========================= */

    @Transactional
    @Override
    public CommentResponseDTO update(
            CommentRequestDTO commentRequestDTO
    ) {

        CommentVO vo =
                commentRequestDTO.toVo();

        if (commentMapper.get(vo.getCommentId()) == null) {
            throw new CustomException(
                    ErrorCode.COMMENT_NOT_FOUND
            );
        }

        commentMapper.update(vo);

        return get(
                vo.getCommentId(),
                vo.getUserId()
        );
    }
}