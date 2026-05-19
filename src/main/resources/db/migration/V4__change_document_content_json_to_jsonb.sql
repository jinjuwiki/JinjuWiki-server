alter table document
    alter column content_json type jsonb
    using (
        case
            when content_json is null or btrim(content_json) = '' then
                jsonb_build_object('type', 'doc', 'content', jsonb_build_array())
            when btrim(content_json) like '{%' or btrim(content_json) like '[%' then
                content_json::jsonb
            else
                jsonb_build_object(
                    'type', 'doc',
                    'content', jsonb_build_array(
                        jsonb_build_object(
                            'type', 'paragraph',
                            'content', jsonb_build_array(
                                jsonb_build_object(
                                    'type', 'text',
                                    'text', content_json
                                )
                            )
                        )
                    )
                )
        end
    );
