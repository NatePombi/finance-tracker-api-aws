package org.example.financetrackerapi.account.repository;

import org.example.financetrackerapi.account.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {

    Page<Account> findAllByUserId(long userId, Pageable pageable);

    Optional<Account> findByIdAndUserEmail(Long id,String email);


    Account findByNameAndUserEmail(String s, String mail);
}
