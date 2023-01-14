create table label
(
    id         bigserial   not null primary key,
    owner      uuid        not null references auth_identity (id),
    text       varchar(31) not null unique,
    color      varchar(31),
    text_color varchar(31)
);

create table activity_label
(
    id       bigserial   not null primary key,
    activity uuid        not null references activity (id),
    text     varchar(31) not null
);
