create table if not exists profiles
(
    id      bigserial
        primary key,
    email   varchar(255) not null
        constraint uk_lnk8iosvsrn5614xw3lgnybgk
            unique,
    name    varchar(255) not null,
    role    varchar(255) not null,
    surname varchar(255) not null
);

alter table profiles
    owner to postgres;

