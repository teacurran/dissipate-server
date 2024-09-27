package app.dissipate.controllers;

import app.dissipate.api.grpc.RegisterMethod;
import app.dissipate.beans.DissipateRenardeUser;
import app.dissipate.grpc.RegisterRequest;
import app.dissipate.grpc.RegisterResponse;
import io.quarkiverse.renarde.security.ControllerWithUser;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.NewCookie;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import org.hibernate.validator.constraints.Length;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkiverse.renarde.router.Router;
import io.quarkiverse.renarde.security.ControllerWithUser;
import io.quarkiverse.renarde.security.RenardeSecurity;
import io.quarkiverse.renarde.util.StringUtils;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.webauthn.WebAuthnLoginResponse;
import io.quarkus.security.webauthn.WebAuthnRegisterResponse;
import io.quarkus.security.webauthn.WebAuthnSecurity;
import io.smallrye.common.annotation.Blocking;
import io.vertx.ext.auth.webauthn.Authenticator;
import io.vertx.ext.web.RoutingContext;


public class Login extends ControllerWithUser<DissipateRenardeUser> {
  @Inject
  RenardeSecurity security;

  @Inject
  WebAuthnSecurity webAuthnSecurity;

  @Inject
  RegisterMethod registerMethod;

  @CheckedTemplate
  static class Templates {
    public static native TemplateInstance confirm(RegisterResponse registerResponse);
  }

  /**
   * Manual registration form, sends confirmation email
   */
  @POST
  public Uni<TemplateInstance> register(@RestForm @NotBlank @Email String email) {
    return registerMethod.register(RegisterRequest.newBuilder().setEmail(email).build()).onItem().transform(registerResponse -> {
        return Templates.confirm(registerResponse);
    });
  }
}
