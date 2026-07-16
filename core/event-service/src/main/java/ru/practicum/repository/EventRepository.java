package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.Event;
import ru.practicum.model.event.enums.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Query("SELECT e FROM Event e WHERE e.id = :eventId AND e.state = 'PUBLISHED'")
    Optional<Event> findPublishedEventById(Long eventId);

    boolean existsByCategoryId(Long categoryId);

    boolean existsById(Long id);

    Page<Event> findByInitiatorId(Long userId, Pageable pageable);

    Set<Event> findAllByIdIn(Set<Long> eventIds);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    List<Event> findByCategoryId(Long categoryId);

    long countByCategoryId(Long categoryId);

    @Query("SELECT e FROM Event e WHERE " +
            "(:users IS NULL OR e.initiatorId IN :users) AND " +
            "(:states IS NULL OR e.state IN :states) AND " +
            "(:categories IS NULL OR e.categoryId IN :categories) AND " +
            "(e.eventDate BETWEEN :rangeStart AND :rangeEnd)")
    Page<Event> findEventsByAdmin(@Param("users") List<Long> users,
                                  @Param("states") List<State> states,
                                  @Param("categories") List<Long> categories,
                                  @Param("rangeStart") LocalDateTime rangeStart,
                                  @Param("rangeEnd") LocalDateTime rangeEnd,
                                  Pageable pageable);
}
