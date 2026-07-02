# Design note: UUIDv7 primary keys + region-as-data routing

Status: **Proposed** · Date: 2026-07-02 · Supersedes the Snowflake/region-encoded-id model.

## Decision

Replace Snowflake-encoded `BIGINT` primary keys with **UUIDv7** (RFC 9562) across all
entities, and move data/routing locality out of the id and into **explicit columns**:

- **Where data lives** is a property of the *account* (`Account.home_region`), not of the id.
- **Where a user is connected right now** (for real-time routing) is a property of their
  *live session* in a **presence registry**, not of the id.

The id itself becomes a pure, portable, sortable identifier that leaks nothing about
infrastructure and never has to change when an object is cached, replicated, or re-homed.

## Why (and why now)

The Snowflake model encoded `region + instance` into every id so routing could be done
broker-lessly by decoding the id. The cost is that **location is frozen into the id forever**:
an account can never change home region, and replicated/cached copies of content still claim
their origin region in their id. That directly fights the target architecture:

- **3 regions** (us-east, us-west, eu). Account-domain data (identity, logins, sessions,
  payments) has **one** source-of-truth region per account.
- **Cross-region caching**: an EU user visiting the US has their account data pulled + cached
  in-region. A US user following a UK user has that content synced/cached into US.
- **Federation** (ActivityPub / AT Protocol) is future work, and both use opaque/portable
  identifiers (URIs, DIDs, CIDs), never a host's internal integer id.

Under UUIDv7 + region columns:

- **Re-homing an account** is a column update, not an impossible id rewrite.
- **Cached/replicated content** keeps one stable identity everywhere.
- **The primary key *is* the protocol-neutral federation handle** — so the separate
  `global_id` UUID proposed in PR #23 becomes redundant.
- **Real-time routing** (the chat notify case) is served by presence, which is inherently
  dynamic and could never have lived in an immutable id anyway.

Now is the right time: pre-production, no external system references any id yet, and the
entity/data footprint is still small.

## What UUIDv7 gives us

RFC 9562 (May 2024) layout: 48-bit Unix-ms timestamp (big-endian, high bits) · version ·
`rand_a` (optionally a sub-ms monotonic counter) · `rand_b` (62 random bits).

- **Sortable / indexable**: time-ordered high bits give B-tree insert locality, avoiding the
  random-insert page splits of UUIDv4. This preserves the property that made Snowflakes
  index-friendly.
- **Collision-free without coordination**: 62 random bits make cross-node collisions
  astronomically unlikely, so **no instance-lease is needed for id generation**.
- **Infra-opaque**: reveals nothing about region, node, or minting rate (unlike a Snowflake).
- **Reactive-safe**: generated in-app as a value, not via a blocking ORM event listener
  (contrast Hibernate Envers, which is why we don't use it).

Trade-off: **16 bytes vs 8** per key. Every PK and FK doubles in width, which matters on the
large social-graph join tables (follows, reactions, views). Postgres stores `uuid` natively at
16 bytes, and UUIDv7's ordering keeps indexes healthy, so this is an accepted cost, not a
blocker.

## Id generation

Mint UUIDv7 in application code, mirroring today's `x.id = generator.generate(...)` ergonomics
so entity-creation call sites change minimally. A small `@ApplicationScoped UuidGenerator`
bean wrapping a library generator:

- `com.github.f4b6a3:uuid-creator` → `UuidCreator.getTimeOrderedEpoch()` (monotonic variant
  available), **or**
- `com.fasterxml.uuid:java-uuid-generator` (JUG 5) → `Generators.timeBasedEpochGenerator()`.

A bean (vs a static util or a Hibernate `@UuidGenerator`) keeps generation injectable/mockable
so tests can produce deterministic ids, and keeps id assignment explicit and in-app the way the
Snowflake generator already is.

`DefaultPanacheEntityWithTimestamps.id` becomes:

```java
@Id
@Column(columnDefinition = "uuid")
public UUID id;   // UUIDv7, assigned at creation via UuidGenerator
```

The `@JsonSerialize/@JsonDeserialize` base-36 codecs are dropped; `UUID` serializes to its
standard string form.

## Region model

- `enum Region { US_EAST, US_WEST, EU }` (stored as a short string/enum column).
- `Account.home_region` — the source-of-truth region for that account's data. Set at signup.
- `dissipate.region` config changes from an `int` (0–31) to a `Region` — it names **this
  node's** region.
- Content routing resolves through the author's account: content is homed in
  `author.account.home_region`. Where a hot query can't afford the join, denormalize the
  region onto the content row (e.g. `Post.origin_region`), sourced from the account at write
  time — *not* decoded from the id.

## Real-time routing (presence)

The chat-notify path replaces id-bit routing with presence:

1. A message needs to notify user X.
2. Look up X's **active session(s)** → `Session.connected_server_id`.
3. That `Server` row carries `region + hostname + port` → the node-to-node address.
4. Send a **lightweight notify** ("load event E and push to your connected client") — the DB
   remains source of truth, so the payload stays small.

`Server` stays as the node registry + heartbeat/liveness (`seen`, abandoned-cleanup). It
**loses** only its Snowflake `instanceNumber` lease role. `ServerInstance.onStart()` no longer
needs to lease a 0–1023 instance number; it registers the node (region, host, port) and
heartbeats. `Server` should gain a `region` column.

## Blast radius (from repo inventory)

| Area | Change |
|---|---|
| `DefaultPanacheEntityWithTimestamps.id` | `Long`→`UUID`; drop base-36 JSON codecs; `toString` uses `UUID` |
| ~86 entity models | inherit the new id type; no per-entity id declaration changes |
| `SnowflakeIdGenerator` + `Snowflake` bit-layout | **removed**; replaced by `UuidGenerator` bean |
| `ID_GENERATOR_KEY` constants + `generate(KEY)` call sites | `x.id = uuidGenerator.generate()` (no per-name key) |
| `ServerInstance` | drop instance-number lease; keep register + heartbeat; add `region` |
| `Server` | `id` → UUIDv7 (or keep IDENTITY — see open decision); add `region` |
| `SnowflakeBase36Serializer/Deserializer`, `SnowflakeIdParamConverterProvider`, `@SnowflakeId` | removed; ids are UUID strings on the wire |
| `PageCursor` | already string-based; tiebreaker `id < :cursorId` stays valid under UUIDv7 order |
| Protos | public messages already use `string id` — **contract shape unchanged**, only string content differs. `dissipate_internal.proto` `int64 server_id`→`string server_id` |
| `Long.parseLong(..., 36)` id parsers (`ChangeIdentityMethod`, `DeveloperServiceImpl`, `ChatNotificationService`) | parse `UUID.fromString(...)` |
| `regionOf` / `FederatedEntity.federate()` / `decode` | removed; region comes from account, not id |
| Migrations | ~200 PK/FK columns `bigint`→`uuid`, 100+ FK constraints (see strategy) |
| PR #23 (audit/federation) | reworked — see below |

`version` (optimistic-lock) columns are unrelated to the id and stay `bigint`.

## Migration strategy (open decision)

Two options; there is **no production data** to preserve (pre-launch), which strongly favors A:

- **A — Squash to a fresh UUIDv7 baseline.** Rewrite the authoritative baseline schema with
  `uuid` PK/FKs and drop the now-obsolete incremental migrations. Far fewer moving parts than
  a 200-column in-place `ALTER`, and no risk of half-migrated FK graphs. Costs: throws away
  local/dev data; rewrites the baseline that's been treated as append-only.
- **B — In-place `ALTER` migration.** One big migration converting every PK/FK `bigint`→`uuid`
  with backfilled UUIDv7 values and FK re-mapping. Preserves migration history and any data,
  but is large, order-sensitive, and error-prone across 100+ constraints.

**Recommendation: A**, given no prod data and MyBatis migrations being authoritative anyway.

## Impact on PR #23 (entity audit + federation foundation)

The **audit log concept survives**; the **federation-id columns do not**:

- Drop `FederatedEntity.global_id` and `federate()` — the UUIDv7 PK is the portable handle.
- `EntityRevision.entityId` becomes `UUID`; `origin_region` is sourced from the author's
  account `home_region`, not `regionOf(id)`.
- `SnowflakeIdGenerator.regionOf` is removed.

Plan: **hold/close #23** and re-land the audit log on top of the UUIDv7 base, since its
schema and the `FederatedEntity` superclass both change materially.

## Phased plan

1. **Foundation**: add `UuidGenerator` bean + lib; `Region` enum + config; flip
   `DefaultPanacheEntityWithTimestamps.id` to `UUID`; remove Snowflake machinery + base-36
   codecs. Fix call sites (`generate()`, `Long.parseLong(...,36)`).
2. **Schema**: strategy A — fresh UUIDv7 baseline; add `Account.home_region`, `Server.region`.
3. **Presence/routing seam**: `ServerInstance` register+heartbeat (no lease); confirm
   `Session → connected_server` presence lookup is queryable for node-to-node notify.
4. **Re-land audit log** (reworked #23) on UUIDv7.
5. **Verify**: full suite green on JDK 25; sanity-check index/plan health on a couple of
   large-join queries.

## Decisions (signed off 2026-07-02)

1. **Migration strategy: A — squash to a fresh UUIDv7 baseline.** No prod data; simplest and
   least error-prone. The PR #23 migrations (`entity_revisions`, `posts_federation`) are
   dropped since that PR is being reworked.
2. **`Server.id` → UUIDv7** too, for full consistency. `dissipate_internal.proto`'s
   `int64 server_id` becomes `string server_id` (node-to-node only).
3. **Id-gen library: `com.github.f4b6a3:uuid-creator`** (`UuidCreator.getTimeOrderedEpoch()`),
   monotonic variant for same-ms ordering.
4. **`Region`: enum-backed short string** (`us-east`, `us-west`, `eu`).
