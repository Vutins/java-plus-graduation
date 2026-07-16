package ru.practicum.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.entity.Reaction;
import ru.practicum.model.comment.dto.CommentDto;
import ru.practicum.model.comment.dto.CommentResponseDto;
import ru.practicum.model.comment.dto.ReactionResponseDto;
import ru.practicum.model.user.client.UserServiceClient;
import ru.practicum.service.CommentService;
import ru.practicum.user.dto.UserShortDto;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ReactionMapper {

    private final CommentMapper commentMapper;
    private final CommentService commentService;
    private final UserServiceClient userServiceClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReactionResponseDto toReactionResponseDto(Reaction reaction) {
        UserShortDto userShortDto = userServiceClient.getUserShortById(reaction.getEvaluatorId());
        CommentDto commentDto = commentService.getCommentById(reaction.getCommentId());

        CommentResponseDto commentResponseDto = commentMapper.toCommentResponseDto(commentDto);

        ReactionResponseDto reactionResponseDto = ReactionResponseDto.builder()
                .id(reaction.getId())
                .voteType(reaction.getVoteType())
                .evaluator(userShortDto)
                .commentResponseDto(commentResponseDto)
                .created(formatter.format(reaction.getCreated()))
                .build();

        if (reaction.getUpdated() != null) {
            reactionResponseDto.setUpdated(formatter.format(reaction.getUpdated()));
        }

        return reactionResponseDto;
    }
}
