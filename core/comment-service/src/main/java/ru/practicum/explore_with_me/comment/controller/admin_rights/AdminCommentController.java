package ru.practicum.explore_with_me.comment.controller.admin_rights;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.interaction_api.model.comment.dto.CommentDto;
import ru.practicum.explore_with_me.comment.service.admin_rights.AdminCommentService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class AdminCommentController {

    private final AdminCommentService commentService;

    @GetMapping
    public List<CommentDto> getCommentsByAdmin(@RequestParam(required = false) String text,
                                             @RequestParam(required = false) List<Long> users,
                                             @RequestParam(required = false) List<Long> events,
                                             @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                             @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("GET-запрос админ-контроллера на поиск отзывов по заданным параметрам");
        log.info("Параметры поиска: text={}, users={}, events={}", text, users, events);
        return commentService.getCommentsByAdmin(text, users, events, from, size);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@Positive @PathVariable Long commentId) {
        log.info("DELETE-запрос админ-контроллера на удаление отзыва с id={}", commentId);
        commentService.deleteComment(commentId);
    }
}