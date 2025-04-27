package com.onboarding.mapper;

import com.onboarding.dto.InvoiceDTO;
import com.onboarding.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.List;


@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "id", ignore = true)
    Invoice mapDtoToEntity(InvoiceDTO dto);

    default List<Invoice> mapDtosToEntities(List<InvoiceDTO> dtos) {
        return dtos.stream()
                .map(dto -> {
                    Invoice entity = mapDtoToEntity(dto);
                    entity.setCreatedAt(LocalDateTime.now());
                    return entity;
                })
                .toList();
    }



//

}
