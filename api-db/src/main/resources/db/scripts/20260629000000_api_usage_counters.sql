-- // api_usage_counters
-- Migration SQL that makes the change goes here.
--
-- Per-minute API usage counters for rate limiting and reporting. Each node flushes its own counts,
-- so the grain is (principal_type, principal_id, node_id, minute). Rows are retained for charting
-- usage per client over time. principal_id is polymorphic (account id for USER, app id for APP) and
-- node_id denormalizes the recording server, so neither carries a foreign key.

CREATE TABLE public.api_usage_counters (
    id bigint NOT NULL,
    principal_type character varying(255) NOT NULL,
    principal_id bigint NOT NULL,
    node_id bigint NOT NULL,
    minute timestamp(6) with time zone NOT NULL,
    requests integer NOT NULL,
    cost bigint NOT NULL,
    created timestamp(6) with time zone NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    CONSTRAINT api_usage_counters_pkey PRIMARY KEY (id),
    CONSTRAINT api_usage_counters_type_check CHECK (((principal_type)::text = ANY ((ARRAY['USER'::character varying, 'APP'::character varying])::text[])))
);

CREATE UNIQUE INDEX uidx_api_usage_counters_grain
    ON public.api_usage_counters (principal_type, principal_id, node_id, minute);

-- Reporting: usage for a given client over time (charts of requests/cost per minute).
CREATE INDEX ix_api_usage_counters_principal_minute
    ON public.api_usage_counters (principal_type, principal_id, minute);

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE IF EXISTS public.api_usage_counters;
