package org.example.financetrackerapi.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.financetrackerapi.user.enums.UserRole;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "users")
public class User implements UserDetails {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    @JsonIgnore
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static User create(String email, String hashPassword) {
        User user = new User();
        user.email = email;
        user.password = hashPassword;
        user.role = UserRole.USER;
        return user;
    }

    User(Long id,String email,String hashPassword){
        this.id = id;
        this.email = email;
        this.password = hashPassword;
    }

    public void changeRole(UserRole newRole) {
        this.role = newRole;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+ role.toString()));
    }

    @Override
    public String getUsername() {
        return email;
    }
}
