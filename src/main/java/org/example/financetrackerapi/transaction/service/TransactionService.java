package org.example.financetrackerapi.transaction.service;

import lombok.RequiredArgsConstructor;
import org.example.financetrackerapi.account.entity.Account;
import org.example.financetrackerapi.account.repository.AccountRepository;
import org.example.financetrackerapi.category.entity.Category;
import org.example.financetrackerapi.category.enums.CategoryType;
import org.example.financetrackerapi.category.repository.CategoryRepository;
import org.example.financetrackerapi.exception.*;
import org.example.financetrackerapi.transaction.dto.CategorySummaryResponse;
import org.example.financetrackerapi.transaction.dto.MonthlySummaryReport;
import org.example.financetrackerapi.transaction.dto.TransactionRequest;
import org.example.financetrackerapi.transaction.dto.TransactionResponse;
import org.example.financetrackerapi.transaction.entity.Transaction;
import org.example.financetrackerapi.transaction.enums.TransactionType;
import org.example.financetrackerapi.transaction.mapper.TransactionMapper;
import org.example.financetrackerapi.transaction.repository.TransactionRepository;
import org.example.financetrackerapi.user.entity.User;
import org.example.financetrackerapi.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(TransactionService.class);


    /**
     * Creating Transaction
     *
     * @param transactionRequest is a {@link TransactionRequest} object with transaction details
     * @param email is email of logged in user
     * @return a {@link TransactionResponse} object
     * @throws UserNotFoundException if user was not found
     * @throws AccountNotFoundException if account was not found
     * @throws TypeMismatchException if category types does not match
     * @throws CategoryNotFoundException if category was not found
     * @throws InvalidDateRangeException if From date is after To date
     *
     */
    @Transactional
    public TransactionResponse create(TransactionRequest transactionRequest, String email) {

        User user = getUser(email);

        Category category = getCategory(transactionRequest.getCategoryId(),user);

        typeValidation(transactionRequest.getType(),category.getType());

        Account acc = accountRepository.findByIdAndUserEmail(transactionRequest.getAccountId(), user.getEmail()).orElseThrow(()-> new AccountNotFoundException("Account not found"));

        Transaction trans =
                Transaction.createTransaction(transactionRequest.getAmount(),
                        transactionRequest.getType(),
                        transactionRequest.getDate(),
                        transactionRequest.getDescription(),
                        acc,category);

        transactionRepository.save(trans);

        return TransactionMapper.toTransactionResponse(trans);
    }

    public Page<TransactionResponse> getTransactions(String email, Pageable pageable){
        User user = getUser(email);

        return transactionRepository.findAllByAccountUserWithCategory(user, pageable)
                .map(TransactionMapper::toTransactionResponse);
    }

    public Page<TransactionResponse> getTransactionsByDate(String email, LocalDate from , LocalDate to,Pageable pageable){
        User user = getUser(email);


        validateDateRange(from,to);

        return transactionRepository.findAllByAccountUserWithCategoryAndDateBetween(user,from,to,pageable)
                .map(TransactionMapper::toTransactionResponse);
    }

    public Page<TransactionResponse> getTransactionsByFromDate(String email, LocalDate from,Pageable pageable){
        User user = getUser(email);

        return transactionRepository.findAllByAccountUserFromDate(user,from,pageable)
                .map(TransactionMapper::toTransactionResponse);
    }

    public Page<TransactionResponse> getTransactionByToDate(String email, LocalDate to,Pageable pageable){
        User user = getUser(email);

        return transactionRepository.findAllByAccountUserToDate(user,to,pageable)
                .map(TransactionMapper::toTransactionResponse);
    }

    public MonthlySummaryReport getMonthlySummaryReport(String email, int year, int month){
        User user = getUser(email);

        BigDecimal income = transactionRepository.sumCreditByMonth(user,year,month);
        BigDecimal expense = transactionRepository.sumDebitByMonth(user,year,month);

        BigDecimal balance = income.subtract(expense);

        return new MonthlySummaryReport(year,month,income,expense,balance);
    }

    public List<CategorySummaryResponse> getCategorySummaryResponse(String email, int year, int month) {
        User user = getUser(email);


        return transactionRepository.sumDebitByCategoryForMonth(user,year,month);
    }

    private User getUser(String email){
        return userRepository.findByEmail(email).orElseThrow(()-> {
            log.warn("Failed to find User");
            return new UserNotFoundException("User Not Found");
        });

    }

    private void typeValidation(TransactionType transactionType , CategoryType categoryType){
        if(!categoryType.name().equals(transactionType.name())){
            log.warn("Category Type: {} and Transaction Type: {} does not match",categoryType,transactionType);
            throw new TypeMismatchException("Transaction type does not match Category Type");
        }
    }

    private Category getCategory(Long categoryId, User user){
        return categoryRepository.findByIdAndUser(categoryId, user).orElseThrow(()-> {
            log.warn("Failed to find Categories. No categories available");
            return new CategoryNotFoundException("Category not found");
        });
    }

    private void validateDateRange(LocalDate from, LocalDate to){
        if(from.isAfter(to)){
            throw new InvalidDateRangeException("From date must be before TO date");
        }
    }




}



