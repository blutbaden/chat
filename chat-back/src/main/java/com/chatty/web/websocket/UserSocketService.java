package com.chatty.web.websocket;

import com.chatty.domain.Notification;
import com.chatty.domain.enumeration.UserState;
import com.chatty.web.websocket.dto.UserSocketDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserSocketService {

    private final Set<UserSocketDTO> userSocketDTOS = new HashSet<>();

    @Autowired
    SimpUserRegistry userRegistry;

    public UserSocketDTO findUserSocketByUsername(String username) {
        return
            userSocketDTOS.stream()
                .filter(userState -> userState.getUsername().equals(username))
                .findAny()
                .orElse(null);
    }

    public void updateStatus(String username, UserState userState) {
        UserSocketDTO userSocketDTO = findUserSocketByUsername(username);
        if (userSocketDTO != null) {
            userSocketDTO.setState(userState);
        } else {
            userSocketDTOS.add(
                new UserSocketDTO(username, userState)
            );
        }
    }

    public void removeByUsername(String username) {
        userSocketDTOS.removeIf(userSocketDTO -> userSocketDTO.getUsername().equals(username));
    }

    public List<UserSocketDTO> getSubscribedSocketUsersByDestination(String destination) {
        return userRegistry.findSubscriptions(subscription -> subscription.getDestination().equals(destination))
            .stream()
            .map(simpSubscription -> {
                SimpSession simpSession = simpSubscription.getSession();
                SimpUser simpUser = simpSession.getUser();
                UserSocketDTO userSocketDTO = findUserSocketByUsername(simpUser.getName());
                if (userSocketDTO != null) {
                    return userSocketDTO;
                }
                return new UserSocketDTO(simpUser.getName(), UserState.ONLINE);
            })
            .collect(Collectors.toList());
    }
}
