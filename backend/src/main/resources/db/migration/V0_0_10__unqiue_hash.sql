alter table album_element add constraint uq_album_element_hash unique (album_id, content_hash);
