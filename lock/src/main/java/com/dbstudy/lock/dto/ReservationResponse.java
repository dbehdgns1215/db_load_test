package com.dbstudy.lock.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    
    private boolean success;
    private String message;
    private String seatNumber;
}
