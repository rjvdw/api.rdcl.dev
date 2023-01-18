alter table activity
    alter column starts type timestamp(0) without time zone,
    alter column ends type timestamp(0) without time zone,
    add column timezone varchar(63);

update activity
set
    timezone = 'Z'
where
    timezone is null;

alter table activity
    alter column timezone set not null;
