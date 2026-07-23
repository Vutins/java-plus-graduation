package ru.practicum.explore_with_me.comment.service.public_rights;

import ru.practicum.explore_with_me.interaction_api.model.comment.dto.CommentDto;

import java.util.List;

public interface PublicCommentService {

    List<CommentDto> getEventCommentsByPublic(Long eventId, Integer from, Integer size);

}