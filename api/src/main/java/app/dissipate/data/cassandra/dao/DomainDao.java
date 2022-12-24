package app.dissipate.data.cassandra.dao;

import app.dissipate.data.cassandra.models.Domain;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;

@Dao
public interface DomainDao {

    @Select
    Domain findByDomain(String domain);

    @Insert
    void save(Domain domain);

    @Update
    void update(Domain domain);

}
