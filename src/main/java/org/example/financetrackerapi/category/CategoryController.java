package org.example.financetrackerapi.category;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.financetrackerapi.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@AllArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest categoryRequest, @AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(email,categoryRequest.getCategoryName(),categoryRequest.getCategoryType()));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(@AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.ok(categoryService.getAll(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id, @AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.ok(categoryService.getById(email,id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest categoryRequest, @AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.ok(categoryService.update(email,id,categoryRequest.getCategoryName(),categoryRequest.getCategoryType()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal(expression = "username") String email) {
       categoryService.delete(email,id);
        return ResponseEntity.noContent().build();
    }
}
