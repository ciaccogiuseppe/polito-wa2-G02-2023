create table if not exists tickets
(
    id                bigserial
        primary key,
    created_timestamp timestamp(6) not null,
    description       varchar(255) not null,
    priority          integer      not null,
    status            varchar(255) not null,
    title             varchar(255) not null,
    customer_id       bigint       not null
        constraint fkwsg96xnnr1cobwin0fj5xtqe
            references profiles,
    expert_id         bigint
        constraint fk8ojtqms4badovjb5mj7w4se56
            references profiles,
    product_id        varchar(255) not null
        constraint fkavo2av2fyyehcvlec0vowwu1j
            references products
);

alter table tickets
    owner to postgres;

