package com.onboarding.repo;

import com.onboarding.entity.Invoice;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface InvoiceRepository extends MongoRepository<Invoice , String > {

    @Cacheable(value = "accountInvoices", key = "#accountId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    Page<Invoice> findByAccountId(String accountId , Pageable pageable);
}
