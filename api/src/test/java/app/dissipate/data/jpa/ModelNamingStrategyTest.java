package app.dissipate.data.jpa;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

@QuarkusTest
class ModelNamingStrategyTest {

  private final ModelNamingStrategy namingStrategy = new ModelNamingStrategy();
  private final JdbcEnvironment jdbcEnvironment = mock(JdbcEnvironment.class);

  @Test
  void classToTableName_NullInput() {
    Identifier result = namingStrategy.classToTableName(null, jdbcEnvironment);
    assertNull(result);
  }

  @Test
  void classToTableName_SimpleCamelCase() {
    Identifier input = Identifier.toIdentifier("SimpleClassName");
    Identifier result = namingStrategy.classToTableName(input, jdbcEnvironment);
    assertEquals("simple_class_name", result.getText());
  }

  @Test
  void classToTableName_ComplexCamelCase() {
    Identifier input = Identifier.toIdentifier("ComplexClassNameWithMultipleParts");
    Identifier result = namingStrategy.classToTableName(input, jdbcEnvironment);
    assertEquals("complex_class_name_with_multiple_parts", result.getText());
  }

  @Test
  void classToTableName_InputWithDots() {
    Identifier input = Identifier.toIdentifier("com.example.ClassName");
    Identifier result = namingStrategy.classToTableName(input, jdbcEnvironment);
    assertEquals("com_example_class_name", result.getText());
  }
}
