package com.onboarding.mapper;

import com.onboarding.dto.InvoiceDTO;
import com.onboarding.entity.Invoice;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface InvoiceDTOMapper {

    default Page<InvoiceDTO> mapToPageDto(Page<Invoice> invoices) {
        return invoices.map(this::mapEntityToDto);
    }

    InvoiceDTO mapEntityToDto(Invoice invoice);


}
