package searchengine.model.site;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.tuple.GenerationTiming;

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
    @CurrentTimestamp(timing = GenerationTiming.ALWAYS)
    private LocalDateTime creationTime;
    private String error;
    private String url;
    private String name;
}
