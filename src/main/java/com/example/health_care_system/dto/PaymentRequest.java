package com.example.health_care_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PaymentRequest {

    private Long amount;
    private Long quantity;
    private String name;
    private String currency;

}
