--
--    Baseline schema (authoritative consolidated initial schema) -- UUIDv7 ids.
--

-- // baseline schema
-- Consolidated full schema squashed to a single UUIDv7 baseline. All primary
-- keys and foreign-key columns are UUID (populated as UUIDv7 by the application),
-- EXCEPT the internal node registry public.servers, whose id remains a bigint
-- IDENTITY, and the four columns that reference servers.id (node_id,
-- connected_server_id, locked_by_id, last_run_by_id), which stay bigint.
-- The optimistic-lock / count / money columns (version, counter, cost) also
-- remain bigint. This baseline subsumes the former bigint baseline plus all
-- post-baseline increments (password hash, validated-contact uniqueness, PII
-- encryption, audit events, session/OTP hardening, account role, login lockout,
-- API app platform, API usage counters). Hibernate schema auto-update is
-- disabled and MyBatis migrations are authoritative.

CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE public.account_emails (
    is_primary boolean NOT NULL,
    account_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    validated timestamp(6) with time zone,
    version bigint NOT NULL,
    email character varying(255)
);

CREATE TABLE public.account_memberships (
    account_id uuid,
    begins timestamp(6) with time zone,
    created timestamp(6) with time zone NOT NULL,
    ends timestamp(6) with time zone,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    status character varying(255),
    CONSTRAINT account_memberships_status_check CHECK (((status)::text = ANY (ARRAY[('PAUSED'::character varying)::text, ('CANCELLED'::character varying)::text, ('ACTIVE'::character varying)::text, ('EXPIRED'::character varying)::text])))
);

CREATE TABLE public.account_phones (
    is_primary boolean NOT NULL,
    account_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    validated timestamp(6) with time zone,
    version bigint NOT NULL,
    phone character varying(255)
);

CREATE TABLE public.account_sales_taxes (
    account_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.account_validated_names (
    account_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    name character varying(255)
);

CREATE TABLE public.account_web_authns (
    account_id uuid,
    counter bigint NOT NULL,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    aaguid character varying(255),
    alg character varying(255),
    credential_id character varying(255),
    fmt character varying(255),
    public_key character varying(255)
);

CREATE TABLE public.accounts (
    region integer NOT NULL,
    country_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    state_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    locale character varying(255),
    password_hash_str character varying(255),
    status character varying(255),
    timezone character varying(255),
    address1_enc bytea,
    address2_enc bytea,
    city_enc bytea,
    first_name_enc bytea,
    last_name_enc bytea,
    password_hash bytea,
    password_salt bytea,
    postal_code_enc bytea,
    role character varying(255) DEFAULT 'USER'::character varying NOT NULL,
    failed_login_attempts integer DEFAULT 0 NOT NULL,
    locked_until timestamp(6) with time zone,
    CONSTRAINT accounts_role_check CHECK (((role)::text = ANY (ARRAY[('USER'::character varying)::text, ('VERIFIED'::character varying)::text, ('ADMIN'::character varying)::text]))),
    CONSTRAINT accounts_status_check CHECK (((status)::text = ANY (ARRAY[('DISABLED'::character varying)::text, ('ANONYMOUS'::character varying)::text, ('ACTIVE'::character varying)::text, ('SUSPENDED'::character varying)::text, ('BANNED'::character varying)::text])))
);

CREATE TABLE public.api_app_tokens (
    id uuid NOT NULL,
    api_app_id uuid NOT NULL,
    token_hash character varying(255) NOT NULL,
    scopes text,
    expires_at timestamp(6) with time zone NOT NULL,
    revoked timestamp(6) with time zone,
    created timestamp(6) with time zone NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.api_apps (
    id uuid NOT NULL,
    owner_account_id uuid NOT NULL,
    client_id character varying(255) NOT NULL,
    client_secret_hash character varying(255) NOT NULL,
    name character varying(255),
    granted_scopes text,
    rate_tier character varying(255) DEFAULT 'default'::character varying NOT NULL,
    status character varying(255) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created timestamp(6) with time zone NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    CONSTRAINT api_apps_status_check CHECK (((status)::text = ANY (ARRAY[('ACTIVE'::character varying)::text, ('DISABLED'::character varying)::text])))
);

CREATE TABLE public.api_usage_counters (
    id uuid NOT NULL,
    principal_type character varying(255) NOT NULL,
    principal_id uuid NOT NULL,
    node_id bigint NOT NULL,
    minute timestamp(6) with time zone NOT NULL,
    requests integer NOT NULL,
    cost bigint NOT NULL,
    created timestamp(6) with time zone NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    CONSTRAINT api_usage_counters_type_check CHECK (((principal_type)::text = ANY (ARRAY[('USER'::character varying)::text, ('APP'::character varying)::text])))
);

CREATE TABLE public.asset_attributes (
    numeric_value double precision,
    rank double precision,
    asset_collection_attribute_id uuid,
    asset_id uuid NOT NULL,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    asset_attribute_type character varying(255),
    text_value character varying(255),
    CONSTRAINT asset_attributes_asset_attribute_type_check CHECK (((asset_attribute_type)::text = ANY (ARRAY[('DATE'::character varying)::text, ('NUMBER'::character varying)::text, ('STRING'::character varying)::text, ('BOOLEAN'::character varying)::text])))
);

CREATE TABLE public.asset_collection_attributes (
    max_value double precision,
    min_value double precision,
    rank double precision,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    asset_attribute_type character varying(255),
    description character varying(255),
    name character varying(255),
    unit character varying(255),
    CONSTRAINT asset_collection_attributes_asset_attribute_type_check CHECK (((asset_attribute_type)::text = ANY (ARRAY[('DATE'::character varying)::text, ('NUMBER'::character varying)::text, ('STRING'::character varying)::text, ('BOOLEAN'::character varying)::text])))
);

CREATE TABLE public.asset_collections (
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.asset_histories (
    actor2_id uuid,
    actor_id uuid,
    asset_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    action character varying(255)
);

CREATE TABLE public.asset_scans (
    asset_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    results text
);

CREATE TABLE public.assets (
    created timestamp(6) with time zone NOT NULL,
    creator_id uuid,
    id uuid NOT NULL,
    owner_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    tft uuid,
    hash character varying(255),
    type character varying(255),
    CONSTRAINT assets_type_check CHECK (((type)::text = ANY (ARRAY[('IMAGE'::character varying)::text, ('VIDEO'::character varying)::text, ('TEXT'::character varying)::text])))
);

CREATE TABLE public.audit_events (
    id uuid NOT NULL,
    version bigint NOT NULL,
    created timestamp(6) with time zone NOT NULL,
    updated timestamp(6) with time zone,
    occurred_at timestamp(6) with time zone NOT NULL,
    event_type character varying(255) NOT NULL,
    outcome character varying(255) NOT NULL,
    actor_account_id uuid,
    actor_identity_id uuid,
    session_id uuid,
    target_type character varying(255),
    target_id uuid,
    client_ip character varying(255),
    user_agent text,
    reason character varying(255),
    metadata jsonb,
    published_at timestamp(6) with time zone,
    archived_at timestamp(6) with time zone
);

CREATE TABLE public.chat_event_assets (
    asset_id uuid,
    created timestamp(6) with time zone NOT NULL,
    event_id uuid,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.chat_events (
    type smallint,
    chat_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    identity_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    message text,
    CONSTRAINT chat_events_type_check CHECK (((type >= 0) AND (type <= 9)))
);

CREATE TABLE public.chat_participants (
    type smallint,
    chat_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    identity_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    CONSTRAINT chat_participants_type_check CHECK (((type >= 0) AND (type <= 2)))
);

CREATE TABLE public.chats (
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.cities (
    country_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    state_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    name character varying(255),
    location bytea
);

CREATE TABLE public.content_review_comments (
    chat_event_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    identity_id uuid,
    post_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    comment text
);

CREATE TABLE public.content_reviews (
    bot boolean NOT NULL,
    chat_event_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    identity_id uuid,
    post_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    result character varying(255),
    type character varying(255),
    CONSTRAINT content_reviews_result_check CHECK (((result)::text = ANY (ARRAY[('TERMS_VIOLATION_ADULT'::character varying)::text, ('TERMS_VIOLATION'::character varying)::text, ('TERMS_VIOLATION_VIOLENCE'::character varying)::text, ('TERMS_VIOLATION_HATE'::character varying)::text, ('TERMS_VIOLATION_SPAM'::character varying)::text, ('TERMS_VIOLATION_OTHER'::character varying)::text, ('DOES_NOT_VIOLATE_TERMS'::character varying)::text]))),
    CONSTRAINT content_reviews_type_check CHECK (((type)::text = ANY (ARRAY[('IDENTITY'::character varying)::text, ('CHAT_EVENT'::character varying)::text, ('POST'::character varying)::text])))
);

CREATE TABLE public.countries (
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    capital character varying(255),
    currency character varying(255),
    currency_name character varying(255),
    currency_symbol character varying(255),
    emoji character varying(255),
    emojiu character varying(255),
    iso2 character varying(255),
    iso3 character varying(255),
    name character varying(255),
    nationality character varying(255),
    native_name character varying(255),
    numeric_code character varying(255),
    phone_code character varying(255),
    region character varying(255),
    region_id character varying(255),
    subregion character varying(255),
    subregion_id character varying(255),
    tld character varying(255),
    location public.geometry
);

CREATE TABLE public.country_timezones (
    country_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    timezone character varying(255)
);

CREATE TABLE public.country_translations (
    country_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    locale character varying(255),
    name character varying(255)
);

CREATE TABLE public.delayed_jobs (
    attempts integer,
    complete boolean NOT NULL,
    completed_with_failure boolean NOT NULL,
    locked boolean NOT NULL,
    priority integer,
    actor_id uuid,
    completed_at timestamp(6) with time zone,
    created timestamp(6) with time zone NOT NULL,
    failed_at timestamp(6) with time zone,
    id uuid NOT NULL,
    last_run_by_id bigint,
    locked_at timestamp(6) with time zone,
    locked_by_id bigint,
    run_at timestamp(6) with time zone,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    failure_reason character varying(255),
    last_error text,
    queue character varying(255),
    CONSTRAINT delayed_jobs_queue_check CHECK (((queue)::text = ANY (ARRAY[('URL_CRAWL'::character varying)::text, ('ETL_LOCATION'::character varying)::text, ('PHONE_AUTH'::character varying)::text, ('EMAIL_AUTH'::character varying)::text, ('EMAIL_MARKETING'::character varying)::text, ('LICENSE_PROVISION'::character varying)::text])))
);

CREATE TABLE public.flags (
    chat_event_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    identity_id uuid,
    post_id uuid,
    reported_by_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    comment text,
    comment_locale character varying(255),
    type character varying(255),
    CONSTRAINT flags_type_check CHECK (((type)::text = ANY (ARRAY[('IDENTITY'::character varying)::text, ('CHAT_EVENT'::character varying)::text, ('POST'::character varying)::text])))
);

CREATE TABLE public.hierarchies (
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    organization_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    description character varying(255),
    name character varying(255)
);

CREATE TABLE public.hierarchy_levels (
    created timestamp(6) with time zone NOT NULL,
    hierarchy_id uuid,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    name character varying(255)
);

CREATE TABLE public.identities (
    account_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    locale character varying(255),
    name character varying(255),
    public_key character varying(255),
    timezone character varying(255),
    username character varying(255),
    username_normalized character varying(255),
    private_key_encrypted bytea
);

CREATE TABLE public.identity_actions (
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    identity_id uuid,
    post_id uuid,
    target_identity_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    type character varying(255),
    CONSTRAINT identity_actions_type_check CHECK (((type)::text = ANY (ARRAY[('UNFOLLOW'::character varying)::text, ('FOLLOW'::character varying)::text, ('UNBLOCK'::character varying)::text, ('POST_CREATE'::character varying)::text, ('POST_DELETE'::character varying)::text, ('BLOCK'::character varying)::text, ('UNMUTE'::character varying)::text, ('MUTE'::character varying)::text, ('POST_EDIT'::character varying)::text])))
);

CREATE TABLE public.identity_avatars (
    asset_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    identity_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    name character varying(255)
);

CREATE TABLE public.identity_blocks (
    is_mutual boolean NOT NULL,
    created timestamp(6) with time zone NOT NULL,
    expires timestamp(6) with time zone,
    id uuid NOT NULL,
    identity2_id uuid,
    identity_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    type character varying(255),
    CONSTRAINT identity_blocks_type_check CHECK (((type)::text = ANY (ARRAY[('BLOCKED'::character varying)::text, ('IGNORED'::character varying)::text])))
);

CREATE TABLE public.identity_follows (
    is_mutual boolean NOT NULL,
    approved timestamp(6) with time zone,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    identity2_id uuid,
    identity_id uuid NOT NULL,
    requested timestamp(6) with time zone,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.identity_organizations (
    accepted timestamp(6) with time zone,
    approved timestamp(6) with time zone,
    approved_by_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    identity_id uuid,
    organization_id uuid,
    requested timestamp(6) with time zone,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.identity_permissions (
    accepted timestamp(6) with time zone,
    approved timestamp(6) with time zone,
    approved_by_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    identity_id uuid,
    organization_id uuid,
    permission_id uuid,
    requested timestamp(6) with time zone,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.membership_receipts (
    account_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.order_item (
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.orders (
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    identity_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.organization_hierarchies (
    created timestamp(6) with time zone NOT NULL,
    hierarchy_id uuid,
    id uuid NOT NULL,
    organization_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.organizations (
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    level_id uuid,
    parent_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    name character varying(255)
);

CREATE TABLE public.permissions (
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    key character varying(255),
    name character varying(255)
);

CREATE TABLE public.post_assets (
    asset_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    post_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.post_reactions (
    emoji character varying(4) NOT NULL,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    identity_id uuid NOT NULL,
    post_id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.post_views (
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    identity_id uuid NOT NULL,
    post_id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    session_id uuid NOT NULL
);

CREATE TABLE public.posts (
    default_reaction_emoji character varying(4),
    deleted boolean DEFAULT false NOT NULL,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    identity_avatar_id uuid,
    identity_id uuid NOT NULL,
    organization_id uuid,
    reply_to_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    body text,
    caption text
);

CREATE TABLE public.product_price_variations (
    price numeric(38,2),
    country_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    product_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.products (
    price numeric(38,2),
    type smallint,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    identity_id uuid,
    organization_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    description character varying(255),
    name character varying(255),
    summary character varying(255),
    url character varying(255),
    CONSTRAINT products_type_check CHECK (((type >= 0) AND (type <= 6)))
);

CREATE TABLE public.servers (
    instance_number integer,
    is_shutdown boolean DEFAULT false NOT NULL,
    port integer,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    launched timestamp(6) with time zone,
    seen timestamp(6) with time zone,
    updated timestamp(6) with time zone,
    hostname character varying(255),
    status character varying(255),
    token character varying(255),
    CONSTRAINT servers_status_check CHECK (((status)::text = ANY (ARRAY[('ACTIVE'::character varying)::text, ('ABANDONED'::character varying)::text, ('UNKNOWN'::character varying)::text, ('SHUTDOWN'::character varying)::text])))
);

CREATE TABLE public.session_validations (
    created timestamp(6) with time zone NOT NULL,
    email_id uuid,
    id uuid NOT NULL,
    phone_id uuid,
    updated timestamp(6) with time zone,
    validated timestamp(6) with time zone,
    version bigint NOT NULL,
    session_id uuid,
    token character varying(255),
    expires timestamp(6) with time zone,
    attempts integer DEFAULT 0 NOT NULL
);

CREATE TABLE public.sessions (
    logged_in boolean NOT NULL,
    account_id uuid,
    connected_server_id bigint,
    created timestamp(6) with time zone NOT NULL,
    ended timestamp(6) with time zone,
    identity_id uuid,
    updated timestamp(6) with time zone,
    id uuid NOT NULL,
    client_ip character varying(255)
);

CREATE TABLE public.states (
    country_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    name character varying(255),
    state_code character varying(255),
    type character varying(255),
    location bytea
);

CREATE TABLE public.urls (
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    last_crawled_at timestamp(6) with time zone,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    domain character varying(255),
    value character varying(255)
);

CREATE TABLE public.verification_request_names (
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    verification_request_id uuid,
    version bigint NOT NULL,
    name character varying(255)
);

CREATE TABLE public.verification_request_notes (
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    identity_id uuid,
    updated timestamp(6) with time zone,
    verification_request_id uuid,
    version bigint NOT NULL,
    note character varying(255)
);

CREATE TABLE public.verification_requests (
    account_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    order_id uuid,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.verifications (
    account_id uuid,
    approved_at timestamp(6) with time zone,
    approved_by_id uuid,
    created timestamp(6) with time zone NOT NULL,
    expires_at timestamp(6) with time zone,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);

CREATE TABLE public.webauthn_certificates (
    account_web_authn_id uuid,
    created timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    x5c character varying(255)
);

ALTER TABLE public.servers ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.servers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

ALTER TABLE ONLY public.account_emails
    ADD CONSTRAINT account_emails_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.account_memberships
    ADD CONSTRAINT account_memberships_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.account_phones
    ADD CONSTRAINT account_phones_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.account_sales_taxes
    ADD CONSTRAINT account_sales_taxes_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.account_validated_names
    ADD CONSTRAINT account_validated_names_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.account_web_authns
    ADD CONSTRAINT account_web_authns_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.api_app_tokens
    ADD CONSTRAINT api_app_tokens_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.api_apps
    ADD CONSTRAINT api_apps_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.api_usage_counters
    ADD CONSTRAINT api_usage_counters_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.asset_attributes
    ADD CONSTRAINT asset_attributes_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.asset_collection_attributes
    ADD CONSTRAINT asset_collection_attributes_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.asset_collections
    ADD CONSTRAINT asset_collections_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.asset_histories
    ADD CONSTRAINT asset_histories_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.asset_scans
    ADD CONSTRAINT asset_scans_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.assets
    ADD CONSTRAINT assets_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.assets
    ADD CONSTRAINT assets_tft_key UNIQUE (tft);
ALTER TABLE ONLY public.audit_events
    ADD CONSTRAINT audit_events_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.chat_event_assets
    ADD CONSTRAINT chat_event_assets_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.chat_events
    ADD CONSTRAINT chat_events_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.chat_participants
    ADD CONSTRAINT chat_participants_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.chats
    ADD CONSTRAINT chats_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.cities
    ADD CONSTRAINT cities_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.content_review_comments
    ADD CONSTRAINT content_review_comments_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.content_reviews
    ADD CONSTRAINT content_reviews_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.countries
    ADD CONSTRAINT countries_iso2_key UNIQUE (iso2);
ALTER TABLE ONLY public.countries
    ADD CONSTRAINT countries_iso3_key UNIQUE (iso3);
ALTER TABLE ONLY public.countries
    ADD CONSTRAINT countries_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.country_timezones
    ADD CONSTRAINT country_timezones_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.country_translations
    ADD CONSTRAINT country_translations_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.delayed_jobs
    ADD CONSTRAINT delayed_jobs_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.flags
    ADD CONSTRAINT flags_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.hierarchies
    ADD CONSTRAINT hierarchies_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.hierarchy_levels
    ADD CONSTRAINT hierarchy_levels_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.identities
    ADD CONSTRAINT identities_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.identity_actions
    ADD CONSTRAINT identity_actions_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.identity_avatars
    ADD CONSTRAINT identity_avatars_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.identity_blocks
    ADD CONSTRAINT identity_blocks_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.identity_follows
    ADD CONSTRAINT identity_follows_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.identity_organizations
    ADD CONSTRAINT identity_organizations_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.identity_permissions
    ADD CONSTRAINT identity_permissions_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.membership_receipts
    ADD CONSTRAINT membership_receipts_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.order_item
    ADD CONSTRAINT order_item_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.organization_hierarchies
    ADD CONSTRAINT organization_hierarchies_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.organizations
    ADD CONSTRAINT organizations_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.post_assets
    ADD CONSTRAINT post_assets_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.post_reactions
    ADD CONSTRAINT post_reactions_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.post_views
    ADD CONSTRAINT post_views_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.posts
    ADD CONSTRAINT posts_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.product_price_variations
    ADD CONSTRAINT product_price_variations_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.servers
    ADD CONSTRAINT servers_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.session_validations
    ADD CONSTRAINT session_validations_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.sessions
    ADD CONSTRAINT sessions_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.states
    ADD CONSTRAINT states_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.identities
    ADD CONSTRAINT uidx_identity_username_normalized UNIQUE (username_normalized);
ALTER TABLE ONLY public.urls
    ADD CONSTRAINT urls_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.verification_request_names
    ADD CONSTRAINT verification_request_names_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.verification_request_notes
    ADD CONSTRAINT verification_request_notes_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.verification_requests
    ADD CONSTRAINT verification_requests_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.verifications
    ADD CONSTRAINT verifications_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.webauthn_certificates
    ADD CONSTRAINT webauthn_certificates_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.api_app_tokens
    ADD CONSTRAINT api_app_tokens_app_fk FOREIGN KEY (api_app_id) REFERENCES public.api_apps(id);
ALTER TABLE ONLY public.api_apps
    ADD CONSTRAINT api_apps_owner_fk FOREIGN KEY (owner_account_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.identity_permissions
    ADD CONSTRAINT fk1h6yiqn424gp4jcns8m4w4dql FOREIGN KEY (organization_id) REFERENCES public.organizations(id);
ALTER TABLE ONLY public.verification_requests
    ADD CONSTRAINT fk1ujdqgox95tqvrh8bnfwteiu2 FOREIGN KEY (account_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.sessions
    ADD CONSTRAINT fk2hk7gsn2gcsiqmr07ggl677qr FOREIGN KEY (account_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.delayed_jobs
    ADD CONSTRAINT fk388t4xvw3m4cp9fubifanhxpo FOREIGN KEY (last_run_by_id) REFERENCES public.servers(id);
ALTER TABLE ONLY public.identity_permissions
    ADD CONSTRAINT fk3ntvl40b6r0bhpfv1x80bhn3m FOREIGN KEY (permission_id) REFERENCES public.permissions(id);
ALTER TABLE ONLY public.content_review_comments
    ADD CONSTRAINT fk42l5cpqj5bq8xct72083yfhyk FOREIGN KEY (post_id) REFERENCES public.posts(id);
ALTER TABLE ONLY public.product_price_variations
    ADD CONSTRAINT fk4h5nc6vrsn0f1y6f7a2nonplg FOREIGN KEY (product_id) REFERENCES public.products(id);
ALTER TABLE ONLY public.organization_hierarchies
    ADD CONSTRAINT fk57m3mcr6fyis1t889wvvqjef4 FOREIGN KEY (hierarchy_id) REFERENCES public.hierarchies(id);
ALTER TABLE ONLY public.organization_hierarchies
    ADD CONSTRAINT fk591fj4mxa46fqcewg31v7u5kw FOREIGN KEY (organization_id) REFERENCES public.organizations(id);
ALTER TABLE ONLY public.account_web_authns
    ADD CONSTRAINT fk6f2en2br5t5yxdqumwtkj03q2 FOREIGN KEY (account_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.cities
    ADD CONSTRAINT fk6gatmv9dwedve82icy8wrkdmk FOREIGN KEY (country_id) REFERENCES public.countries(id);
ALTER TABLE ONLY public.content_review_comments
    ADD CONSTRAINT fk70y7flwc3urq4xjts1aptiyn1 FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.verification_request_notes
    ADD CONSTRAINT fk78g0691wk4l2348u37bd7tsa FOREIGN KEY (verification_request_id) REFERENCES public.verification_requests(id);
ALTER TABLE ONLY public.hierarchy_levels
    ADD CONSTRAINT fk7a56vntynyrm8x82cd8ymv661 FOREIGN KEY (hierarchy_id) REFERENCES public.hierarchies(id);
ALTER TABLE ONLY public.identity_actions
    ADD CONSTRAINT fk7cfb8qj4nak3yrnja2fu5quqr FOREIGN KEY (post_id) REFERENCES public.posts(id);
ALTER TABLE ONLY public.sessions
    ADD CONSTRAINT fk7cqdd8ulfc3qknegm613r35qu FOREIGN KEY (connected_server_id) REFERENCES public.servers(id);
ALTER TABLE ONLY public.asset_histories
    ADD CONSTRAINT fk7ctbm3rjmrkf38l9p44urnjfg FOREIGN KEY (actor_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.flags
    ADD CONSTRAINT fk83lhj85k0akldoh5m92h2w7cl FOREIGN KEY (reported_by_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.post_views
    ADD CONSTRAINT fk8ud5qae5n3tpxls37acjhb1ha FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT fk943irua9hqdyxtlc3v3sm2yox FOREIGN KEY (country_id) REFERENCES public.countries(id);
ALTER TABLE ONLY public.posts
    ADD CONSTRAINT fk9kwkjsj2iai6s1r8sdsdg0fv6 FOREIGN KEY (identity_avatar_id) REFERENCES public.identity_avatars(id);
ALTER TABLE ONLY public.verification_requests
    ADD CONSTRAINT fk9qsdrkfc8vbdu34syq3fo8wjm FOREIGN KEY (order_id) REFERENCES public.orders(id);
ALTER TABLE ONLY public.assets
    ADD CONSTRAINT fka2v31h5m9hfhf4vgxt8qbt9jt FOREIGN KEY (creator_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.post_assets
    ADD CONSTRAINT fkaj794bvjinnn51lq0vqbhypod FOREIGN KEY (asset_id) REFERENCES public.assets(id);
ALTER TABLE ONLY public.identity_avatars
    ADD CONSTRAINT fkao7knwsgcycrhtmfjmbnd1srx FOREIGN KEY (asset_id) REFERENCES public.assets(id);
ALTER TABLE ONLY public.flags
    ADD CONSTRAINT fkatideb0w08tcnlvkangrjj847 FOREIGN KEY (post_id) REFERENCES public.posts(id);
ALTER TABLE ONLY public.identity_organizations
    ADD CONSTRAINT fkbb25vsifi74jobdupvybgkn1p FOREIGN KEY (approved_by_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.products
    ADD CONSTRAINT fkcq2q5sr56he3idwu8yamusw04 FOREIGN KEY (organization_id) REFERENCES public.organizations(id);
ALTER TABLE ONLY public.hierarchies
    ADD CONSTRAINT fkcxrkx73mo13e0vmy7vplvn7fm FOREIGN KEY (organization_id) REFERENCES public.organizations(id);
ALTER TABLE ONLY public.session_validations
    ADD CONSTRAINT fkd0i7h3tc6l9q4o0jyt9676447 FOREIGN KEY (email_id) REFERENCES public.account_emails(id);
ALTER TABLE ONLY public.post_views
    ADD CONSTRAINT fkd1vgy0lcsruh93177lhj97giv FOREIGN KEY (session_id) REFERENCES public.sessions(id);
ALTER TABLE ONLY public.orders
    ADD CONSTRAINT fkdc7k6xpdl9eojct8lyp7pn5b5 FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.posts
    ADD CONSTRAINT fkdor22mv7c8vs14e9cwmgxmn7o FOREIGN KEY (organization_id) REFERENCES public.organizations(id);
ALTER TABLE ONLY public.identity_avatars
    ADD CONSTRAINT fkdq4l0m76gl6xvjpw5h9bd6gmc FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.asset_attributes
    ADD CONSTRAINT fke3ncu6lhle4tl91i70qghuwnn FOREIGN KEY (asset_id) REFERENCES public.assets(id);
ALTER TABLE ONLY public.verifications
    ADD CONSTRAINT fketmgehailgkb0re5d771ukiia FOREIGN KEY (approved_by_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.chat_event_assets
    ADD CONSTRAINT fkf2ios5qm1ynd47uhtqls5rbxc FOREIGN KEY (event_id) REFERENCES public.chat_events(id);
ALTER TABLE ONLY public.webauthn_certificates
    ADD CONSTRAINT fkftwtgd0phm0b1m5kk2x6950b5 FOREIGN KEY (account_web_authn_id) REFERENCES public.account_web_authns(id);
ALTER TABLE ONLY public.identity_blocks
    ADD CONSTRAINT fkgb07xnom6eo2pym0g3rrymr8n FOREIGN KEY (identity2_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.organizations
    ADD CONSTRAINT fkgsay2unfbsax6k1ulw0k557o7 FOREIGN KEY (parent_id) REFERENCES public.organizations(id);
ALTER TABLE ONLY public.asset_attributes
    ADD CONSTRAINT fkgtgdbyxfopoohm68a0av181bp FOREIGN KEY (asset_collection_attribute_id) REFERENCES public.asset_collection_attributes(id);
ALTER TABLE ONLY public.chat_events
    ADD CONSTRAINT fkhhrsyupwyjyyy4klfcneiuwky FOREIGN KEY (chat_id) REFERENCES public.chats(id);
ALTER TABLE ONLY public.chat_event_assets
    ADD CONSTRAINT fkhpyyhcesb2sl7ahfm9yyllbv9 FOREIGN KEY (asset_id) REFERENCES public.assets(id);
ALTER TABLE ONLY public.country_translations
    ADD CONSTRAINT fkhtmcim22qewg0jlap9pexnpo1 FOREIGN KEY (country_id) REFERENCES public.countries(id);
ALTER TABLE ONLY public.country_timezones
    ADD CONSTRAINT fki65w6fys54epb8t2ei5jbo1n FOREIGN KEY (country_id) REFERENCES public.countries(id);
ALTER TABLE ONLY public.identity_blocks
    ADD CONSTRAINT fkig5yfrj6g3nkhbv9h5pq98qg8 FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.verification_request_names
    ADD CONSTRAINT fkjbtdcb1nowpjo1idroffavt8v FOREIGN KEY (verification_request_id) REFERENCES public.verification_requests(id);
ALTER TABLE ONLY public.session_validations
    ADD CONSTRAINT fkk2ttyd2e7nggxt8vtnhfipp61 FOREIGN KEY (phone_id) REFERENCES public.account_phones(id);
ALTER TABLE ONLY public.content_reviews
    ADD CONSTRAINT fkk3r8k5fu28x4f26c2gsxcg7d6 FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.posts
    ADD CONSTRAINT fkk7thi4q2l66ef0vtd9gf68ey4 FOREIGN KEY (reply_to_id) REFERENCES public.posts(id);
ALTER TABLE ONLY public.account_validated_names
    ADD CONSTRAINT fkkej726u2bpsmnnddvrsjf733i FOREIGN KEY (account_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.identities
    ADD CONSTRAINT fkkf7vkbs4s2aakwf6rv2mgnsss FOREIGN KEY (account_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.identity_permissions
    ADD CONSTRAINT fkkm5tlfkfd480xthvnngx1bw08 FOREIGN KEY (approved_by_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.organizations
    ADD CONSTRAINT fkkm9gavv1l7bs4uxkn8mnoanpf FOREIGN KEY (level_id) REFERENCES public.hierarchy_levels(id);
ALTER TABLE ONLY public.chat_events
    ADD CONSTRAINT fkl0ui9rd5i6t0p1trqvhsppo7g FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT fkl7dm9ly1v16f3fnbdid8ow6sr FOREIGN KEY (state_id) REFERENCES public.states(id);
ALTER TABLE ONLY public.verification_request_notes
    ADD CONSTRAINT fkl99wvqlkciuumlkpoxqa99sam FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.identity_follows
    ADD CONSTRAINT fkle3lb2xutfn22lqs31tbexo52 FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.membership_receipts
    ADD CONSTRAINT fkleci4qe402j3rcg6dfu4wak FOREIGN KEY (account_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.account_memberships
    ADD CONSTRAINT fklgutw3qmb8vadnm3p0fbum6at FOREIGN KEY (account_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.asset_scans
    ADD CONSTRAINT fkm0pbur4ubaaj937qrpitloaro FOREIGN KEY (asset_id) REFERENCES public.assets(id);
ALTER TABLE ONLY public.post_views
    ADD CONSTRAINT fkm1fm9hc7487k4j6qd2g1iq0k2 FOREIGN KEY (post_id) REFERENCES public.posts(id);
ALTER TABLE ONLY public.account_sales_taxes
    ADD CONSTRAINT fkmu38ufnkh4niu5jd04d0w9x7p FOREIGN KEY (account_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.asset_histories
    ADD CONSTRAINT fkmw06rdnaqc5mlursy7cy7p0x2 FOREIGN KEY (asset_id) REFERENCES public.assets(id);
ALTER TABLE ONLY public.posts
    ADD CONSTRAINT fkn08lcgsen2sc1mk4lxv57ojr7 FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.flags
    ADD CONSTRAINT fkn3xwvofyp4qp51opkctygrhm1 FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.chat_participants
    ADD CONSTRAINT fkn4feij8janlba38q59kl2ebgg FOREIGN KEY (chat_id) REFERENCES public.chats(id);
ALTER TABLE ONLY public.delayed_jobs
    ADD CONSTRAINT fknbfk1f3jquwlyeay0wv1j7vc0 FOREIGN KEY (locked_by_id) REFERENCES public.servers(id);
ALTER TABLE ONLY public.product_price_variations
    ADD CONSTRAINT fknonxq4phimg1gpntmr6wmqq0p FOREIGN KEY (country_id) REFERENCES public.countries(id);
ALTER TABLE ONLY public.identity_permissions
    ADD CONSTRAINT fko222y2akgaw4y8jlrnwp0jwn7 FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.content_reviews
    ADD CONSTRAINT fkom54kfimg0h85luxp30tp3qns FOREIGN KEY (post_id) REFERENCES public.posts(id);
ALTER TABLE ONLY public.content_review_comments
    ADD CONSTRAINT fkot9lkheur6fqywabdnx4275kt FOREIGN KEY (chat_event_id) REFERENCES public.chat_events(id);
ALTER TABLE ONLY public.account_phones
    ADD CONSTRAINT fkplcgyv14jxtpwv4obgdxtduv9 FOREIGN KEY (account_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.sessions
    ADD CONSTRAINT fkpshn5ponbo8jlh6hj1gj9ld5m FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.post_reactions
    ADD CONSTRAINT fkq9ivjiqt8flog43og7gtmoyqw FOREIGN KEY (post_id) REFERENCES public.posts(id);
ALTER TABLE ONLY public.identity_actions
    ADD CONSTRAINT fkqctdvh16rfb2t7mlw7wdpgva9 FOREIGN KEY (target_identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.content_reviews
    ADD CONSTRAINT fkqqkiuxnnkgfi0fghqlyd83ks1 FOREIGN KEY (chat_event_id) REFERENCES public.chat_events(id);
ALTER TABLE ONLY public.verifications
    ADD CONSTRAINT fkqytsg9on8yelqcuyxhivra9hg FOREIGN KEY (account_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.identity_actions
    ADD CONSTRAINT fkr57c0d6kl98sfj1vnhp7v6gbo FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.post_assets
    ADD CONSTRAINT fkraninxij4osr6dr819sly0m2f FOREIGN KEY (post_id) REFERENCES public.posts(id);
ALTER TABLE ONLY public.session_validations
    ADD CONSTRAINT fkrgeyka7sn4sskb45ug06rh44w FOREIGN KEY (session_id) REFERENCES public.sessions(id);
ALTER TABLE ONLY public.identity_follows
    ADD CONSTRAINT fkrt39p3f6va8fuck2msb2094r FOREIGN KEY (identity2_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.identity_organizations
    ADD CONSTRAINT fkrx6wo0j556krlhq0kvri0hevc FOREIGN KEY (organization_id) REFERENCES public.organizations(id);
ALTER TABLE ONLY public.account_emails
    ADD CONSTRAINT fks1koskjx666pbid0gh61vt2s8 FOREIGN KEY (account_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.identity_organizations
    ADD CONSTRAINT fks6jm1vmh7i3bklcbl6g0yys7f FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.assets
    ADD CONSTRAINT fksjrwgxv21pk57ru95bphaj528 FOREIGN KEY (owner_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.states
    ADD CONSTRAINT fkskkdphjml9vjlrqn4m5hi251y FOREIGN KEY (country_id) REFERENCES public.countries(id);
ALTER TABLE ONLY public.cities
    ADD CONSTRAINT fksu54e1tlhaof4oklvv7uphsli FOREIGN KEY (state_id) REFERENCES public.states(id);
ALTER TABLE ONLY public.asset_histories
    ADD CONSTRAINT fksxux5t9pv0rm8jmtmr1noguiy FOREIGN KEY (actor2_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.post_reactions
    ADD CONSTRAINT fkt866nvsreb1syxl6w37u4co6x FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.products
    ADD CONSTRAINT fktc0rq1h33lxfodddq765g5s3 FOREIGN KEY (identity_id) REFERENCES public.identities(id);
ALTER TABLE ONLY public.flags
    ADD CONSTRAINT fktc41n92g1wx7chsm6ctieirk5 FOREIGN KEY (chat_event_id) REFERENCES public.chat_events(id);
ALTER TABLE ONLY public.chat_participants
    ADD CONSTRAINT fktflqn8uut4ecvr27irpk9ysjq FOREIGN KEY (identity_id) REFERENCES public.identities(id);

CREATE INDEX idx3y72hgbgg197audcvcb4id5pn ON public.chat_events USING btree (chat_id, created);
CREATE INDEX idx_country_timezone ON public.country_timezones USING btree (country_id, timezone);
CREATE INDEX idx_country_timezone_country ON public.country_timezones USING btree (country_id);
CREATE INDEX idxi6gvkovsf5tmyrvjfbak7p66m ON public.assets USING btree (hash);
CREATE INDEX ix_account_emails_email_validated ON public.account_emails USING btree (email, validated);
CREATE INDEX ix_api_app_tokens_app ON public.api_app_tokens USING btree (api_app_id);
CREATE INDEX ix_api_usage_counters_principal_minute ON public.api_usage_counters USING btree (principal_type, principal_id, minute);
CREATE INDEX ix_audit_events_actor_account ON public.audit_events USING btree (actor_account_id);
CREATE INDEX ix_audit_events_actor_identity ON public.audit_events USING btree (actor_identity_id);
CREATE INDEX ix_audit_events_event_type ON public.audit_events USING btree (event_type);
CREATE INDEX ix_audit_events_occurred_at ON public.audit_events USING btree (occurred_at);
CREATE INDEX ix_audit_events_unpublished ON public.audit_events USING btree (occurred_at) WHERE (published_at IS NULL);
CREATE INDEX ix_delayed_jobs_queue_run_at ON public.delayed_jobs USING btree (queue, run_at, complete, locked);
CREATE UNIQUE INDEX uidx_account_emails_validated_lower ON public.account_emails USING btree (lower((email)::text)) WHERE (validated IS NOT NULL);
CREATE UNIQUE INDEX uidx_account_phones_validated ON public.account_phones USING btree (phone) WHERE (validated IS NOT NULL);
CREATE UNIQUE INDEX uidx_api_app_tokens_token_hash ON public.api_app_tokens USING btree (token_hash);
CREATE UNIQUE INDEX uidx_api_apps_client_id ON public.api_apps USING btree (client_id);
CREATE UNIQUE INDEX uidx_api_usage_counters_grain ON public.api_usage_counters USING btree (principal_type, principal_id, node_id, minute);

-- //@UNDO

-- Drop the entire baseline schema. Simple and complete: recreate an empty
-- public schema (postgis and all tables/constraints/indexes go with it).
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
