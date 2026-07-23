package ru.practicum.explore_with_me.comment.mapper;

import org.mapstruct.Mapper;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.NewCommentDto;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.CommentDto;
import ru.practicum.explore_with_me.comment.dao.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentDto toCommentDto(Comment comment);

    Comment toComment(NewCommentDto dto);

}