package searchengine.model;

import jakarta.persistence.*;
import lombok.*;
import searchengine.dto.indexing.Status;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Enumerated(EnumType.STRING)
    private Status status;
    private LocalDateTime creationTime;
    private String error;
    private String url;
    private String name;
}
