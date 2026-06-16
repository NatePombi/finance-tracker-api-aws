package org.example.financetrackerapi.categoryTest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.financetrackerapi.category.dto.CategoryRequest;
import org.example.financetrackerapi.category.entity.Category;
import org.example.financetrackerapi.category.enums.CategoryType;
import org.example.financetrackerapi.category.repository.CategoryRepository;
import org.example.financetrackerapi.category.service.CategoryService;
import org.example.financetrackerapi.user.entity.User;
import org.example.financetrackerapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public class CategoryIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private CategoryService service;
    @Autowired
    private CategoryRepository repository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;

    private User testUser;
    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory1;
    private Category testCategory2;

    @BeforeEach
    void startUp() throws Exception {
        testUser = User.create("test@gmail.com",encoder.encode("testPass"));
        userRepository.save(testUser);

        testCategory1 = Category.createCategory("Savings",CategoryType.CREDIT, testUser);
        testCategory2 = Category.createCategory("Groceries",CategoryType.DEBIT,testUser);
        categoryRepository.save(testCategory1);
        categoryRepository.save(testCategory2);
    }


    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldCreateCategory() throws Exception {

        CategoryRequest request = new CategoryRequest("Savings", CategoryType.CREDIT);


        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated());

        assertThat(categoryRepository.findByUser(testUser).size()).isEqualTo(3);
    }


    @Test
    void shouldFailCreateCategory_NoLoggedIn() throws Exception {
        CategoryRequest request = new CategoryRequest("Savings", CategoryType.CREDIT);

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailCreateCategory_NoCategoryName() throws Exception {

        CategoryRequest request = new CategoryRequest(null,CategoryType.CREDIT);

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldGetAllUserCategories() throws Exception {
        mockMvc.perform(get("/api/v1/categories")
                .with(csrf()))
                .andExpect(status().isOk());

        List<Category> cats = categoryRepository.findByUser(testUser);
        assertThat(cats.size()).isEqualTo(2);

        Category cat1 = categoryRepository.findByNameAndUserEmail("Savings","test@gmail.com");
        Category cat2 = categoryRepository.findByNameAndUserEmail("Groceries","test@gmail.com");

        assertThat(cat1.getName()).isEqualTo("Savings");
        assertThat(cat1.getType()).isEqualTo(CategoryType.CREDIT);

        assertThat(cat2.getName()).isEqualTo("Groceries");
        assertThat(cat2.getType()).isEqualTo(CategoryType.DEBIT);
    }

    @Test
    void shouldFailGetAllUserCategories_NotLoggedIn() throws Exception {

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldGetUserCategoryGetById() throws Exception {

        Category cat = repository.findByNameAndUserEmail("Savings","test@gmail.com");


        mockMvc.perform(get("/api/v1/categories/{id}",cat.getId())
                .with(csrf()))
                .andExpect(status().isOk());


        assertThat(cat.getName()).isEqualTo("Savings");
        assertThat(cat.getType()).isEqualTo(CategoryType.CREDIT);
    }

    @Test
    void shouldFailGetUserCategoryById_NotLoggedIn() throws Exception {

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailGetUSerCategoryById_CategoryNotFound() throws Exception {

        mockMvc.perform(get("/api/v1/categories/13")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldUpdateCategory() throws Exception {

        CategoryRequest request = new CategoryRequest("Loan",CategoryType.DEBIT);
        Category cat = categoryRepository.findByNameAndUserEmail("Savings","test@gmail.com");

        Long id = cat.getId();

        mockMvc.perform(patch("/api/v1/categories/{id}",id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk());


        assertThat(cat.getName()).isEqualTo("loan");
        assertThat(cat.getType()).isEqualTo(CategoryType.DEBIT);
    }

    @Test
    void shouldFailUpdateCategory_NotLoggedIn() throws Exception {
        CategoryRequest request = new CategoryRequest("Loan",CategoryType.CREDIT);
        mockMvc.perform(patch("/api/v1/categories/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(value = "test@gmail.com", roles = {"USER"})
    void shouldFailUpdateCategory_CategoryNameBlank() throws Exception {
        CategoryRequest request = new CategoryRequest(null,CategoryType.CREDIT);

        mockMvc.perform(patch("/api/v1/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldFailUpdateCategory_CategoryNotFound() throws Exception {
        CategoryRequest request = new CategoryRequest("loan", CategoryType.DEBIT);

        mockMvc.perform(patch("/api/v1/categories/111")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

//    @Test
//    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
//    void shouldFailUpdateCategory_DuplicateCategory() throws Exception {
//        CategoryRequest request = new CategoryRequest("Groceries",CategoryType.DEBIT);
//
//        Category cat = categoryRepository.findByNameAndUserEmail("Savings","test@gmail.com");
//
//        Long id = cat.getId();
//
//        mockMvc.perform(patch("/api/v1/categories/{id}",id)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(mapper.writeValueAsString(request))
//                .with(csrf()))
//                .andExpect(status().isConflict());
//
//    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldDeleteCategory() throws Exception {
        Category cat = categoryRepository.findByNameAndUserEmail("Savings","test@gmail.com");

        Long id = cat.getId();

        mockMvc.perform(delete("/api/v1/categories/{id}", id)
                .with(csrf()))
                .andExpect(status().isNoContent());

        List<Category> cats = categoryRepository.findByUser(testUser);



        assertThat(cats.size()).isEqualTo(1);
        assertThat(cats.get(0).getName()).isEqualTo("Groceries");
        assertThat(cats.get(0).getType()).isEqualTo(CategoryType.DEBIT);

    }

    @Test
    void shouldFailDeleteCategory_NotLoggedIn() throws Exception {

        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailDeleteCategory_CategoryNotFound() throws Exception {

        mockMvc.perform(delete("/api/v1/categories/1333")
                .with(csrf()))
                .andExpect(status().isNotFound());

    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailUpdateCategory_BadRequest() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/badRequest")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
