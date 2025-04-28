package com.onboarding.mapper;

import com.onboarding.dto.InvoiceDTO;
import com.onboarding.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    Invoice mapDtoToEntity(InvoiceDTO dto);

     List<Invoice> mapDtosToEntities(List<InvoiceDTO> dtos);
}
