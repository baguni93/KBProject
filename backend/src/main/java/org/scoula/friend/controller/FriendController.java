package org.scoula.friend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.feed.dto.FeedResponseDTO;
import org.scoula.friend.domain.FriendRequestVO;
import org.scoula.friend.dto.FriendCreateRequestDTO;
import org.scoula.friend.dto.FriendRequestCreateRequestDTO;
import org.scoula.friend.dto.FriendRequestResponseDTO;
import org.scoula.friend.dto.FriendResponseDTO;
import org.scoula.friend.service.FriendService;
import org.scoula.settlement.dto.SettlementCreateRequestDTO;
import org.scoula.settlement.dto.SettlementResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log4j2
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping
    public ResponseEntity<List<FriendResponseDTO>> getList(@RequestParam int userId){
        return ResponseEntity.ok(friendService.getList(userId));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendRequestResponseDTO>> getRequestList(@RequestParam int userId){
        return ResponseEntity.ok(friendService.getRequestList(userId));
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
    public ResponseEntity<FriendRequestResponseDTO> acceptRequest
            (@PathVariable int requestId){
        return ResponseEntity.ok(friendService.acceptRequest(requestId));
    }


    @PatchMapping("/requests/{requestId}/reject")
    public ResponseEntity<FriendRequestResponseDTO>  rejectRequest
            (@PathVariable int requestId){
        return ResponseEntity.ok(friendService.rejectRequest(requestId));
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ResponseEntity<FriendRequestResponseDTO>  cancelRequest
            (@PathVariable int requestId){
        return ResponseEntity.ok(friendService.cancelRequest(requestId));
    }
}
