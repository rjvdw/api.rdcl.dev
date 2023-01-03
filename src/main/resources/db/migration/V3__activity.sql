create table activity
(
    id          uuid         not null default gen_random_uuid() primary key,
    owner       uuid         not null references auth_identity (id),
    title       varchar(511) not null,
    description text,
    notes       text,
    url         varchar(511),
    location    varchar(511) not null,
    starts      timestamptz  not null,
    ends        timestamptz,
    all_day     bool         not null default false
);
