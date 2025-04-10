package com.onboarding.mapper;

import com.onboarding.dto.InvoiceDTO;
import com.onboarding.dto.SQSMessage;
import com.onboarding.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    Invoice toEntity(InvoiceDTO dto);

    InvoiceDTO mapToDto(Invoice invoice);

    default Page<InvoiceDTO> mapToPageableDto(Page<Invoice> projects) {
        return projects.map(this::mapToDto);
    }


    default List<Invoice> mapDtosToEntities(List<InvoiceDTO> dtos) {
        return dtos.stream()
                .map(dto -> {
                    Invoice entity = toEntity(dto);
                    entity.setCreatedAt(LocalDateTime.now());
                    return entity;
                })
                .collect(Collectors.toList());
    }

    SQSMessage toSqsMessage(InvoiceDTO dto);

    default List<SQSMessage> mapToSqs(List<InvoiceDTO> dtos ){
        return dtos.stream()
                .map(dto -> {
                    SQSMessage message = toSqsMessage(dto);
                    message.setPublishDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    message.setContent(dto.getRawLine());
                    return message;
                })
                .collect(Collectors.toList());
    }
//

}
