package com.chatty.service.mapper;

import com.chatty.domain.Conversation;
import com.chatty.service.dto.ConversationDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Conversation} and its DTO {@link ConversationDTO}.
 */
@Mapper(componentModel = "spring", uses = { RoomMapper.class, UserMapper.class })
public interface ConversationMapper extends EntityMapper<ConversationDTO, Conversation> {
    @Mapping(target = "room", source = "room", qualifiedByName = "id")
    @Mapping(target = "sender", source = "sender", qualifiedByName = "login")
    @Mapping(target = "receiver", source = "receiver", qualifiedByName = "login")
    ConversationDTO toDto(Conversation s);

    @Named("id")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ConversationDTO toDtoId(Conversation conversation);
}
