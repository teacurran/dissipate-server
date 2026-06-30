-- // audit_event
-- Migration SQL that makes the change goes here.
--
-- Operational audit log for the REST API (SOC2). Envers is unavailable on
-- Hibernate Reactive, so audit events are written synchronously, in the same
-- transaction as the action they describe, then drained to an external,
-- append-only sink by a scheduled AuditPublisher. `published_at` marks rows the
-- publisher has emitted; `archived_at` marks rows a later retention job may
-- cold-store/prune once external durability is confirmed.
--
-- event_type / outcome are stored as plain varchar (no CHECK constraint): the
-- AuditEventType enum grows every feature phase, and a value-list CHECK would
-- force a migration on each addition. The Java enum is the source of truth.

CREATE TABLE public.audit_events (
    id bigint NOT NULL,
    version bigint NOT NULL,
    created timestamp(6) with time zone NOT NULL,
    updated timestamp(6) with time zone,
    occurred_at timestamp(6) with time zone NOT NULL,
    event_type character varying(255) NOT NULL,
    outcome character varying(255) NOT NULL,
    actor_account_id bigint,
    actor_identity_id bigint,
    session_id uuid,
    target_type character varying(255),
    target_id bigint,
    client_ip character varying(255),
    user_agent text,
    reason character varying(255),
    metadata jsonb,
    published_at timestamp(6) with time zone,
    archived_at timestamp(6) with time zone,
    CONSTRAINT audit_events_pkey PRIMARY KEY (id)
);

-- Drain query: find the oldest not-yet-published rows.
CREATE INDEX ix_audit_events_unpublished
    ON public.audit_events (occurred_at)
    WHERE published_at IS NULL;

-- Common investigative lookups.
CREATE INDEX ix_audit_events_occurred_at ON public.audit_events (occurred_at);
CREATE INDEX ix_audit_events_event_type ON public.audit_events (event_type);
CREATE INDEX ix_audit_events_actor_account ON public.audit_events (actor_account_id);
CREATE INDEX ix_audit_events_actor_identity ON public.audit_events (actor_identity_id);

-- //@UNDO
-- SQL to undo the change goes here.

DROP INDEX IF EXISTS ix_audit_events_actor_identity;
DROP INDEX IF EXISTS ix_audit_events_actor_account;
DROP INDEX IF EXISTS ix_audit_events_event_type;
DROP INDEX IF EXISTS ix_audit_events_occurred_at;
DROP INDEX IF EXISTS ix_audit_events_unpublished;
DROP TABLE IF EXISTS public.audit_events;
