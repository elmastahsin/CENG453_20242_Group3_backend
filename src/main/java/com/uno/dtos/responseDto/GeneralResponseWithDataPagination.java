package com.uno.dtos.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralResponseWithDataPagination<T> {
    private Status status;
    private T data;
    private PaginationDetails pagination;
}
