package pk.bh.pasir_bialy_hubert.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import pk.bh.pasir_bialy_hubert.dto.TransactionDTO;
import pk.bh.pasir_bialy_hubert.model.Transaction;
import pk.bh.pasir_bialy_hubert.model.TransactionType;
import pk.bh.pasir_bialy_hubert.repository.TransactionRepository;
import pk.bh.pasir_bialy_hubert.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import pk.bh.pasir_bialy_hubert.model.User; // dostosuj do swojej struktury
import pk.bh.pasir_bialy_hubert.dto.BalanceDTO;

import org.springframework.security.access.AccessDeniedException;
//import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
//@RequiredArgsConstructor // To zastępuje ręczny konstruktor z obrazka (Lombok!)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {

            throw new RuntimeException("Użytkownik nie jest uwierzytelniony");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono zalogowanego użytkownika: " + email));
    }


    public List<Transaction> getAllTransactions() {
        User user = getCurrentUser();
        return transactionRepository.findAllByUser(user);
    }

    public Transaction getTransactionById(Long id) {
        // 1. Sprawdzamy czy istnieje
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono transakcji o ID: " + id));

        // 2. Sprawdzamy czy należy do nas
        if (!transaction.getUser().getEmail().equals(getCurrentUser().getEmail())) {
            throw new AccessDeniedException("Nie masz dostępu do tej transakcji");
        }

        return transaction;
    }

    public Transaction updateTransaction(Long id, TransactionDTO transactionDTO) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono transakcji o ID " + id));

        if (!transaction.getUser().getEmail().equals(getCurrentUser().getEmail())) {
            throw new AccessDeniedException("Nie masz dostępu do tej transakcji");
        }

        transaction.setAmount(transactionDTO.getAmount());
        transaction.setType(TransactionType.valueOf(transactionDTO.getType()));
        transaction.setTags(transactionDTO.getTags());
        transaction.setNotes(transactionDTO.getNotes());

        return transactionRepository.save(transaction);
    }

    // Praca samodzielna: Metoda POST
    public Transaction createTransaction(TransactionDTO transactionDTO) {
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setType(TransactionType.valueOf(transactionDTO.getType()));
        transaction.setTags(transactionDTO.getTags());
        transaction.setNotes(transactionDTO.getNotes());
        transaction.setUser(getCurrentUser());
        transaction.setTimestamp(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    /*
    public Transaction deleteTransaction(Long id) {
        // 1. Sprawdzamy czy istnieje (Listing 4.9 - punkt 1)
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono transakcji o ID: " + id));

        // 2. Sprawdzamy czy należy do nas (Listing 4.9 - punkt 2)
        if (!transaction.getUser().getEmail().equals(getCurrentUser().getEmail())) {
            throw new AccessDeniedException("Nie masz dostępu do tej transakcji");
        }

        return  transactionRepository.delete(transaction);
    }*/


    public void deleteTransaction(Long id) {
        // 1. Pobierz aktualnie zalogowanego użytkownika z kontekstu Spring Security
        User currentUser = getCurrentUser();

        // 2. Znajdź transakcję lub wyrzuć błąd, jeśli podane ID nie istnieje w bazie
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono transakcji o ID: " + id));

        // 3. Sprawdź, czy ta transakcja należy do zalogowanego użytkownika
        if (!transaction.getUser().getEmail().equals(currentUser.getEmail())) {
            throw new org.springframework.security.access.AccessDeniedException("Nie masz uprawnień do usunięcia tej transakcji!");
        }

        // 4. Jeśli weryfikacja przeszła pomyślnie - usuń z bazy danych
        transactionRepository.delete(transaction);
    }

    public BalanceDTO getUserBalance(User user, Float days) {
        List<Transaction> userTransactions;

        if (days != null) {
            LocalDateTime from = LocalDateTime.now().minusSeconds((long)(days * 24 * 60 * 60));
            userTransactions = transactionRepository
                    .findAllByUserAndTimestampGreaterThanEqual(user, from);
        } else {
            userTransactions = transactionRepository.findByUser(user);
        }

        double income = userTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double expense = userTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();

        return new BalanceDTO(income, expense, income - expense);
    }


    public BalanceDTO getUserBalance(User user, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime fromDateTime = (fromDate != null) ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = (toDate != null) ? toDate.atTime(23, 59, 59) : null;

        List<Transaction> userTransactions;
        if (fromDateTime != null && toDateTime != null) {
            userTransactions = transactionRepository.findAllByUserAndTimestampBetween(user, fromDateTime, toDateTime);
        } else if (fromDateTime != null) {
            userTransactions = transactionRepository.findAllByUserAndTimestampGreaterThanEqual(user, fromDateTime);
        } else if (toDateTime != null) {
            userTransactions = transactionRepository.findAllByUserAndTimestampLessThanEqual(user, toDateTime);
        } else {
            userTransactions = transactionRepository.findByUser(user);
        }

        double income = userTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
        double expense = userTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
        return new BalanceDTO(income, expense, income - expense);
    }



}