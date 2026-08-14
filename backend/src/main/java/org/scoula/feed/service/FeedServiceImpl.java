package org.scoula.feed.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.UploadFiles;
import org.scoula.common.util.UploadPathName;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.feed.domain.FeedImageVO;
import org.scoula.feed.domain.FeedVO;
import org.scoula.feed.dto.FeedCreateRequestDTO;
import org.scoula.feed.dto.FeedImageDTO;
import org.scoula.feed.dto.FeedResponseDTO;
import org.scoula.feed.dto.FeedUpdateRequestDTO;
import org.scoula.feed.mapper.FeedMapper;
import org.scoula.like.service.LikeService;
import org.scoula.settlement.mapper.SettlementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class FeedServiceImpl implements FeedService {

    private final FeedMapper feedMapper;
    private final SettlementMapper settlementMapper;
    private final LikeService likeService;

    @Transactional
    @Override
    public FeedResponseDTO create(FeedCreateRequestDTO request) {

        FeedVO feedVO = request.toVo();

        //피드 생성
        feedMapper.create(feedVO);

        List<MultipartFile> files = request.getFiles();
        if(files != null && !files.isEmpty()) { // 첨부 파일이 있는 경우
            upload(feedVO.getFeedId(), files);
        }

        return get(feedVO.getFeedId());
    }


    @Transactional
    @Override
    public FeedResponseDTO get(int feedId){
        // 공통 정보 조회
        FeedVO feed = feedMapper.getFeedCommon(feedId);

        if (feed == null) {
            throw new CustomException(ErrorCode.FEED_NOT_FOUND);
        }

        enrichFeed(feed);

        return FeedResponseDTO.of(feed);

    }

    @Transactional
    @Override
    public List<FeedResponseDTO> getList(int userId, int page, int size){
        int offset = page * size;

       List<FeedVO> list = feedMapper.getList(userId,offset,size);
        return getFeedRespoonseDTOList(list);
    }

    @Transactional
    @Override
    public List<FeedResponseDTO> getFriendList(int userId) {

        List<FeedVO> list = feedMapper.getFriendList(userId);

        return getFeedRespoonseDTOList(list);
    }

    @Transactional
    @Override
    public List<FeedResponseDTO> getMyList(int userId) {

        List<FeedVO> list = feedMapper.getMyList(userId);

        return getFeedRespoonseDTOList(list);
    }


    @Transactional
    @Override
    public FeedResponseDTO delete(int feedId){

        FeedResponseDTO responseDTO = get(feedId);

        feedMapper.delete(feedId);

        return responseDTO;
    }

    @Override
    public FeedImageDTO getImage(int imageId) {
        FeedImageVO image = feedMapper.getImage(imageId);

        return FeedImageDTO.of(image);
    }

    @Override
    public List<FeedResponseDTO> geMemberList(int memberUserId, int userId) {

        //친구 여부 확인 후
        //공개 설정 피드 or 공개 + 친구 설정 피드
        List<FeedVO> list = feedMapper.geMemberList(memberUserId,userId);
        log.info(list);
        return getFeedRespoonseDTOList(list);

    }

    @Transactional
    @Override
    public void updateFeed(FeedUpdateRequestDTO feedUpdateRequestDTO) {

        feedMapper.update(feedUpdateRequestDTO.toVo());

        if(feedUpdateRequestDTO.getDeleteFiles() !=null){
            for(var imageId : feedUpdateRequestDTO.getDeleteFiles()){

                FeedImageVO imageVO = feedMapper.getImage(imageId);

                if(imageVO != null){

                    File file = new File(UploadPathName.getFeedPath() + imageVO.getImageName());
                    // 서버에 실제 파일이 있으면 삭제
                    if (file.exists()) {
                        file.delete();
                    }

                    // DB 첨부파일 정보 삭제
                    feedMapper.deleteImage(imageId);
                }

            }
        }

        // 3. 새 첨부파일 추가
        List<MultipartFile> files = feedUpdateRequestDTO.getFiles();
        if(files != null && !files.isEmpty()) { // 첨부 파일이 있는 경우
            upload(feedUpdateRequestDTO.getFeedId(), files);
        }
    }

    private List<FeedResponseDTO> getFeedRespoonseDTOList(List<FeedVO> feedList){

        if(feedList == null){
            log.error("feedMapper.getList() returned null");
            throw new CustomException(ErrorCode.DATA_ACCESS_ERROR);
        }

        for(var feed : feedList){
            enrichFeed(feed);

        }

        List<FeedResponseDTO> feedResponseDTOList =  feedList.stream().map(FeedResponseDTO::of).toList();

        for(var feedResponseDTO : feedResponseDTOList){

            feedResponseDTO.setLikeCount(
                    likeService.getLikeCount(feedResponseDTO.getFeedId())
            );

            feedResponseDTO.setLiked(
                    likeService.isLiked(
                            feedResponseDTO.getFeedId(),
                            feedResponseDTO.getUserId()
                    )
            );
        }

        return feedResponseDTOList;
    }



    private void enrichFeed(FeedVO feed){

        switch (feed.getFeedType()) {

            case TRANSFER:
            case PAYMENT:
                log.info("TRANSFER , PAYMENT");
                feed.setTransaction(feedMapper.getTransaction(feed.getTargetId()));
                break;

            case SETTLEMENT:
                log.info("SETTLEMENT");
                feed.setSettlement(settlementMapper.get(feed.getTargetId()));
                break;

            case CARD:
                log.info("CARD");
                feed.setCard(feedMapper.getCard(feed.getTargetId()));
                break;

            case ANALYSIS:
                log.info("ANALYSIS");
                feed.setAnalysis(feedMapper.getAnalysis(feed.getTargetId()));
                break;

            case EVENT:
                log.info("Event");
                feed.setEvent(feedMapper.getEvent(feed.getTargetId()));
                break;
        }
    }


    private void upload(int feedId, List<MultipartFile> files) {
        for(MultipartFile part: files) {
            if(part.isEmpty()) continue;
            try {
                String fileName =  UploadFiles.uploadAndGetFileName(UploadPathName.getFeedPath(), part);
                FeedImageVO feedImageVO = FeedImageVO.of(fileName, feedId);
                feedMapper.createFeedImage(feedImageVO);
            } catch (IOException e) {
                throw new RuntimeException(e); // @Transactional에서 감지, 자동 rollback
            }
        }
    }
}
