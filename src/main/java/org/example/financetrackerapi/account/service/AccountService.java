package org.example.financetrackerapi.account.service;

import lombok.RequiredArgsConstructor;
import org.example.financetrackerapi.account.dto.BalanceResponse;
import org.example.financetrackerapi.account.dto.AccountRequest;
import org.example.financetrackerapi.account.dto.AccountResponse;
import org.example.financetrackerapi.account.entity.Account;
import org.example.financetrackerapi.account.enums.AccountType;
import org.example.financetrackerapi.account.mapper.AccountMapper;
import org.example.financetrackerapi.account.repository.AccountRepository;
import org.example.financetrackerapi.exception.AccountNotFoundException;
import org.example.financetrackerapi.exception.AccountsNotFoundException;
import org.example.financetrackerapi.exception.UserNotFoundException;
import org.example.financetrackerapi.transaction.repository.TransactionRepository;
import org.example.financetrackerapi.user.entity.User;
import org.example.financetrackerapi.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService{
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);


    /**
     * Creating Account for User
     *
     * @param request {@link AccountRequest} object with details of request
     * @param email is email of the signed-in User
     * @return a {@link AccountResponse} object
     * @throws UserNotFoundException if the user does not exist
     * @throws org.example.financetrackerapi.exception.AccountTypeMismatchException if AccountType entered is Invalid
     */
    @Transactional
    @Override
    public AccountResponse create(AccountRequest request, String email) {
        log.info("Attempting to Create Account for user: {}", email);

        // finds user by email, if not found throws an exception
        User user = getUser(email);

        //checks if given account type valid, if not throws an exception
        AccountType type = AccountType.checkType(request.getAccountType().toString());

        //creates Account object with given details from request
        Account account = Account.create(request.getAccountName(),type,user);

        //persists account object and returns an account entity
        Account savedAccount = accountRepository.save(account);
        log.info("Successfully created Account: {} for user: {}", savedAccount.getId(), user.getEmail());

        //maps account entity to AccountResponse dto and returns it to controller
        return AccountMapper.toAccountResponse(savedAccount,user);
    }


    /**
     * Fetches List of Account of User
     *
     * @param email of User that's signed in
     * @return a {@link Page} of {@link AccountResponse} object
     * @throws UserNotFoundException if given email does not belong to a User
     * @throws AccountsNotFoundException if user does not have any accounts
     */
    @Override
    public Page<AccountResponse> getAccounts(String email,int page, int size, String sortBy,String direction) {
        log.info("Attempting to Get Accounts for user");

        // finds user by email, if not found throws an exception
        User user = getUser(email);

        Sort sort = direction.equalsIgnoreCase("desc")?Sort.by(sortBy).descending():Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page,size,sort);



        Page<Account> accounts = accountRepository.findAllByUserId(user.getId(),pageable);

        if(accounts == null || accounts.isEmpty()){
            log.warn("Accounts not found for user: {}", user.getEmail());
            throw new AccountsNotFoundException("No Accounts found for user");
        }


        log.info("Successfully retrieved all user accounts");
        return accounts.map(acc -> AccountMapper.toAccountResponse(acc,user));
    }

    /**
     * Getting Balance for User
     *
     * @param email is email of User that's signed in
     * @param accId a
     * @return a {@link BalanceResponse} object
     * @throws AccountNotFoundException if given account id was not found for signed-in user
     */
    @Override
    public BalanceResponse getBalance(String email, Long accId) {
        log.info("Attempting to Get Balance for user");

        Account acc = accountRepository.findByIdAndUserEmail(accId,email).orElseThrow(()-> {
            log.warn("Account not found. Failed to Get Balance");
            return new AccountNotFoundException("Account not found");
        });

        BigDecimal balance = transactionRepository.balance(acc);
        log.info("Successfully Get Balance for user");
        return new BalanceResponse(balance);
    }


    private User getUser(String email){
        return userRepository.findByEmail(email).orElseThrow(()-> {
            log.warn("User not found. Failed to Create Account");
            return new UserNotFoundException("User not found");
        });

    }



}
