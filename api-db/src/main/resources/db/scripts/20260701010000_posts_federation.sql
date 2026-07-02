-- // posts_federation
-- Migration SQL that makes the change goes here.
--
-- Give posts a federation identity: a stable protocol-neutral global handle (never the BIGINT id)
-- and the origin region decoded from the Snowflake id (bits 15-19). Backfill any existing rows,
-- then enforce NOT NULL. gen_random_uuid() is built into Postgres 13+.

ALTER TABLE public.posts ADD COLUMN global_id uuid;
ALTER TABLE public.posts ADD COLUMN origin_region integer;

UPDATE public.posts SET global_id = gen_random_uuid() WHERE global_id IS NULL;
UPDATE public.posts SET origin_region = ((id >> 15) & 31) WHERE origin_region IS NULL;

ALTER TABLE public.posts ALTER COLUMN global_id SET NOT NULL;
ALTER TABLE public.posts ALTER COLUMN origin_region SET NOT NULL;

CREATE UNIQUE INDEX uidx_posts_global_id ON public.posts (global_id);

-- //@UNDO
-- SQL to undo the change goes here.

DROP INDEX IF EXISTS public.uidx_posts_global_id;
ALTER TABLE public.posts DROP COLUMN IF EXISTS origin_region;
ALTER TABLE public.posts DROP COLUMN IF EXISTS global_id;
