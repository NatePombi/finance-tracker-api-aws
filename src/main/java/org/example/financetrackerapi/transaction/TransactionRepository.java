package org.example.financetrackerapi.transaction;

import org.example.financetrackerapi.account.Account;
import org.example.financetrackerapi.category.Category;
import org.example.financetrackerapi.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {


    @Query(value = """
            SELECT t 
            FROM Transaction t 
            JOIN FETCH t.account
            JOIN FETCH t.category 
            WHERE t.account.user = :user AND t.date
            BETWEEN :from AND :to 
            """,
            countQuery = """
                    SELECT COUNT(t)
                    FROM Transaction t
                    WHERE t.account.user = :user
                    AND t.date
                    BETWEEN :from AND :to
                    """
    )
    Page<Transaction> findAllByAccountUserWithCategoryAndDateBetween(@Param("user") User user, @Param("from") LocalDate from, @Param("to") LocalDate to, Pageable pageable);


    @Query(value = """
            SELECT t 
            FROM Transaction t 
            JOIN FETCH t.account
            JOIN  FETCH t.category 
            WHERE t.account.user = :user
                        """,
            countQuery = """
                SELECT COUNT(t)
                FROM Transaction t
                WHERE t.account.user = :user
                """
    )
    Page<Transaction> findAllByAccountUserWithCategory(@Param("user") User user,Pageable pageable);

    @Query(value = """
            SELECT t
             FROM  Transaction t
             JOIN FETCH t.account
             JOIN FETCH t.category 
             WHERE t.account.user = :user
              AND t.date <= :date
""",
    countQuery = """
        SELECT COUNT(t)
        FROM Transaction t
        WHERE t.account.user = :user
        And t.date <= :date
""")
    Page<Transaction> findAllByAccountUserToDate(@Param("user") User user,@Param("date") LocalDate date,Pageable pageable);

    @Query(value = """
        SELECT  t 
        FROM Transaction t
        JOIN FETCH t.account
        JOIN FETCH t.category 
        WHERE t.account.user = :user AND t.date >= :date
""",
    countQuery = """
        SELECT COUNT(t)
        FROM Transaction t 
        WHERE t.account.user = :user
        AND t.date >= :date
""")
    Page<Transaction> findAllByAccountUserFromDate(@Param("user") User user, @Param("date") LocalDate date, Pageable pageable);

    @Query(value = """
            SELECT COALESCE(SUM(t.amount),0) 
            FROM Transaction t 
            WHERE t.account.user = :user
            AND t.type = 'CREDIT'
            AND YEAR(t.date) = :year
            AND MONTH(t.date) = :month
            """)
    BigDecimal sumCreditByMonth(@Param("user") User user ,@Param("year") int year, @Param("month") int month);

    @Query(value = """
          SELECT COALESCE(SUM(t.amount),0)
          FROM Transaction t
          WHERE t.account.user = :user
          AND t.type = 'DEBIT'
          AND YEAR(t.date) = :year
          AND MONTH(t.date) = :month
""")
    BigDecimal sumDebitByMonth(@Param("user") User user ,@Param("year") int year,@Param("month") int month);


    @Query(value = """
            SELECT new org.example.financetrackerapi.transaction.CategorySummaryResponse(t.category.name,SUM(t.amount)
            )
            FROM Transaction t
            WHERE t.account.user = :user
            AND t.type = 'DEBIT'
            AND YEAR(t.date) = :year
            AND MONTH(t.date) = :month
            GROUP BY t.category.name
            ORDER BY SUM(t.amount) DESC
"""
    )
    List<CategorySummaryResponse> sumDebitByCategoryForMonth(@Param("user") User user,@Param("year") int year,@Param("month") int month);


    @Query(
            """
            SELECT COALESCE(SUM(
            CASE 
               WHEN t.type = org.example.financetrackerapi.transaction.TransactionType.CREDIT
               THEN t.amount
               ELSE 0
               END
            ),0) 
            -
            COALESCE(SUM( 
            CASE 
                WHEN t.type = org.example.financetrackerapi.transaction.TransactionType.DEBIT
                THEN t.amount
                ELSE 0 
                END 
                ),0)
            FROM Transaction t
            WHERE t.account = :account
"""
    )
    BigDecimal balance(@Param("account") Account account);
}
