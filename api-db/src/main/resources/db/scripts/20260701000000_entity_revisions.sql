-- // entity_revisions
-- Migration SQL that makes the change goes here.
--
-- Append-only audit + versioning log for Auditable entities. For federated (publishable) entities a
-- row also carries the stable global_id, origin_region, and a content_hash of the snapshot, making
-- it the unit of cross-region reconciliation. Rows are retained. entity_id / actor_id are polymorphic
-- (any entity / any principal) and global_id is a portable handle, so none carry a foreign key.

CREATE TABLE public.entity_revisions (
    id bigint NOT NULL,
    entity_type character varying(255) NOT NULL,
    entity_id bigint NOT NULL,
    global_id uuid,
    revision integer NOT NULL,
    op character varying(16) NOT NULL,
    actor_kind character varying(16),
    actor_id bigint,
    changed_at timestamp(6) with time zone NOT NULL,
    content_hash character varying(255),
    origin_region integer,
    snapshot jsonb,
    created timestamp(6) with time zone NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    CONSTRAINT entity_revisions_pkey PRIMARY KEY (id),
    CONSTRAINT entity_revisions_op_check CHECK (((op)::text = ANY ((ARRAY['INSERT'::character varying, 'UPDATE'::character varying, 'DELETE'::character varying])::text[]))),
    CONSTRAINT entity_revisions_actor_kind_check CHECK (actor_kind IS NULL OR (actor_kind)::text = ANY ((ARRAY['USER'::character varying, 'APP'::character varying])::text[]))
);

-- One row per entity per revision.
CREATE UNIQUE INDEX uidx_entity_revisions_grain ON public.entity_revisions (entity_type, entity_id, revision);
-- History of one entity over time.
CREATE INDEX ix_entity_revisions_entity ON public.entity_revisions (entity_type, entity_id);
-- Cross-region lookup by the portable handle.
CREATE INDEX ix_entity_revisions_global_id ON public.entity_revisions (global_id);
-- Reporting over time.
CREATE INDEX ix_entity_revisions_changed_at ON public.entity_revisions (changed_at);

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE IF EXISTS public.entity_revisions;
