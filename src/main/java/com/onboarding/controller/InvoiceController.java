package com.onboarding.controller;

import com.onboarding.annotation.NotBlankOrNull;
import com.onboarding.annotation.ValidInvoiceName;
import com.onboarding.dto.InvoiceDTO;
import com.onboarding.dto.response.ApiResponse;
import com.onboarding.dto.ProcessResult;
import com.onboarding.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/v1/invoice")
@RequiredArgsConstructor
@Validated
public class InvoiceController {
    private final InvoiceService invoiceService;

    @GetMapping("/{accountId}")
    public ResponseEntity<Page<InvoiceDTO>> getInvoicesByAccountId(
            @PathVariable @NotBlankOrNull String accountId,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

            Page<InvoiceDTO> result = invoiceService.getInvoicesByAccountId(accountId, pageNum, pageSize);
            return new ResponseEntity<>(result , HttpStatus.OK);

    }


    @PostMapping("/{invoiceName}")
    public ResponseEntity<ApiResponse<String>> processInvoiceFile (
            @PathVariable @ValidInvoiceName String invoiceName) throws ExecutionException, InterruptedException {

            ProcessResult result = invoiceService.processFileAsync(invoiceName).get();
        log.info("Result: {}", result);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .body(result.getSummary())
                .httpStatus(HttpStatus.OK)
                .message("Success")
                .errors(result.hasErrors() ? result.getErrors() : null)
                .timestamp(LocalDateTime.now())
                .build());
    }



}
