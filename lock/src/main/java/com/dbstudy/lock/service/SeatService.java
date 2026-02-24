package com.dbstudy.lock.service;

import com.dbstudy.lock.domain.Seat;
import com.dbstudy.lock.dto.ReservationResponse;
import com.dbstudy.lock.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    @Value("${app.server-id:SERVER-?}")
    private String serverId;

    /**
     * Phase 1: 동시성 제어 없는 좌석 예약
     * 의도적으로 락을 사용하지 않아 동시성 문제를 재현합니다.
     */
    @Transactional
    public ReservationResponse reserveSeatWithoutLock(Long seatId, String userName) {
        String thread = Thread.currentThread().getName();

        try {
            // 1. 좌석 조회
            log.info("🔍 [{}] [{}] STEP 1: 좌석 조회 시작 - seatId: {}, userName: {}",
                    serverId, thread, seatId, userName);

            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다"));

            // 2. 예약 가능 여부 확인 (동시성 문제 발생 지점!)
            boolean reserved = seat.isReserved();
            log.info("🚦 [{}] [{}] STEP 2: 예약 가능 체크 - seatId: {}, isReserved: {} → {}",
                    serverId, thread, seatId, reserved, reserved ? "❌ 차단" : "✅ 통과!");

            if (reserved) {
                log.warn("🚫 [{}] [{}] 이미 예약된 좌석 - seatId: {}, reservedBy: {}",
                        serverId, thread, seatId, seat.getReservedBy());
                return new ReservationResponse(false, "이미 예약된 좌석입니다", seat.getSeatNumber());
            }

            // 3. 의도적인 지연으로 동시성 문제 확대
            log.info("⏳ [{}] [{}] STEP 3: 100ms 지연 시작 (동시성 문제 확대 구간)",
                    serverId, thread);
            Thread.sleep(100);

            // 4. 예약 처리
            seat.reserve(userName);
            seatRepository.save(seat);

            log.info("✅ [{}] [{}] STEP 4: 예약 성공! seatId: {} → userName: {}",
                    serverId, thread, seatId, userName);

            return new ReservationResponse(true, "예약 성공", seat.getSeatNumber());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("💥 [{}] 예약 중 인터럽트 발생", serverId, e);
            return new ReservationResponse(false, "예약 처리 중 오류 발생", null);
        } catch (Exception e) {
            log.error("💥 [{}] 예약 중 오류 발생 - {}", serverId, e.getMessage());
            return new ReservationResponse(false, e.getMessage(), null);
        }
    }

    /**
     * 좌석 조회
     */
    @Transactional(readOnly = true)
    public Seat getSeat(Long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다"));
    }

    /**
     * 초기 데이터 생성 (테스트용)
     */
    @Transactional
    public void initSeats(int count) {
        seatRepository.deleteAll();

        for (int i = 1; i <= count; i++) {
            Seat seat = new Seat("A-" + i);
            seatRepository.save(seat);
        }

        log.info("📋 [{}] 초기 좌석 {}개 생성 완료", serverId, count);
    }

    /**
     * 예약 취소 (테스트용)
     */
    @Transactional
    public void cancelReservation(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다"));
        seat.cancel();
        seatRepository.save(seat);
        log.info("🔄 [{}] 좌석 예약 취소 - seatId: {}", serverId, seatId);
    }
}
