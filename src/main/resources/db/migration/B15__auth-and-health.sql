-- authentication

create table auth_identity
(
    id    uuid         not null default gen_random_uuid() primary key,
    name  varchar(255) not null,
    email varchar(511) not null unique
);

create table auth_login_attempt
(
    id                uuid         not null default gen_random_uuid() primary key,
    session_token     varchar(255) not null,
    verification_code varchar(255) not null,
    identity          uuid         not null references auth_identity (id),
    created           timestamp    not null default now(),

    unique (session_token, verification_code)
);

create table auth_allowed_callback
(
    id  uuid         not null default gen_random_uuid() primary key,
    url varchar(511) not null unique
);

create table auth_authenticator
(
    id              uuid   not null default gen_random_uuid() primary key,
    name            varchar(255),
    owner           uuid   not null references auth_identity (id),
    key_id          bytea  not null,
    cose            bytea  not null,
    signature_count bigint not null,
    last_used       timestamp
);
create index on auth_authenticator (key_id);

create table auth_authenticator_assertion
(
    id      uuid      not null default gen_random_uuid() primary key,
    owner   uuid      not null references auth_identity (id),
    options text,
    created timestamp not null default now(),
    timeout bigint    not null
);

-- health

create table health
(
    date  date not null default now(),
    owner uuid not null references auth_identity (id),
    data  jsonb,

    primary key (date, owner)
);

create table health_settings
(
    owner    uuid not null primary key references auth_identity (id),
    settings jsonb
);
