package com.chatty.service;

import com.chatty.domain.Group;
import com.chatty.domain.User;
import com.chatty.repository.GroupRepository;
import com.chatty.service.dto.GroupDTO;
import com.chatty.service.dto.UserDTO;
import com.chatty.service.mapper.GroupMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Group}.
 */
@Service
@Transactional
public class GroupService {

    private final Logger log = LoggerFactory.getLogger(GroupService.class);

    private final GroupRepository groupRepository;

    private final UserService userService;

    private final GroupMapper groupMapper;

    public GroupService(GroupRepository groupRepository, UserService userService, GroupMapper groupMapper) {
        this.groupRepository = groupRepository;
        this.userService = userService;
        this.groupMapper = groupMapper;
    }

    /**
     * Save a group.
     *
     * @param groupDTO the entity to save.
     * @return the persisted entity.
     */
    public GroupDTO save(GroupDTO groupDTO) {
        log.debug("Request to save Group : {}", groupDTO);
        Group group = groupMapper.toEntity(groupDTO);
        group = groupRepository.save(group);
        return groupMapper.toDto(group);
    }

    /**
     * Partially update a group.
     *
     * @param groupDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<GroupDTO> partialUpdate(GroupDTO groupDTO) {
        log.debug("Request to partially update Group : {}", groupDTO);

        return groupRepository
            .findById(groupDTO.getId())
            .map(existingGroup -> {
                groupMapper.partialUpdate(existingGroup, groupDTO);

                return existingGroup;
            })
            .map(groupRepository::save)
            .map(groupMapper::toDto);
    }

    /**
     * Get all the groups.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<GroupDTO> findAll() {
        log.debug("Request to get all Groups");
        return groupRepository
            .findAllWithEagerRelationships()
            .stream()
            .map(groupMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get all the groups with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<GroupDTO> findAllWithEagerRelationships(Pageable pageable) {
        return groupRepository.findAllWithEagerRelationships(pageable).map(groupMapper::toDto);
    }

    /**
     * Get all Groups joined by user.
     *
     * @return the list of entities.
     */
    public List<GroupDTO> findGroupsByLoggedUser() {
        Optional<User> optionalUser = userService.getUserWithAuthorities();
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return groupRepository.findAllByUsersIsContaining(user)
                .stream()
                .map(groupMapper::toDto)
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Get all distinct users joined to same groups as logged user.
     *
     * @return the list of entities.
     */
    public List<UserDTO> getUsersJoinedToSameGroupsAsLoggedUser() {
        Optional<User> optionalUser = userService.getUserWithAuthorities();
        if (optionalUser.isPresent()) {
            User loggedUser = optionalUser.get();
            UserDTO userDTO = new UserDTO();
            return groupRepository.findAllByUsersIsContaining(loggedUser)
                .stream()
                .map(Group::getUsers)
                .flatMap(Collection::stream)
                .filter(users -> !users.getLogin().equals(loggedUser.getLogin()))
                .map(user -> {
                    userDTO.setId(user.getId());
                    userDTO.setLogin(user.getLogin());
                    return userDTO;
                })
                .distinct()
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Get users by group.
     *
     * @return the list of entities.
     */
    public Set<UserDTO> getUsersByGroup(Long id) {
        Optional<GroupDTO> optionalGroupDTO = findOne(id);
        if(optionalGroupDTO.isPresent()) {
            GroupDTO groupDTO = optionalGroupDTO.get();
            return groupDTO.getUsers();
        }
        return new HashSet<>();
    }

    /**
     * Get one group by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<GroupDTO> findOne(Long id) {
        log.debug("Request to get Group : {}", id);
        return groupRepository.findOneWithEagerRelationships(id).map(groupMapper::toDto);
    }

    /**
     * Delete the group by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Group : {}", id);
        groupRepository.deleteById(id);
    }
}
