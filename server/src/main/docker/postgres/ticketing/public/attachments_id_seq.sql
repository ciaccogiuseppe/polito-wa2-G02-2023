create sequence public.attachments_id_seq;

alter sequence public.attachments_id_seq owner to postgres;

alter sequence public.attachments_id_seq owned by public.attachments.id;

