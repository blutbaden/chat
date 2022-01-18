package com.chatty.service.mapper;

import com.chatty.domain.Room;
import com.chatty.service.dto.RoomDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Room} and its DTO {@link RoomDTO}.
 */
@Mapper(componentModel = "spring", uses = { UserMapper.class, GroupMapper.class })
public interface RoomMapper extends EntityMapper<RoomDTO, Room> {
    @Mapping(target = "users", source = "users", qualifiedByName = "loginSet")
    @Mapping(target = "group", source = "group", qualifiedByName = "id")
    RoomDTO toDto(Room s);

    @Named("id")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    RoomDTO toDtoId(Room room);

    @Mapping(target = "removeUser", ignore = true)
    Room toEntity(RoomDTO roomDTO);
}
