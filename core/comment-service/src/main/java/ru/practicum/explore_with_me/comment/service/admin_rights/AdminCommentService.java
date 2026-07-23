package ru.practicum.explore_with_me.comment.service.admin_rights;

import ru.practicum.explore_with_me.interaction_api.model.comment.dto.CommentDto;

import java.util.List;

public interface AdminCommentService {

    List<CommentDto> getCommentsByAdmin(String text,
                                      List<Long> users,
                                      List<Long> events,
                                      Integer from,
                                      Integer size);

    void deleteComment(Long commentId);

}