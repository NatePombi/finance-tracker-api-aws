package org.example.financetrackerapi.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.financetrackerapi.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "Category Controller", description = "Operations related to Category Management")
@RestController
@RequestMapping("/api/v1/categories")
@AllArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(summary = "Create a new Category",
            description = "Creates Category for authenticated User")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity")
    })
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest categoryRequest, @Parameter(hidden = true) @AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(email,categoryRequest.getCategoryName(),categoryRequest.getCategoryType()));
    }

    @Operation(summary = "Get All Categories",
            description = "Gets All Category from authenticated User")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gets All Category Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity")
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(@Parameter(hidden = true) @AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.ok(categoryService.getAll(email));
    }

    @Operation(summary = "Get Category by Id ",
            description = "Get Category by Id from authenticated User")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get Category Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@Parameter(description = "Category ID") @PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.ok(categoryService.getById(email,id));
    }

    @Operation(summary = "Update Category",
            description = "Update Category from authenticated User")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated Category Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@Parameter(description = "Category ID") @PathVariable Long id, @Valid @RequestBody CategoryRequest categoryRequest, @Parameter(hidden = true) @AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.ok(categoryService.update(email,id,categoryRequest.getCategoryName(),categoryRequest.getCategoryType()));
    }

    @Operation(summary = "Delete Account",
            description = "Creates a financial for authenticated User")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction created Successfully"),
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Category ID") @PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal(expression = "username") String email) {
       categoryService.delete(email,id);
        return ResponseEntity.noContent().build();
    }
}
