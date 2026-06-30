package app.dissipate.data.models;

import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * A registered third-party API application (the platform half of the product). Created by a verified
 * account owner; authenticates to the gRPC channel via OAuth2 client-credentials. The client secret
 * is shown to the owner once and stored only as a SHA-256 hash.
 */
@Entity
@Table(name = "api_apps")
@NamedQuery(name = ApiApp.QUERY_BY_CLIENT_ID,
  query = """
    FROM ApiApp a
    WHERE a.clientId = :clientId
    """)
@NamedQuery(name = ApiApp.QUERY_BY_OWNER,
  query = """
    FROM ApiApp a
    WHERE a.ownerAccountId = :ownerAccountId
    ORDER BY a.created DESC
    """)
@NamedQuery(name = ApiApp.QUERY_BY_ID_AND_OWNER,
  query = """
    FROM ApiApp a
    WHERE a.id = :id
    AND a.ownerAccountId = :ownerAccountId
    """)
public class ApiApp extends DefaultPanacheEntityWithTimestamps {

  public static final String ID_GENERATOR_KEY = "ApiApp";
  public static final String QUERY_BY_CLIENT_ID = "ApiApp.findByClientId";
  public static final String QUERY_BY_OWNER = "ApiApp.findByOwner";
  public static final String QUERY_BY_ID_AND_OWNER = "ApiApp.findByIdAndOwner";

  /** Account that registered this app (a verified owner). */
  @Column(name = "owner_account_id", nullable = false)
  public Long ownerAccountId;

  /** Public, non-secret client identifier presented on the token grant. */
  @Column(nullable = false, unique = true)
  public String clientId;

  /** SHA-256 (Base64) of the client secret; the plaintext is shown to the owner only at creation. */
  @Column(nullable = false)
  public String clientSecretHash;

  public String name;

  /** Space-delimited resource:action scopes this app may request (OAuth {@code scope} format). */
  @Column(name = "granted_scopes")
  public String grantedScopes;

  /** Rate-limit tier key (resolved to concrete limits in Phase 3). */
  @Column(name = "rate_tier", nullable = false)
  public String rateTier = "default";

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public ApiAppStatus status = ApiAppStatus.ACTIVE;

  @Override
  @SuppressWarnings("unchecked")
  public Uni<ApiApp> persistAndFlush() {
    return super.persistAndFlush();
  }

  public boolean isActive() {
    return ApiAppStatus.ACTIVE.equals(status);
  }

  public static Uni<ApiApp> findByClientId(String clientId) {
    return find("#" + QUERY_BY_CLIENT_ID, Parameters.with("clientId", clientId)).firstResult();
  }

  /** All apps registered by the given owner, newest first. */
  public static Uni<java.util.List<ApiApp>> findByOwner(Long ownerAccountId) {
    return find("#" + QUERY_BY_OWNER, Parameters.with("ownerAccountId", ownerAccountId)).list();
  }

  /** A single app by id, scoped to its owner (null if it does not exist or is owned by someone else). */
  public static Uni<ApiApp> findByIdAndOwner(Long id, Long ownerAccountId) {
    return find("#" + QUERY_BY_ID_AND_OWNER,
        Parameters.with("id", id).and("ownerAccountId", ownerAccountId)).firstResult();
  }
}
