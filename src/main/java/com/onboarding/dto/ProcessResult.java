package com.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class ProcessResult {
    private final String filename;
    private int successCount = 0;
    private final Map<Integer, String> errors = new LinkedHashMap<>();

    public ProcessResult(String filename) {
        this.filename = filename;
    }

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