package app.dissipate.data.jpa;

import app.dissipate.constants.ApplicationConstants;
import app.dissipate.services.ServerInstance;
import com.callicoder.snowflake.Snowflake;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Properties;

public class SnowflakeIdGenerator implements IdentifierGenerator {

    @Inject
    ServerInstance serverInstance;

    Snowflake snowflake;

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        IdentifierGenerator.super.configure(type, params, serviceRegistry);
    }

    @Override
    public void registerExportables(Database database) {
        IdentifierGenerator.super.registerExportables(database);
    }

    @Override
    public void initialize(SqlStringGenerationContext context) {
        snowflake = new Snowflake(serverInstance.getServer().instanceNumber, ApplicationConstants.APP_EPOCH);
        IdentifierGenerator.super.initialize(context);
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        return snowflake.nextId();
    }

    @Override
    public boolean supportsJdbcBatchInserts() {
        return IdentifierGenerator.super.supportsJdbcBatchInserts();
    }
}
