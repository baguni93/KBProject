package org.scoula.friend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.feed.dto.FeedResponseDTO;
import org.scoula.friend.domain.FriendRequestVO;
import org.scoula.friend.dto.*;
import org.scoula.friend.service.FriendService;
import org.scoula.settlement.dto.SettlementCreateRequestDTO;
import org.scoula.settlement.dto.SettlementResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log4j2
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    //친구 여부 확인
    @GetMapping("/{checkUserId}/friendStatus")
    public ResponseEntity<FriendStatusResponseDTO> getFriendStatus(
            @PathVariable int checkUserId,
            @RequestParam int userId
    ){
        return ResponseEntity.ok(friendService.getFriendStatus(checkUserId, userId));
    }


    @GetMapping
    public ResponseEntity<List<FriendResponseDTO>> getList(@RequestParam int userId){
        return ResponseEntity.ok(friendService.getList(userId));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendRequestResponseDTO>> getRequestList(@RequestParam int userId){
        return ResponseEntity.ok(friendService.getRequestList(userId));
    }

    @GetMapping("/sendRequests")
    public ResponseEntity<List<FriendRequestResponseDTO>> getSendRequestList(@RequestParam int userId){
        return ResponseEntity.ok(friendService.getsendRequestList(userId));
    }

    @PostMapping
    public ResponseEntity<FriendResponseDTO> createFriend(@RequestBody FriendCreateRequestDTO friendCreateRequestDTO){
        return ResponseEntity.ok(friendService.createFriend(friendCreateRequestDTO));
    }

    @GetMapping("/{friendId}")
    public ResponseEntity<FriendResponseDTO> getFriend(@PathVariable int friendId){
        return ResponseEntity.ok(friendService.getFriend(friendId));
    }

    @DeleteMapping("/{friendUserId}")
    public ResponseEntity<List<FriendResponseDTO>> deleteFriend(
            @PathVariable int friendUserId
            , @RequestParam int userId){
        return ResponseEntity.ok(friendService.deleteFriend(friendUserId,userId));
    }

    @PostMapping("/request")
    public ResponseEntity<FriendRequestResponseDTO> createRequest
            (@RequestBody FriendRequestCreateRequestDTO friendRequestCreateRequestDTO){
            log.info("feed create request : {}",friendRequestCreateRequestDTO);
        return ResponseEntity.ok(friendService.createRequest(friendRequestCreateRequestDTO));
    }

    @GetMapping("/requests/{requestId}")
    public ResponseEntity<FriendRequestResponseDTO> get
            (@PathVariable int requestId){
        return ResponseEntity.ok(friendService.getRequest(requestId));
    }

    @PatchMapping("/requests/{requestId}/accept")
    public ResponseEntity<HttpStatus> acceptRequest
            (@PathVariable int requestId){
        friendService.acceptRequest(requestId);
        return ResponseEntity.ok(HttpStatus.OK);
    }


    @PatchMapping("/requests/{requestId}/reject")
    public ResponseEntity<HttpStatus>  rejectRequest
            (@PathVariable int requestId){
        friendService.rejectRequest(requestId);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ResponseEntity<HttpStatus>  cancelRequest
            (@PathVariable int requestId){
        friendService.cancelRequest(requestId);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
