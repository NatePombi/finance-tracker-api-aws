package org.example.financetrackerapi.category.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.financetrackerapi.category.enums.CategoryType;
import org.example.financetrackerapi.user.entity.User;

@Entity
@Table(name = "categories",
uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","name"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    public static Category createCategory(String name, CategoryType type, User user) {
        Category category = new Category();
        category.name = name;
        category.type = type;
        category.user = user;
        return category;
    }

    public void changeCategoryType(CategoryType newType) {
        this.type = newType;
    }

    public void changeName(String newName) {
        this.name = newName;
    }

}
