alter table health_settings
    alter column settings type jsonb using settings::jsonb;

alter table health
    alter column data type jsonb using data::jsonb;

alter table label
    alter column styles type jsonb using styles::jsonb;

alter table label
    rename column styles to settings;
