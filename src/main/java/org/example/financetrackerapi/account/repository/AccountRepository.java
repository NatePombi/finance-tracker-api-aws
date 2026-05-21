package org.example.financetrackerapi.account.repository;

import org.example.financetrackerapi.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {

    List<Account> findByUserEmail(String email);


    Optional<Account> findByIdAndUserEmail(Long id,String email);


}
