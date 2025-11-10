create table queue_count_stats
(
    id               bigint auto_increment
        primary key,
    album_id         bigint not null,
    user_id          bigint not null,
    processing_count int    not null default 0,
    constraint uk_queue_count_stats_album_user
        unique (album_id, user_id),
    constraint fk_queue_count_stats_album
        foreign key (album_id) references album (id) on delete cascade,
    constraint fk_queue_count_stats_user
        foreign key (user_id) references user (id) on delete cascade
);