create sequence public.profiles_id_seq;

alter sequence public.profiles_id_seq owner to postgres;

alter sequence public.profiles_id_seq owned by public.profiles.id;

