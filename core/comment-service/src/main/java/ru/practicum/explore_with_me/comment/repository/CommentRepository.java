package ru.practicum.explore_with_me.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.explore_with_me.comment.dao.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    List<Comment> findAllByEventId(Long eventId, Pageable pageable);

    Optional<Comment> findByIdAndAuthorId(Long id, Long authorId);

    List<Comment> findAllByAuthorId(Long authorId);

    Optional<Comment> findByEventIdAndAuthorId(Long eventId, Long authorId);

}