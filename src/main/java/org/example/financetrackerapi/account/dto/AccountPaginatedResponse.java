package org.example.financetrackerapi.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class AccountPaginatedResponse<T> {
    private List<T> list;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private boolean hasNext;
}
