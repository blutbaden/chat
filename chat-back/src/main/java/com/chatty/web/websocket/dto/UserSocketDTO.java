package com.chatty.web.websocket.dto;

import com.chatty.domain.enumeration.UserState;

public class UserSocketDTO {
    private String username;
    private UserState state;

    public UserSocketDTO(String username, UserState state) {
        this.username = username;
        this.state = state;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState state) {
        this.state = state;
    }
}
