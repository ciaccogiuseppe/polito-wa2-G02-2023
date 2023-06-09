create sequence public.tickets_history_id_seq;

alter sequence public.tickets_history_id_seq owner to postgres;

alter sequence public.tickets_history_id_seq owned by public.tickets_history.id;

