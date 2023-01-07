create table auth_allowed_callback
(
    id  uuid         not null default gen_random_uuid() primary key,
    url varchar(511) not null unique
);
