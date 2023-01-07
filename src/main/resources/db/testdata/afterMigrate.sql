insert into
    auth_identity (id, name, email)
values
    ('f277b076-f061-403c-bf7b-266eab926677', 'John Doe', 'john.doe@example.com')
on conflict do nothing;

insert into
    auth_allowed_callback (url)
values
    ('https://example.com/login/verify')
on conflict do nothing;
