package com.school.scheduling.websocket;

import com.school.scheduling.domain.ScheduleConflict;
import com.school.scheduling.dto.response.ConflictResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConflictWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("WebSocket connection established: {}", session.getId());

        // Send initial conflict count to new client
        sendInitialData(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("WebSocket connection closed: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            log.debug("Received message from client {}: {}", session.getId(), payload);

            // Handle client messages (e.g., subscription requests)
            WebSocketMessage clientMessage = objectMapper.readValue(payload, WebSocketMessage.class);

            switch (clientMessage.getType()) {
                case "subscribe":
                    handleSubscription(session, clientMessage);
                    break;
                case "ping":
                    sendPong(session);
                    break;
                default:
                    log.warn("Unknown message type: {}", clientMessage.getType());
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
        }
    }

    @Async
    public void broadcastConflictDetected(ScheduleConflict conflict) {
        ConflictResponse response = convertToResponse(conflict);
        WebSocketMessage message = WebSocketMessage.builder()
            .type("conflict_detected")
            .data(response)
            .timestamp(java.time.LocalDateTime.now())
            .build();

        broadcastMessage(message);
    }

    @Async
    public void broadcastConflictResolved(Long conflictId, String resolutionNotes) {
        WebSocketMessage message = WebSocketMessage.builder()
            .type("conflict_resolved")
            .data(new ConflictResolutionData(conflictId, resolutionNotes))
            .timestamp(java.time.LocalDateTime.now())
            .build();

        broadcastMessage(message);
    }

    @Async
    public void broadcastConflictStats(ConflictStatsData stats) {
        WebSocketMessage message = WebSocketMessage.builder()
            .type("conflict_stats")
            .data(stats)
            .timestamp(java.time.LocalDateTime.now())
            .build();

        broadcastMessage(message);
    }

    @Async
    public void broadcastSystemAlert(SystemAlertData alert) {
        WebSocketMessage message = WebSocketMessage.builder()
            .type("system_alert")
            .data(alert)
            .timestamp(java.time.LocalDateTime.now())
            .build();

        broadcastMessage(message);
    }

    private void broadcastMessage(WebSocketMessage message) {
        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error serializing WebSocket message", e);
            return;
        }

        sessions.forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonMessage));
                } catch (IOException e) {
                    log.error("Error sending WebSocket message to session {}", session.getId(), e);
                }
            }
        });
    }

    private void sendInitialData(WebSocketSession session) {
        try {
            // Send initial stats
            ConflictStatsData initialStats = ConflictStatsData.builder()
                .totalConflicts(0)
                .pendingConflicts(0)
                .criticalConflicts(0)
                .highSeverityConflicts(0)
                .lastUpdated(java.time.LocalDateTime.now())
                .build();

            WebSocketMessage initialMessage = WebSocketMessage.builder()
                .type("initial_data")
                .data(initialStats)
                .timestamp(java.time.LocalDateTime.now())
                .build();

            String jsonMessage = objectMapper.writeValueAsString(initialMessage);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (Exception e) {
            log.error("Error sending initial data to session {}", session.getId(), e);
        }
    }

    private void handleSubscription(WebSocketSession session, WebSocketMessage message) {
        log.info("Client {} subscribed to conflict updates", session.getId());

        // Send confirmation
        WebSocketMessage confirmation = WebSocketMessage.builder()
            .type("subscription_confirmed")
            .data("Subscribed to conflict updates")
            .timestamp(java.time.LocalDateTime.now())
            .build();

        sendMessage(session, confirmation);
    }

    private void sendPong(WebSocketSession session) {
        WebSocketMessage pong = WebSocketMessage.builder()
            .type("pong")
            .data("pong")
            .timestamp(java.time.LocalDateTime.now())
            .build();

        sendMessage(session, pong);
    }

    private void sendMessage(WebSocketSession session, WebSocketMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (Exception e) {
            log.error("Error sending message to session {}", session.getId(), e);
        }
    }

    private ConflictResponse convertToResponse(ScheduleConflict conflict) {
        ConflictResponse response = new ConflictResponse();
        response.setId(conflict.getId());
        response.setConflictType(conflict.getConflictType().name());
        response.setSeverity(conflict.getSeverity().name());
        response.setDescription(conflict.getDescription());
        response.setResolutionStatus(conflict.getResolutionStatus().name());
        response.setDetectedAt(conflict.getDetectedAt());
        response.setRequiresImmediateAttention(conflict.requiresImmediateAttention());
        response.setHoursSinceDetection(conflict.getHoursSinceDetection());
        response.setConflictSummary(conflict.getConflictSummary());
        return response;
    }

    public int getActiveConnections() {
        return sessions.size();
    }

    public void sendHeartbeat() {
        WebSocketMessage heartbeat = WebSocketMessage.builder()
            .type("heartbeat")
            .data("heartbeat")
            .timestamp(java.time.LocalDateTime.now())
            .build();

        broadcastMessage(heartbeat);
    }

    // Inner classes for WebSocket message structure
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WebSocketMessage {
        private String type;
        private Object data;
        private java.time.LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ConflictStatsData {
        private int totalConflicts;
        private int pendingConflicts;
        private int criticalConflicts;
        private int highSeverityConflicts;
        private int mediumSeverityConflicts;
        private int lowSeverityConflicts;
        private java.time.LocalDateTime lastUpdated;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ConflictResolutionData {
        private Long conflictId;
        private String resolutionNotes;
        private java.time.LocalDateTime resolvedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SystemAlertData {
        private String level; // "info", "warning", "error", "critical"
        private String title;
        private String message;
        private String entityType;
        private Long entityId;
        private java.time.LocalDateTime timestamp;
    }
}