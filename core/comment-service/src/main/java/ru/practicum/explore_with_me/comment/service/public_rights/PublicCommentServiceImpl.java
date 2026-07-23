package ru.practicum.explore_with_me.comment.service.public_rights;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.interaction_api.model.event.client.EventServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.CommentDto;
import ru.practicum.explore_with_me.comment.dao.Comment;
import ru.practicum.explore_with_me.comment.mapper.CommentMapper;
import ru.practicum.explore_with_me.comment.repository.CommentRepository;


import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicCommentServiceImpl implements PublicCommentService {
    private final CommentRepository commentRepository;
    private final EventServiceClient eventServiceClient;

    private final CommentMapper commentMapper;

    private static final String serviceName = "[COMMENT-SERVICE]";

    @Override
    public List<CommentDto> getEventCommentsByPublic(Long eventId, Integer from, Integer size) {
        log.debug("Запрос на получение event клиентом из getEventCommentsByPublic сервиса {}", serviceName);
        eventServiceClient.validateEventExistingById(eventId);

        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAllByEventId(eventId, pageRequest);
        return comments
                .stream()
                .map(commentMapper::toCommentDto)
                .toList();
    }
}