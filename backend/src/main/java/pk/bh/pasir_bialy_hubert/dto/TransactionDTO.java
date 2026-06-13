package pk.bh.pasir_bialy_hubert.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionDTO {

    // Kwota nie może być pusta i musi być większa od 0
    @NotNull(message = "Kwota nie może być pusta")
    @DecimalMin(value = "0.01", message = "Kwota musi być większa od 0")
    private Double amount;

    // Typ transakcji jest wymagany i musi być wartością INCOME lub EXPENSE
    @NotNull(message = "Typ transakcji jest wymagany")
    @Pattern(regexp = "INCOME|EXPENSE", message = "Typ musi być wartością INCOME lub EXPENSE")
    private String type;

    // Tagi nie mogą przekraczać 50 znaków
    @Size(max = 50, message = "Tagi nie mogą przekraczać 50 znaków")
    private String tags;

    // Notatka może mieć maksymalnie 255 znaków
    @Size(max = 255, message = "Notatka może mieć maksymalnie 255 znaków")
    private String notes;
}