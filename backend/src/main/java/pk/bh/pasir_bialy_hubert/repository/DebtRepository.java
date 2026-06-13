package pk.bh.pasir_bialy_hubert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pk.bh.pasir_bialy_hubert.model.Debt;

import java.util.List;

public interface DebtRepository extends JpaRepository<Debt, Long> {

    List<Debt> findByGroupId(Long groupId);

    void deleteByGroupId(Long groupId);
}