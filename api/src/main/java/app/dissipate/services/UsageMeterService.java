package app.dissipate.services;

import app.dissipate.auth.Principal;
import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.ApiUsageCounter;
import app.dissipate.data.models.PrincipalKind;
import app.dissipate.data.models.Server;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Per-node API usage metering. Each authenticated call increments an in-memory counter for the
 * caller's current minute (cheap, off the request's DB path); a scheduled flush drains the
 * accumulated deltas and upserts them into {@link ApiUsageCounter} rows keyed by this node, so the
 * database holds one retained, chartable counter per (principal, node, minute).
 *
 * <p>This slice records usage only; enforcement (tier ceilings) reads these counters in a follow-up.
 */
@ApplicationScoped
public class UsageMeterService {

  @Inject
  SnowflakeIdGenerator idGenerator;

  @Inject
  ServerInstance serverInstance;

  private final ConcurrentMap<CounterKey, Accumulator> counters = new ConcurrentHashMap<>();

  /** Record one authenticated call of the given method cost. No-op for anonymous callers. */
  public void record(Principal principal, long cost) {
    if (principal == null || !principal.isAuthenticated()) {
      return;
    }
    Long principalId = principal.meteredId();
    if (principalId == null) {
      return;
    }
    Instant minute = Instant.now().truncatedTo(ChronoUnit.MINUTES);
    Accumulator accumulator = counters.computeIfAbsent(
        new CounterKey(principal.kind(), principalId, minute), key -> new Accumulator());
    accumulator.requests.incrementAndGet();
    accumulator.cost.addAndGet(Math.max(cost, 1));
  }

  @Scheduled(every = "{dissipate.usage.flush-interval}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
  @WithTransaction
  @WithSpan("UsageMeterService.flush")
  Uni<Void> flush() {
    Server node = serverInstance.getCurrentServer();
    if (node == null || node.id == null) {
      return Uni.createFrom().voidItem(); // node not registered yet; keep accumulating
    }
    return flushTo(node.id);
  }

  /** Drain and upsert this node's pending deltas. Must run inside a reactive transaction/session. */
  Uni<Void> flushTo(Long nodeId) {
    List<Drained> drained = drain();
    if (drained.isEmpty()) {
      return Uni.createFrom().voidItem();
    }
    return Multi.createFrom().iterable(drained)
        .onItem().transformToUniAndConcatenate(d -> upsert(nodeId, d))
        .collect().last()
        .replaceWithVoid();
  }

  // -- test visibility into the pending (un-flushed) in-memory deltas --

  int pendingRequests(PrincipalKind kind, long principalId, Instant minute) {
    Accumulator accumulator = counters.get(new CounterKey(kind, principalId, minute));
    return accumulator == null ? 0 : accumulator.requests.get();
  }

  long pendingCost(PrincipalKind kind, long principalId, Instant minute) {
    Accumulator accumulator = counters.get(new CounterKey(kind, principalId, minute));
    return accumulator == null ? 0 : accumulator.cost.get();
  }

  /** Atomically take and zero the accumulated deltas (holders stay so concurrent records continue). */
  private List<Drained> drain() {
    List<Drained> out = new ArrayList<>();
    counters.forEach((key, accumulator) -> {
      int requests = accumulator.requests.getAndSet(0);
      long cost = accumulator.cost.getAndSet(0);
      if (requests > 0) {
        out.add(new Drained(key, requests, cost));
      }
    });
    return out;
  }

  private Uni<Void> upsert(Long nodeId, Drained d) {
    return ApiUsageCounter.findByKey(d.key.principalType(), d.key.principalId(), nodeId, d.key.minute())
        .onItem().transformToUni(existing -> {
          if (existing != null) {
            existing.requests += d.requests;
            existing.cost += d.cost;
            return existing.persistAndFlush().replaceWithVoid();
          }
          ApiUsageCounter counter = new ApiUsageCounter();
          counter.id = idGenerator.generate(ApiUsageCounter.ID_GENERATOR_KEY);
          counter.principalType = d.key.principalType();
          counter.principalId = d.key.principalId();
          counter.nodeId = nodeId;
          counter.minute = d.key.minute();
          counter.requests = d.requests;
          counter.cost = d.cost;
          return counter.persistAndFlush().replaceWithVoid();
        });
  }

  private record CounterKey(PrincipalKind principalType, Long principalId, Instant minute) {
  }

  private record Drained(CounterKey key, int requests, long cost) {
  }

  private static final class Accumulator {
    private final AtomicInteger requests = new AtomicInteger();
    private final AtomicLong cost = new AtomicLong();
  }
}
