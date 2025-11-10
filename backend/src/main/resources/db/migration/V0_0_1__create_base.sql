

create table user
(
    id            bigint auto_increment
        primary key,
    email         varchar(255) null,
    token_subject varchar(255) null
);

create table album
(
    id          bigint auto_increment
        primary key,
    user_id     bigint       null,
    description varchar(255) null,
    secret_id   varchar(255) not null,
    constraint FKmi5m81x9aswan1ci0wnw04dq1
        foreign key (user_id) references user (id)
);

create table album_element
(
    latitude      double                           null,
    longitude     double                           null,
    album_id      bigint                           null,
    creation_date datetime(6)                      null,
    id            bigint auto_increment
        primary key,
    order_no      bigint                           not null,
    description   varchar(255)                     null,
    element_type  enum ('IMAGE', 'MAP', 'SECTION') not null,
    filename      varchar(255)                     null,
    content_hash      varchar(255)                     null,
    secret_id     varchar(255)                     not null,
    constraint FK9owvvliln4eq11g5nhqxi0jy1
        foreign key (album_id) references album (id)
);
