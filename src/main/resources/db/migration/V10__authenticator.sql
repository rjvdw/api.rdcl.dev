create table auth_authenticator
(
    id              uuid   not null default gen_random_uuid() primary key,
    name            varchar(255),
    owner           uuid   not null references auth_identity (id),
    key_id          bytea  not null,
    cose            bytea  not null,
    signature_count bigint not null
);

create table auth_authenticator_registration
(
    id      uuid      not null default gen_random_uuid() primary key,
    owner   uuid      not null references auth_identity (id),
    options text,
    created timestamp not null default now(),
    timeout bigint    not null
);
