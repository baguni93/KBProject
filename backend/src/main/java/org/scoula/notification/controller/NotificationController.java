package org.scoula.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.notification.dto.NotificationRequestDTO;
import org.scoula.notification.dto.NotificationResponseDTO;
import org.scoula.notification.service.NotificationService;
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


    @DeleteMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponseDTO> read(@PathVariable int notificationId){
        return  ResponseEntity.ok(notificationService.read(notificationId));    }

    @DeleteMapping("/read-all")
    public ResponseEntity<List<NotificationResponseDTO>> allRead(@RequestParam int userId){
        return  ResponseEntity.ok(notificationService.readAll(userId));
    }

}
