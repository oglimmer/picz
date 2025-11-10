alter table album_element add column entry_last_update_date datetime(6) default current_timestamp(6) not null on update current_timestamp(6);
alter table album_element add column entry_creation_date    datetime(6) default current_timestamp(6) not null;

alter table user add column last_update_date datetime(6) default current_timestamp(6) not null on update current_timestamp(6);
alter table user add column creation_date    datetime(6) default current_timestamp(6) not null;

alter table album add column last_update_date   datetime(6) default current_timestamp(6) not null on update current_timestamp(6);
alter table album add column creation_date      datetime(6) default current_timestamp(6) not null;
