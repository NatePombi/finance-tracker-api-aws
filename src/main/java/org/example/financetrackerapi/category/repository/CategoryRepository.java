package org.example.financetrackerapi.category.repository;

import org.example.financetrackerapi.category.entity.Category;
import org.example.financetrackerapi.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser(User user);

    Optional<Category> findByIdAndUser(Long id, User user);

    boolean existsByUserAndName(User user, String name);

    Category findByNameAndUserEmail(String savings, String mail);
}
