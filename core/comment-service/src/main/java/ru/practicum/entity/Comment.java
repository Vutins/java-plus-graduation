package ru.practicum.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commentator_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_comments_users"))
    Long commentatorId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_comments_events"))
    Long eventId;

    @NotNull
    @PastOrPresent
    @Column(name = "created", nullable = false)
    LocalDateTime created;

    @Column(name = "text", nullable = false)
    @NotBlank(message = "текст комментария не может быть пустым")
    String text;
}