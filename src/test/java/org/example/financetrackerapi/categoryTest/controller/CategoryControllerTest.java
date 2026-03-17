package org.example.financetrackerapi.categoryTest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.financetrackerapi.auth.JwtService;
import org.example.financetrackerapi.category.*;
import org.example.financetrackerapi.exception.CategoryNameAlreadyExistsException;
import org.example.financetrackerapi.exception.CategoryNameEmptyException;
import org.example.financetrackerapi.exception.CategoryNotFoundException;
import org.example.financetrackerapi.user.User;
import org.example.financetrackerapi.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import(CategoryControllerTest.MethodConfig.class)
public class CategoryControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CategoryService service;
    @Autowired
    private MockMvc mockMvc;


    @TestConfiguration
    static class MethodConfig {
        @Bean
        public CategoryService categoryService() {
            return mock(CategoryService.class);
        }

        @Bean
        public JwtService jwtService() {
            return mock(JwtService.class);
        }
    }

    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldCreateCategory() throws Exception {
        User testUser = User.create("test@gmail.com","testPass");

        CategoryRequest request = new CategoryRequest("Salary", CategoryType.CREDIT);
        CategoryResponse response = service.create(testUser.getEmail(),"Salary",CategoryType.CREDIT);

        when(service.create(testUser.getEmail(),"Salary",CategoryType.CREDIT)).thenReturn(response);

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldFailCreateCategory_UserNotLoggedIn() throws Exception {

        CategoryRequest request = new CategoryRequest("Salary", CategoryType.CREDIT);


        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailCreateCategory_CategoryNameEmpty() throws Exception {
        User testUser = User.create("test@gmail.com","testPass");

        CategoryRequest request = new CategoryRequest("", CategoryType.CREDIT);
        CategoryResponse response = service.create(testUser.getEmail(),"Salary",CategoryType.CREDIT);

        when(service.create(testUser.getEmail(),"Salary",CategoryType.CREDIT)).thenReturn(response);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldGetAllUserCategories() throws Exception {
        User testUser = User.create("test@gmail.com","testPass");

        CategoryResponse response = new CategoryResponse(1L,"Savings",CategoryType.CREDIT);
        CategoryResponse response1 = new CategoryResponse(2L,"Groceries",CategoryType.DEBIT);

        when(service.getAll(testUser.getEmail())).thenReturn(List.of(response,response1));



        mockMvc.perform(get("/api/v1/categories")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name").value("Savings"))
                .andExpect(jsonPath("$.[0].type").value("CREDIT"))
                .andExpect(jsonPath("$.[1].name").value("Groceries"))
                .andExpect(jsonPath("$.[1].type").value("DEBIT"));
    }


    @Test
    void shouldFailGetAllUserCategory_NotLoggedIn() throws Exception {
            User testUser = User.create("test@gmail.com","testPass");

            CategoryResponse response = new CategoryResponse(1L,"Savings",CategoryType.CREDIT);
            CategoryResponse response1 = new CategoryResponse(2L,"Groceries",CategoryType.DEBIT);

            when(service.getAll(testUser.getEmail())).thenReturn(List.of(response,response1));


            mockMvc.perform(get("/api/v1/categories")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

    }

   @Test
   @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldGetCategoryById() throws Exception {
        User testUser = User.create("test@gmail.com","testPass");
        CategoryResponse response = new CategoryResponse(1L,"Savings",CategoryType.CREDIT);

        when(service.getById(testUser.getEmail(),1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/categories/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Savings"));
   }

   @Test
    void shouldFailGetCategoryById_NotLoggedIn() throws Exception {
       User testUser = User.create("test@gmail.com","testPass");

       CategoryResponse response = new CategoryResponse(1L,"Savings",CategoryType.CREDIT);

       when(service.getById(testUser.getEmail(),1L)).thenReturn(response);

       mockMvc.perform(get("/api/v1/categories/1")
                       .with(csrf()))
               .andExpect(status().isUnauthorized());
   }


    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldFailGetCategoryById_CategoryNotFound() throws Exception {

        when(service.getById("test@gmail.com",1L)).thenThrow(new CategoryNotFoundException("Not Found"));

        mockMvc.perform(get("/api/v1/categories/1")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }



    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldUpdateCategory() throws Exception {
        User testUser = User.create("test@gmail.com","testPass");
        CategoryResponse response = new CategoryResponse(1L,"savings",CategoryType.CREDIT);
        CategoryRequest request = new CategoryRequest("loan",CategoryType.CREDIT);

        when(service.update(testUser.getEmail(),1L,"loan",CategoryType.CREDIT)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("savings"))
                .andExpect(jsonPath("$.type").value("CREDIT"));
    }


    @Test
    void shouldFailUpdateCategory_NotLoggedIn() throws Exception {
        CategoryRequest request = new CategoryRequest("Savings",CategoryType.CREDIT);


        mockMvc.perform(patch("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailUpdateCategory_CategoryNameEmpty() throws Exception {
        CategoryRequest request = new CategoryRequest(null,CategoryType.CREDIT);

        mockMvc.perform(patch("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity());
    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailUpdateCategory_CategoryTypeEmpty() throws Exception {
        User testUser = User.create("test@gmail.com","testPass");
        CategoryRequest request = new CategoryRequest("Savings",CategoryType.CREDIT);

        when(service.update(testUser.getEmail(),1L,"Savings",CategoryType.CREDIT)).thenThrow(new CategoryNameAlreadyExistsException("Duplicate"));

        mockMvc.perform(patch("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isConflict());

    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldDeleteCategory() throws Exception {
        doNothing().when(service).delete("test@gmail.com",1L);

        mockMvc.perform(delete("/api/v1/categories/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }


    @Test
    void shouldFailDeleteCategory_NotLoggedIn() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldFailDeleteCategory_CategoryNotFound() throws Exception {
        doThrow(new CategoryNotFoundException("Not Found")).when(service).delete(eq("test@gmail.com"),eq(1L));

        mockMvc.perform(delete("/api/v1/categories/1")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

}
