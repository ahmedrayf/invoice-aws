package com.onboarding.mapper;

import com.onboarding.dto.InvoiceDTO;
import com.onboarding.dto.SQSMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface SQSMessageMapper {

    @Mapping(target = "content", source = "rawLine")
    SQSMessage mapDtoToSqsMessage(InvoiceDTO dto);

    default List<SQSMessage> mapDtosToSqsMessages(List<InvoiceDTO> dtos ){
        return dtos.stream()
                .map(dto -> {
                    SQSMessage message = mapDtoToSqsMessage(dto);
                    message.setPublishDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    return message;
                })
                .toList();
    }
}
