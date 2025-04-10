package com.onboarding.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
@JsonInclude(value= JsonInclude.Include.NON_EMPTY, content= JsonInclude.Include.NON_NULL)
public class PageableResponse<T> extends ApiResponse<T>{

    private Integer currentPage;
    private Long totalItems;
    private Integer totalPages;
    private Integer currentItems;

}