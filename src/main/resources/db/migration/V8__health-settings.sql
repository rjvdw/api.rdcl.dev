create table health_settings
(
    owner    uuid not null primary key references auth_identity (id),
    settings text
);
