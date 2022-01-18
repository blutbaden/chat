package com.chatty.service;

import com.chatty.domain.Conversation;
import com.chatty.domain.Room;
import com.chatty.domain.User;
import com.chatty.domain.enumeration.ConversationState;
import com.chatty.repository.ConversationRepository;
import com.chatty.service.dto.ConversationDTO;
import com.chatty.service.dto.RoomDTO;
import com.chatty.service.dto.UserDTO;
import com.chatty.service.mapper.ConversationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Conversation}.
 */
@Service
@Transactional
public class ConversationService {

    private final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final ConversationRepository conversationRepository;

    private final ConversationMapper conversationMapper;

    private final UserService userService;

    private final RoomService roomService;

    public ConversationService(ConversationRepository conversationRepository, ConversationMapper conversationMapper, UserService userService, RoomService roomService) {
        this.conversationRepository = conversationRepository;
        this.conversationMapper = conversationMapper;
        this.userService = userService;
        this.roomService = roomService;
    }

    /**
     * Save a conversation.
     *
     * @param conversationDTO the entity to save.
     * @return the persisted entity.
     */
    public ConversationDTO save(ConversationDTO conversationDTO) {
        log.debug("Request to save Conversation : {}", conversationDTO);
        Conversation conversation = conversationMapper.toEntity(conversationDTO);
        String sender = conversationDTO.getSender().getLogin();
        String receiver = conversationDTO.getSender().getLogin();
        userService.findUserByLogin(sender).ifPresent(conversation::sender);
        userService.findUserByLogin(receiver).ifPresent(conversation::receiver);
        conversation = conversationRepository.save(conversation);
        return conversationMapper.toDto(conversation);
    }

    /**
     * Save the conversation.
     *
     * @param message  the message of the entity.
     * @param roomId   the id of the room entity.
     * @param sender the login of the sender user entity.
     * @param receiver the login of the receiver user entity.
     */
    public void save(String message, Long roomId, String sender, String receiver) {
        Conversation conversation = new Conversation();
        conversation.setContent(message);
        Optional<Room> optionalRoom = roomService.findById(roomId);
        userService.findUserByLogin(sender).ifPresent(conversation::sender);
        userService.findUserByLogin(receiver).ifPresent(conversation::receiver);
        if (optionalRoom.isPresent()) {
            Room room = optionalRoom.get();
            conversation.setRoom(room);
            conversation.setConversationState(ConversationState.DELIVERED);
            conversationRepository.save(conversation);
        }
    }

    /**
     * Partially update a conversation.
     *
     * @param conversationDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ConversationDTO> partialUpdate(ConversationDTO conversationDTO) {
        log.debug("Request to partially update Conversation : {}", conversationDTO);

        return conversationRepository
            .findById(conversationDTO.getId())
            .map(existingConversation -> {
                conversationMapper.partialUpdate(existingConversation, conversationDTO);
                return existingConversation;
            })
            .map(conversationRepository::save)
            .map(conversationMapper::toDto);
    }

    /**
     * Get all the conversations.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ConversationDTO> findAll() {
        log.debug("Request to get all Conversations");
        return conversationRepository.findAll().stream().map(conversationMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }


    /**
     * Get Current User Conversations Count By State grouped by Room
     **/
    @Transactional(readOnly = true)
    public List<?> getCurrentUserConversationsCountByStateGroupedByRoom(ConversationState conversationState) {
        log.debug("Request to get current user conversations count by state grouped by room");
        Optional<User> optionalUser = userService.getUserWithAuthorities();
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return conversationRepository.getConversationsCountByReceiverAndStateGroupedByRoom(conversationState, user.getId());
        }
        return new ArrayList<>();
    }

    /**
     * Get Current User Conversations Count By State grouped by Sender
     **/
    @Transactional(readOnly = true)
    public List<?> getCurrentUserConversationsCountByStateGroupedBySender(ConversationState conversationState) {
        log.debug("Request to get current user conversations count by state grouped by sender");
        Optional<User> optionalUser = userService.getUserWithAuthorities();
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return conversationRepository.getConversationsCountByReceiverAndStateGroupedBySender(conversationState, user.getId());
        }
        return new ArrayList<>();
    }

    /**
     * Get Current User Conversations Count By State and Room
     **/
    @Transactional(readOnly = true)
    public int getConversationCountByRoomAndReceiverAndState(ConversationState conversationState, Long roomId) {
        log.debug("Request to get current user conversations count by state and Room");
        Optional<User> optionalUser = userService.getUserWithAuthorities();
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return conversationRepository.getConversationCountByRoomAndReceiverAndState(conversationState, user.getId(), roomId);
        }
        return 0;
    }

    /**
     * Update Current User Conversation State By Room
     **/
    public void updateConversationStateByRoom(ConversationState conversationState, Long roomId) {
        Optional<User> optionalUser = userService.getUserWithAuthorities();
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            this.conversationRepository.updateConversationStateByRoom(conversationState, roomId, user.getId());
        }
    }

    /**
     * Get all the conversations Of Room.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ConversationDTO> findAllByRoom(Pageable pageable, long roomId) {
        return conversationRepository.findAllByRoomId(pageable, roomId).map(conversationMapper::toDto);
    }

    /**
     * Get one conversation by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ConversationDTO> findOne(Long id) {
        log.debug("Request to get Conversation : {}", id);
        return conversationRepository.findById(id).map(conversationMapper::toDto);
    }

    /**
     * Delete the conversation by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Conversation : {}", id);
        conversationRepository.deleteById(id);
    }
}
