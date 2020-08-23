SET SCHEMA 'public';
create extension if not exists "uuid-ossp";
CREATE TABLE thing (
                       created_by TEXT                                      NOT NULL,
                       created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP     NOT NULL,
                       updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP     NOT NULL,
                       updated_by TEXT                                              ,
                       deleted_at TIMESTAMPTZ                                       ,
                       deleted_by TEXT
);

CREATE TABLE admin(
    id UUID DEFAULT uuid_generate_v1() PRIMARY KEY ,
    name TEXT NOT NULL,
    slack_user_id TEXT NOT NULL,
    slack_username TEXT
) INHERITS (thing);
