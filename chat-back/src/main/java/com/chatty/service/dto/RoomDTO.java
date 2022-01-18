package com.chatty.service.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link com.chatty.domain.Room} entity.
 */
public class RoomDTO implements Serializable {

    private Long id;

    private String name;

    private Boolean isActivated;

    private Boolean allowImageMessage;

    private Boolean allowVoiceMessage;

    private Boolean allowStickerMessage;

    private Set<UserDTO> users = new HashSet<>();

    private GroupDTO group;

    private String createdBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsActivated() {
        return isActivated;
    }

    public void setIsActivated(Boolean isActivated) {
        this.isActivated = isActivated;
    }

    public Boolean getAllowImageMessage() {
        return allowImageMessage;
    }

    public void setAllowImageMessage(Boolean allowImageMessage) {
        this.allowImageMessage = allowImageMessage;
    }

    public Boolean getAllowVoiceMessage() {
        return allowVoiceMessage;
    }

    public void setAllowVoiceMessage(Boolean allowVoiceMessage) {
        this.allowVoiceMessage = allowVoiceMessage;
    }

    public Boolean getAllowStickerMessage() {
        return allowStickerMessage;
    }

    public void setAllowStickerMessage(Boolean allowStickerMessage) {
        this.allowStickerMessage = allowStickerMessage;
    }

    public Set<UserDTO> getUsers() {
        return users;
    }

    public void setUsers(Set<UserDTO> users) {
        this.users = users;
    }

    public GroupDTO getGroup() {
        return group;
    }

    public void setGroup(GroupDTO group) {
        this.group = group;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RoomDTO)) {
            return false;
        }

        RoomDTO roomDTO = (RoomDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, roomDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RoomDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", isActivated='" + getIsActivated() + "'" +
            ", allowImageMessage='" + getAllowImageMessage() + "'" +
            ", allowVoiceMessage='" + getAllowVoiceMessage() + "'" +
            ", allowStickerMessage='" + getAllowStickerMessage() + "'" +
            ", users=" + getUsers() +
            ", group=" + getGroup() +
            "}";
    }
}
