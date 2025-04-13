package com.onboarding.service;

import com.onboarding.dto.InvoiceDTO;
import com.onboarding.entity.Invoice;
import com.onboarding.mapper.InvoiceMapper;
import com.onboarding.repo.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MongoService {
    private final InvoiceRepository invoiceRepo;
    private final InvoiceMapper invoiceMapper;

    @Transactional
    public void saveAll(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty())
            throw new IllegalArgumentException("Invoices is null or empty");

        log.info("Adding invoices to Mongo");
        invoiceRepo.saveAll(invoices);
    }

    public Page<InvoiceDTO> getByAccountId(String accountId ,int pageNumber ,int pageCount)
    {
        if (accountId == null)
            throw new IllegalArgumentException("AccountId is null");
        Page<Invoice> invoices = invoiceRepo.findByAccountId(accountId ,PageRequest.of(pageNumber,pageCount));
        return invoiceMapper.mapToPageableDto(invoices);

    }
}
