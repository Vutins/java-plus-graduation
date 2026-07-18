package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.entity.Reaction;
import ru.practicum.model.comment.dto.ReactionResponseDto;
import ru.practicum.model.user.dto.UserShortDto;

@Mapper(componentModel = "spring", uses = {CommentMapper.class})
public interface ReactionMapper {

    @Mapping(source = "reaction.id", target = "id")
    @Mapping(source = "reaction.comment", target = "commentResponseDto")
    @Mapping(source = "evaluator", target = "evaluator")
    @Mapping(source = "reaction.created", target = "created", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(source = "reaction.updated", target = "updated", dateFormat = "yyyy-MM-dd HH:mm:ss")
    ReactionResponseDto toReactionResponseDto(Reaction reaction, UserShortDto evaluator);
}