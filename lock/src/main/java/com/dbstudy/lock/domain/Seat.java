package com.dbstudy.lock.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "seat")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String seatNumber;

    @Column(nullable = false)
    private boolean isReserved = false;

    @Column
    private String reservedBy;

    @Version
    private Long version; // 낙관적 락용 (Phase 1에서는 미사용, Phase 2 대비)

    public Seat(String seatNumber) {
        this.seatNumber = seatNumber;
        this.isReserved = false;
    }

    public void reserve(String userName) {
        this.isReserved = true;
        this.reservedBy = userName;
    }

    public void cancel() {
        this.isReserved = false;
        this.reservedBy = null;
    }
}
