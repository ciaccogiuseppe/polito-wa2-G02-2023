create table if not exists attachments
(
    id         bigserial
        primary key,
    attachment bytea        not null,
    name       varchar(255) not null,
    message_id bigint       not null
        constraint fkcf4ta8qdkixetfy7wnqfv3vkv
            references messages
);

alter table attachments
    owner to postgres;

