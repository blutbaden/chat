package com.chatty.config;

import com.chatty.domain.Notification;
import com.chatty.domain.enumeration.NotificationType;
import com.chatty.domain.enumeration.UserState;
import com.chatty.web.websocket.UserSocketService;
import com.chatty.web.websocket.dto.UserSocketDTO;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    private final SimpMessagingTemplate template;
    private final UserSocketService userSocketService;

    public WebSocketEventListener(SimpMessagingTemplate template, UserSocketService userSocketService) {
        this.template = template;
        this.userSocketService = userSocketService;
    }

    private void handleSession(String username, String logText, UserState userState) {
        logger.info(logText);
        Notification notification = new Notification();
        notification.setTime(Instant.now());
        notification.setContent(logText);
        notification.setType(NotificationType.USER_STATE);
        notification.addToMetadata("USER", username);
        notification.addToMetadata("STATE", userState.name());
        // Notify everyone about user.
        template.convertAndSend("/topic/public", notification);
    }

    private void onUserSubscribe(String username) {
        Notification notification = new Notification();
        notification.setTime(Instant.now());
        notification.setType(NotificationType.ONLINE_USERS);
        List<UserSocketDTO> userSocketDTOS = userSocketService.getSubscribedSocketUsersByDestination("/topic/public");
        userSocketDTOS.removeIf(userState -> userState.getUsername().equals(username));
        notification.addToMetadata("USERS", String.join(", ", new Gson().toJson(userSocketDTOS)));
        template.convertAndSendToUser(username
            , "/queue/messages", notification);
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            String username = principal.getName();
            handleSession(username, "User «" + username + "» Connected", UserState.ONLINE);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            String username = principal.getName();
            handleSession(username, "User «" + username + "» Disconnected", UserState.OFFLINE);
            userSocketService.removeByUsername(username);
        }
    }

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            String user = principal.getName();
            GenericMessage message = (GenericMessage) event.getMessage();
            String simpDestination = (String) message.getHeaders().get("simpDestination");
            String destination = "/user/" + user + "/queue/messages";
            if (simpDestination.startsWith(destination)) {
                onUserSubscribe(principal.getName());
            }
        }
    }
}
