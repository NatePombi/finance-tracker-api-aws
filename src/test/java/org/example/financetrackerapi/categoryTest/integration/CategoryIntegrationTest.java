package org.example.financetrackerapi.categoryTest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.financetrackerapi.category.*;
import org.example.financetrackerapi.user.User;
import org.example.financetrackerapi.user.UserRepository;
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

    @BeforeEach
    void startUp() throws Exception {
        testUser = User.create("test@gmail.com",encoder.encode("testPass"));
        userRepository.save(testUser);
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

        assertThat(categoryRepository.findAll().size()).isEqualTo(1);
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
                .andExpect(status().isUnprocessableEntity());
    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldGetAllUserCategories() throws Exception {
        Category cat1 = Category.createCategory("Savings",CategoryType.CREDIT, testUser);
        Category cat2 = Category.createCategory("Groceries",CategoryType.DEBIT,testUser);
        categoryRepository.save(cat1);
        categoryRepository.save(cat2);

        mockMvc.perform(get("/api/v1/categories")
                .with(csrf()))
                .andExpect(status().isOk());

        List<Category> cats = categoryRepository.findAll();
        assertThat(cats.size()).isEqualTo(2);

        assertThat(cats.get(0).getName()).isEqualTo("Savings");
        assertThat(cats.get(0).getType()).isEqualTo(CategoryType.CREDIT);

        assertThat(cats.get(1).getName()).isEqualTo("Groceries");
        assertThat(cats.get(1).getType()).isEqualTo(CategoryType.DEBIT);
    }

    @Test
    void shouldFailGetAllUserCategories_NotLoggedIn() throws Exception {

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldUserCategoryGetById() throws Exception {
        Category cat1 = Category.createCategory("Savings",CategoryType.CREDIT, testUser);
        Category cat2 = Category.createCategory("Groceries",CategoryType.DEBIT,testUser);
        categoryRepository.save(cat1);
        categoryRepository.save(cat2);

        mockMvc.perform(get("/api/v1/categories/1")
                .with(csrf()))
                .andExpect(status().isOk());

        Category cat = repository.findByIdAndUser(1L,testUser).get();

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

        mockMvc.perform(get("/api/v1/categories/1")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldUpdateCategory() throws Exception {
        Category cat1 = Category.createCategory("Savings",CategoryType.CREDIT, testUser);
        Category cat2 = Category.createCategory("Groceries",CategoryType.DEBIT,testUser);
        categoryRepository.save(cat1);
        categoryRepository.save(cat2);

        CategoryRequest request = new CategoryRequest("Loan",CategoryType.DEBIT);

        mockMvc.perform(patch("/api/v1/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk());

        Category cat = categoryRepository.findByIdAndUser(1L,testUser).get();

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
                .andExpect(status().isUnprocessableEntity());
    }


    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldFailUpdateCategory_CategoryNotFound() throws Exception {
        CategoryRequest request = new CategoryRequest("loan", CategoryType.DEBIT);

        mockMvc.perform(patch("/api/v1/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailUpdateCategory_DuplicateCategory() throws Exception {

        Category cat1 = Category.createCategory("Savings",CategoryType.CREDIT, testUser);
        Category cat2 = Category.createCategory("groceries",CategoryType.DEBIT,testUser);
        categoryRepository.save(cat1);
        categoryRepository.save(cat2);

        CategoryRequest request = new CategoryRequest("Groceries",CategoryType.DEBIT);

        mockMvc.perform(patch("/api/v1/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isConflict());

    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldDeleteCategory() throws Exception {
        Category cat1 = Category.createCategory("Savings",CategoryType.CREDIT, testUser);
        Category cat2 = Category.createCategory("Groceries",CategoryType.DEBIT,testUser);
        categoryRepository.save(cat1);
        categoryRepository.save(cat2);

        mockMvc.perform(delete("/api/v1/categories/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        List<Category> cats = categoryRepository.findAll();



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

        mockMvc.perform(delete("/api/v1/categories/1")
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
