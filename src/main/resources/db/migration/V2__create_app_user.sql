create table if not exists app_user (
                                        id bigserial primary key,
                                        name varchar(255),
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    created_at timestamp not null
    );
