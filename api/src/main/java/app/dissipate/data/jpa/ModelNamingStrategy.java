package app.dissipate.data.jpa;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.jvnet.inflector.Noun;

public class ModelNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {

        final Identifier tableIdentifier = classToTableName(name, jdbcEnvironment);

        if (tableIdentifier == null) {
            return super.toPhysicalTableName(name, jdbcEnvironment);
        }
        final String tableName = tableIdentifier.getText();

        if (tableName.contains("_")) {
            final String lastWord = tableName.substring(tableName.lastIndexOf("_") + 1);
            final String prefix = tableName.substring(0, tableName.lastIndexOf("_"));
            final String combined = String.format("%s_%s", prefix, Noun.pluralOf(lastWord));
            return this.getIdentifier(Noun.pluralOf(combined), name.isQuoted(), jdbcEnvironment);
        } else {
            return this.getIdentifier(Noun.pluralOf(tableName), name.isQuoted(), jdbcEnvironment);
        }
    }

    public Identifier classToTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
      if (name == null) {
        return null;
      }
      String text = name.getText().replace('.', '_');
      StringBuilder builder = new StringBuilder(text);

      int i = 1;
      while (i < builder.length() - 1) {
        if (needsUnderscore(builder.charAt(i - 1), builder.charAt(i), builder.charAt(i + 1))) {
          builder.insert(i, '_');
          i += 2; // Skip the next character to avoid infinite loop
        } else {
          i++;
        }
      }

      return getIdentifier(builder.toString(), name.isQuoted(), jdbcEnvironment);
    }

    private boolean needsUnderscore(char before, char current, char after) {
        return Character.isLowerCase(before) && Character.isUpperCase(current) && Character.isLowerCase(after);
    }

}
