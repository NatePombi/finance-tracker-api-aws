package org.example.financetrackerapi.transaction;

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

    Optional<Transaction> findByIdAndUser(Long id, User user);

    @Query(value = """
            SELECT t 
            FROM Transaction t 
            JOIN FETCH t.category WHERE t.user = :user AND t.date
            BETWEEN :from AND :to 
            """,
            countQuery = """
                    SELECT COUNT(t)
                    FROM Transaction t
                    WHERE t.user = :user
                    AND t.date
                    BETWEEN :from AND :to
                    """
    )
    Page<Transaction> findAllByUserWithCategoryAndDateBetween(@Param("user") User user, @Param("from") LocalDate from, @Param("to") LocalDate to, Pageable pageable);

    List<Transaction> findByCategoryAndUser(Category category,User user);

    @Query(value = """
            SELECT t 
            FROM Transaction t 
            JOIN  FETCH t.category WHERE t.user = :user
                        """,
            countQuery = """
                SELECT COUNT(t)
                FROM Transaction t
                WHERE t.user = :user
                """
    )
    Page<Transaction> findAllByUserWithCategory(@Param("user") User user,Pageable pageable);

    @Query(value = """
            SELECT t
             FROM  Transaction t
             JOIN FETCH t.category 
             WHERE t.user = :user
              AND t.date <= :date
""",
    countQuery = """
        SELECT COUNT(t)
        FROM Transaction t
        WHERE t.user = :user
        And t.date <= :date
""")
    Page<Transaction> findAllByUserToDate(@Param("user") User user,@Param("date") LocalDate date,Pageable pageable);

    @Query(value = """
        SELECT  t 
        FROM Transaction t 
        JOIN FETCH t.category 
        WHERE t.user = :user AND t.date >= :date
""",
    countQuery = """
        SELECT COUNT(t)
        FROM Transaction t 
        WHERE t.user = :user
        AND t.date >= :date
""")
    Page<Transaction> findAllByUserFromDate(@Param("user") User user, @Param("date") LocalDate date, Pageable pageable);

    @Query(value = """
            SELECT COALESCE(SUM(t.amount),0) 
            FROM Transaction t 
            WHERE t.user = :user
            AND t.type = 'CREDIT'
            AND YEAR(t.date) = :year
            AND MONTH(t.date) = :month
            """)
    BigDecimal sumCreditByMonth(@Param("user") User user ,@Param("year") int year, @Param("month") int month);

    @Query(value = """
          SELECT COALESCE(SUM(t.amount),0)
          FROM Transaction t
          WHERE t.user = :user
          AND t.type = 'DEBIT'
          AND YEAR(t.date) = :year
          AND MONTH(t.date) = :month
""")
    BigDecimal sumDebitByMonth(@Param("user") User user ,@Param("year") int year,@Param("month") int month);


    @Query(value = """
            SELECT new org.example.financetrackerapi.transaction.CategorySummaryResponse(t.category.name,SUM(t.amount)
            )
            FROM Transaction t
            WHERE t.user = :user
            AND t.type = 'DEBIT'
            AND YEAR(t.date) = :year
            AND MONTH(t.date) = :month
            GROUP BY t.category.name
            ORDER BY SUM(t.amount) DESC
"""
    )
    List<CategorySummaryResponse> sumDebitByCategoryForMonth(@Param("user") User user,@Param("year") int year,@Param("month") int month);
}
