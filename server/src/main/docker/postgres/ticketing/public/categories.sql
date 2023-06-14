create table if not exists categories
(
    id bigserial primary key,
    name varchar (255) not null
);

alter table categories
    owner to postgres;