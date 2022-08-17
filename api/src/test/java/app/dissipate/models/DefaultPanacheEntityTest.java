package app.dissipate.models;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DefaultPanacheEntityTest {

    /**
     * Tests the toString() method of the DefaultPanacheEntity class.
     */
    @Test
    public void testToString() {
        DefaultPanacheEntity entity = new SimpleEntity();
        entity.id = 1L;
        Assertions.assertEquals("SimpleEntity<1>", entity.toString());
    }

    class SimpleEntity extends DefaultPanacheEntity {
    }
}
