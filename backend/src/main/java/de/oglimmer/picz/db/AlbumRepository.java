package de.oglimmer.picz.db;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends CrudRepository<Album, Long> {

    Optional<Album> findBySecretId(String secretId);

    Iterable<Album> findByUserTokenSubject(String tokenSubject);

    @Query("select ae.album.id as id, count(ae) as count from AlbumElement ae join ae.album a where a.user.id = :userId and ae.elementType = :elementType group by ae.album.id")
    List<CountElementByAlbum> findCountElementByAlbum(long userId, AlbumElementType elementType);
}
