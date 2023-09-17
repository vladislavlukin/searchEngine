package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import searchengine.dto.indexing.Status;

import java.time.LocalDateTime;
@Getter
@Setter
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
