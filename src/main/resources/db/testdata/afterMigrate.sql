insert into
    auth_identity (id, name, email)
values
    ('f277b076-f061-403c-bf7b-266eab926677', 'John Doe', 'john.doe@example.com')
on conflict do nothing;
