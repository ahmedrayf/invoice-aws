package com.onboarding.service;

import com.mongodb.MongoException;
import com.onboarding.dto.InvoiceDTO;
import com.onboarding.entity.Invoice;
import exception.InvoiceProcessingException;
import com.onboarding.mapper.InvoiceDTOMapper;
import com.onboarding.repo.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MongoService {
    private final InvoiceRepository invoiceRepo;
    private final InvoiceDTOMapper invoiceDTOMapper;


    public void saveAll(List<Invoice> invoices) {
        log.info("Adding invoices to Mongo");
        try {
            invoiceRepo.saveAll(invoices);
        } catch (DuplicateKeyException e) {
            log.error( "Duplicate bill ID found: ex:{}" , e.getMessage());
            throw new InvoiceProcessingException("Duplicate bill ID found", e);
        } catch (MongoException e) {
            String errorMsg = "Failed to save invoices to MongoDB: " + e.getMessage();
            log.error(errorMsg);
            throw new MongoException(errorMsg, e);
        }
    }

    public Page<InvoiceDTO> getInvoicesByAccountId(String accountId , int pageNumber , int pageCount)
    {
        Page<Invoice> invoices = invoiceRepo.findByAccountId(accountId ,PageRequest.of(pageNumber,pageCount));
        return invoiceDTOMapper.mapToPageDto(invoices);

    }
}
