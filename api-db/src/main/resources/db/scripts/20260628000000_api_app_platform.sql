-- // api_app_platform
-- Migration SQL that makes the change goes here.
--
-- Third-party API platform: registered apps (owned by a verified account) authenticate via OAuth2
-- client-credentials. The client secret and issued access tokens are stored only as SHA-256 hashes.

CREATE TABLE public.api_apps (
    id bigint NOT NULL,
    owner_account_id bigint NOT NULL,
    client_id character varying(255) NOT NULL,
    client_secret_hash character varying(255) NOT NULL,
    name character varying(255),
    granted_scopes text,
    rate_tier character varying(255) NOT NULL DEFAULT 'default',
    status character varying(255) NOT NULL DEFAULT 'ACTIVE',
    created timestamp(6) with time zone NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    CONSTRAINT api_apps_pkey PRIMARY KEY (id),
    CONSTRAINT api_apps_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'DISABLED'::character varying])::text[]))),
    CONSTRAINT api_apps_owner_fk FOREIGN KEY (owner_account_id) REFERENCES public.accounts(id)
);

CREATE UNIQUE INDEX uidx_api_apps_client_id ON public.api_apps (client_id);

CREATE TABLE public.api_app_tokens (
    id bigint NOT NULL,
    api_app_id bigint NOT NULL,
    token_hash character varying(255) NOT NULL,
    scopes text,
    expires_at timestamp(6) with time zone NOT NULL,
    revoked timestamp(6) with time zone,
    created timestamp(6) with time zone NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    CONSTRAINT api_app_tokens_pkey PRIMARY KEY (id),
    CONSTRAINT api_app_tokens_app_fk FOREIGN KEY (api_app_id) REFERENCES public.api_apps(id)
);

CREATE UNIQUE INDEX uidx_api_app_tokens_token_hash ON public.api_app_tokens (token_hash);
CREATE INDEX ix_api_app_tokens_app ON public.api_app_tokens (api_app_id);

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE IF EXISTS public.api_app_tokens;
DROP TABLE IF EXISTS public.api_apps;
