package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.Transaction;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySender(User sender);

    List<Transaction> findByReceiver(User receiver);

    @Query("SELECT COALESCE(SUM(t.amountBRL), 0) " +
            "FROM Transaction t " +
            "WHERE t.sender = :user " +
            "AND t.timestamp >= :startDate " +
            "AND t.timestamp < :endDate")
    BigDecimal calculateDailyTotal(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.timestamp BETWEEN :start AND :end " +
            "ORDER BY t.timestamp DESC")
    List<Transaction> findBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    @Query("SELECT COALESCE(SUM(t.amountBRL), 0) FROM Transaction t " +
            "WHERE t.sender.id = :senderId AND DATE(t.timestamp) = :date")
    BigDecimal calculateDailyTotalBySender(
            @Param("senderId") Long senderId,
            @Param("date") LocalDate date
    );
}
