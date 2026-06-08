--
--    Baseline schema (authoritative consolidated initial schema).
--

-- // baseline schema
-- Consolidated full schema generated from the Hibernate-mapped entities
-- (Quarkus 3.36.1 / Hibernate Reactive) against PostgreSQL 17. As of this
-- migration, Hibernate schema auto-update is disabled and MyBatis migrations
-- are authoritative. This script runs after first_migration and before the
-- 2026 incremental scripts, which remain idempotent (IF [NOT] EXISTS) no-ops
-- on a fresh database.

-- Required for the geometry column on public.countries (and future spatial use).
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE public.account_emails (
    is_primary boolean NOT NULL,
    account_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    validated timestamp(6) with time zone,
    version bigint NOT NULL,
    email character varying(255)
);


--
-- Name: account_memberships; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.account_memberships (
    account_id bigint,
    begins timestamp(6) with time zone,
    created timestamp(6) with time zone NOT NULL,
    ends timestamp(6) with time zone,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    status character varying(255),
    CONSTRAINT account_memberships_status_check CHECK (((status)::text = ANY ((ARRAY['PAUSED'::character varying, 'CANCELLED'::character varying, 'ACTIVE'::character varying, 'EXPIRED'::character varying])::text[])))
);


--
-- Name: account_phones; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.account_phones (
    is_primary boolean NOT NULL,
    account_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    validated timestamp(6) with time zone,
    version bigint NOT NULL,
    phone character varying(255)
);


--
-- Name: account_sales_taxes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.account_sales_taxes (
    account_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);


--
-- Name: account_validated_names; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.account_validated_names (
    account_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    name character varying(255)
);


--
-- Name: account_web_authns; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.account_web_authns (
    account_id bigint,
    counter bigint NOT NULL,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    aaguid character varying(255),
    alg character varying(255),
    credential_id character varying(255),
    fmt character varying(255),
    public_key character varying(255)
);


--
-- Name: accounts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.accounts (
    region integer NOT NULL,
    country_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    state_id bigint,
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
    CONSTRAINT accounts_status_check CHECK (((status)::text = ANY ((ARRAY['DISABLED'::character varying, 'ANONYMOUS'::character varying, 'ACTIVE'::character varying, 'SUSPENDED'::character varying, 'BANNED'::character varying])::text[])))
);


--
-- Name: asset_attributes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.asset_attributes (
    numeric_value double precision,
    rank double precision,
    asset_collection_attribute_id bigint,
    asset_id bigint NOT NULL,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    asset_attribute_type character varying(255),
    text_value character varying(255),
    CONSTRAINT asset_attributes_asset_attribute_type_check CHECK (((asset_attribute_type)::text = ANY ((ARRAY['DATE'::character varying, 'NUMBER'::character varying, 'STRING'::character varying, 'BOOLEAN'::character varying])::text[])))
);


--
-- Name: asset_collection_attributes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.asset_collection_attributes (
    max_value double precision,
    min_value double precision,
    rank double precision,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    asset_attribute_type character varying(255),
    description character varying(255),
    name character varying(255),
    unit character varying(255),
    CONSTRAINT asset_collection_attributes_asset_attribute_type_check CHECK (((asset_attribute_type)::text = ANY ((ARRAY['DATE'::character varying, 'NUMBER'::character varying, 'STRING'::character varying, 'BOOLEAN'::character varying])::text[])))
);


--
-- Name: asset_collections; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.asset_collections (
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);


--
-- Name: asset_histories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.asset_histories (
    actor2_id bigint,
    actor_id bigint,
    asset_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    action character varying(255)
);


--
-- Name: asset_scans; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.asset_scans (
    asset_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    results text
);


--
-- Name: assets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.assets (
    created timestamp(6) with time zone NOT NULL,
    creator_id bigint,
    id bigint NOT NULL,
    owner_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    tft uuid,
    hash character varying(255),
    type character varying(255),
    CONSTRAINT assets_type_check CHECK (((type)::text = ANY ((ARRAY['IMAGE'::character varying, 'VIDEO'::character varying, 'TEXT'::character varying])::text[])))
);


--
-- Name: chat_event_assets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_event_assets (
    asset_id bigint,
    created timestamp(6) with time zone NOT NULL,
    event_id bigint,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);


--
-- Name: chat_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_events (
    type smallint,
    chat_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    identity_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    message text,
    CONSTRAINT chat_events_type_check CHECK (((type >= 0) AND (type <= 9)))
);


--
-- Name: chat_participants; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_participants (
    type smallint,
    chat_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    identity_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    CONSTRAINT chat_participants_type_check CHECK (((type >= 0) AND (type <= 2)))
);


--
-- Name: chats; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chats (
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);


--
-- Name: cities; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cities (
    country_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    state_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    name character varying(255),
    location bytea
);


--
-- Name: content_review_comments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.content_review_comments (
    chat_event_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    identity_id bigint,
    post_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    comment text
);


--
-- Name: content_reviews; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.content_reviews (
    bot boolean NOT NULL,
    chat_event_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    identity_id bigint,
    post_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    result character varying(255),
    type character varying(255),
    CONSTRAINT content_reviews_result_check CHECK (((result)::text = ANY ((ARRAY['TERMS_VIOLATION_ADULT'::character varying, 'TERMS_VIOLATION'::character varying, 'TERMS_VIOLATION_VIOLENCE'::character varying, 'TERMS_VIOLATION_HATE'::character varying, 'TERMS_VIOLATION_SPAM'::character varying, 'TERMS_VIOLATION_OTHER'::character varying, 'DOES_NOT_VIOLATE_TERMS'::character varying])::text[]))),
    CONSTRAINT content_reviews_type_check CHECK (((type)::text = ANY ((ARRAY['IDENTITY'::character varying, 'CHAT_EVENT'::character varying, 'POST'::character varying])::text[])))
);


--
-- Name: countries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.countries (
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
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


--
-- Name: country_timezones; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.country_timezones (
    country_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    timezone character varying(255)
);


--
-- Name: country_translations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.country_translations (
    country_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    locale character varying(255),
    name character varying(255)
);


--
-- Name: delayed_jobs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.delayed_jobs (
    attempts integer,
    complete boolean NOT NULL,
    completed_with_failure boolean NOT NULL,
    locked boolean NOT NULL,
    priority integer,
    actor_id bigint,
    completed_at timestamp(6) with time zone,
    created timestamp(6) with time zone NOT NULL,
    failed_at timestamp(6) with time zone,
    id bigint NOT NULL,
    last_run_by_id bigint,
    locked_at timestamp(6) with time zone,
    locked_by_id bigint,
    run_at timestamp(6) with time zone,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    failure_reason character varying(255),
    last_error text,
    queue character varying(255),
    CONSTRAINT delayed_jobs_queue_check CHECK (((queue)::text = ANY ((ARRAY['URL_CRAWL'::character varying, 'ETL_LOCATION'::character varying, 'PHONE_AUTH'::character varying, 'EMAIL_AUTH'::character varying, 'EMAIL_MARKETING'::character varying, 'LICENSE_PROVISION'::character varying])::text[])))
);


--
-- Name: flags; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flags (
    chat_event_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    identity_id bigint,
    post_id bigint,
    reported_by_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    comment text,
    comment_locale character varying(255),
    type character varying(255),
    CONSTRAINT flags_type_check CHECK (((type)::text = ANY ((ARRAY['IDENTITY'::character varying, 'CHAT_EVENT'::character varying, 'POST'::character varying])::text[])))
);


--
-- Name: hierarchies; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.hierarchies (
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    organization_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    description character varying(255),
    name character varying(255)
);


--
-- Name: hierarchy_levels; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.hierarchy_levels (
    created timestamp(6) with time zone NOT NULL,
    hierarchy_id bigint,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    name character varying(255)
);


--
-- Name: identities; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.identities (
    account_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
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


--
-- Name: identity_actions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.identity_actions (
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    identity_id bigint,
    post_id bigint,
    target_identity_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    type character varying(255),
    CONSTRAINT identity_actions_type_check CHECK (((type)::text = ANY ((ARRAY['UNFOLLOW'::character varying, 'FOLLOW'::character varying, 'UNBLOCK'::character varying, 'POST_CREATE'::character varying, 'POST_DELETE'::character varying, 'BLOCK'::character varying, 'UNMUTE'::character varying, 'MUTE'::character varying, 'POST_EDIT'::character varying])::text[])))
);


--
-- Name: identity_avatars; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.identity_avatars (
    asset_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    identity_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    name character varying(255)
);


--
-- Name: identity_blocks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.identity_blocks (
    is_mutual boolean NOT NULL,
    created timestamp(6) with time zone NOT NULL,
    expires timestamp(6) with time zone,
    id bigint NOT NULL,
    identity2_id bigint,
    identity_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    type character varying(255),
    CONSTRAINT identity_blocks_type_check CHECK (((type)::text = ANY ((ARRAY['BLOCKED'::character varying, 'IGNORED'::character varying])::text[])))
);


--
-- Name: identity_follows; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.identity_follows (
    is_mutual boolean NOT NULL,
    approved timestamp(6) with time zone,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    identity2_id bigint,
    identity_id bigint NOT NULL,
    requested timestamp(6) with time zone,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);


--
-- Name: identity_organizations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.identity_organizations (
    accepted timestamp(6) with time zone,
    approved timestamp(6) with time zone,
    approved_by_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    identity_id bigint,
    organization_id bigint,
    requested timestamp(6) with time zone,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);


--
-- Name: identity_permissions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.identity_permissions (
    accepted timestamp(6) with time zone,
    approved timestamp(6) with time zone,
    approved_by_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    identity_id bigint,
    organization_id bigint,
    permission_id bigint,
    requested timestamp(6) with time zone,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);


--
-- Name: membership_receipts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.membership_receipts (
    account_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);


--
-- Name: order_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.order_item (
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);


--
-- Name: orders; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.orders (
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    identity_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);


--
-- Name: organization_hierarchies; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.organization_hierarchies (
    created timestamp(6) with time zone NOT NULL,
    hierarchy_id bigint,
    id bigint NOT NULL,
    organization_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);


--
-- Name: organizations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.organizations (
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    level_id bigint,
    parent_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    name character varying(255)
);


--
-- Name: permissions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.permissions (
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    key character varying(255),
    name character varying(255)
);


--
-- Name: post_assets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.post_assets (
    asset_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    post_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);


--
-- Name: post_reactions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.post_reactions (
    emoji character varying(4) NOT NULL,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    identity_id bigint NOT NULL,
    post_id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);


--
-- Name: post_views; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.post_views (
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    identity_id bigint NOT NULL,
    post_id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    session_id uuid NOT NULL
);


--
-- Name: posts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.posts (
    default_reaction_emoji character varying(4),
    deleted boolean DEFAULT false NOT NULL,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    identity_avatar_id bigint,
    identity_id bigint NOT NULL,
    organization_id bigint,
    reply_to_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    body text,
    caption text
);


--
-- Name: product_price_variations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_price_variations (
    price numeric(38,2),
    country_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    product_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);


--
-- Name: products; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.products (
    price numeric(38,2),
    type smallint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    identity_id bigint,
    organization_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    description character varying(255),
    name character varying(255),
    summary character varying(255),
    url character varying(255),
    CONSTRAINT products_type_check CHECK (((type >= 0) AND (type <= 6)))
);


--
-- Name: servers; Type: TABLE; Schema: public; Owner: -
--

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
    CONSTRAINT servers_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'ABANDONED'::character varying, 'UNKNOWN'::character varying, 'SHUTDOWN'::character varying])::text[])))
);


--
-- Name: servers_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.servers ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.servers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: session_validations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.session_validations (
    created timestamp(6) with time zone NOT NULL,
    email_id bigint,
    id bigint NOT NULL,
    phone_id bigint,
    updated timestamp(6) with time zone,
    validated timestamp(6) with time zone,
    version bigint NOT NULL,
    session_id uuid,
    token character varying(255)
);


--
-- Name: sessions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sessions (
    logged_in boolean NOT NULL,
    account_id bigint,
    connected_server_id bigint,
    created timestamp(6) with time zone NOT NULL,
    ended timestamp(6) with time zone,
    identity_id bigint,
    updated timestamp(6) with time zone,
    id uuid NOT NULL,
    client_ip character varying(255)
);


--
-- Name: states; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.states (
    country_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    name character varying(255),
    state_code character varying(255),
    type character varying(255),
    location bytea
);


--
-- Name: urls; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.urls (
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    last_crawled_at timestamp(6) with time zone,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    domain character varying(255),
    value character varying(255)
);


--
-- Name: verification_request_names; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.verification_request_names (
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    verification_request_id bigint,
    version bigint NOT NULL,
    name character varying(255)
);


--
-- Name: verification_request_notes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.verification_request_notes (
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    identity_id bigint,
    updated timestamp(6) with time zone,
    verification_request_id bigint,
    version bigint NOT NULL,
    note character varying(255)
);


--
-- Name: verification_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.verification_requests (
    account_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    order_id bigint,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);


--
-- Name: verifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.verifications (
    account_id bigint,
    approved_at timestamp(6) with time zone,
    approved_by_id bigint,
    created timestamp(6) with time zone NOT NULL,
    expires_at timestamp(6) with time zone,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL
);


--
-- Name: webauthn_certificates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.webauthn_certificates (
    account_web_authn_id bigint,
    created timestamp(6) with time zone NOT NULL,
    id bigint NOT NULL,
    updated timestamp(6) with time zone,
    version bigint NOT NULL,
    x5c character varying(255)
);


--
-- Name: account_emails account_emails_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_emails
    ADD CONSTRAINT account_emails_pkey PRIMARY KEY (id);


--
-- Name: account_memberships account_memberships_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_memberships
    ADD CONSTRAINT account_memberships_pkey PRIMARY KEY (id);


--
-- Name: account_phones account_phones_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_phones
    ADD CONSTRAINT account_phones_pkey PRIMARY KEY (id);


--
-- Name: account_sales_taxes account_sales_taxes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_sales_taxes
    ADD CONSTRAINT account_sales_taxes_pkey PRIMARY KEY (id);


--
-- Name: account_validated_names account_validated_names_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_validated_names
    ADD CONSTRAINT account_validated_names_pkey PRIMARY KEY (id);


--
-- Name: account_web_authns account_web_authns_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_web_authns
    ADD CONSTRAINT account_web_authns_pkey PRIMARY KEY (id);


--
-- Name: accounts accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_pkey PRIMARY KEY (id);


--
-- Name: asset_attributes asset_attributes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_attributes
    ADD CONSTRAINT asset_attributes_pkey PRIMARY KEY (id);


--
-- Name: asset_collection_attributes asset_collection_attributes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_collection_attributes
    ADD CONSTRAINT asset_collection_attributes_pkey PRIMARY KEY (id);


--
-- Name: asset_collections asset_collections_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_collections
    ADD CONSTRAINT asset_collections_pkey PRIMARY KEY (id);


--
-- Name: asset_histories asset_histories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_histories
    ADD CONSTRAINT asset_histories_pkey PRIMARY KEY (id);


--
-- Name: asset_scans asset_scans_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_scans
    ADD CONSTRAINT asset_scans_pkey PRIMARY KEY (id);


--
-- Name: assets assets_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT assets_pkey PRIMARY KEY (id);


--
-- Name: assets assets_tft_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT assets_tft_key UNIQUE (tft);


--
-- Name: chat_event_assets chat_event_assets_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_event_assets
    ADD CONSTRAINT chat_event_assets_pkey PRIMARY KEY (id);


--
-- Name: chat_events chat_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_events
    ADD CONSTRAINT chat_events_pkey PRIMARY KEY (id);


--
-- Name: chat_participants chat_participants_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_participants
    ADD CONSTRAINT chat_participants_pkey PRIMARY KEY (id);


--
-- Name: chats chats_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chats
    ADD CONSTRAINT chats_pkey PRIMARY KEY (id);


--
-- Name: cities cities_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cities
    ADD CONSTRAINT cities_pkey PRIMARY KEY (id);


--
-- Name: content_review_comments content_review_comments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_review_comments
    ADD CONSTRAINT content_review_comments_pkey PRIMARY KEY (id);


--
-- Name: content_reviews content_reviews_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_reviews
    ADD CONSTRAINT content_reviews_pkey PRIMARY KEY (id);


--
-- Name: countries countries_iso2_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.countries
    ADD CONSTRAINT countries_iso2_key UNIQUE (iso2);


--
-- Name: countries countries_iso3_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.countries
    ADD CONSTRAINT countries_iso3_key UNIQUE (iso3);


--
-- Name: countries countries_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.countries
    ADD CONSTRAINT countries_pkey PRIMARY KEY (id);


--
-- Name: country_timezones country_timezones_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.country_timezones
    ADD CONSTRAINT country_timezones_pkey PRIMARY KEY (id);


--
-- Name: country_translations country_translations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.country_translations
    ADD CONSTRAINT country_translations_pkey PRIMARY KEY (id);


--
-- Name: delayed_jobs delayed_jobs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.delayed_jobs
    ADD CONSTRAINT delayed_jobs_pkey PRIMARY KEY (id);


--
-- Name: flags flags_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flags
    ADD CONSTRAINT flags_pkey PRIMARY KEY (id);


--
-- Name: hierarchies hierarchies_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hierarchies
    ADD CONSTRAINT hierarchies_pkey PRIMARY KEY (id);


--
-- Name: hierarchy_levels hierarchy_levels_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hierarchy_levels
    ADD CONSTRAINT hierarchy_levels_pkey PRIMARY KEY (id);


--
-- Name: identities identities_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identities
    ADD CONSTRAINT identities_pkey PRIMARY KEY (id);


--
-- Name: identity_actions identity_actions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_actions
    ADD CONSTRAINT identity_actions_pkey PRIMARY KEY (id);


--
-- Name: identity_avatars identity_avatars_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_avatars
    ADD CONSTRAINT identity_avatars_pkey PRIMARY KEY (id);


--
-- Name: identity_blocks identity_blocks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_blocks
    ADD CONSTRAINT identity_blocks_pkey PRIMARY KEY (id);


--
-- Name: identity_follows identity_follows_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_follows
    ADD CONSTRAINT identity_follows_pkey PRIMARY KEY (id);


--
-- Name: identity_organizations identity_organizations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_organizations
    ADD CONSTRAINT identity_organizations_pkey PRIMARY KEY (id);


--
-- Name: identity_permissions identity_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_permissions
    ADD CONSTRAINT identity_permissions_pkey PRIMARY KEY (id);


--
-- Name: membership_receipts membership_receipts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.membership_receipts
    ADD CONSTRAINT membership_receipts_pkey PRIMARY KEY (id);


--
-- Name: order_item order_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_item
    ADD CONSTRAINT order_item_pkey PRIMARY KEY (id);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- Name: organization_hierarchies organization_hierarchies_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.organization_hierarchies
    ADD CONSTRAINT organization_hierarchies_pkey PRIMARY KEY (id);


--
-- Name: organizations organizations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.organizations
    ADD CONSTRAINT organizations_pkey PRIMARY KEY (id);


--
-- Name: permissions permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_pkey PRIMARY KEY (id);


--
-- Name: post_assets post_assets_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.post_assets
    ADD CONSTRAINT post_assets_pkey PRIMARY KEY (id);


--
-- Name: post_reactions post_reactions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.post_reactions
    ADD CONSTRAINT post_reactions_pkey PRIMARY KEY (id);


--
-- Name: post_views post_views_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.post_views
    ADD CONSTRAINT post_views_pkey PRIMARY KEY (id);


--
-- Name: posts posts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT posts_pkey PRIMARY KEY (id);


--
-- Name: product_price_variations product_price_variations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_price_variations
    ADD CONSTRAINT product_price_variations_pkey PRIMARY KEY (id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- Name: servers servers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.servers
    ADD CONSTRAINT servers_pkey PRIMARY KEY (id);


--
-- Name: session_validations session_validations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.session_validations
    ADD CONSTRAINT session_validations_pkey PRIMARY KEY (id);


--
-- Name: sessions sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sessions
    ADD CONSTRAINT sessions_pkey PRIMARY KEY (id);


--
-- Name: states states_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.states
    ADD CONSTRAINT states_pkey PRIMARY KEY (id);


--
-- Name: identities uidx_identity_username_normalized; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identities
    ADD CONSTRAINT uidx_identity_username_normalized UNIQUE (username_normalized);


--
-- Name: urls urls_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.urls
    ADD CONSTRAINT urls_pkey PRIMARY KEY (id);


--
-- Name: verification_request_names verification_request_names_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.verification_request_names
    ADD CONSTRAINT verification_request_names_pkey PRIMARY KEY (id);


--
-- Name: verification_request_notes verification_request_notes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.verification_request_notes
    ADD CONSTRAINT verification_request_notes_pkey PRIMARY KEY (id);


--
-- Name: verification_requests verification_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.verification_requests
    ADD CONSTRAINT verification_requests_pkey PRIMARY KEY (id);


--
-- Name: verifications verifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.verifications
    ADD CONSTRAINT verifications_pkey PRIMARY KEY (id);


--
-- Name: webauthn_certificates webauthn_certificates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.webauthn_certificates
    ADD CONSTRAINT webauthn_certificates_pkey PRIMARY KEY (id);


--
-- Name: idx3y72hgbgg197audcvcb4id5pn; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx3y72hgbgg197audcvcb4id5pn ON public.chat_events USING btree (chat_id, created);


--
-- Name: idx_country_timezone; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_country_timezone ON public.country_timezones USING btree (country_id, timezone);


--
-- Name: idx_country_timezone_country; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_country_timezone_country ON public.country_timezones USING btree (country_id);


--
-- Name: idxi6gvkovsf5tmyrvjfbak7p66m; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idxi6gvkovsf5tmyrvjfbak7p66m ON public.assets USING btree (hash);


--
-- Name: ix_account_emails_email_validated; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_account_emails_email_validated ON public.account_emails USING btree (email, validated);


--
-- Name: ix_delayed_jobs_queue_run_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_delayed_jobs_queue_run_at ON public.delayed_jobs USING btree (queue, run_at, complete, locked);


--
-- Name: identity_permissions fk1h6yiqn424gp4jcns8m4w4dql; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_permissions
    ADD CONSTRAINT fk1h6yiqn424gp4jcns8m4w4dql FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: verification_requests fk1ujdqgox95tqvrh8bnfwteiu2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.verification_requests
    ADD CONSTRAINT fk1ujdqgox95tqvrh8bnfwteiu2 FOREIGN KEY (account_id) REFERENCES public.accounts(id);


--
-- Name: sessions fk2hk7gsn2gcsiqmr07ggl677qr; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sessions
    ADD CONSTRAINT fk2hk7gsn2gcsiqmr07ggl677qr FOREIGN KEY (account_id) REFERENCES public.accounts(id);


--
-- Name: delayed_jobs fk388t4xvw3m4cp9fubifanhxpo; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.delayed_jobs
    ADD CONSTRAINT fk388t4xvw3m4cp9fubifanhxpo FOREIGN KEY (last_run_by_id) REFERENCES public.servers(id);


--
-- Name: identity_permissions fk3ntvl40b6r0bhpfv1x80bhn3m; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_permissions
    ADD CONSTRAINT fk3ntvl40b6r0bhpfv1x80bhn3m FOREIGN KEY (permission_id) REFERENCES public.permissions(id);


--
-- Name: content_review_comments fk42l5cpqj5bq8xct72083yfhyk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_review_comments
    ADD CONSTRAINT fk42l5cpqj5bq8xct72083yfhyk FOREIGN KEY (post_id) REFERENCES public.posts(id);


--
-- Name: product_price_variations fk4h5nc6vrsn0f1y6f7a2nonplg; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_price_variations
    ADD CONSTRAINT fk4h5nc6vrsn0f1y6f7a2nonplg FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: organization_hierarchies fk57m3mcr6fyis1t889wvvqjef4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.organization_hierarchies
    ADD CONSTRAINT fk57m3mcr6fyis1t889wvvqjef4 FOREIGN KEY (hierarchy_id) REFERENCES public.hierarchies(id);


--
-- Name: organization_hierarchies fk591fj4mxa46fqcewg31v7u5kw; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.organization_hierarchies
    ADD CONSTRAINT fk591fj4mxa46fqcewg31v7u5kw FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: account_web_authns fk6f2en2br5t5yxdqumwtkj03q2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_web_authns
    ADD CONSTRAINT fk6f2en2br5t5yxdqumwtkj03q2 FOREIGN KEY (account_id) REFERENCES public.accounts(id);


--
-- Name: cities fk6gatmv9dwedve82icy8wrkdmk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cities
    ADD CONSTRAINT fk6gatmv9dwedve82icy8wrkdmk FOREIGN KEY (country_id) REFERENCES public.countries(id);


--
-- Name: content_review_comments fk70y7flwc3urq4xjts1aptiyn1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_review_comments
    ADD CONSTRAINT fk70y7flwc3urq4xjts1aptiyn1 FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: verification_request_notes fk78g0691wk4l2348u37bd7tsa; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.verification_request_notes
    ADD CONSTRAINT fk78g0691wk4l2348u37bd7tsa FOREIGN KEY (verification_request_id) REFERENCES public.verification_requests(id);


--
-- Name: hierarchy_levels fk7a56vntynyrm8x82cd8ymv661; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hierarchy_levels
    ADD CONSTRAINT fk7a56vntynyrm8x82cd8ymv661 FOREIGN KEY (hierarchy_id) REFERENCES public.hierarchies(id);


--
-- Name: identity_actions fk7cfb8qj4nak3yrnja2fu5quqr; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_actions
    ADD CONSTRAINT fk7cfb8qj4nak3yrnja2fu5quqr FOREIGN KEY (post_id) REFERENCES public.posts(id);


--
-- Name: sessions fk7cqdd8ulfc3qknegm613r35qu; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sessions
    ADD CONSTRAINT fk7cqdd8ulfc3qknegm613r35qu FOREIGN KEY (connected_server_id) REFERENCES public.servers(id);


--
-- Name: asset_histories fk7ctbm3rjmrkf38l9p44urnjfg; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_histories
    ADD CONSTRAINT fk7ctbm3rjmrkf38l9p44urnjfg FOREIGN KEY (actor_id) REFERENCES public.identities(id);


--
-- Name: flags fk83lhj85k0akldoh5m92h2w7cl; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flags
    ADD CONSTRAINT fk83lhj85k0akldoh5m92h2w7cl FOREIGN KEY (reported_by_id) REFERENCES public.identities(id);


--
-- Name: post_views fk8ud5qae5n3tpxls37acjhb1ha; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.post_views
    ADD CONSTRAINT fk8ud5qae5n3tpxls37acjhb1ha FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: accounts fk943irua9hqdyxtlc3v3sm2yox; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT fk943irua9hqdyxtlc3v3sm2yox FOREIGN KEY (country_id) REFERENCES public.countries(id);


--
-- Name: posts fk9kwkjsj2iai6s1r8sdsdg0fv6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT fk9kwkjsj2iai6s1r8sdsdg0fv6 FOREIGN KEY (identity_avatar_id) REFERENCES public.identity_avatars(id);


--
-- Name: verification_requests fk9qsdrkfc8vbdu34syq3fo8wjm; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.verification_requests
    ADD CONSTRAINT fk9qsdrkfc8vbdu34syq3fo8wjm FOREIGN KEY (order_id) REFERENCES public.orders(id);


--
-- Name: assets fka2v31h5m9hfhf4vgxt8qbt9jt; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT fka2v31h5m9hfhf4vgxt8qbt9jt FOREIGN KEY (creator_id) REFERENCES public.identities(id);


--
-- Name: post_assets fkaj794bvjinnn51lq0vqbhypod; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.post_assets
    ADD CONSTRAINT fkaj794bvjinnn51lq0vqbhypod FOREIGN KEY (asset_id) REFERENCES public.assets(id);


--
-- Name: identity_avatars fkao7knwsgcycrhtmfjmbnd1srx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_avatars
    ADD CONSTRAINT fkao7knwsgcycrhtmfjmbnd1srx FOREIGN KEY (asset_id) REFERENCES public.assets(id);


--
-- Name: flags fkatideb0w08tcnlvkangrjj847; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flags
    ADD CONSTRAINT fkatideb0w08tcnlvkangrjj847 FOREIGN KEY (post_id) REFERENCES public.posts(id);


--
-- Name: identity_organizations fkbb25vsifi74jobdupvybgkn1p; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_organizations
    ADD CONSTRAINT fkbb25vsifi74jobdupvybgkn1p FOREIGN KEY (approved_by_id) REFERENCES public.identities(id);


--
-- Name: products fkcq2q5sr56he3idwu8yamusw04; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT fkcq2q5sr56he3idwu8yamusw04 FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: hierarchies fkcxrkx73mo13e0vmy7vplvn7fm; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hierarchies
    ADD CONSTRAINT fkcxrkx73mo13e0vmy7vplvn7fm FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: session_validations fkd0i7h3tc6l9q4o0jyt9676447; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.session_validations
    ADD CONSTRAINT fkd0i7h3tc6l9q4o0jyt9676447 FOREIGN KEY (email_id) REFERENCES public.account_emails(id);


--
-- Name: post_views fkd1vgy0lcsruh93177lhj97giv; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.post_views
    ADD CONSTRAINT fkd1vgy0lcsruh93177lhj97giv FOREIGN KEY (session_id) REFERENCES public.sessions(id);


--
-- Name: orders fkdc7k6xpdl9eojct8lyp7pn5b5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT fkdc7k6xpdl9eojct8lyp7pn5b5 FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: posts fkdor22mv7c8vs14e9cwmgxmn7o; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT fkdor22mv7c8vs14e9cwmgxmn7o FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: identity_avatars fkdq4l0m76gl6xvjpw5h9bd6gmc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_avatars
    ADD CONSTRAINT fkdq4l0m76gl6xvjpw5h9bd6gmc FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: asset_attributes fke3ncu6lhle4tl91i70qghuwnn; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_attributes
    ADD CONSTRAINT fke3ncu6lhle4tl91i70qghuwnn FOREIGN KEY (asset_id) REFERENCES public.assets(id);


--
-- Name: verifications fketmgehailgkb0re5d771ukiia; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.verifications
    ADD CONSTRAINT fketmgehailgkb0re5d771ukiia FOREIGN KEY (approved_by_id) REFERENCES public.identities(id);


--
-- Name: chat_event_assets fkf2ios5qm1ynd47uhtqls5rbxc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_event_assets
    ADD CONSTRAINT fkf2ios5qm1ynd47uhtqls5rbxc FOREIGN KEY (event_id) REFERENCES public.chat_events(id);


--
-- Name: webauthn_certificates fkftwtgd0phm0b1m5kk2x6950b5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.webauthn_certificates
    ADD CONSTRAINT fkftwtgd0phm0b1m5kk2x6950b5 FOREIGN KEY (account_web_authn_id) REFERENCES public.account_web_authns(id);


--
-- Name: identity_blocks fkgb07xnom6eo2pym0g3rrymr8n; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_blocks
    ADD CONSTRAINT fkgb07xnom6eo2pym0g3rrymr8n FOREIGN KEY (identity2_id) REFERENCES public.identities(id);


--
-- Name: organizations fkgsay2unfbsax6k1ulw0k557o7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.organizations
    ADD CONSTRAINT fkgsay2unfbsax6k1ulw0k557o7 FOREIGN KEY (parent_id) REFERENCES public.organizations(id);


--
-- Name: asset_attributes fkgtgdbyxfopoohm68a0av181bp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_attributes
    ADD CONSTRAINT fkgtgdbyxfopoohm68a0av181bp FOREIGN KEY (asset_collection_attribute_id) REFERENCES public.asset_collection_attributes(id);


--
-- Name: chat_events fkhhrsyupwyjyyy4klfcneiuwky; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_events
    ADD CONSTRAINT fkhhrsyupwyjyyy4klfcneiuwky FOREIGN KEY (chat_id) REFERENCES public.chats(id);


--
-- Name: chat_event_assets fkhpyyhcesb2sl7ahfm9yyllbv9; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_event_assets
    ADD CONSTRAINT fkhpyyhcesb2sl7ahfm9yyllbv9 FOREIGN KEY (asset_id) REFERENCES public.assets(id);


--
-- Name: country_translations fkhtmcim22qewg0jlap9pexnpo1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.country_translations
    ADD CONSTRAINT fkhtmcim22qewg0jlap9pexnpo1 FOREIGN KEY (country_id) REFERENCES public.countries(id);


--
-- Name: country_timezones fki65w6fys54epb8t2ei5jbo1n; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.country_timezones
    ADD CONSTRAINT fki65w6fys54epb8t2ei5jbo1n FOREIGN KEY (country_id) REFERENCES public.countries(id);


--
-- Name: identity_blocks fkig5yfrj6g3nkhbv9h5pq98qg8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_blocks
    ADD CONSTRAINT fkig5yfrj6g3nkhbv9h5pq98qg8 FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: verification_request_names fkjbtdcb1nowpjo1idroffavt8v; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.verification_request_names
    ADD CONSTRAINT fkjbtdcb1nowpjo1idroffavt8v FOREIGN KEY (verification_request_id) REFERENCES public.verification_requests(id);


--
-- Name: session_validations fkk2ttyd2e7nggxt8vtnhfipp61; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.session_validations
    ADD CONSTRAINT fkk2ttyd2e7nggxt8vtnhfipp61 FOREIGN KEY (phone_id) REFERENCES public.account_phones(id);


--
-- Name: content_reviews fkk3r8k5fu28x4f26c2gsxcg7d6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_reviews
    ADD CONSTRAINT fkk3r8k5fu28x4f26c2gsxcg7d6 FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: posts fkk7thi4q2l66ef0vtd9gf68ey4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT fkk7thi4q2l66ef0vtd9gf68ey4 FOREIGN KEY (reply_to_id) REFERENCES public.posts(id);


--
-- Name: account_validated_names fkkej726u2bpsmnnddvrsjf733i; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_validated_names
    ADD CONSTRAINT fkkej726u2bpsmnnddvrsjf733i FOREIGN KEY (account_id) REFERENCES public.accounts(id);


--
-- Name: identities fkkf7vkbs4s2aakwf6rv2mgnsss; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identities
    ADD CONSTRAINT fkkf7vkbs4s2aakwf6rv2mgnsss FOREIGN KEY (account_id) REFERENCES public.accounts(id);


--
-- Name: identity_permissions fkkm5tlfkfd480xthvnngx1bw08; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_permissions
    ADD CONSTRAINT fkkm5tlfkfd480xthvnngx1bw08 FOREIGN KEY (approved_by_id) REFERENCES public.identities(id);


--
-- Name: organizations fkkm9gavv1l7bs4uxkn8mnoanpf; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.organizations
    ADD CONSTRAINT fkkm9gavv1l7bs4uxkn8mnoanpf FOREIGN KEY (level_id) REFERENCES public.hierarchy_levels(id);


--
-- Name: chat_events fkl0ui9rd5i6t0p1trqvhsppo7g; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_events
    ADD CONSTRAINT fkl0ui9rd5i6t0p1trqvhsppo7g FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: accounts fkl7dm9ly1v16f3fnbdid8ow6sr; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT fkl7dm9ly1v16f3fnbdid8ow6sr FOREIGN KEY (state_id) REFERENCES public.states(id);


--
-- Name: verification_request_notes fkl99wvqlkciuumlkpoxqa99sam; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.verification_request_notes
    ADD CONSTRAINT fkl99wvqlkciuumlkpoxqa99sam FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: identity_follows fkle3lb2xutfn22lqs31tbexo52; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_follows
    ADD CONSTRAINT fkle3lb2xutfn22lqs31tbexo52 FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: membership_receipts fkleci4qe402j3rcg6dfu4wak; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.membership_receipts
    ADD CONSTRAINT fkleci4qe402j3rcg6dfu4wak FOREIGN KEY (account_id) REFERENCES public.accounts(id);


--
-- Name: account_memberships fklgutw3qmb8vadnm3p0fbum6at; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_memberships
    ADD CONSTRAINT fklgutw3qmb8vadnm3p0fbum6at FOREIGN KEY (account_id) REFERENCES public.accounts(id);


--
-- Name: asset_scans fkm0pbur4ubaaj937qrpitloaro; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_scans
    ADD CONSTRAINT fkm0pbur4ubaaj937qrpitloaro FOREIGN KEY (asset_id) REFERENCES public.assets(id);


--
-- Name: post_views fkm1fm9hc7487k4j6qd2g1iq0k2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.post_views
    ADD CONSTRAINT fkm1fm9hc7487k4j6qd2g1iq0k2 FOREIGN KEY (post_id) REFERENCES public.posts(id);


--
-- Name: account_sales_taxes fkmu38ufnkh4niu5jd04d0w9x7p; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_sales_taxes
    ADD CONSTRAINT fkmu38ufnkh4niu5jd04d0w9x7p FOREIGN KEY (account_id) REFERENCES public.accounts(id);


--
-- Name: asset_histories fkmw06rdnaqc5mlursy7cy7p0x2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_histories
    ADD CONSTRAINT fkmw06rdnaqc5mlursy7cy7p0x2 FOREIGN KEY (asset_id) REFERENCES public.assets(id);


--
-- Name: posts fkn08lcgsen2sc1mk4lxv57ojr7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT fkn08lcgsen2sc1mk4lxv57ojr7 FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: flags fkn3xwvofyp4qp51opkctygrhm1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flags
    ADD CONSTRAINT fkn3xwvofyp4qp51opkctygrhm1 FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: chat_participants fkn4feij8janlba38q59kl2ebgg; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_participants
    ADD CONSTRAINT fkn4feij8janlba38q59kl2ebgg FOREIGN KEY (chat_id) REFERENCES public.chats(id);


--
-- Name: delayed_jobs fknbfk1f3jquwlyeay0wv1j7vc0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.delayed_jobs
    ADD CONSTRAINT fknbfk1f3jquwlyeay0wv1j7vc0 FOREIGN KEY (locked_by_id) REFERENCES public.servers(id);


--
-- Name: product_price_variations fknonxq4phimg1gpntmr6wmqq0p; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_price_variations
    ADD CONSTRAINT fknonxq4phimg1gpntmr6wmqq0p FOREIGN KEY (country_id) REFERENCES public.countries(id);


--
-- Name: identity_permissions fko222y2akgaw4y8jlrnwp0jwn7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_permissions
    ADD CONSTRAINT fko222y2akgaw4y8jlrnwp0jwn7 FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: content_reviews fkom54kfimg0h85luxp30tp3qns; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_reviews
    ADD CONSTRAINT fkom54kfimg0h85luxp30tp3qns FOREIGN KEY (post_id) REFERENCES public.posts(id);


--
-- Name: content_review_comments fkot9lkheur6fqywabdnx4275kt; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_review_comments
    ADD CONSTRAINT fkot9lkheur6fqywabdnx4275kt FOREIGN KEY (chat_event_id) REFERENCES public.chat_events(id);


--
-- Name: account_phones fkplcgyv14jxtpwv4obgdxtduv9; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_phones
    ADD CONSTRAINT fkplcgyv14jxtpwv4obgdxtduv9 FOREIGN KEY (account_id) REFERENCES public.accounts(id);


--
-- Name: sessions fkpshn5ponbo8jlh6hj1gj9ld5m; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sessions
    ADD CONSTRAINT fkpshn5ponbo8jlh6hj1gj9ld5m FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: post_reactions fkq9ivjiqt8flog43og7gtmoyqw; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.post_reactions
    ADD CONSTRAINT fkq9ivjiqt8flog43og7gtmoyqw FOREIGN KEY (post_id) REFERENCES public.posts(id);


--
-- Name: identity_actions fkqctdvh16rfb2t7mlw7wdpgva9; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_actions
    ADD CONSTRAINT fkqctdvh16rfb2t7mlw7wdpgva9 FOREIGN KEY (target_identity_id) REFERENCES public.identities(id);


--
-- Name: content_reviews fkqqkiuxnnkgfi0fghqlyd83ks1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_reviews
    ADD CONSTRAINT fkqqkiuxnnkgfi0fghqlyd83ks1 FOREIGN KEY (chat_event_id) REFERENCES public.chat_events(id);


--
-- Name: verifications fkqytsg9on8yelqcuyxhivra9hg; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.verifications
    ADD CONSTRAINT fkqytsg9on8yelqcuyxhivra9hg FOREIGN KEY (account_id) REFERENCES public.accounts(id);


--
-- Name: identity_actions fkr57c0d6kl98sfj1vnhp7v6gbo; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_actions
    ADD CONSTRAINT fkr57c0d6kl98sfj1vnhp7v6gbo FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: post_assets fkraninxij4osr6dr819sly0m2f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.post_assets
    ADD CONSTRAINT fkraninxij4osr6dr819sly0m2f FOREIGN KEY (post_id) REFERENCES public.posts(id);


--
-- Name: session_validations fkrgeyka7sn4sskb45ug06rh44w; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.session_validations
    ADD CONSTRAINT fkrgeyka7sn4sskb45ug06rh44w FOREIGN KEY (session_id) REFERENCES public.sessions(id);


--
-- Name: identity_follows fkrt39p3f6va8fuck2msb2094r; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_follows
    ADD CONSTRAINT fkrt39p3f6va8fuck2msb2094r FOREIGN KEY (identity2_id) REFERENCES public.identities(id);


--
-- Name: identity_organizations fkrx6wo0j556krlhq0kvri0hevc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_organizations
    ADD CONSTRAINT fkrx6wo0j556krlhq0kvri0hevc FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: account_emails fks1koskjx666pbid0gh61vt2s8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_emails
    ADD CONSTRAINT fks1koskjx666pbid0gh61vt2s8 FOREIGN KEY (account_id) REFERENCES public.accounts(id);


--
-- Name: identity_organizations fks6jm1vmh7i3bklcbl6g0yys7f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_organizations
    ADD CONSTRAINT fks6jm1vmh7i3bklcbl6g0yys7f FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: assets fksjrwgxv21pk57ru95bphaj528; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT fksjrwgxv21pk57ru95bphaj528 FOREIGN KEY (owner_id) REFERENCES public.identities(id);


--
-- Name: states fkskkdphjml9vjlrqn4m5hi251y; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.states
    ADD CONSTRAINT fkskkdphjml9vjlrqn4m5hi251y FOREIGN KEY (country_id) REFERENCES public.countries(id);


--
-- Name: cities fksu54e1tlhaof4oklvv7uphsli; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cities
    ADD CONSTRAINT fksu54e1tlhaof4oklvv7uphsli FOREIGN KEY (state_id) REFERENCES public.states(id);


--
-- Name: asset_histories fksxux5t9pv0rm8jmtmr1noguiy; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_histories
    ADD CONSTRAINT fksxux5t9pv0rm8jmtmr1noguiy FOREIGN KEY (actor2_id) REFERENCES public.identities(id);


--
-- Name: post_reactions fkt866nvsreb1syxl6w37u4co6x; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.post_reactions
    ADD CONSTRAINT fkt866nvsreb1syxl6w37u4co6x FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: products fktc0rq1h33lxfodddq765g5s3; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT fktc0rq1h33lxfodddq765g5s3 FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- Name: flags fktc41n92g1wx7chsm6ctieirk5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flags
    ADD CONSTRAINT fktc41n92g1wx7chsm6ctieirk5 FOREIGN KEY (chat_event_id) REFERENCES public.chat_events(id);


--
-- Name: chat_participants fktflqn8uut4ecvr27irpk9ysjq; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_participants
    ADD CONSTRAINT fktflqn8uut4ecvr27irpk9ysjq FOREIGN KEY (identity_id) REFERENCES public.identities(id);


--
-- PostgreSQL database dump complete
--


-- //@UNDO

DROP TABLE IF EXISTS public.account_emails CASCADE;
DROP TABLE IF EXISTS public.account_memberships CASCADE;
DROP TABLE IF EXISTS public.account_phones CASCADE;
DROP TABLE IF EXISTS public.account_sales_taxes CASCADE;
DROP TABLE IF EXISTS public.account_validated_names CASCADE;
DROP TABLE IF EXISTS public.account_web_authns CASCADE;
DROP TABLE IF EXISTS public.accounts CASCADE;
DROP TABLE IF EXISTS public.asset_attributes CASCADE;
DROP TABLE IF EXISTS public.asset_collection_attributes CASCADE;
DROP TABLE IF EXISTS public.asset_collections CASCADE;
DROP TABLE IF EXISTS public.asset_histories CASCADE;
DROP TABLE IF EXISTS public.asset_scans CASCADE;
DROP TABLE IF EXISTS public.assets CASCADE;
DROP TABLE IF EXISTS public.chat_event_assets CASCADE;
DROP TABLE IF EXISTS public.chat_events CASCADE;
DROP TABLE IF EXISTS public.chat_participants CASCADE;
DROP TABLE IF EXISTS public.chats CASCADE;
DROP TABLE IF EXISTS public.cities CASCADE;
DROP TABLE IF EXISTS public.content_review_comments CASCADE;
DROP TABLE IF EXISTS public.content_reviews CASCADE;
DROP TABLE IF EXISTS public.countries CASCADE;
DROP TABLE IF EXISTS public.country_timezones CASCADE;
DROP TABLE IF EXISTS public.country_translations CASCADE;
DROP TABLE IF EXISTS public.delayed_jobs CASCADE;
DROP TABLE IF EXISTS public.flags CASCADE;
DROP TABLE IF EXISTS public.hierarchies CASCADE;
DROP TABLE IF EXISTS public.hierarchy_levels CASCADE;
DROP TABLE IF EXISTS public.identities CASCADE;
DROP TABLE IF EXISTS public.identity_actions CASCADE;
DROP TABLE IF EXISTS public.identity_avatars CASCADE;
DROP TABLE IF EXISTS public.identity_blocks CASCADE;
DROP TABLE IF EXISTS public.identity_follows CASCADE;
DROP TABLE IF EXISTS public.identity_organizations CASCADE;
DROP TABLE IF EXISTS public.identity_permissions CASCADE;
DROP TABLE IF EXISTS public.membership_receipts CASCADE;
DROP TABLE IF EXISTS public.order_item CASCADE;
DROP TABLE IF EXISTS public.orders CASCADE;
DROP TABLE IF EXISTS public.organization_hierarchies CASCADE;
DROP TABLE IF EXISTS public.organizations CASCADE;
DROP TABLE IF EXISTS public.permissions CASCADE;
DROP TABLE IF EXISTS public.post_assets CASCADE;
DROP TABLE IF EXISTS public.post_reactions CASCADE;
DROP TABLE IF EXISTS public.post_views CASCADE;
DROP TABLE IF EXISTS public.posts CASCADE;
DROP TABLE IF EXISTS public.product_price_variations CASCADE;
DROP TABLE IF EXISTS public.products CASCADE;
DROP TABLE IF EXISTS public.servers CASCADE;
DROP TABLE IF EXISTS public.session_validations CASCADE;
DROP TABLE IF EXISTS public.sessions CASCADE;
DROP TABLE IF EXISTS public.states CASCADE;
DROP TABLE IF EXISTS public.urls CASCADE;
DROP TABLE IF EXISTS public.verification_request_names CASCADE;
DROP TABLE IF EXISTS public.verification_request_notes CASCADE;
DROP TABLE IF EXISTS public.verification_requests CASCADE;
DROP TABLE IF EXISTS public.verifications CASCADE;
DROP TABLE IF EXISTS public.webauthn_certificates CASCADE;
