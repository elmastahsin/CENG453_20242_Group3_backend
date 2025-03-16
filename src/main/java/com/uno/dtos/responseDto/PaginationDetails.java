package com.uno.dtos.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationDetails {
    private long totalItems;
    private int totalPages;
    private int currentPage;
    private int itemsPerPage;
}
