package com.onboarding.controller;

import com.onboarding.dto.InvoiceDTO;
import com.onboarding.dto.response.ApiResponse;
import com.onboarding.dto.ProcessResult;
import com.onboarding.dto.response.PageableResponse;
import com.onboarding.service.InvoiceService;
import com.onboarding.service.MongoService;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/v1/invoice")
@RequiredArgsConstructor
@Validated
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final MongoService mongoService;

    @GetMapping("/{accountId}")
    public ResponseEntity<PageableResponse<List<InvoiceDTO>>> getInvoicesByAccountId(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "10") int count) {

            Page<InvoiceDTO> result = mongoService.getByAccountId(accountId, pageNum, count);
            return ResponseEntity.ok(PageableResponse.<List<InvoiceDTO>>builder()
                    .body(result.getContent())
                    .httpStatus(HttpStatus.OK)
                    .message(result.getContent().isEmpty() ? "Not Found" : "Success")
                    .timestamp(LocalDateTime.now()).body(result.getContent()).currentPage(result.getNumber()).
                    totalItems(result.getTotalElements()).totalPages(result.getTotalPages()).currentItems(result.getNumberOfElements())
                    .build());
    }


    @PostMapping("/{invoiceName}")
    public ResponseEntity<ApiResponse<String>> processInvoiceFile(
            @PathVariable @Pattern(regexp = "invoice_\\d{8}\\.csv",
                    message = "Invalid file name format") String invoiceName) throws ExecutionException, InterruptedException {

        ProcessResult result = invoiceService.processFileAsync(invoiceName).get();

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .body(result.getSummary())
                .httpStatus(HttpStatus.OK)
                .message("Success")
                .errors(result.hasErrors() ? result.getErrors() : null)
                .timestamp(LocalDateTime.now())
                .build());
    }

}
