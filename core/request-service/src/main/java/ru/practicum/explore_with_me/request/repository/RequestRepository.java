package ru.practicum.explore_with_me.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.explore_with_me.request.entity.ParticipationRequest;
import ru.practicum.explore_with_me.interaction_api.model.request.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByRequesterId(Long userId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    Optional<ParticipationRequest> findByRequesterIdAndEventId(Long userId, Long eventId);

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr " +
            "WHERE pr.eventId = :eventId AND pr.status = 'CONFIRMED'")
    Long countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT pr.eventId, COUNT(pr) " +
            "FROM ParticipationRequest pr " +
            "WHERE pr.eventId IN :events AND pr.status = 'CONFIRMED' " +
            "GROUP BY pr.eventId")
    List<Object[]> countConfirmedRequestsForEvents(@Param("events") List<Long> events);

    List<ParticipationRequest> findAllByIdInAndEventId(List<Long> ids, Long eventId);

    @Modifying
    @Query("UPDATE ParticipationRequest pr SET pr.status = 'REJECTED' " +
            "WHERE pr.eventId = :eventId AND pr.status = 'PENDING'")
    void rejectAllPendingRequestsByEventId(@Param("eventId") Long eventId);

    List<ParticipationRequest> findAllByEventIdInAndStatus(List<Long> eventIds, RequestStatus status);

    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);
}