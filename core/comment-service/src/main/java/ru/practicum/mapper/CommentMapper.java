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
    @Mapping(target = "commentatorId", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    Comment toComment(CommentRequestDto commentRequestDto);

    @Mapping(source = "commentatorId", target = "commentatorId")
    @Mapping(source = "created", target = "created")
    @Mapping(source = "text", target = "text")
    CommentResponseDto toCommentResponseDto(Comment comment);

    @Mapping(source = "commentatorId", target = "commentatorId")
    @Mapping(source = "eventId", target = "eventId")
    @Mapping(source = "created", target = "created")
    @Mapping(source = "text", target = "text")
    CommentDto toCommentDto(Comment comment);

    CommentResponseDto toCommentResponseDto(CommentDto commentDto);
}