package ru.practicum.explore_with_me.comment.service.private_rights;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.interaction_api.exception.ConflictException;
import ru.practicum.explore_with_me.interaction_api.exception.NotFoundException;
import ru.practicum.explore_with_me.interaction_api.model.event.EventState;
import ru.practicum.explore_with_me.interaction_api.model.event.client.EventServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventFullDto;
import ru.practicum.explore_with_me.interaction_api.model.request.RequestStatus;
import ru.practicum.explore_with_me.interaction_api.model.request.client.ParticipationRequestServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.ParticipationRequestDto;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.NewCommentDto;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.CommentDto;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.UpdateCommentDto;
import ru.practicum.explore_with_me.interaction_api.model.user.client.UserServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.user.dto.UserShortDto;
import ru.practicum.explore_with_me.comment.dao.Comment;
import ru.practicum.explore_with_me.comment.mapper.CommentMapper;
import ru.practicum.explore_with_me.comment.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateCommentServiceImpl implements PrivateCommentService {
    private final CommentRepository commentRepository;

    private final UserServiceClient userServiceClient;
    private final EventServiceClient eventServiceClient;
    private final ParticipationRequestServiceClient requestServiceClient;

    private final CommentMapper commentMapper;

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
}