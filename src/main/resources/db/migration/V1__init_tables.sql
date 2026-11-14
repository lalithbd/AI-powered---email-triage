create table if not exists email(
    id int8 not null,
    emailMessageId varchar(255) not null,
    threadId varchar(255),
    subject varchar(255),
    body text,
    sender varchar(255),
    attachmentCount int4,
    receivedAt timestamptz,
    fetchedAt timestamptz,
    status varchar(255),
    failedCount int4,
    constraint email_pkey primary key (id),
    constraint email_message_id_uq unique (emailMessageId)
);
create sequence if not exists email_seq;

create table if not exists classification(
    id int8 not null,
    category varchar(255) not null,
    reason text,
    draft text,
    classifiedAt timestamptz,
    modelId varchar(255),
    promptVersion varchar(255),
    email_id int8 not null,
    constraint classification_pkey primary key (id),
    constraint classification_email_uq unique (email_id),
    constraint classification_email_fkey foreign key (email_id) references email(id)
);
create sequence if not exists classification_seq;

create table if not exists feedback(
    id int8 not null,
    correctedCategory varchar(255),
    createdAt timestamptz,
    classification_id int8 not null,
    constraint feedback_pkey primary key (id),
    constraint feedback_classification_uq unique (classification_id),
    constraint feedback_classification_fkey foreign key (classification_id) references classification(id)
);
create sequence if not exists feedback_seq;
