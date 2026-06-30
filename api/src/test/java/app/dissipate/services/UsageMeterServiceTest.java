package app.dissipate.services;

import app.dissipate.auth.Principal;
import app.dissipate.data.models.AccountRole;
import app.dissipate.data.models.PrincipalKind;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit coverage for the in-memory accumulation side of {@link UsageMeterService#record}: per-kind/id
 * bucketing, cost weighting (0 counts as 1), and that anonymous callers are not metered. The
 * flush-to-database side is covered by {@link UsageMeterFlushTest}.
 */
class UsageMeterServiceTest {

  private static Principal user(long accountId) {
    return new Principal(accountId, null, AccountRole.USER, Set.of(), null, null);
  }

  private static Principal app(long appId) {
    return new Principal(null, null, null, Set.of(), appId, "default");
  }

  @Test
  void accumulatesPerPrincipalWithCostWeighting() {
    UsageMeterService meter = new UsageMeterService();
    Instant minute = Instant.now().truncatedTo(ChronoUnit.MINUTES);

    meter.record(user(1001L), 3);
    meter.record(user(1001L), 3);
    meter.record(app(7L), 0); // cost 0 -> weighted as 1

    assertEquals(2, meter.pendingRequests(PrincipalKind.USER, 1001L, minute));
    assertEquals(6, meter.pendingCost(PrincipalKind.USER, 1001L, minute));
    assertEquals(1, meter.pendingRequests(PrincipalKind.APP, 7L, minute));
    assertEquals(1, meter.pendingCost(PrincipalKind.APP, 7L, minute));
  }

  @Test
  void anonymousAndNullAreNotMetered() {
    UsageMeterService meter = new UsageMeterService();
    Instant minute = Instant.now().truncatedTo(ChronoUnit.MINUTES);

    meter.record(Principal.anonymous(), 5);
    meter.record(null, 5);

    assertEquals(0, meter.pendingRequests(PrincipalKind.USER, 0L, minute));
  }
}
