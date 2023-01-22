alter table label
    drop constraint label_text_key;

create unique index on label (owner, text);
