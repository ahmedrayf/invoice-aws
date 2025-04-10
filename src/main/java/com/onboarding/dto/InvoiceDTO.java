package com.onboarding.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceDTO {
    @NotBlank
    private String billId;

    @NotBlank
    private String accountId;

    @NotNull
    private LocalDate issueDate;

    @NotNull
    private LocalDate billPeriodFrom;

    @NotNull
    private LocalDate billPeriodTo;

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal grossAmount;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal netAmount;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal taxAmount;

    private String rawLine;
}
