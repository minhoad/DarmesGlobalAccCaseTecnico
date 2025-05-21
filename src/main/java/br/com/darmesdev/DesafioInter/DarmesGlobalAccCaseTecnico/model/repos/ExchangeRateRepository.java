package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    @Query("SELECT e FROM ExchangeRate e WHERE e.date = :date AND e.rate IS NOT NULL")
    Optional<ExchangeRate> findByDate(@Param("date") LocalDate date);

    @Query("SELECT e FROM ExchangeRate e WHERE e.rate IS NOT NULL ORDER BY e.date DESC")
    Optional<ExchangeRate> findTopByOrderByDateDesc();

    @Query("SELECT er FROM ExchangeRate er WHERE er.date <= :date ORDER BY er.date DESC")
    Optional<ExchangeRate> findLatestUntilDate(@Param("date") LocalDate date);

    @Query("SELECT CASE WHEN COUNT(er) > 0 THEN true ELSE false END FROM ExchangeRate er WHERE er.date = :date")
    boolean existsByDate(@Param("date") LocalDate date);

    @Query("SELECT e FROM ExchangeRate e WHERE e.date <= :date AND e.rate IS NOT NULL ORDER BY e.date DESC")
    Optional<ExchangeRate> findLatestValidRateBefore(@Param("date") LocalDate date);
}