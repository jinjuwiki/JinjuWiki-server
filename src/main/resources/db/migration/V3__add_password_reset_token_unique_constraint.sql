alter table password_reset_tokens
    add constraint uk_password_reset_tokens_reset_token unique (reset_token);
