package app.dissipate.data.jpa;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class ModelNamingStrategyTest {

  private final ModelNamingStrategy namingStrategy = new ModelNamingStrategy();
  private final JdbcEnvironment jdbcEnvironment = mock(JdbcEnvironment.class);

  @Test
  void classToTableName_NullInput() {
    assertNull(convertClassToTableName(null));
  }

  @Test
  void classToTableName_SimpleCamelCase() {
    assertEquals("simple_class_name", convertClassToTableName("SimpleClassName"));
  }

  @Test
  void classToTableName_ComplexCamelCase() {
    assertEquals("complex_class_name_with_multiple_parts", convertClassToTableName("ComplexClassNameWithMultipleParts"));
  }

  @Test
  void classToTableName_InputWithDots() {
    assertEquals("com_example_class_name", convertClassToTableName("com.example.ClassName"));
  }

  private String convertClassToTableName(String className) {
    Identifier input = className == null ? null : Identifier.toIdentifier(className);
    Identifier result = namingStrategy.classToTableName(input, jdbcEnvironment);
    return result == null ? null : result.getText();
  }
}
