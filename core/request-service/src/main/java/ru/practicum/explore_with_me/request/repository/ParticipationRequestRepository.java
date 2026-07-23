package ru.practicum.explore_with_me.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explore_with_me.interaction_api.model.request.RequestStatus;
import ru.practicum.explore_with_me.request.dao.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    List<ParticipationRequest> findByEventId(Long eventId);

    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    List<ParticipationRequest> findByIdIn(List<Long> requestIds);

    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.eventId = :eventId AND pr.status = 'CONFIRMED'")
    Long countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findAllByEventIdInAndStatus(List<Long> eventIds, RequestStatus status);

    @Modifying
    @Query("UPDATE ParticipationRequest pr SET pr.status = :status WHERE pr.id IN :requestIds")
    void updateStatusForRequests(@Param("requestIds") List<Long> requestIds, @Param("status") RequestStatus status);

    @Modifying
    @Query("UPDATE ParticipationRequest pr SET pr.status = :status WHERE pr.eventId = :eventId AND pr.status = :currentStatus")
    void updateStatusForPendingRequests(@Param("eventId") Long eventId, @Param("currentStatus") RequestStatus currentStatus, @Param("status") RequestStatus status);
}