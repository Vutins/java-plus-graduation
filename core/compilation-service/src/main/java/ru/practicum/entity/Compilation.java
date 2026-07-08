package ru.practicum.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "compilations")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "pinned", nullable = false)
    Boolean pinned;

    @Column(name = "title", unique = true, nullable = false, length = 50)
    String title;

    @ElementCollection(targetClass = Locale.class)
    @CollectionTable(
            name = "compilations_events",
            joinColumns = @JoinColumn(name = "compilation_id")
    )
    @Builder.Default
    @Column(name = "event_id")
    Set<Long> events = new HashSet<>();
}