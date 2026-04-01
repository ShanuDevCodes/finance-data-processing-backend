package com.shanudevcodes.fdpacb.features.records.data.dto;

import com.shanudevcodes.fdpacb.common.exception.validation.anotation.ValidEnum;
import com.shanudevcodes.fdpacb.features.records.data.enums.Category;
import com.shanudevcodes.fdpacb.features.records.data.enums.PaymentMethod;
import com.shanudevcodes.fdpacb.features.records.data.enums.RecordType;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRecordRequest {
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    @ValidEnum(enumClass = RecordType.class, message = "Invalid record type")
    private String type;
    @ValidEnum(enumClass = Category.class, message = "Invalid category")
    private String category;
    private String note;
    @ValidEnum(enumClass = PaymentMethod.class, message = "Invalid payment method")
    private String paymentMethod;
    private Boolean isRecurring;
    private String currency;
}