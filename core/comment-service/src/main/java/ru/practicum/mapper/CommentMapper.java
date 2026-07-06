package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.entity.Comment;
import ru.practicum.model.comment.dto.CommentDto;
import ru.practicum.model.comment.dto.CommentRequestDto;
import ru.practicum.model.comment.dto.CommentResponseDto;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "commentator", ignore = true)
    Comment toComment(CommentRequestDto commentRequestDto);

    @Mapping(source = "commentator.id", target = "commentatorId")
    CommentResponseDto toCommentResponseDto(Comment comment);

    @Mapping(source = "commentator.id", target = "commentatorId")
    @Mapping(source = "event.id", target = "eventId")
    CommentDto toCommentDto(Comment comment);
}