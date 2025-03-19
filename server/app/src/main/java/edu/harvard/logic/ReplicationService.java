package edu.harvard.logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.ReplicationServiceGrpc;
import edu.harvard.Logreplay.IntroductionRequest;
import edu.harvard.Logreplay.IntroductionResponse;
import edu.harvard.Logreplay.LogMessage;
import edu.harvard.Logreplay.RelayResponse;
import edu.harvard.Logreplay.ReplicaInfo;
import edu.harvard.Logreplay.LogMessage.LogMessageCase;
import edu.harvard.ReplicationServiceGrpc.ReplicationServiceBlockingStub;
import edu.harvard.ReplicationServiceGrpc.ReplicationServiceImplBase;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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
  Map<String, ReplicaInfo> liveReplicas;

  public ReplicationService(Configuration config, LogReplay logReplay) {
    this.logReplay = logReplay;
    this.config = config;
    this.liveReplicas = new HashMap<>();
    this.logReplay.registerReplicationService(this);
  }

  synchronized IntroductionResponse handleIntroduction(IntroductionRequest request) {
    IntroductionResponse response = IntroductionResponse.newBuilder()
        .setId(config.replicaID)
        .addAllReplicas(liveReplicas.values())
        .addAllMessages(logReplay.getNewerMessages(request.getSyncStatesList()))
        .build();
    liveReplicas.put(request.getInfo().getId(), request.getInfo());
    return response;
  }

  synchronized RelayResponse handleRelay(LogMessage message) {
    ReplicaInfo replica = liveReplicas.get(message.getOriginatingReplicaId());
    if (replica == null) {
      return RelayResponse.newBuilder().setNeedsResync(true).build();
    } else {
      logReplay.receiveMessage(message);
      return RelayResponse.newBuilder().setNeedsResync(false).build();
    }
  }

  public Server startService() throws IOException {
    Server server = Grpc.newServerBuilderForPort(config.replicaPort, InsecureServerCredentials.create())
        .addService(new GrpcReplicationService(this)).build();
    server.start();
    return server;
  }

  public synchronized void relayMessage(LogMessage message) {
    // relay to all live replicas (copy is necessary since we're modifying it)
    for (ReplicaInfo replica : List.copyOf(liveReplicas.values())) {
      try {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(replica.getHostname(), replica.getPort())
            .usePlaintext().build();
        ReplicationServiceBlockingStub stub = ReplicationServiceGrpc.newBlockingStub(channel);
        RelayResponse response = stub.relay(message);
        // if this replica doesn't know we exist for some reason, introduce then relay
        if (response.getNeedsResync()) {
          introduce(replica);
          stub.relay(message);
        }
      } catch (Exception e) {
        // remove from live replicas on failure
        System.out.println("Exception while relaying to replica ".concat(replica.getId()).concat(" at ")
            .concat(replica.getHostname()).concat(":").concat(String.valueOf(replica.getPort())));
        System.out.println(e);
        System.out.println(e.getMessage());
        liveReplicas.remove(replica.getId());
      }
    }
  }

  private synchronized void handleIntroductionResponse(List<LogMessage> messages) {
    // filter for each type and then apply all of those
    // create account
    messages.stream().filter(m -> m.getLogMessageCase().equals(LogMessageCase.NEW_ACCOUNT))
        .forEach(logReplay::receiveMessage);
    // send message
    messages.stream().filter(m -> m.getLogMessageCase().equals(LogMessageCase.NEW_CHAT_MESSAGE))
        .forEach(logReplay::receiveMessage);
    // mark as read
    messages.stream().filter(m -> m.getLogMessageCase().equals(LogMessageCase.MARK_AS_READ))
        .forEach(logReplay::receiveMessage);
    // delete message
    messages.stream().filter(m -> m.getLogMessageCase().equals(LogMessageCase.DELETE_MESSAGES))
        .forEach(logReplay::receiveMessage);
    // delete account
    messages.stream().filter(m -> m.getLogMessageCase().equals(LogMessageCase.DELETE_ACCOUNT))
        .forEach(logReplay::receiveMessage);
  }

  // repeatedly make introductions until done!
  private synchronized void introduce(ReplicaInfo initialReplica) {
    // build its own ReplicaInfo
    ReplicaInfo myInfo = ReplicaInfo.newBuilder().setHostname(config.hostname).setPort(config.replicaPort)
        .setId(config.replicaID).build();
    List<ReplicaInfo> introductionPoints = new ArrayList<>();
    introductionPoints.add(initialReplica);
    while (introductionPoints.size() > 0) {
      ReplicaInfo introPoint = introductionPoints.remove(0);
      // introduce to first introduction point
      ManagedChannel channel = ManagedChannelBuilder.forAddress(introPoint.getHostname(), introPoint.getPort())
          .usePlaintext().build();
      ReplicationServiceBlockingStub stub = ReplicationServiceGrpc.newBlockingStub(channel);
      IntroductionResponse response = stub.introduction(
          IntroductionRequest.newBuilder().setInfo(myInfo).addAllSyncStates(logReplay.getLatestTimestamps()).build());
      handleIntroductionResponse(response.getMessagesList());
      // add to liveReplicas
      liveReplicas.put(response.getId(), ReplicaInfo.newBuilder(introPoint).setId(response.getId()).build());
      // add its replicas to the list of introduction points if not in liveReplicas
      for (ReplicaInfo replica : response.getReplicasList()) {
        if (!liveReplicas.containsKey(replica.getId()) && !replica.getId().equals(config.replicaID)) {
          introductionPoints.add(replica);
        }
      }
    }
  }

  public synchronized void introduce() {
    if (config.introductionHostname != null) {
      introduce(
          ReplicaInfo.newBuilder().setHostname(config.introductionHostname).setPort(config.introductionPort).build());
    }
  }

  public synchronized List<ReplicaInfo> getOtherReplicas() {
    return List.copyOf(liveReplicas.values());
  }
}
