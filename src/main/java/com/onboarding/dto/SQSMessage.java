package com.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SQSMessage {
    private String accountId;
    private LocalDate issueDate;
    private LocalDate publishDate;
    private String content;
}
