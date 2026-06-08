package app.dissipate.api.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Standard error envelope returned by every REST endpoint on failure.
 *
 * @param code    stable machine-readable error code (an i18n key, e.g. {@code error.auth.required})
 * @param message human-readable message, localized to the request's {@code Accept-Language}
 * @param traceId OpenTelemetry trace id for support correlation, or {@code null} when no span is active
 */
@Schema(name = "ApiError", description = "Standard error payload returned by REST endpoints on failure")
public record ApiErrorResponse(
  @Schema(description = "Stable machine-readable error code", example = "error.auth.required")
  String code,

  @Schema(description = "Human-readable message, localized to the request locale")
  String message,

  @Schema(description = "OpenTelemetry trace id for support correlation", nullable = true)
  String traceId
) {
}
