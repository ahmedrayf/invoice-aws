package com.onboarding.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document("invoice")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Invoice {
    @MongoId
    @Field(name = "id")
    private String id;

    @NotBlank
    @Field(name = "bill_id")
    @Indexed(unique = true)
    private String billId;

    @NotBlank
    @Indexed
    @Field(name = "account_id")
    private String accountId;

    @NotNull
    @Field(name = "issue_date")
    private LocalDate issueDate;

    @NotNull
    @Field(name = "bill_period_from")
    private LocalDate billPeriodFrom;

    @NotNull
    @Field(name = "bill_period_to")
    private LocalDate billPeriodTo;

    @NotBlank
    @Field(name = "name")
    private String name;

    @NotNull
    @DecimalMin("0.00")
    @Field(name = "gross_amount")
    private BigDecimal grossAmount;

    @NotNull
    @DecimalMin("0.00")
    @Field(name = "net_amount")
    private BigDecimal netAmount;

    @NotNull
    @DecimalMin("0.00")
    @Field(name = "tax_amount")
    private BigDecimal taxAmount;

    @NotNull
    @Field(name = "created_at")
    private LocalDateTime createdAt;
}
