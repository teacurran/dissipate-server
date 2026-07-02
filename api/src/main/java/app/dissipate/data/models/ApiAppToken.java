package app.dissipate.data.models;

import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * An issued OAuth2 access token for an {@link ApiApp}. Tokens are opaque and high-entropy; only their
 * SHA-256 hash is stored, so a leaked database row cannot be replayed as a bearer token. The authn
 * interceptor resolves an inbound app token by its hash here.
 */
@Entity
@Table(name = "api_app_tokens")
@NamedQuery(name = ApiAppToken.QUERY_ACTIVE_BY_HASH,
  query = """
    SELECT t
    FROM ApiAppToken t
    JOIN FETCH t.apiApp a
    WHERE t.tokenHash = :tokenHash
    AND t.revoked IS NULL
    """)
public class ApiAppToken extends DefaultPanacheEntityWithTimestamps {

  public static final String QUERY_ACTIVE_BY_HASH = "ApiAppToken.findActiveByHash";

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "api_app_id", nullable = false)
  public ApiApp apiApp;

  /** SHA-256 (Base64) of the opaque access token. */
  @Column(name = "token_hash", nullable = false, unique = true)
  public String tokenHash;

  /** Space-delimited scopes granted to this token (a subset of the app's granted scopes). */
  public String scopes;

  @Column(name = "expires_at", nullable = false)
  public Instant expiresAt;

  /** When set, the token has been revoked and must not be accepted. */
  public Instant revoked;

  @Override
  @SuppressWarnings("unchecked")
  public Uni<ApiAppToken> persistAndFlush() {
    return super.persistAndFlush();
  }

  public boolean isExpired(Instant now) {
    return expiresAt != null && now.isAfter(expiresAt);
  }

  /** Resolve a live (non-revoked) token by its SHA-256 hash, eagerly fetching the owning app. */
  public static Uni<ApiAppToken> findActiveByHash(String tokenHash) {
    return find("#" + QUERY_ACTIVE_BY_HASH, Parameters.with("tokenHash", tokenHash)).firstResult();
  }
}
