-- Ensure functions like gen_random_uuid() are available
create extension if not exists pgcrypto;

create table saved_job (
                           id          uuid primary key,
                           title       text not null,
                           company     text,
                           location    text,
                           remote      boolean not null default false,
                           salary_min  numeric,
                           salary_max  numeric,
                           currency    varchar(8),
                           posted_at   timestamptz,
                           description text,
                           apply_url   text,
                           source      text,
                           created_at  timestamptz not null default now()
);

create index if not exists idx_saved_job_created_at
    on saved_job (created_at desc);
