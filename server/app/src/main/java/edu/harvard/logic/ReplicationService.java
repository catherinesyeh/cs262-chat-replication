package edu.harvard.logic;

import java.io.IOException;

import edu.harvard.Logreplay.IntroductionRequest;
import edu.harvard.Logreplay.IntroductionResponse;
import edu.harvard.Logreplay.LogMessage;
import edu.harvard.Logreplay.RelayResponse;
import edu.harvard.ReplicationServiceGrpc.ReplicationServiceImplBase;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;

class GrpcReplicationService extends ReplicationServiceImplBase {
  private final ReplicationService replicationService;

  GrpcReplicationService(ReplicationService replicationService) {
    this.replicationService = replicationService;
  }

  @Override
  public void introduction(IntroductionRequest request, StreamObserver<IntroductionResponse> response) {
    IntroductionResponse introductionResponse = replicationService.handleIntroduction(request);
    response.onNext(introductionResponse);
    response.onCompleted();
  }

  @Override
  public void relay(LogMessage request, StreamObserver<RelayResponse> response) {
    RelayResponse relayResponse = replicationService.handleRelay(request);
    response.onNext(relayResponse);
    response.onCompleted();
  }
}

public class ReplicationService {
  Configuration config;
  LogReplay logReplay;

  public ReplicationService(Configuration config, LogReplay logReplay) {
    this.logReplay = logReplay;
    this.config = config;
  }

  synchronized IntroductionResponse handleIntroduction(IntroductionRequest request) {
    return IntroductionResponse.newBuilder().build();
  }

  synchronized RelayResponse handleRelay(LogMessage message) {
    return RelayResponse.newBuilder().setNeedsResync(true).build();
  }

  public Server startService() throws IOException {
    Server server = Grpc.newServerBuilderForPort(config.replicaPort, InsecureServerCredentials.create())
        .addService(new GrpcReplicationService(this)).build();
    server.start();
    return server;
  }

  public synchronized void relayMessage() {

  }

  public synchronized void introduce() {

  }
}
