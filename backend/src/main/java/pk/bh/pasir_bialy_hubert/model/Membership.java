package pk.bh.pasir_bialy_hubert.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "memberships")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group; // Group to which the user belongs

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // User who is a member of the group
}