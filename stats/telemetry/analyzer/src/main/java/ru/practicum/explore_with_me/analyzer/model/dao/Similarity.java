package ru.practicum.explore_with_me.analyzer.model.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "similarities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Similarity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "event_id1", nullable = false)
    private Long eventId1;
    @Column(name = "event_id2", nullable = false)
    private Long eventId2;
    @Column(name = "similarity", nullable = false)
    private Double similarity;
    @Column(name = "ts", nullable = false)
    private LocalDateTime ts;
}