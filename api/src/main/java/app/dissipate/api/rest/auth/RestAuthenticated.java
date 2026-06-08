package app.dissipate.api.rest.auth;

import jakarta.ws.rs.NameBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a REST resource (class or method) as requiring a valid session. Triggers
 * {@link RestAuthenticationFilter}, which rejects requests lacking a Bearer token and stashes the
 * token for {@link CurrentSession#require()}.
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RestAuthenticated {
}
