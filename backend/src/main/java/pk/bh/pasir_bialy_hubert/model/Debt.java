package pk.bh.pasir_bialy_hubert.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "debts")
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private String title; // Transaction title or description

    // NOWE POLA - dodaj je tutaj
    private boolean paidByDebtor = false;
    private boolean confirmedByCreditor = false;

    public String getTitle() { return title != null ? title : "Brak opisu"; }

    @ManyToOne
    @JoinColumn(name = "debtor_id")
    private User debtor; // User who owes the money

    @ManyToOne
    @JoinColumn(name = "creditor_id")
    private User creditor; // User who is owed the money

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group; // Group to which this debt belongs


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isPaidByDebtor() {
        return paidByDebtor;
    }

    public void setPaidByDebtor(boolean paidByDebtor) {
        this.paidByDebtor = paidByDebtor;
    }

    public boolean isConfirmedByCreditor() {
        return confirmedByCreditor;
    }

    public void setConfirmedByCreditor(boolean confirmedByCreditor) {
        this.confirmedByCreditor = confirmedByCreditor;
    }

    public User getDebtor() {
        return debtor;
    }

    public void setDebtor(User debtor) {
        this.debtor = debtor;
    }

    public User getCreditor() {
        return creditor;
    }

    public void setCreditor(User creditor) {
        this.creditor = creditor;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}