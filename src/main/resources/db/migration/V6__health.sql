create table health
(
    date  date not null default now(),
    owner uuid not null references auth_identity (id),
    data  text,

    primary key (date, owner)
);
