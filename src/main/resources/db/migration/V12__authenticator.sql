alter table auth_authenticator
    add column last_used timestamp;

create index on auth_authenticator (key_id);
