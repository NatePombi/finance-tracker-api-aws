package org.example.financetrackerapi.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Schema(description = "Category Request")
@Getter
@AllArgsConstructor
public class CategoryRequest {
    @Schema(description = "Name of the category you want to create", example = "Savings")
    @NotBlank(message = "category name cannot be empty")
    private String categoryName;
    @Schema(description = "Type of category you creating e.g DEBIT or CREDIT", example = "DEBIT")
    @NotNull(message = "category type cannot be null")
    private CategoryType categoryType;
}
