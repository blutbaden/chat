package com.chatty.web.websocket;

import com.chatty.domain.Notification;
import com.chatty.domain.enumeration.NotificationType;
import com.chatty.domain.enumeration.UserState;
import com.chatty.service.ConversationService;
import com.chatty.service.RoomService;
import com.chatty.service.dto.GroupDTO;
import com.chatty.service.dto.RoomDTO;
import com.chatty.service.dto.UserDTO;
import com.chatty.web.websocket.dto.NotificationDTO;
import com.chatty.web.websocket.dto.UserSocketDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.hibernate.boot.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.*;

@Controller
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final SimpMessageSendingOperations messagingTemplate;
    private final RoomService roomService;
    private final ConversationService conversationService;
    private final UserSocketService userSocketService;

    public ChatService(SimpMessageSendingOperations messagingTemplate, RoomService roomService, ConversationService conversationService, UserSocketService userSocketService) {
        this.messagingTemplate = messagingTemplate;
        this.roomService = roomService;
        this.conversationService = conversationService;
        this.userSocketService = userSocketService;
    }

    @MessageMapping("/online-users")
    public void getOnlineUsers(Principal principal) {
        Notification notification = new Notification();
        notification.setTime(Instant.now());
        notification.setType(NotificationType.ONLINE_USERS);
        List<UserSocketDTO> userSocketDTOS = userSocketService.getSubscribedSocketUsersByDestination("/topic/public");
        userSocketDTOS.removeIf(userState -> userState.getUsername().equals(principal.getName()));
        notification.addToMetadata("USERS", String.join(", ", new Gson().toJson(userSocketDTOS)));
        messagingTemplate.convertAndSendToUser(principal.getName()
            , "/queue/messages", notification);
    }

    @MessageMapping("/update-user-state")
    public void updateUserState(@Payload String state, Principal principal) {
        UserState userState = UserState.valueOf(state);
        Notification notification = new Notification();
        notification.setTime(Instant.now());
        notification.setType(NotificationType.USER_STATE);
        notification.addToMetadata("USER", principal.getName());
        notification.addToMetadata("STATE", userState.name());
        userSocketService.updateStatus(principal.getName(), userState);
        sendToSubscribers("/topic/public", "/queue/messages", principal.getName(), notification);
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload NotificationDTO notificationDTO, Principal principal) {
        Notification notification = new Notification();
        notification.setTime(Instant.now());
        notification.setType(notificationDTO.getType());
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> metadata = new HashMap<>();
        String loggedUser = principal.getName();
        try {
            metadata = mapper.readValue(notificationDTO.getMetadata(), Map.class);
            notification.addToMetadata("ROOM", metadata.get("ROOM"));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        switch (notificationDTO.getType()) {
            case INCOMING_CALL:
                notification.setContent(principal.getName() + " is calling you!");
                sendToUser(loggedUser, notification, false);
                break;
            case ACCEPTED_CALL:
                notification.setContent(principal.getName() + " accepted your call!");
                sendToUser(loggedUser, notification, false);
                break;
            case CANCELLED_CALL:
                notification.setContent(principal.getName() + " cancelled the call!");
                sendToUser(loggedUser, notification, false);
                break;
            case REJECTED_CALL:
                notification.setContent(principal.getName() + " rejected your call!");
                sendToUser(loggedUser, notification, false);
                break;
            case INCOMING_MESSAGE:
                notification.setContent("New message from " + loggedUser);
                String message = metadata.get("MESSAGE");
                notification.addToMetadata("MESSAGE", message);
                notification.addToMetadata("USER", loggedUser);
                sendToUser(loggedUser, notification, true);
                break;
        }
    }

    private Set<UserDTO> getUsersInSameRoom(Long roomId) {
        Optional<RoomDTO> optionalRoomDTO = roomService.findOne(roomId);
        if (optionalRoomDTO.isPresent()) {
            RoomDTO roomDTO = optionalRoomDTO.get();
            return roomDTO.getUsers();
        }
        return new HashSet<>();
    }

    private void sendToUser(String sender, Notification notification, boolean saveMessage) {
        Map<String, String > metadata = notification.getMetadata();
        String roomIdStr = metadata.get("ROOM");
        if (roomIdStr != null) {
            Long roomIdLong = Long.parseLong(roomIdStr);
            Set<UserDTO> userDTOS = getUsersInSameRoom(roomIdLong);
            userDTOS.stream()
                .map(UserDTO::getLogin)
                .filter(s -> !s.equals(sender))
                .forEach(user -> {
                    notification.addToMetadata("USER", new Gson().toJson(
                        new UserDTO(null, sender)
                    ));
                    messagingTemplate.convertAndSendToUser(user, "/queue/messages", notification);
                    if (saveMessage) {
                        String message = metadata.get("MESSAGE");
                        this.conversationService.save(message, roomIdLong, sender, user);
                    }
                });
        }
    }

    private void sendToSubscribers(String subscribersDestination, String destination, String loggedUser, Object payload) {
        userSocketService.getSubscribedSocketUsersByDestination(subscribersDestination)
            .stream()
            .map(UserSocketDTO::getUsername)
            .filter(name -> !loggedUser.equals(name))
            .forEach(subscriber -> messagingTemplate.convertAndSendToUser(subscriber, destination, payload));
    }

}
