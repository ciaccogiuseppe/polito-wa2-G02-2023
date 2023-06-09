create sequence public.messages_id_seq;

alter sequence public.messages_id_seq owner to postgres;

alter sequence public.messages_id_seq owned by public.messages.id;

