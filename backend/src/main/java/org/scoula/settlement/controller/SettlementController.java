package org.scoula.settlement.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.settlement.dto.SettlementCreateRequestDTO;
import org.scoula.settlement.dto.SettlementResponseDTO;
import org.scoula.settlement.service.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log4j2
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping
    public ResponseEntity<SettlementResponseDTO> create
            (@RequestBody SettlementCreateRequestDTO settlementCreateRequestDTO){

        return ResponseEntity.ok(settlementService.create(settlementCreateRequestDTO));
    }

    @GetMapping("/me")
    public ResponseEntity<List<SettlementResponseDTO>> getMyList(@RequestParam int userId){
        return ResponseEntity.ok(settlementService.getMyList(userId));
    }

    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponseDTO> get(@PathVariable  int settlementId) {
        return ResponseEntity.ok(settlementService.get(settlementId));
    }

    @PatchMapping("/{settlementId}/payment")
    public ResponseEntity<SettlementResponseDTO>  payment(@PathVariable int settlementId , @RequestParam int userId){
        return ResponseEntity.ok( settlementService.payment(settlementId, userId));
    }

    @PatchMapping("/{settlementId}/cancel")
    public ResponseEntity<Boolean>  cancel(@PathVariable int settlementId , @RequestParam int userId){
        return ResponseEntity.ok(settlementService.cancel(settlementId , userId));
    }

    @PatchMapping("/{settlementId}/remine")
    public ResponseEntity<Boolean> remine(@PathVariable int settlementId , @RequestParam int userId){
        return ResponseEntity.ok(settlementService.remine(settlementId , userId));
    }
}
