package org.example.financetrackerapi.category.service;

import lombok.AllArgsConstructor;
import org.example.financetrackerapi.category.enums.CategoryType;
import org.example.financetrackerapi.category.dto.CategoryResponse;
import org.example.financetrackerapi.category.entity.Category;
import org.example.financetrackerapi.category.repository.CategoryRepository;
import org.example.financetrackerapi.exception.*;
import org.example.financetrackerapi.user.entity.User;
import org.example.financetrackerapi.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryService {
    private final CategoryRepository repo;
    private final UserRepository userRepo;

    public CategoryResponse create(String email, String name, CategoryType type){
        String nameTrim = name.trim().toLowerCase();

        User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        if(repo.existsByUserAndName(user, nameTrim)){
            throw new CategoryNameAlreadyExistsException("Duplicate category name");
        }



        Category category = Category.createCategory(nameTrim, type, user);

        repo.save(category);

        return new CategoryResponse(category.getId(),category.getName(), category.getType());
    }

    public List<CategoryResponse> getAll(String email){
        User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        return repo.findByUser(user).stream()
                .map(category-> new CategoryResponse(category.getId(),category.getName(), category.getType()))
                .toList();
    }

    public CategoryResponse getById(String email, Long id){
      User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        Category category =  repo.findByIdAndUser(id,user).orElseThrow(()-> new CategoryNotFoundException("Category not found"));
        return new CategoryResponse(category.getId(),category.getName(), category.getType());
    }

    @Transactional
    public CategoryResponse update(String email, Long id,String name, CategoryType type){
        String nameTrim = name.trim().toLowerCase();

        User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        if(nameTrim.isBlank()){
            throw new CategoryNameEmptyException("Category name cannot be blank");

        }

        Category category = repo.findByIdAndUser(id,user).orElseThrow(()-> new CategoryNotFoundException("Category not found"));

        if(repo.existsByUserAndName(user, nameTrim) && !category.getName().equals(nameTrim)){
            throw new CategoryNameAlreadyExistsException("Duplicate category name");
        }


        category.changeCategoryType(type);
        category.changeName(nameTrim);

        return new CategoryResponse(category.getId(),category.getName(), category.getType());
    }


    public void delete(String email, Long id){
        User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        Category category = repo.findByIdAndUser(id,user).orElseThrow(()-> new CategoryNotFoundException("Category not found"));

        repo.delete(category);
    }
}
