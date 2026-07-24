package ru.practicum.explore_with_me.comment.controller.private_rights;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.comment.service.CommentService;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.NewCommentDto;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.CommentDto;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.UpdateCommentDto;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@Positive @PathVariable Long userId,
                               @Positive @PathVariable Long eventId,
                               @RequestBody @Valid NewCommentDto dto) {
        log.info("POST-запрос на добавление отзыва к ивенту с id={}", eventId);
        return commentService.addComment(userId, eventId, dto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@Positive @PathVariable Long userId,
                                  @Positive @PathVariable Long commentId,
                                  @RequestBody @Valid UpdateCommentDto dto) {
        log.info("PATCH-запрос на обновление отзыва с id={}", commentId);
        return commentService.updateComment(userId, commentId, dto);
    }

    @DeleteMapping("/{commentId}/events/{eventId}")
    public void deleteCommentByAuthor(@Positive @PathVariable Long userId,
                                     @Positive @PathVariable Long commentId) {
        log.info("DELETE-запрос на удаление автором отзыва с id={}", commentId);
        commentService.deleteCommentByAuthor(userId, commentId);
    }

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(@Positive @PathVariable Long userId,
                                   @Positive @PathVariable Long commentId) {
        log.info("GET-запрос на просмотр автором отзыва с id={}", commentId);
        return commentService.getCommentById(userId, commentId);
    }

    @GetMapping
    public List<CommentDto> getCommentsByAuthor(@Positive @PathVariable Long userId) {
        log.info("GET-запрос на просмотр всех отзывов автором с id={}", userId);
        return commentService.getCommentsByAuthor(userId);
    }
}