package app.dissipate.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for Snowflake-typed REST parameters (path/query) so that
 * {@link SnowflakeIdParamConverterProvider} can convert their base-36
 * representation to {@link Long}.
 *
 * <p>Usage:
 * <pre>{@code
 * public Uni<Response> get(@PathParam("chatId") @SnowflakeId Long chatId) { ... }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
public @interface SnowflakeId {
}
