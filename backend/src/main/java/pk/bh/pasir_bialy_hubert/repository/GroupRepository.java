package pk.bh.pasir_bialy_hubert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pk.bh.pasir_bialy_hubert.model.Group;
import pk.bh.pasir_bialy_hubert.model.User;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findByMemberships_User(User user);
}