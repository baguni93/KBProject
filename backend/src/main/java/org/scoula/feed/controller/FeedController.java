package org.scoula.feed.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.UploadFiles;
import org.scoula.common.util.UploadPathName;
import org.scoula.feed.dto.FeedCreateRequestDTO;
import org.scoula.feed.dto.FeedImageDTO;
import org.scoula.feed.dto.FeedResponseDTO;
import org.scoula.feed.service.FeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
@Log4j2
public class FeedController {

    private final FeedService feedService;

    //전체 피드
    @GetMapping
    public ResponseEntity<List<FeedResponseDTO>>getFeedList(@RequestParam int userId) {
        return ResponseEntity.ok(feedService.getList(userId));
    }

    // 친구 피드
    @GetMapping("/friends")
    public ResponseEntity<List<FeedResponseDTO>> getFriendFeedList(@RequestParam int userId) {
        return ResponseEntity.ok(feedService.getFriendList(userId));
    }

    // 내 피드
    @GetMapping("/me")
    public ResponseEntity<List<FeedResponseDTO>> getMyFeedList(@RequestParam int userId) {

        return ResponseEntity.ok(feedService.getMyList(userId));
    }

    @GetMapping("/{feedId}")
    public ResponseEntity<FeedResponseDTO> get(@PathVariable int feedId){
        return ResponseEntity.ok(feedService.get(feedId));
    }

    @PostMapping
    public ResponseEntity<FeedResponseDTO> createFeed(@RequestBody FeedCreateRequestDTO feedCreateRequestDTO) {
        log.info("feed create request : {}", feedCreateRequestDTO);
        return ResponseEntity.ok(feedService.create(feedCreateRequestDTO));
    }

    @PatchMapping("/{feedId}")
    public ResponseEntity<FeedResponseDTO> delete(@PathVariable int feedId){
        return ResponseEntity.ok(feedService.delete(feedId));
    }


    @GetMapping("/image/{imageId}")
    public void viewImage(@PathVariable int imageId, HttpServletResponse response) {
        FeedImageDTO image = feedService.getImage(imageId);
        File file = new File(image.getPath());
        UploadFiles.downloadImage(response, file);
    }

    @GetMapping("/cardImage/{imageName}")
    public void viewCardImage(@PathVariable String imageName, HttpServletResponse response) {
        File file = new File(UploadPathName.getCardPath()+imageName);
        UploadFiles.downloadImage(response, file);
    }

    @GetMapping("/eventImage/{imageName}")
    public void viewEventImage(@PathVariable String imageName, HttpServletResponse response) {
        File file = new File(UploadPathName.getEventPath()+imageName);
        UploadFiles.downloadImage(response, file);
    }


//JWT 붙이면 처리
//        @AuthenticationPrincipal CustomUser customUser
//            MemberVO member = customUser.getMember();
}
