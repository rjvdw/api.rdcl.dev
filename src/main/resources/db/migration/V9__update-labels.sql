alter table label
    add column styles text;

update label
set
    styles = '{"background-color":' ||
             coalesce('"' || label.color || '"', 'null') ||
             ',"color":' ||
             coalesce('"' || label.text_color || '"', 'null') ||
             '}';

alter table label
    drop column color,
    drop column text_color;
