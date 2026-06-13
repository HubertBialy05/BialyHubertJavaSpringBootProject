package pk.bh.pasir_bialy_hubert.repository;

import pk.bh.pasir_bialy_hubert.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List; // BARDZO WAŻNE
import pk.bh.pasir_bialy_hubert.model.User; // Import Twojej klasy User


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByUser(User user);
    List<Transaction> findByUser(User user);
    List<Transaction> findAllByUserAndTimestampGreaterThanEqual(User user, LocalDateTime timestamp);
    // Dodajemy nowe zapytania
    List<Transaction> findAllByUserAndTimestampBetween(User user, LocalDateTime from, LocalDateTime to);
    List<Transaction> findAllByUserAndTimestampLessThanEqual(User user, LocalDateTime to);

}

