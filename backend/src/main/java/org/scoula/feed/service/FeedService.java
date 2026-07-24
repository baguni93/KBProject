package org.scoula.feed.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.UploadFiles;
import org.scoula.common.util.UploadPathName;
import org.scoula.feed.domain.FeedImageVO;
import org.scoula.feed.domain.FeedVO;
import org.scoula.feed.domain.TransactionVO;
import org.scoula.feed.dto.FeedCreateRequestDTO;
import org.scoula.feed.dto.FeedImageDTO;
import org.scoula.feed.dto.FeedResponseDTO;
import org.scoula.feed.mapper.FeedMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

// 외부에서 호출하는 서비스
@Service
@RequiredArgsConstructor
@Log4j2
public class FeedService {

    private final FeedMapper feedMapper;

    @Transactional
    public FeedResponseDTO create(FeedCreateRequestDTO request) {

        FeedVO feedVO = request.toVo();

        //피드 생성
        feedMapper.create(feedVO);
        FeedResponseDTO response = FeedResponseDTO.of(feedVO);

        List<MultipartFile> files = request.getFiles();
        if(files != null && !files.isEmpty()) { // 첨부 파일이 있는 경우
            upload(feedVO.getFeedId(), files);
        }

        return get(response.getFeedId());
    }


    public FeedResponseDTO get(int feedId) {

        log.info(feedMapper.get(feedId));

        return FeedResponseDTO.of(feedMapper.get(feedId));
    }


    private void upload(int feedId, List<MultipartFile> files) {
        for(MultipartFile part: files) {
            if(part.isEmpty()) continue;
            try {
                String uploadPath = UploadFiles.upload(UploadPathName.getFeedPath(), part);
                FeedImageVO feedImageVO = FeedImageVO.of(part, feedId, uploadPath);
                feedMapper.createFeedImage(feedImageVO);
            } catch (IOException e) {
                throw new RuntimeException(e); // @Transactional에서 감지, 자동 rollback
            }
        }
    }



//
//    public List<FeedCreateRequestDTO> getList(){
//
//       List<FeedVO> list = feedMapper.getList();
//
//       if(list == null){
//           log.info("List<FeedVO> list null");
//           throw new RuntimeException();
//       }
//        return list.stream().map(FeedCreateRequestDTO::of).toList();
//    }
//
//    public List<FeedCreateRequestDTO> getFriendList(int userId) {
//        List<FeedVO> list = feedMapper.getFriendList(userId);
//
//        if(list == null){
//            log.info("List<FeedVO> list null");
//            throw new RuntimeException();
//        }
//        return list.stream().map(FeedCreateRequestDTO::of).toList();
//    }
//
//    public List<FeedCreateRequestDTO> getMyList(int userId) {
//        List<FeedVO> list = feedMapper.getMyList(userId);
//
//        if(list == null){
//            log.info("List<FeedVO> list null");
//            throw new RuntimeException();
//        }
//        return list.stream().map(FeedCreateRequestDTO::of).toList();
//    }
//
//
//    public FeedImageDTO getImage(int imageId) {
//        FeedImageVO image = feedMapper.getImage(imageId);
//        return FeedImageDTO.of(image);
//
//    }
//
//    public void delete(int feedId){
//        feedMapper.delete(feedId);
//    }


}
