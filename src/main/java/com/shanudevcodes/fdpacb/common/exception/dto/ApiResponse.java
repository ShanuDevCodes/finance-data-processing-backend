package com.shanudevcodes.fdpacb.common.exception.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {
    private String status;   // success / error
    private String message;
    private T data;
}
