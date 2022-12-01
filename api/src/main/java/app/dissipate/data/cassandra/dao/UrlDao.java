package app.dissipate.data.cassandra.dao;

import app.dissipate.data.cassandra.models.Url;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;
import com.datastax.oss.quarkus.runtime.api.reactive.mapper.MutinyMappedReactiveResultSet;
import io.smallrye.mutiny.Uni;

@Dao
public interface UrlDao {

    @Update
    Uni<Void> updateAsync(Url url);

    @Select
    MutinyMappedReactiveResultSet<Url> findAll();

    @Select
    MutinyMappedReactiveResultSet<Url> findByUrl(String url);

}
