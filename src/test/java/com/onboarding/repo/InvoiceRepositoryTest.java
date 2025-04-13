package com.onboarding.repo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.onboarding.config.CacheTestConfig;
import com.onboarding.entity.Invoice;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(CacheTestConfig.class)
@ActiveProfiles("junit")
 class  InvoiceRepositoryTest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CacheManager cacheManager;

    private static List<Invoice> testInvoices;

    @BeforeEach
    void setUp() throws IOException {
        invoiceRepository.deleteAll();
        File file = ResourceUtils.getFile("src/test/resources/invoices/success/entity/invoices-entities.json");
        String content = Files.readString(file.toPath());
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        testInvoices = mapper.readValue(content, new TypeReference<List<Invoice>>() {});
        invoiceRepository.saveAll(testInvoices);}

    @AfterEach
    void cleanUp(){
        invoiceRepository.deleteAll();
    }


    @Test
    void testFindByAccountId_WithPaginationAndCache() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        String accountId = testInvoices.get(0).getAccountId();

        // First call to DB
        Page<Invoice> result1 = invoiceRepository.findByAccountId(accountId, pageable);
        assertThat(result1).hasSize(1);

        // Second call cache
        Page<Invoice> result2 = invoiceRepository.findByAccountId(accountId, pageable);
        assertThat(result2).hasSize(1);

        // Check cache
        Object cached = Objects.requireNonNull(cacheManager.getCache("accountInvoices"))
                .get(accountId + "_" + pageable.getPageNumber() + "_" + pageable.getPageSize(), Page.class);
        assertThat(cached).isNotNull();
    }
}
