alter table document
    alter column content_json type jsonb
    using content_json::jsonb;
