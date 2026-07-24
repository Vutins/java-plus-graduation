package ru.practicum.explore_with_me.comment.service;

import ru.practicum.explore_with_me.interaction_api.model.comment.dto.CommentDto;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.NewCommentDto;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    List<CommentDto> getEventCommentsByPublic(Long eventId, Integer from, Integer size);

    CommentDto addComment(Long userId, Long eventId, NewCommentDto dto);

    void deleteCommentByAuthor(Long userId, Long commentId);

    CommentDto getCommentById(Long userId, Long commentId);

    List<CommentDto> getCommentsByAuthor(Long userId);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto);

    List<CommentDto> getCommentsByAdmin(String text,
                                        List<Long> users,
                                        List<Long> events,
                                        Integer from,
                                        Integer size);

    void deleteComment(Long commentId);
}
