package app.dissipate.services.audit;

import app.dissipate.data.models.AuditEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.arc.DefaultBean;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Default {@link AuditPublisher}: emits each event as a single structured JSON log line under a
 * dedicated category, so existing log shipping forwards audit events to the external sink. Replace
 * with a direct-to-SIEM/object-store implementation by providing another {@code AuditPublisher}
 * bean (this one is a {@link DefaultBean} and steps aside automatically).
 */
@ApplicationScoped
@DefaultBean
public class StructuredLogAuditPublisher implements AuditPublisher {

  /** Dedicated logger so audit lines can be routed/retained independently of app logs. */
  private static final Logger AUDIT_LOG = Logger.getLogger("audit");

  @Inject
  ObjectMapper objectMapper;

  @Override
  public Uni<Void> publish(List<AuditEvent> events) {
    for (AuditEvent event : events) {
      AUDIT_LOG.info(toJsonLine(event));
    }
    return Uni.createFrom().voidItem();
  }

  private String toJsonLine(AuditEvent e) {
    ObjectNode node = objectMapper.createObjectNode();
    node.put("id", base36(e.id));
    node.put("occurredAt", e.occurredAt != null ? e.occurredAt.toString() : null);
    node.put("eventType", e.eventType != null ? e.eventType.name() : null);
    node.put("outcome", e.outcome != null ? e.outcome.name() : null);
    node.put("actorAccountId", base36(e.actorAccountId));
    node.put("actorIdentityId", base36(e.actorIdentityId));
    node.put("sessionId", e.sessionId != null ? e.sessionId.toString() : null);
    node.put("targetType", e.targetType);
    node.put("targetId", base36(e.targetId));
    node.put("clientIp", e.clientIp);
    node.put("userAgent", e.userAgent);
    node.put("reason", e.reason);
    node.set("metadata", parseMetadata(e.metadata));

    try {
      return objectMapper.writeValueAsString(node);
    } catch (Exception ex) {
      // Never let a serialization hiccup drop an audit line entirely.
      return "{\"id\":\"" + base36(e.id) + "\",\"eventType\":\""
        + (e.eventType != null ? e.eventType.name() : "null") + "\",\"_serializationError\":\""
        + ex.getClass().getSimpleName() + "\"}";
    }
  }

  /** Parse stored metadata (jsonb) back into a JSON node; fall back to a raw string node. */
  private com.fasterxml.jackson.databind.JsonNode parseMetadata(String metadata) {
    if (metadata == null || metadata.isBlank()) {
      return objectMapper.nullNode();
    }
    try {
      return objectMapper.readTree(metadata);
    } catch (Exception ex) {
      return objectMapper.getNodeFactory().textNode(metadata);
    }
  }

  private static String base36(Long id) {
    return id == null ? null : Long.toString(id, 36);
  }
}
