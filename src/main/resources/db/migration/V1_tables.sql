create table if not exists email(
    id int8 not null,
    emailMessageId varchar(255) not null,
    threadId varchar(255),
    subject varchar(255),
    body varchar(1000),
    sender varchar(255),
    attachmentCount int8,
    receivedAt timestamptz,
    fetchedAt timestamptz,
    status varchar(255),
    failedCount int8,
    constraint email_pkey primary key (id),
    constraint emailMessageId unique (emailMessageId)
);
create table if not exists classification(
    id int8 not null,
    category varchar(255) not null,
    reason varchar(255) not null,
    draft varchar(255) not null,
    classifiedAt timestamptz,
    modelId varchar(255),
    promptVersion varchar(255),
    email_id int8 not null,
    constraint classification_pkey primary key (id),
    constraint email_fkey foreign key (email_id)
);
create table if not exists feedbackCategory(
    id int8 not null,
    category varchar(255),
    createdAt timestamptz,
    email_id int8 not null,
    constraint feedback_pkey primary key (id),
    constraint classification_fkey foreign key (email_id)
)

