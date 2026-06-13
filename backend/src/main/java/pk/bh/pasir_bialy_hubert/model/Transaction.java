package pk.bh.pasir_bialy_hubert.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * The Transaction entity represents a single financial transaction.
 * Each transaction has a unique identifier, amount, type, tags, notes, and creation date.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")

@SuppressWarnings("JpaDataSourceORMInspection")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String tags;

    private String notes;

    private LocalDateTime timestamp = LocalDateTime.now(); // Inicjalizacja tutaj zastępuje konstruktor

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Konstruktor z parametrami
    public Transaction(Double amount, TransactionType type, String tags, String notes, User user) {
        this.amount = amount;
        this.type = type;
        this.tags = tags;
        this.notes = notes;
        this.user = user; // To dodajemy
        this.timestamp = LocalDateTime.now();
    }
}
