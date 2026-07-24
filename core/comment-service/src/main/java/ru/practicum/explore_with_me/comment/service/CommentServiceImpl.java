package ru.practicum.explore_with_me.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.comment.dao.Comment;
import ru.practicum.explore_with_me.comment.mapper.CommentMapper;
import ru.practicum.explore_with_me.comment.repository.CommentRepository;
import ru.practicum.explore_with_me.interaction_api.exception.ConflictException;
import ru.practicum.explore_with_me.interaction_api.exception.NotFoundException;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.CommentDto;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.NewCommentDto;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.UpdateCommentDto;
import ru.practicum.explore_with_me.interaction_api.model.event.EventState;
import ru.practicum.explore_with_me.interaction_api.model.event.client.EventServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventFullDto;
import ru.practicum.explore_with_me.interaction_api.model.request.RequestStatus;
import ru.practicum.explore_with_me.interaction_api.model.request.client.ParticipationRequestServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.ParticipationRequestDto;
import ru.practicum.explore_with_me.interaction_api.model.user.client.UserServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventServiceClient eventServiceClient;
    private final ParticipationRequestServiceClient requestServiceClient;
    private final CommentMapper commentMapper;
    private final UserServiceClient userServiceClient;

    @Override
    public List<CommentDto> getEventCommentsByPublic(Long eventId, Integer from, Integer size) {
        log.debug("Запрос на получение event клиентом из getEventCommentsByPublic сервиса");
        eventServiceClient.validateEventExistingById(eventId);

        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAllByEventId(eventId, pageRequest);
        return comments
                .stream()
                .map(commentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto dto) {

        UserShortDto userShortDto = userServiceClient.getUserShortDtoClientById(userId);

        log.debug("Запрос на получение event клиентом из addComment сервиса PrivateCommentServiceImpl");
        EventFullDto eventFullDto = eventServiceClient.getEventFullDtoByIdClient(eventId);

        if (commentRepository.findByEventIdAndAuthorId(eventId, userId).isPresent()) {
            throw new ConflictException("Юзер с id=" + userId + " уже написал отзыв к ивенту с id=" + eventId + "!");
        }
        verifyComment(userShortDto, eventFullDto);
        Comment comment = commentMapper.toComment(dto);
        comment.setAuthorId(userShortDto.getId());
        comment.setEventId(eventFullDto.getId());
        comment.setCreatedOn(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toCommentDto(savedComment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto) {
        userServiceClient.validateUserExistingById(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Отзыва с id=" + commentId + " нет в БД!"));

        if (!comment.getAuthorId().equals(userId)) {
            throw new ConflictException("Пользователь не является автором отзыва");
        }

        if (dto.getText() == null || dto.getText().isBlank() || dto.getText().equals(comment.getText())) {
            return commentMapper.toCommentDto(comment);
        }
        comment.setText(dto.getText());
        comment.setLastUpdatedOn(LocalDateTime.now());
        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toCommentDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteCommentByAuthor(Long userId, Long commentId) {
        UserShortDto userShortDto = userServiceClient.getUserShortDtoClientById(userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Отзыва с id=" + commentId + " нет в БД!"));

        if (!userShortDto.getId().equals(comment.getAuthorId())) {
            throw new ConflictException("Пользователь не является автором отзыва");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public CommentDto getCommentById(Long userId, Long commentId) {
        userServiceClient.validateUserExistingById(userId);

        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Юзер с id=" + commentId + " не писал отзыв с id=" + commentId + "!"));
        return commentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentDto> getCommentsByAuthor(Long userId) {
        userServiceClient.validateUserExistingById(userId);

        return commentRepository.findAllByAuthorId(userId)
                .stream()
                .map(commentMapper::toCommentDto)
                .toList();
    }

    private void verifyComment(UserShortDto user, EventFullDto event) {
        if (user.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException("Инициатор ивента не может оставлять отзыв на свой ивент!");
        }
        if (!event.getState().equals(EventState.PUBLISHED.toString())) {
            throw new ConflictException("Чтобы оставить отзыв, статус ивента должен быть PUBLISHED!");
        }
        if (!event.getEventDate().plusHours(1).isBefore(LocalDateTime.now())) {
            throw new ConflictException("Нельзя оставить отзыв на ивент, который ещё не закончился!");
        }
        ParticipationRequestDto requestDto = requestServiceClient.getUserRequestByUserIdAndEventId(user.getId(), event.getId());

        if (!requestDto.getStatus().equals(RequestStatus.CONFIRMED.toString())) {
            throw new ConflictException("Чтобы оставить отзыв, статус заявки юзера на участие в ивенте должен быть CONFIRMED!");
        }
    }

    @Override
    public List<CommentDto> getCommentsByAdmin(String text,
                                               List<Long> users,
                                               List<Long> events,
                                               Integer from,
                                               Integer size) {
        Specification<Comment> spec = Specification.where(null);
        if (text != null && !text.isBlank()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("text")), "%" + text.toLowerCase() + "%"));
        }
        if (users != null && !users.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    root.get("author").get("id").in(users));
        }
        if (events != null && !events.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    root.get("event").get("id").in(events));
        }
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAll(spec, pageable).toList();
        return comments
                .stream()
                .map(commentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Отзыва с id=" + commentId + " нет в БД!");
        }
        commentRepository.deleteById(commentId);
    }
}
