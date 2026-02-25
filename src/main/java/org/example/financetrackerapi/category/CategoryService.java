package org.example.financetrackerapi.category;

import lombok.AllArgsConstructor;
import org.example.financetrackerapi.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CategoryService {
    private final CategoryRepository repo;

    public CategoryResponse create(User user, String name, CategoryType type){
        String nameTrim = name.trim().toLowerCase();

        if(repo.existsByUserAndName(user, nameTrim)){
            throw new IllegalArgumentException("Duplicate category name");
        }


        Category category = Category.createCategory(nameTrim, type, user);

        repo.save(category);

        return new CategoryResponse(category.getId(),category.getName(), category.getType());
    }

    public List<CategoryResponse> getAll(User user){
        return repo.findByUser(user).stream()
                .map(category-> new CategoryResponse(category.getId(),category.getName(), category.getType()))
                .toList();
    }

    public CategoryResponse getById(User user, Long id){
        Category category =  repo.findByIdAndUser(id,user).orElseThrow(()-> new IllegalArgumentException("Category not found"));
        return new CategoryResponse(category.getId(),category.getName(), category.getType());
    }

    @Transactional
    public CategoryResponse update(User user, Long id,String name, CategoryType type){
        String nameTrim = name.trim().toLowerCase();


        if(nameTrim.isBlank()){
            throw new IllegalArgumentException("Category name cannot be blank");

        }

        Category category = repo.findByIdAndUser(id,user).orElseThrow(()-> new IllegalArgumentException("Category not found"));

        if(repo.existsByUserAndName(user, nameTrim) && !category.getName().equals(nameTrim)){
            throw new IllegalArgumentException("Duplicate category name");
        }


        category.changeCategoryType(type);
        category.changeName(nameTrim);

        return new CategoryResponse(category.getId(),category.getName(), category.getType());
    }


    public void delete(User user, Long id){
        Category category = repo.findByIdAndUser(id,user).orElseThrow(()-> new IllegalArgumentException("Category not found"));

        repo.delete(category);
    }
}
