package ru.practicum.explore_with_me.comment.service.private_rights;

import ru.practicum.explore_with_me.interaction_api.model.comment.dto.NewCommentDto;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.CommentDto;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.UpdateCommentDto;

import java.util.List;

public interface PrivateCommentService {

    CommentDto addComment(Long userId, Long eventId, NewCommentDto dto);

    void deleteCommentByAuthor(Long userId, Long commentId);

    CommentDto getCommentById(Long userId, Long commentId);

    List<CommentDto> getCommentsByAuthor(Long userId);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto);

}