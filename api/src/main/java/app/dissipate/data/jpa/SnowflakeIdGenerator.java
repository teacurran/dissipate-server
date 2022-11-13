package app.dissipate.data.jpa;

import app.dissipate.constants.ApplicationConstants;
import app.dissipate.services.ServerInstance;
import com.callicoder.snowflake.Snowflake;
import org.hibernate.HibernateException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigInteger;
import java.util.Hashtable;

@ApplicationScoped
public class SnowflakeIdGenerator {

    @Inject
    ServerInstance serverInstance;

    Hashtable<String, Snowflake> snowflakes = new Hashtable<>();

    public BigInteger generate(String idName) throws HibernateException {
        Snowflake snowflake;
        if (snowflakes.containsKey(idName)) {
            snowflake = snowflakes.get(idName);
        } else {
            snowflake = new Snowflake(serverInstance.getServer().instanceNumber, ApplicationConstants.APP_EPOCH);
            snowflakes.put(idName, snowflake);
        }
        return BigInteger.valueOf(snowflake.nextId());
    }
}
