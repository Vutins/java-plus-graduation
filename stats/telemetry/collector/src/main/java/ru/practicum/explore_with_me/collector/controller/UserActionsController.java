package ru.practicum.explore_with_me.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.explore_with_me.collector.service.UserActionService;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc.UserActionControllerImplBase;
import ru.practicum.ewm.stats.proto.UserActionProto;


@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionsController extends UserActionControllerImplBase {

    private final UserActionService userActionService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        log.info("GRPC method collectUserAction started userAction={} ", request.toString());
        try {
            userActionService.collectUserAction(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getLocalizedMessage()).withCause(e)));
        }
    }
}