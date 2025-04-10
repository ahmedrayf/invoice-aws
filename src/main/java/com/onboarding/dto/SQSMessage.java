package com.onboarding.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class SQSMessage {
    private String accountId;
    private String issueDate;
    private String publishDate;
    private String content;
}
