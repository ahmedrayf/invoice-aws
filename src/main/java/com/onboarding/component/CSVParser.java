package com.onboarding.component;

import com.onboarding.dto.InvoiceDTO;
import exception.InvoiceProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class CSVParser {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public InvoiceDTO parseLine(String csvLine, int lineNumber) throws InvoiceProcessingException {
        try {
            String[] fields = csvLine.split("\\|;", -1);
            if (fields.length < 18) {
                throw new InvoiceProcessingException("Insufficient fields. Expected at least 18");
            }
            log.debug("Parsing line {}: {}", lineNumber, csvLine);

            return InvoiceDTO.builder()
                    .billId(fields[0].trim())
                    .accountId(fields[1].trim())
                    .issueDate(LocalDate.parse(fields[2], DATE_FORMATTER))
                    .billPeriodFrom(LocalDate.parse(fields[3], DATE_FORMATTER))
                    .billPeriodTo(LocalDate.parse(fields[4], DATE_FORMATTER))
                    .name(fields[10] + " " + fields[11])
                    .grossAmount(new BigDecimal(fields[15].trim()))
                    .netAmount(new BigDecimal(fields[16].trim()))
                    .taxAmount(new BigDecimal(fields[17].trim()))
                    .rawLine(csvLine)
                    .build();
        } catch (Exception e) {
            log.error("Error Parsing Line {} : {}",lineNumber, csvLine);
            throw new InvoiceProcessingException(
                    String.format("Line %d: %s", lineNumber, e.getMessage()), e);
        }
    }
}
