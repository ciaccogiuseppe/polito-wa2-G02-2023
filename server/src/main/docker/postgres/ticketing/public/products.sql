create table if not exists products
(
    product_id varchar(255) not null
        primary key,
    brand      varchar(255) not null,
    name       varchar(255) not null,
    category   bigint
        references categories
);

alter table products
    owner to postgres;

