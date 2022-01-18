package com.chatty.service.mapper;

import com.chatty.domain.Group;
import com.chatty.service.dto.GroupDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Group} and its DTO {@link GroupDTO}.
 */
@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface GroupMapper extends EntityMapper<GroupDTO, Group> {
    @Mapping(target = "users", source = "users", qualifiedByName = "loginSet")
    GroupDTO toDto(Group s);

    @Named("id")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    GroupDTO toDtoId(Group group);

    @Mapping(target = "removeUsers", ignore = true)
    Group toEntity(GroupDTO groupDTO);
}
