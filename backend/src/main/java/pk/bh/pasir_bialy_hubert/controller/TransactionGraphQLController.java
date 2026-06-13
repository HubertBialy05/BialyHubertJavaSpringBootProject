package pk.bh.pasir_bialy_hubert.controller;

import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import pk.bh.pasir_bialy_hubert.dto.BalanceDTO;
import pk.bh.pasir_bialy_hubert.dto.TransactionDTO;
import pk.bh.pasir_bialy_hubert.model.Transaction;
import pk.bh.pasir_bialy_hubert.model.User;
import pk.bh.pasir_bialy_hubert.service.TransactionService;

import java.time.LocalDate;
import java.util.List;

@NullMarked
@Controller
public class TransactionGraphQLController {

    private final TransactionService transactionService;

    public TransactionGraphQLController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @QueryMapping
    public List<Transaction> transactions() {
        return transactionService.getAllTransactions();
    }

    @MutationMapping
    public Transaction addTransaction(
            @Valid @Argument TransactionDTO transactionDTO) {
        return transactionService.createTransaction(transactionDTO);
    }

    @MutationMapping
    public Transaction updateTransaction(
            @Argument Long id,
            @Valid @Argument TransactionDTO transactionDTO) {
        return transactionService.updateTransaction(id, transactionDTO);
    }

    @MutationMapping
    public Boolean deleteTransaction(@Argument Long id) {
        transactionService.deleteTransaction(id);
        return true; // zwracamy true, co w GraphQL przełoży się na "Boolean"
    }
/*
    @QueryMapping
    public BalanceDTO userBalance(@Argument Float days) {
        User user = transactionService.getCurrentUser();
        return transactionService.getUserBalance(user, days);
    }*/

    @QueryMapping
    public BalanceDTO userBalance(@Argument Float days,
                                  @Argument String fromDate,
                                  @Argument String toDate) {
        User user = transactionService.getCurrentUser();
        if (fromDate != null || toDate != null) {
            LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : null;
            LocalDate to = toDate != null ? LocalDate.parse(toDate) : null;
            return transactionService.getUserBalance(user, from, to);
        } else if (days != null) {
            return transactionService.getUserBalance(user, days);
        } else {
            return transactionService.getUserBalance(user, (Float) null);
        }
    }
}