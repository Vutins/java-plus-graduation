package ru.practicum.explore_with_me.comment.controller.public_rights;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.comment.service.CommentService;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.CommentDto;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping("/{eventId}")
    public List<CommentDto> getEventCommentsByPublic(@Positive @PathVariable Long eventId,
                                                   @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                   @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET-запрос публичного контроллера на получение списка отзывов к ивенту с id={}", eventId);
        return commentService.getEventCommentsByPublic(eventId, from, size);
    }
}