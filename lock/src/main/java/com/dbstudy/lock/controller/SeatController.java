package com.dbstudy.lock.controller;

import com.dbstudy.lock.domain.Seat;
import com.dbstudy.lock.dto.ReservationRequest;
import com.dbstudy.lock.dto.ReservationResponse;
import com.dbstudy.lock.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    /**
     * 좌석 예약 API (동시성 제어 없음)
     */
    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserveSeat(@RequestBody ReservationRequest request) {
        log.info("예약 요청 수신 - seatId: {}, userName: {}", request.getSeatId(), request.getUserName());
        
        ReservationResponse response = seatService.reserveSeatWithoutLock(
                request.getSeatId(), 
                request.getUserName()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * 좌석 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<Seat> getSeat(@PathVariable Long id) {
        Seat seat = seatService.getSeat(id);
        return ResponseEntity.ok(seat);
    }

    /**
     * 초기 데이터 생성 API (테스트용)
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, String>> initSeats(@RequestParam(defaultValue = "10") int count) {
        seatService.initSeats(count);
        return ResponseEntity.ok(Map.of("message", count + "개의 좌석이 생성되었습니다"));
    }

    /**
     * 예약 취소 API (테스트용)
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancelReservation(@PathVariable Long id) {
        seatService.cancelReservation(id);
        return ResponseEntity.ok(Map.of("message", "예약이 취소되었습니다"));
    }

    /**
     * 헬스체크 API
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "server", System.getenv().getOrDefault("HOSTNAME", "unknown")
        ));
    }
}
