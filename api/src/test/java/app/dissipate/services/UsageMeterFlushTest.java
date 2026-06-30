package app.dissipate.services;

import app.dissipate.auth.Principal;
import app.dissipate.data.models.AccountRole;
import app.dissipate.data.models.ApiUsageCounter;
import app.dissipate.data.models.PrincipalKind;
import app.dissipate.data.models.Server;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.vertx.VertxContextSupport;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration coverage for the flush side of {@link UsageMeterService}: recorded deltas are upserted
 * into {@link ApiUsageCounter} rows for this node, repeated flushes accumulate onto the same
 * (principal, node, minute) row, and the in-memory deltas are cleared after a flush.
 */
@QuarkusTest
class UsageMeterFlushTest {

  @Inject
  UsageMeterService meter;

  @Inject
  ServerInstance serverInstance;

  private Long awaitNodeId() throws InterruptedException {
    long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
    while (System.nanoTime() < deadline) {
      Server server = serverInstance.getCurrentServer();
      if (server != null && server.id != null) {
        return server.id;
      }
      Thread.sleep(100);
    }
    throw new IllegalStateException("current server not registered in time");
  }

  private ApiUsageCounter counter(long principalId, Long nodeId, Instant minute) throws Throwable {
    return VertxContextSupport.subscribeAndAwait(() ->
        Panache.withSession(() -> ApiUsageCounter.findByKey(PrincipalKind.USER, principalId, nodeId, minute)));
  }

  @Test
  void flushUpsertsAndAccumulatesPerNodeMinute() throws Throwable {
    Long nodeId = awaitNodeId();
    long principalId = System.nanoTime(); // unique principal so the row is deterministic
    Instant minute = Instant.now().truncatedTo(ChronoUnit.MINUTES);
    Principal user = new Principal(principalId, null, AccountRole.USER, Set.of(), null, null);

    meter.record(user, 5);
    meter.record(user, 5);
    VertxContextSupport.subscribeAndAwait(() -> Panache.withTransaction(() -> meter.flushTo(nodeId)));

    ApiUsageCounter afterFirst = counter(principalId, nodeId, minute);
    assertNotNull(afterFirst, "expected a usage counter row after flush");
    assertEquals(2, afterFirst.requests);
    assertEquals(10, afterFirst.cost);
    assertEquals(nodeId, afterFirst.nodeId);
    // deltas were drained
    assertEquals(0, meter.pendingRequests(PrincipalKind.USER, principalId, minute));

    // A second batch accumulates onto the same row (upsert), not a duplicate.
    meter.record(user, 5);
    VertxContextSupport.subscribeAndAwait(() -> Panache.withTransaction(() -> meter.flushTo(nodeId)));

    ApiUsageCounter afterSecond = counter(principalId, nodeId, minute);
    assertEquals(3, afterSecond.requests);
    assertEquals(15, afterSecond.cost);
  }

  @Test
  void scheduledFlushPersistsForTheCurrentNode() throws Throwable {
    Long nodeId = awaitNodeId();
    long principalId = System.nanoTime();
    Instant minute = Instant.now().truncatedTo(ChronoUnit.MINUTES);
    meter.record(new Principal(principalId, null, AccountRole.USER, Set.of(), null, null), 4);

    // Exercise the @Scheduled flush() entry point (resolves the current node, delegates to flushTo).
    VertxContextSupport.subscribeAndAwait(() -> meter.flush());

    ApiUsageCounter row = counter(principalId, nodeId, minute);
    assertNotNull(row);
    assertEquals(1, row.requests);
    assertEquals(4, row.cost);
  }
}
