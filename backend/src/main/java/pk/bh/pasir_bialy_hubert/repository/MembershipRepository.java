package pk.bh.pasir_bialy_hubert.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import pk.bh.pasir_bialy_hubert.model.Membership;

import java.util.List;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByGroupId(Long groupId);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    void deleteByGroupId(Long groupId);
}