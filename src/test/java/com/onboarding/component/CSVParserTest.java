package com.onboarding.component;

import com.onboarding.dto.InvoiceDTO;
import com.onboarding.handler.InvoiceProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

class CSVParserTest {

    private final CSVParser parser = new CSVParser();

    @Test
    void parseLine_validInputs() throws InvoiceProcessingException {
        String validLine = "BILL001|;ACC123|;01.01.2023|;01.12.2022|;31.12.2022|;field5|;field6|;field7|;field8|;field9|;John|;Doe|;field12|;field13|;field14|;100.00|;80.00|;20.00";

        InvoiceDTO result = parser.parseLine(validLine, 1);

        assertEquals("BILL001", result.getBillId());
        assertEquals("ACC123", result.getAccountId());
        assertEquals(LocalDate.of(2023, 1, 1), result.getIssueDate());
        assertEquals(LocalDate.of(2022, 12, 1), result.getBillPeriodFrom());
        assertEquals(LocalDate.of(2022, 12, 31), result.getBillPeriodTo());
        assertEquals("John Doe", result.getName());
        assertEquals(new BigDecimal("100.00"), result.getGrossAmount());
        assertEquals(new BigDecimal("80.00"), result.getNetAmount());
        assertEquals(new BigDecimal("20.00"), result.getTaxAmount());
        assertEquals(validLine, result.getRawLine());
    }

    @Test
    void parseLine_emptyFields_usesEmptyStrings() throws InvoiceProcessingException {
        String line = "|;|;01.01.2023|;01.01.2023|;01.01.2023|;|;|;|;|;|;|;|;|;|;|;0.00|;0.00|;0.00";

        InvoiceDTO result = parser.parseLine(line, 1);

        assertEquals("", result.getBillId());
        assertEquals("", result.getAccountId());
        assertEquals(" ", result.getName());
    }


    @ParameterizedTest(name = "[{index}] {0}")
    @ValueSource(strings = {
            "", // empty line (0 fields)
            "|;|;|;|;|;|;|;|;|;|;|;|;|;|;|;|;", // 17 delimiters but only 17 fields (needs 18)
            "|||||||||||||||||", // all empty fields (wrong delimiter format)
            "BILL001;ACC123;01.01.2023;...;20.00", // wrong delimiter (using ; instead of |;)
            "BILL001|;ACC123|;invalid-date|;01.01.2023|;01.01.2023|;f5|;f6|;f7|;f8|;f9|;John|;Doe|;f12|;f13|;f14|;100.00|;80.00|;20.00", // invalid issueDate
            "BILL001|;ACC123|;01.01.2023|;invalid-date|;01.01.2023|;f5|;f6|;f7|;f8|;f9|;John|;Doe|;f12|;f13|;f14|;100.00|;80.00|;20.00", // invalid bill_period_from
            "BILL001|;ACC123|;01.01.2023|;01.01.2023|;invalid-date|;f5|;f6|;f7|;f8|;f9|;John|;Doe|;f12|;f13|;f14|;100.00|;80.00|;20.00", // invalid bill_period_to
            "BILL001|;ACC123|;01.01.2023|;01.01.2023|;01.01.2023|;f5|;f6|;f7|;f8|;f9|;John|;Doe|;f12|;f13|;f14|;not-a-number|;80.00|;20.00", // invalid grossAmount
            "BILL001|;ACC123|;01.01.2023|;01.01.2023|;01.01.2023|;f5|;f6|;f7|;f8|;f9|;John|;Doe|;f12|;f13|;f14|;100.00|;not-a-number|;20.00", // invalid netAmount
            "BILL001|;ACC123|;01.01.2023|;01.01.2023|;01.01.2023|;f5|;f6|;f7|;f8|;f9|;John|;Doe|;f12|;f13|;f14|;100.00|;80.00|;not-a-number", // invalid taxAmount
            "BILL001|;ACC123|;32.01.2023|;01.01.2023|;01.01.2023|;f5|;f6|;f7|;f8|;f9|;John|;Doe|;f12|;f13|;f14|;100.00|;80.00|;20.00", // invalid day in date
            "BILL001|;ACC123|;01.13.2023|;01.01.2023|;01.01.2023|;f5|;f6|;f7|;f8|;f9|;John|;Doe|;f12|;f13|;f14|;100.00|;80.00|;20.00", // invalid month in date
            "BILL001|;ACC123|;01.01.2023|;01.01.2023|;01.01.2023|;f5|;f6|;f7|;f8|;f9|;John|;Doe|;f12|;f13|;f14|;100,00|;80.00|;20.00", // comma decimal separator
            "BILL001|;ACC123|;01.01.2023|;01.01.2023|;01.01.2023|;f5|;f6|;f7|;f8|;f9|;John|;Doe|;f12|;f13|;f14|;100.00|;80,00|;20.00", // mixed decimal separators
    })
    void parseLine_invalidFormats_throwsExceptions(String line) {
        InvoiceProcessingException exception = assertThrows(
                InvoiceProcessingException.class,
                () -> parser.parseLine(line, 3)
        );

        assertTrue(exception.getMessage().startsWith("Line 3:"),
                "Exception message should include line number");

        // Additional assertions for specific error types
        if (line.contains("invalid-date")) {
            assertInstanceOf(DateTimeParseException.class, exception.getCause(), "Should contain date parsing exception");
        } else if (line.contains("not-a-number") || line.contains(",")) {
            assertInstanceOf(NumberFormatException.class, exception.getCause(), "Should contain number format exception");
        }
    }

}
