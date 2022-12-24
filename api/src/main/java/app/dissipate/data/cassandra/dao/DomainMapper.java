package app.dissipate.data.cassandra.dao;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface DomainMapper {

    @DaoFactory
    DomainDao domainDao();
}
