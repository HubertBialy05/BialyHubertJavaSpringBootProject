package pk.bh.pasir_bialy_hubert.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GroupResponseDTO {

    private Long id;
    private String name;
    private Long ownerId;
}