package com.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class ProcessResult {
    private final String filename;
    private int successCount;
    private final Map<Integer, String> errors = new LinkedHashMap<>();



    public void incrementSuccessCount(int count) {
        successCount+= count;
    }

    public void addError(int lineNumber, String message) {
        errors.put(lineNumber, message);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public String getSummary() {
        return String.format(
                "File: %s | Success: %d | Errors: %d",
                filename, successCount, errors.size()
        );
    }


}