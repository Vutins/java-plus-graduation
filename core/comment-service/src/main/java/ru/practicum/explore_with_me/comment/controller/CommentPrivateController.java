package ru.practicum.explore_with_me.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.CommentDto;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.CommentRequestDto;
import ru.practicum.explore_with_me.comment.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events/{eventId}/users/{commentatorId}/comment")
@Validated
public class CommentPrivateController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> create(
            @Valid @RequestBody CommentRequestDto commentRequestDto,
            @NotNull @Positive @PathVariable("eventId") Long eventId,
            @NotNull @Positive @PathVariable("commentatorId") Long commentatorId) {
        CommentDto commentDto = commentService.create(commentRequestDto, commentatorId, eventId);
        return ResponseEntity.ok(commentDto);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> update(
            @Valid @RequestBody CommentRequestDto commentRequestDto,
            @NotNull @Positive @PathVariable("eventId") Long eventId,
            @NotNull @Positive @PathVariable("commentatorId") Long commentatorId,
            @NotNull @Positive @PathVariable("commentId") Long commentId) {
        CommentDto commentDto = commentService.update(commentRequestDto, eventId, commentatorId, commentId);
        return ResponseEntity.ok(commentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @NotNull @Positive @PathVariable("eventId") Long eventId,
            @NotNull @Positive @PathVariable("commentatorId") Long commentatorId,
            @NotNull @Positive @PathVariable("commentId") Long commentId) {
        commentService.delete(eventId, commentId, commentatorId);
    }

    @GetMapping("/{commentId}")
    public CommentDto getCommentsById(@Positive @PathVariable Long commentId) {
        return commentService.getCommentById(commentId);
    }

    @GetMapping
    public List<CommentDto> getCommentsByAuthor(@Positive @PathVariable Long userId) {
        return commentService.getCommentsByAuthor(userId);
    }
}