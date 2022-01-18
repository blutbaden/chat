package com.chatty.service;

import com.chatty.domain.Group;
import com.chatty.domain.Room;
import com.chatty.domain.User;
import com.chatty.repository.GroupRepository;
import com.chatty.repository.RoomRepository;
import com.chatty.service.dto.GroupDTO;
import com.chatty.service.dto.RoomDTO;
import com.chatty.service.mapper.RoomMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Room}.
 */
@Service
@Transactional
public class RoomService {

    private final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;

    private final GroupRepository groupRepository;

    private final RoomMapper roomMapper;

    private final UserService userService;

    public RoomService(RoomRepository roomRepository, GroupRepository groupRepository, RoomMapper roomMapper, UserService userService) {
        this.roomRepository = roomRepository;
        this.groupRepository = groupRepository;
        this.roomMapper = roomMapper;
        this.userService = userService;
    }

    /**
     * Save a room.
     *
     * @param roomDTO the entity to save.
     * @return the persisted entity.
     */
    public RoomDTO save(RoomDTO roomDTO) {
        log.debug("Request to save Room : {}", roomDTO);
        Room room = roomMapper.toEntity(roomDTO);
        room = roomRepository.save(room);
        return roomMapper.toDto(room);
    }

    /**
     * Partially update a room.
     *
     * @param roomDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<RoomDTO> partialUpdate(RoomDTO roomDTO) {
        log.debug("Request to partially update Room : {}", roomDTO);

        return roomRepository
            .findById(roomDTO.getId())
            .map(existingRoom -> {
                roomMapper.partialUpdate(existingRoom, roomDTO);

                return existingRoom;
            })
            .map(roomRepository::save)
            .map(roomMapper::toDto);
    }

    /**
     * Get all the rooms.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<RoomDTO> findAll() {
        log.debug("Request to get all Rooms");
        return roomRepository
            .findAllWithEagerRelationships()
            .stream()
            .map(roomMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get all the rooms with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<RoomDTO> findAllWithEagerRelationships(Pageable pageable) {
        return roomRepository.findAllWithEagerRelationships(pageable).map(roomMapper::toDto);
    }

    /**
     * Get all rooms of group.
     *
     * @return the list of entities.
     */
    public List<RoomDTO> findAllByGroup(Long idGroup) {
        Optional<Group> optionalGroup = groupRepository.findById(idGroup);
        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();
            return roomRepository.findAllByGroup(group)
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Get all rooms of a group joined by the logged user.
     *
     * @return the list of entities.
     */
    public List<RoomDTO> findAllOfGroupJoinedByLoggedUser(Long idGroup) {
        Optional<Group> optionalGroup = groupRepository.findById(idGroup);
        Optional<User> optionalUser = userService.getUserWithAuthorities();
        if (optionalUser.isPresent() && optionalGroup.isPresent()) {
            User user = optionalUser.get();
            Group group = optionalGroup.get();
            return roomRepository.findAllByUsersIsContainingAndGroup(user, group)
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Get one room by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<RoomDTO> findOne(Long id) {
        log.debug("Request to get Room : {}", id);
        return roomRepository.findOneWithEagerRelationships(id).map(roomMapper::toDto);
    }

    /**
     * Get one room by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Room> findById(Long id) {
        log.debug("Request to get Room : {}", id);
        return roomRepository.findById(id);
    }

    /**
     * Get all Rooms joined by Logged User.
     *
     * @return the list of entities.
     */
    public List<RoomDTO> findAllByLoggedUser() {
        Optional<User> optionalUser = userService.getUserWithAuthorities();
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return roomRepository.findAllByUsersIsContaining(user)
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }


    /**
     * Get all Public Rooms joined by Logged User.
     *
     * @return the list of entities.
     */
    public List<RoomDTO> findAllByLoggedUserAndGroupNotNull() {
        Optional<User> optionalUser = userService.getUserWithAuthorities();
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return roomRepository.findAllByUsersIsContainingAndGroupNotNull(user)
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Get Private Room by Logged User And Selected User.
     *
     * @param id the id of the selected user.
     * @return the entity.
     */
    public RoomDTO getPrivateRoomByLoggedUserAndSelectedUser(Long id) {
        Set<User> users = new HashSet<>();
        Optional<User> optionalLoggedUser = userService.getUserWithAuthorities();
        Optional<User> optionalSelectedUser = userService.findUserById(id);
        if (optionalLoggedUser.isPresent() && optionalSelectedUser.isPresent()) {
            User loggedUser = optionalLoggedUser.get();
            User selectedUser = optionalSelectedUser.get();
            boolean isSameUser = loggedUser.equals(selectedUser);
            if (isSameUser) {
                return new RoomDTO();
            }
            users.addAll(Arrays.asList(loggedUser, selectedUser));
            Optional<Room> optionalRoom = this.roomRepository.findFirstByUsersInAndGroupIsNull(users);
            Room room;
            if (optionalRoom.isPresent()) {
                room = optionalRoom.get();
            } else {
                // create a private room for 2 users
                room = new Room();
                room.setIsActivated(true);
                room.setUsers(users);
                room = roomRepository.save(room);
            }
            return roomMapper.toDto(room);
        }
        return new RoomDTO();
    }

    /**
     * Delete the room by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Room : {}", id);
        roomRepository.deleteById(id);
    }

    /**
     * Create First Room in Group.
     *
     * @param roomDTO the entity.
     */
    public void createFirstRoomInGroup(RoomDTO roomDTO) {
        GroupDTO groupDTO = roomDTO.getGroup();
        log.debug("Request to create first Room in Group");
        Optional<Group> optionalGroup = groupRepository.findById(groupDTO.getId());
        if(optionalGroup.isPresent()) {
            Group group = optionalGroup.get();
            Set<User> users = new HashSet<>(group.getUsers());
            Room room = new Room();
            room.setName("Public");
            room.setUsers(users);
            room.setIsActivated(true);
            room.setGroup(group);
            room = roomRepository.save(room);
        }
    }
}
