package com.onboarding.mapper;

import com.onboarding.dto.InvoiceDTO;
import com.onboarding.dto.SQSMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SQSMessageMapper {

    @Mapping(target = "content", source = "rawLine")
    @Mapping(target = "publishDate", expression = "java(java.time.LocalDate.now())")
    SQSMessage mapDtoToSqsMessage(InvoiceDTO dto);

    List<SQSMessage> mapDtosToSqsMessages(List<InvoiceDTO> dtoList);

}
