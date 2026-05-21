package org.example.financetrackerapi.category.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.financetrackerapi.category.enums.CategoryType;

@Getter
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private CategoryType type;
}
