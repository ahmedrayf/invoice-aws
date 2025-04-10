package com.onboarding.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document("invoices")
@Builder
public class Invoice {
    @Id
    private String id;

    @NotBlank
    private String billId;

    @NotBlank
    @Indexed
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

    @NotNull
    private LocalDateTime createdAt;
}
