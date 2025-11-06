package com.dkp.exchange.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionRequest {
    @NotBlank(message = "Asset is required")
    private String asset;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00000001", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Address is required")
    private String address;

    private String memo;
}
