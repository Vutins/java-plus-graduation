package ru.practicum.explore_with_me.interaction_api.model.comment.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.explore_with_me.interaction_api.model.user.dto.UserShortDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReactionResponseDto {

    Long id;
    String voteType;
    UserShortDto evaluator;
    CommentResponseDto commentResponseDto;
    String created;
    String updated;
}
