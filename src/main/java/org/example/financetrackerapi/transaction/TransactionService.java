package org.example.financetrackerapi.transaction;

import lombok.RequiredArgsConstructor;
import org.example.financetrackerapi.account.Account;
import org.example.financetrackerapi.account.AccountRepository;
import org.example.financetrackerapi.category.Category;
import org.example.financetrackerapi.category.CategoryRepository;
import org.example.financetrackerapi.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;

    public TransactionResponse create(TransactionRequest transactionRequest, User user) {

        Category category = categoryRepository.findByIdAndUser(transactionRequest.getCategoryId(), user).orElseThrow(()-> new IllegalArgumentException("Category not found"));

        if(!category.getType().name().equals(transactionRequest.getType().name())){
            throw new IllegalArgumentException("Transaction type does not match Category Type");
        }

        Account acc = accountRepository.findByIdAndUserEmail(transactionRequest.getAccountId(), user.getEmail()).orElseThrow(()-> new IllegalArgumentException("Account not found"));

        Transaction trans =Transaction.createTransaction(transactionRequest.getAmount(),transactionRequest.getType(),transactionRequest.getDate(),transactionRequest.getDescription(),acc,category);

        transactionRepository.save(trans);

        return toTransactionResponse(trans);
    }

    public Page<TransactionResponse> getTransactions(User user, Pageable pageable){
        return transactionRepository.findAllByAccountUserWithCategory(user, pageable)
                .map(this::toTransactionResponse);
    }

    public Page<TransactionResponse> getTransactionsByDate(User user, LocalDate from , LocalDate to,Pageable pageable){
        if(from.isAfter(to)){
            throw new IllegalArgumentException("From date must be before TO date");
        }

        return transactionRepository.findAllByAccountUserWithCategoryAndDateBetween(user,from,to,pageable)
                .map(this::toTransactionResponse);
    }

    public Page<TransactionResponse> getTransactionsByFromDate(User user, LocalDate from,Pageable pageable){
        return transactionRepository.findAllByAccountUserFromDate(user,from,pageable)
                .map(this::toTransactionResponse);
    }

    public Page<TransactionResponse> getTransactionByToDate(User user, LocalDate to,Pageable pageable){
        return transactionRepository.findAllByAccountUserToDate(user,to,pageable)
                .map(this::toTransactionResponse);
    }

    public MonthlySummaryReport getMonthlySummaryReport(User user, int year,int month){
        BigDecimal income = transactionRepository.sumCreditByMonth(user,year,month);
        BigDecimal expense = transactionRepository.sumDebitByMonth(user,year,month);

        BigDecimal balance = income.subtract(expense);

        return new MonthlySummaryReport(year,month,income,expense,balance);
    }

    public List<CategorySummaryResponse> getCategorySummaryResponse(User user, int year, int month) {
        return transactionRepository.sumDebitByCategoryForMonth(user,year,month);
    }

    private TransactionResponse toTransactionResponse(Transaction trans) {
        return new TransactionResponse(trans.getId(),trans.getAmount(),trans.getType(),trans.getCategory().getName(),trans.getDescription(),trans.getAccount().getId(),trans.getAccount().getAccountType(), trans.getDate(),trans.getCreatedAt());
    }





}
