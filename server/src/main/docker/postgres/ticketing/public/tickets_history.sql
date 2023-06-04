create table if not exists tickets_history
(
    id                bigserial
        primary key,
    new_state         varchar(255) not null,
    old_state         varchar(255) not null,
    updated_timestamp timestamp(6) not null,
    current_expert_id bigint
        constraint fk17muy6m28qespxgus32o6rtj
            references profiles,
    ticket_id         bigint       not null
        constraint fkc7awrpdvaei350mm2hlabffra
            references tickets,
    user_id           bigint       not null
        constraint fks5x4fttcy87h5hqqheeriml2p
            references profiles
);

alter table tickets_history
    owner to postgres;

