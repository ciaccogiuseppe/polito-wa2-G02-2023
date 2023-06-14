create sequence public.categories_id_seq;

alter sequence public.categories_id_seq owner to postgres;

alter sequence public.categories_id_seq owned by public.categories.id;