package edu.harvard.logic;

import org.junit.jupiter.api.Test;

import edu.harvard.Chat.ListAccountsRequest;
import edu.harvard.Chat.LoginCreateRequest;
import edu.harvard.Logreplay.LogMessage;
import io.grpc.Server;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static org.junit.jupiter.api.Assertions.*;

public class ReplicationTest {
    @Test
    void basicReplicationTest() throws Exception {
        // Set up first replica
        Configuration config1 = new Configuration("../config.example.json");
        config1.introductionHostname = null;
        Database db1 = new Database();
        LogReplay logReplay1 = new LogReplay(config1.replicaID, null, db1);
        ReplicationService replicationService1 = new ReplicationService(config1, logReplay1);
        OperationHandler handler1 = new OperationHandler(db1, logReplay1, config1, replicationService1);
        // Set up second replica
        Configuration config2 = new Configuration("../config.example.json");
        config2.replicaID = "2";
        config2.replicaPort = 55557;
        config2.introductionPort = 55556;
        Database db2 = new Database();
        LogReplay logReplay2 = new LogReplay(config2.replicaID, null, db2);
        ReplicationService replicationService2 = new ReplicationService(config2, logReplay2);
        OperationHandler handler2 = new OperationHandler(db2, logReplay2, config2, replicationService2);
        // Set up third replica
        Configuration config3 = new Configuration("../config.example.json");
        config3.replicaID = "3";
        config3.replicaPort = 55558;
        config3.introductionPort = 55556;
        Database db3 = new Database();
        LogReplay logReplay3 = new LogReplay(config3.replicaID, null, db3);
        ReplicationService replicationService3 = new ReplicationService(config3, logReplay3);
        OperationHandler handler3 = new OperationHandler(db3, logReplay3, config3, replicationService3);
        // start services
        Server s1 = replicationService1.startService();
        Server s2 = replicationService2.startService();
        Server s3 = replicationService3.startService();
        // make sure handleRelay fails
        assertEquals(true,
                replicationService1.handleRelay(LogMessage.newBuilder().setOriginatingReplicaId("2").build())
                        .getNeedsResync());
        // introduce server 2
        replicationService2.introduce();
        // validate their states
        assertEquals(1, replicationService1.getOtherReplicas().size());
        assertEquals(1, replicationService2.getOtherReplicas().size());
        assertEquals(0, logReplay1.messages.size());
        assertEquals(0, logReplay2.messages.size());
        // be sure requests cross-propagate
        LoginCreateRequest u1 = LoginCreateRequest.newBuilder().setUsername("june")
                .setPasswordHash("passwordpasswordpasswordpasswordpasswordpasswordpassword").build();
        String u1key = handler1.createAccount(u1).getSessionKey();
        assertEquals("1-1", handler1.lookupSession(u1key));
        assertEquals("1-1", handler2.lookupSession(u1key));
        LoginCreateRequest u2 = LoginCreateRequest.newBuilder().setUsername("catherine")
                .setPasswordHash("password2passwordpasswordpasswordpasswordpasswordpassword").build();
        String u2key = handler2.createAccount(u2).getSessionKey();
        assertEquals("2-1", handler2.lookupSession(u2key));
        assertEquals("2-1", handler1.lookupSession(u2key));
        // Reusing a username should fail
        assertEquals(false, handler2.createAccount(u1).getSuccess());
        // introduce server 3
        replicationService3.introduce();
        // validate states
        assertEquals(2, replicationService1.getOtherReplicas().size());
        assertEquals(2, replicationService2.getOtherReplicas().size());
        assertEquals(2, replicationService3.getOtherReplicas().size());
        assertEquals(2, handler1.getAvailableReplicas().getReplicasCount());
        assertEquals(58585, handler1.getAvailableReplicas().getReplicas(0).getPort());
        assertEquals(2, logReplay1.messages.size());
        assertEquals(2, logReplay2.messages.size());
        assertEquals(2, logReplay3.messages.size());
        assertEquals("2-1", handler3.lookupSession(u2key));
        // validate propagation again
        handler3.deleteAccount("1-1");
        assertEquals(3, logReplay1.messages.size());
        assertEquals(3, logReplay2.messages.size());
        assertEquals(3, logReplay3.messages.size());
        ListAccountsRequest listRequest1 = ListAccountsRequest.newBuilder().setMaximumNumber(10)
                .setOffsetTimestamp(fromMillis(0))
                .setFilterText("").build();
        assertEquals(1, handler1.listAccounts(listRequest1).getAccountsCount());
        assertEquals(1, handler2.listAccounts(listRequest1).getAccountsCount());
        assertEquals(1, handler3.listAccounts(listRequest1).getAccountsCount());
        // shut down 2 servers, be sure the third still works
        s1.shutdown();
        s2.shutdown();
        LoginCreateRequest u3 = LoginCreateRequest.newBuilder().setUsername("june2")
                .setPasswordHash("passwordpasswordpasswordpasswordpasswordpasswordpassword").build();
        String u3key = handler3.createAccount(u3).getSessionKey();
        assertEquals("3-1", handler3.lookupSession(u3key));
        assertEquals(2, handler3.listAccounts(listRequest1).getAccountsCount());
        assertEquals(0, replicationService3.getOtherReplicas().size());
        assertEquals(0, handler3.getAvailableReplicas().getReplicasCount());
        // cleanup
        s3.shutdown();
    }
}
