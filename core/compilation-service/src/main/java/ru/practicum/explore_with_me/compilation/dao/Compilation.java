package ru.practicum.explore_with_me.compilation.dao;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "compilations")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compilation_id")
    Long id;

    Boolean pinned;

    @Size(max = 50)
    @Column(name = "title", nullable = false)
    String title;

    @ElementCollection(targetClass = Long.class)
    @CollectionTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id")
    )
    @Builder.Default
    @Column(name = "event_id")
    private Set<Long> eventsId = new HashSet<>();
}