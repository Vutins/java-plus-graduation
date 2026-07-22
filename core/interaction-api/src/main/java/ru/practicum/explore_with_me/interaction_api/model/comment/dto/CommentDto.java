package ru.practicum.explore_with_me.interaction_api.model.comment.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {

    Long id;
    Long commentatorId;
    Long eventId;
    LocalDateTime created;
    String text;
}
