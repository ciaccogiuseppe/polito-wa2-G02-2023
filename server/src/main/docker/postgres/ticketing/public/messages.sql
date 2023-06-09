create table if not exists messages
(
    id             bigserial
        primary key,
    sent_timestamp timestamp(6) not null,
    text           varchar(255) not null,
    sender_id      bigint       not null
        constraint fk79kgt6oyju1ma9ly4qmax5933
            references profiles,
    ticket_id      bigint       not null
        constraint fk6iv985o3ybdk63srj731en4ba
            references tickets
);

alter table messages
    owner to postgres;

