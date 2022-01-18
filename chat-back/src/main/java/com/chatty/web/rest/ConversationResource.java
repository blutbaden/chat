package com.chatty.web.rest;

import com.chatty.domain.enumeration.ConversationState;
import com.chatty.repository.ConversationRepository;
import com.chatty.service.ConversationService;
import com.chatty.service.dto.ConversationDTO;
import com.chatty.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link com.chatty.domain.Conversation}.
 */
@RestController
@RequestMapping("/api")
public class ConversationResource {

    private static final String ENTITY_NAME = "conversation";
    private final Logger log = LoggerFactory.getLogger(ConversationResource.class);
    private final ConversationService conversationService;
    private final ConversationRepository conversationRepository;
    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public ConversationResource(ConversationService conversationService, ConversationRepository conversationRepository) {
        this.conversationService = conversationService;
        this.conversationRepository = conversationRepository;
    }

    /**
     * {@code POST  /conversations} : Create a new conversation.
     *
     * @param conversationDTO the conversationDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new conversationDTO, or with status {@code 400 (Bad Request)} if the conversation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/conversations")
    public ResponseEntity<ConversationDTO> createConversation(@RequestBody ConversationDTO conversationDTO) throws URISyntaxException {
        log.debug("REST request to save Conversation : {}", conversationDTO);
        if (conversationDTO.getId() != null) {
            throw new BadRequestAlertException("A new conversation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        if (Objects.isNull(conversationDTO.getSender())) {
            throw new BadRequestAlertException("Invalid association value provided", ENTITY_NAME, "null");
        }
        if (Objects.isNull(conversationDTO.getReceiver())) {
            throw new BadRequestAlertException("Invalid association value provided", ENTITY_NAME, "null");
        }
        ConversationDTO result = conversationService.save(conversationDTO);
        return ResponseEntity
            .created(new URI("/api/conversations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /conversations/:id} : Updates an existing conversation.
     *
     * @param id              the id of the conversationDTO to save.
     * @param conversationDTO the conversationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated conversationDTO,
     * or with status {@code 400 (Bad Request)} if the conversationDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the conversationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/conversations/{id}")
    public ResponseEntity<ConversationDTO> updateConversation(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody ConversationDTO conversationDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Conversation : {}, {}", id, conversationDTO);
        if (conversationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, conversationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!conversationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ConversationDTO result = conversationService.save(conversationDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, conversationDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /conversations/:id} : Partial updates given fields of an existing conversation, field will ignore if it is null
     *
     * @param id              the id of the conversationDTO to save.
     * @param conversationDTO the conversationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated conversationDTO,
     * or with status {@code 400 (Bad Request)} if the conversationDTO is not valid,
     * or with status {@code 404 (Not Found)} if the conversationDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the conversationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/conversations/{id}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<ConversationDTO> partialUpdateConversation(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody ConversationDTO conversationDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Conversation partially : {}, {}", id, conversationDTO);
        if (conversationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, conversationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!conversationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ConversationDTO> result = conversationService.partialUpdate(conversationDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, conversationDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /conversations} : get all the conversations.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of conversations in body.
     */
    @GetMapping("/conversations")
    public List<ConversationDTO> getAllConversations() {
        log.debug("REST request to get all Conversations");
        return conversationService.findAll();
    }

    /**
     * {@code GET  /conversations/logged/delivered/by-room/count} : Get current User Conversations Count By State grouped by Room
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and count.
     */
    @GetMapping("/conversations/logged/delivered/by-room/count")
    public List<?> getCurrentUserConversationsCountByStateGroupedByRoom() {
        log.debug("REST request to get current user conversations count by state grouped by room");
        return conversationService.getCurrentUserConversationsCountByStateGroupedByRoom(ConversationState.DELIVERED);
    }

    /**
     * {@code GET  /conversations/logged/delivered/by-sender/count} : Get current User Conversations Count By State grouped by Sender
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and count.
     */
    @GetMapping("/conversations/logged/delivered/by-sender/count")
    public List<?> getCurrentUserConversationsCountByStateGroupedBySender() {
        log.debug("REST request to get current user conversations count by state grouped by sender");
        return conversationService.getCurrentUserConversationsCountByStateGroupedBySender(ConversationState.DELIVERED);
    }

    /**
     * {@code GET  /conversations/logged/room/{id} : Update current user conversation state by room
     *
     */
    @PutMapping("/conversations/logged/room/{id}")
    public void updateConversationStateByRoom(@PathVariable Long id) {
        log.debug("REST request to update conversation state by room");
        conversationService.updateConversationStateByRoom(ConversationState.SEEN, id);
    }

    /**
     * {@code GET  /conversations/logged/delivered/room/{id}/count} : Get current User Conversation Count By State and Room
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and count.
     */
    @GetMapping("/conversations/logged/delivered/room/{id}/count")
    public int getCurrentUserConversationCountByRoomAndState(@PathVariable Long id) {
        log.debug("REST request to get current user conversation count by room and state");
        return conversationService.getConversationCountByRoomAndReceiverAndState(ConversationState.DELIVERED, id);
    }

    /**
     * {@code GET  /conversations/:roomId} : get all the conversations of Room.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of conversations in body.
     */
    @GetMapping("/conversations/room/{roomId}")
    public Page<ConversationDTO> getAllConversationsByRoom(@PathVariable Long roomId, Pageable pageable) {
        log.debug("REST request to get all Conversations of Room: {}", roomId);
        return conversationService.findAllByRoom(pageable, roomId);
    }

    /**
     * {@code GET  /conversations/:id} : get the "id" conversation.
     *
     * @param id the id of the conversationDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the conversationDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/conversations/{id}")
    public ResponseEntity<ConversationDTO> getConversation(@PathVariable Long id) {
        log.debug("REST request to get Conversation : {}", id);
        Optional<ConversationDTO> conversationDTO = conversationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(conversationDTO);
    }

    /**
     * {@code DELETE  /conversations/:id} : delete the "id" conversation.
     *
     * @param id the id of the conversationDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id) {
        log.debug("REST request to delete Conversation : {}", id);
        conversationService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
