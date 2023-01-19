alter table activity
    alter column starts type timestamp(0) without time zone,
    alter column ends type timestamp(0) without time zone,
    add column timezone   varchar(63),
    add column when_pivot timestamp(0)
                          generated always as (
                              case
                                  when all_day then
                                      date_trunc('day', coalesce(ends, starts) + interval '1 day')
                                  else
                                      coalesce(ends, starts)
                                  end
                              ) stored;

create index on activity (when_pivot);

update activity
set
    timezone = 'Z'
where
    timezone is null;

alter table activity
    alter column timezone set not null;
