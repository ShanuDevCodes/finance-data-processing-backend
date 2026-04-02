package com.shanudevcodes.fdpacb.features.records.data.dto;

import com.shanudevcodes.fdpacb.common.exception.validation.anotation.ValidEnum;
import com.shanudevcodes.fdpacb.features.records.data.enums.Category;
import com.shanudevcodes.fdpacb.features.records.data.enums.PaymentMethod;
import com.shanudevcodes.fdpacb.features.records.data.enums.RecordType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRecordRequest {
    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    @NotNull
    @ValidEnum(enumClass = RecordType.class, message = "Invalid record type")
    private String type;
    @NotNull
    @ValidEnum(enumClass = Category.class, message = "Invalid category")
    private String category;
    private String note;
    @NotNull
    @ValidEnum(enumClass = PaymentMethod.class, message = "Invalid payment method")
    private String paymentMethod;
    private Boolean isRecurring;
    private String currency;
}
