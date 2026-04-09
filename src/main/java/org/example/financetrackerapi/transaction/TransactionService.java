package org.example.financetrackerapi.transaction;

import lombok.RequiredArgsConstructor;
import org.example.financetrackerapi.account.Account;
import org.example.financetrackerapi.account.AccountRepository;
import org.example.financetrackerapi.category.Category;
import org.example.financetrackerapi.category.CategoryRepository;
import org.example.financetrackerapi.exception.AccountNotFoundException;
import org.example.financetrackerapi.exception.CategoryNotFoundException;
import org.example.financetrackerapi.exception.UserNotFoundException;
import org.example.financetrackerapi.user.User;
import org.example.financetrackerapi.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public TransactionResponse create(TransactionRequest transactionRequest, String email) {

        User user = userRepository.findByEmail(email).orElseThrow(()-> new UserNotFoundException("User Not Found"));

        Category category = categoryRepository.findByIdAndUser(transactionRequest.getCategoryId(), user).orElseThrow(()-> new CategoryNotFoundException("Category not found"));

        if(!category.getType().name().equals(transactionRequest.getType().name())){
            throw new IllegalArgumentException("Transaction type does not match Category Type");
        }

        Account acc = accountRepository.findByIdAndUserEmail(transactionRequest.getAccountId(), user.getEmail()).orElseThrow(()-> new AccountNotFoundException("Account not found"));

        Transaction trans =Transaction.createTransaction(transactionRequest.getAmount(),transactionRequest.getType(),transactionRequest.getDate(),transactionRequest.getDescription(),acc,category);

        transactionRepository.save(trans);

        return toTransactionResponse(trans);
    }

    public Page<TransactionResponse> getTransactions(String email, Pageable pageable){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new UserNotFoundException("User Not Found"));

        return transactionRepository.findAllByAccountUserWithCategory(user, pageable)
                .map(this::toTransactionResponse);
    }

    public Page<TransactionResponse> getTransactionsByDate(String email, LocalDate from , LocalDate to,Pageable pageable){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new UserNotFoundException("User Not Found"));


        if(from.isAfter(to)){
            throw new IllegalArgumentException("From date must be before TO date");
        }

        return transactionRepository.findAllByAccountUserWithCategoryAndDateBetween(user,from,to,pageable)
                .map(this::toTransactionResponse);
    }

    public Page<TransactionResponse> getTransactionsByFromDate(String email, LocalDate from,Pageable pageable){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new UserNotFoundException("User Not Found"));

        return transactionRepository.findAllByAccountUserFromDate(user,from,pageable)
                .map(this::toTransactionResponse);
    }

    public Page<TransactionResponse> getTransactionByToDate(String email, LocalDate to,Pageable pageable){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new UserNotFoundException("User Not Found"));


        return transactionRepository.findAllByAccountUserToDate(user,to,pageable)
                .map(this::toTransactionResponse);
    }

    public MonthlySummaryReport getMonthlySummaryReport(String email, int year,int month){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new UserNotFoundException("User Not Found"));


        BigDecimal income = transactionRepository.sumCreditByMonth(user,year,month);
        BigDecimal expense = transactionRepository.sumDebitByMonth(user,year,month);

        BigDecimal balance = income.subtract(expense);

        return new MonthlySummaryReport(year,month,income,expense,balance);
    }

    public List<CategorySummaryResponse> getCategorySummaryResponse(String email, int year, int month) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new UserNotFoundException("User Not Found"));


        return transactionRepository.sumDebitByCategoryForMonth(user,year,month);
    }

    private TransactionResponse toTransactionResponse(Transaction trans) {
        return new TransactionResponse(trans.getId(),trans.getAmount(),trans.getType(),trans.getCategory().getName(),trans.getDescription(),trans.getAccount().getId(),trans.getAccount().getAccountType(), trans.getDate(),trans.getCreatedAt());
    }





}
