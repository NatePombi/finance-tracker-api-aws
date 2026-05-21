package org.example.financetrackerapi.categoryTest.service;

import org.example.financetrackerapi.category.dto.CategoryResponse;
import org.example.financetrackerapi.category.entity.Category;
import org.example.financetrackerapi.category.enums.CategoryType;
import org.example.financetrackerapi.category.repository.CategoryRepository;
import org.example.financetrackerapi.category.service.CategoryService;
import org.example.financetrackerapi.exception.CategoryNameAlreadyExistsException;
import org.example.financetrackerapi.exception.CategoryNameEmptyException;
import org.example.financetrackerapi.exception.CategoryNotFoundException;
import org.example.financetrackerapi.user.entity.User;
import org.example.financetrackerapi.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CategoryService categoryService;

    @Test
    void shouldCreateCategory() {
        User testUser = User.create("test@gmail.com", "test");
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        CategoryResponse response = categoryService.create(testUser.getEmail(),"Salary", CategoryType.DEBIT);


        verify(categoryRepository,atLeast(1)).save(any(Category.class));

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("salary");
        assertThat(response.getType()).isEqualTo(CategoryType.DEBIT);
    }

    @Test
    void shouldFailToCreateCategory() {
        User testUser = User.create("test@gmail.com", "test");
        when(categoryRepository.existsByUserAndName(testUser,"salary")).thenReturn(true);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));


        assertThrows(CategoryNameAlreadyExistsException.class,()->{
            categoryService.create(testUser.getEmail(),"salary", CategoryType.DEBIT);
        });


    }

    @Test
    void shouldGetAllUserCategory(){
        User testUser = User.create("test@gmail.com", "test");
        Category cat1 = Category.createCategory("Salary",CategoryType.CREDIT,testUser);
        Category cat2 = Category.createCategory("Groceries",CategoryType.DEBIT,testUser);

        when(categoryRepository.findByUser(testUser)).thenReturn( List.of(cat1,cat2));
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        List<CategoryResponse> responseList = categoryService.getAll(testUser.getEmail());

        assertThat(responseList).isNotNull();
        assertThat(responseList.size()).isEqualTo(2);
        assertThat(responseList.get(0).getName()).isEqualTo("Salary");
        assertThat(responseList.get(0).getType()).isEqualTo(CategoryType.CREDIT);
        assertThat(responseList.get(1).getName()).isEqualTo("Groceries");
        assertThat(responseList.get(1).getType()).isEqualTo(CategoryType.DEBIT);
    }


    @Test
    void shouldGetById(){
        User testUser = User.create("test@gmail.com", "test");
        Category cat1 = Category.createCategory("Salary",CategoryType.CREDIT,testUser);
        when(categoryRepository.findByIdAndUser(1L,testUser)).thenReturn(Optional.of(cat1));
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        CategoryResponse response = categoryService.getById(testUser.getEmail(),1L);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Salary");
        assertThat(response.getType()).isEqualTo(CategoryType.CREDIT);

    }

    @Test
    void shouldUpdateCategory(){
        User testUser = User.create("test@gmail.com", "test");
        Category cat1 = Category.createCategory("Salary",CategoryType.CREDIT,testUser);

        when(categoryRepository.findByIdAndUser(1L,testUser)).thenReturn(Optional.of(cat1));
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(categoryRepository.existsByUserAndName(testUser,"payment")).thenReturn(false);

        CategoryResponse response = categoryService.update(testUser.getEmail(),1L,"Payment",CategoryType.DEBIT);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("payment");
        assertThat(response.getType()).isEqualTo(CategoryType.DEBIT);

    }

    @Test
    void shouldFailUpdateCategory_CategoryNameAlreadyExists() {
        User testUser = User.create("test@gmail.com", "test");
        Category cat1 = Category.createCategory("Salary",CategoryType.CREDIT,testUser);

        when(categoryRepository.findByIdAndUser(1L,testUser)).thenReturn(Optional.of(cat1));
        when(categoryRepository.existsByUserAndName(testUser,"payment")).thenReturn(true);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));


        assertThrows(CategoryNameAlreadyExistsException.class,()->{
            categoryService.update(testUser.getEmail(),1L,"Payment",CategoryType.CREDIT);
        });
    }

    @Test
    void shouldFailUpdate_CategoryNameBlank(){
        User testUser = User.create("test@gmail.com", "test");
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        assertThrows(CategoryNameEmptyException.class,()->{
            categoryService.update(testUser.getEmail(),1L,"",CategoryType.CREDIT);
        });

    }


    @Test
    void shouldDeleteCategory(){
        User testUser = User.create("test@gmail.com", "test");
        Category cat1 = Category.createCategory("Salary",CategoryType.CREDIT,testUser);

        when(categoryRepository.findByIdAndUser(1L,testUser)).thenReturn(Optional.of(cat1));
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        categoryService.delete(testUser.getEmail(),1L);

        verify(categoryRepository,atLeast(1)).delete(cat1);
    }

    @Test
    void shouldFailDeleteCategory_CategoryNotFound() {
        User testUser = User.create("test@gmail.com", "test");

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        assertThrows(CategoryNotFoundException.class,()->{
            categoryService.delete(testUser.getEmail(),1L);
        });

    }
}
