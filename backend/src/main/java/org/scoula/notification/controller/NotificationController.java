package org.scoula.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.notification.dto.NotificationRequestDTO;
import org.scoula.notification.dto.NotificationResponseDTO;
import org.scoula.notification.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log4j2
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getList(@RequestParam int userId){
        return ResponseEntity.ok(notificationService.getList(userId));
    }

    @PostMapping
    public ResponseEntity<NotificationResponseDTO>
    create(@RequestBody NotificationRequestDTO notificationRequestDTO){
      return  ResponseEntity.ok(notificationService.create(notificationRequestDTO));
    }


    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<HttpStatus> read(@PathVariable int notificationId){
        notificationService.read(notificationId);
        return  ResponseEntity.ok(HttpStatus.OK);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<HttpStatus> allRead(@RequestParam int userId){
        notificationService.readAll(userId);
        return  ResponseEntity.ok(HttpStatus.OK);
    }

}
