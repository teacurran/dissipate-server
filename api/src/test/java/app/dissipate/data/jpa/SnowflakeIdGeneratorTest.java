package app.dissipate.data.jpa;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class SnowflakeIdGeneratorTest {
  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @Test
  public void testGenerateUniqueIds() throws HibernateException {
    String id1 = snowflakeIdGenerator.generate("test");
    String id2 = snowflakeIdGenerator.generate("test");
    assertNotNull(id1);
    assertNotNull(id2);
    assertNotEquals(id1, id2);
  }

  @Test
  public void testGenerateIdsInQuickSuccession() throws HibernateException {
    String id1 = snowflakeIdGenerator.generate("test");
    String id2 = snowflakeIdGenerator.generate("test");
    String id3 = snowflakeIdGenerator.generate("test");
    assertNotNull(id1);
    assertNotNull(id2);
    assertNotNull(id3);
    assertNotEquals(id1, id2);
    assertNotEquals(id2, id3);
    assertNotEquals(id1, id3);
  }

  @Test
  public void testSystemClockMovesBackward() {
    SnowflakeIdGenerator.Snowflake snowflake = new SnowflakeIdGenerator.Snowflake(1, 1, 1);
    snowflake.nextId();
    // Simulate system clock moving backward
    snowflake.setLastTimestamp(snowflake.lastTimestamp.get() + 1000);
    assertThrows(IllegalStateException.class, snowflake::nextId);
  }
}
