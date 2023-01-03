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
