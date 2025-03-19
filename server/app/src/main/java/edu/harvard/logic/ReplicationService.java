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
    liveReplicas.put(request.getInfo().getId(), request.getInfo());
    return IntroductionResponse.newBuilder()
        .setId(config.replicaID)
        .addAllReplicas(liveReplicas.values())
        .addAllMessages(logReplay.getNewerMessages(request.getSyncStatesList()))
        .build();
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
    // relay to all live replicas
    // failed relay = remove from map!
  }

  private synchronized void handleIntroductionResponse(List<LogMessage> messages) {
    // todo filter for each type and then apply all of those
  }

  // repeatedly make introductions until done!
  public synchronized void introduce() {
    // build its own ReplicaInfo
    ReplicaInfo myInfo = ReplicaInfo.newBuilder().setHostname(config.hostname).setPort(config.replicaPort)
        .setId(config.replicaID).build();
    // create list of introduction points
    List<ReplicaInfo> introductionPoints = new ArrayList<>();
    if (config.introductionHostname != null) {
      introductionPoints.add(
          ReplicaInfo.newBuilder().setHostname(config.introductionHostname).setPort(config.introductionPort).build());
    }
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
        if (!liveReplicas.containsKey(replica.getId())) {
          introductionPoints.add(replica);
        }
      }
    }
  }
}
