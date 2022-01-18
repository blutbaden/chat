package com.chatty.web.rest;

import com.chatty.repository.RoomRepository;
import com.chatty.service.RoomService;
import com.chatty.service.dto.GroupDTO;
import com.chatty.service.dto.RoomDTO;
import com.chatty.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
 * REST controller for managing {@link com.chatty.domain.Room}.
 */
@RestController
@RequestMapping("/api")
public class RoomResource {

    private static final String ENTITY_NAME = "room";
    private final Logger log = LoggerFactory.getLogger(RoomResource.class);
    private final RoomService roomService;
    private final RoomRepository roomRepository;
    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public RoomResource(RoomService roomService, RoomRepository roomRepository) {
        this.roomService = roomService;
        this.roomRepository = roomRepository;
    }

    /**
     * {@code POST  /rooms} : Create a new room.
     *
     * @param roomDTO the roomDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new roomDTO, or with status {@code 400 (Bad Request)} if the room has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/rooms")
    public ResponseEntity<RoomDTO> createRoom(@RequestBody RoomDTO roomDTO) throws URISyntaxException {
        log.debug("REST request to save Room : {}", roomDTO);
        if (roomDTO.getId() != null) {
            throw new BadRequestAlertException("A new room cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RoomDTO result = roomService.save(roomDTO);
        return ResponseEntity
            .created(new URI("/api/rooms/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /rooms/:id} : Updates an existing room.
     *
     * @param id      the id of the roomDTO to save.
     * @param roomDTO the roomDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated roomDTO,
     * or with status {@code 400 (Bad Request)} if the roomDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the roomDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/rooms/{id}")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable(value = "id", required = false) final Long id, @RequestBody RoomDTO roomDTO)
        throws URISyntaxException {
        log.debug("REST request to update Room : {}, {}", id, roomDTO);
        if (roomDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, roomDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!roomRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        RoomDTO result = roomService.save(roomDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, roomDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /rooms/:id} : Partial updates given fields of an existing room, field will ignore if it is null
     *
     * @param id      the id of the roomDTO to save.
     * @param roomDTO the roomDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated roomDTO,
     * or with status {@code 400 (Bad Request)} if the roomDTO is not valid,
     * or with status {@code 404 (Not Found)} if the roomDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the roomDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/rooms/{id}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<RoomDTO> partialUpdateRoom(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody RoomDTO roomDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Room partially : {}, {}", id, roomDTO);
        if (roomDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, roomDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!roomRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<RoomDTO> result = roomService.partialUpdate(roomDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, roomDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /rooms} : get all the rooms.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of rooms in body.
     */
    @GetMapping("/rooms")
    public List<RoomDTO> getAllRooms(@RequestParam(required = false, defaultValue = "false") boolean eagerload) {
        log.debug("REST request to get all Rooms");
        return roomService.findAll();
    }

    /**
     * {@code POST  /rooms/group} : Create First Room in Group
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of rooms in body.
     */
    @PostMapping("/rooms/group")
    public void createFirstRoomInGroup(@RequestBody RoomDTO roomDTO) throws URISyntaxException {
        log.debug("REST request to save First Room In Group: {}", roomDTO);
        GroupDTO groupDTO = roomDTO.getGroup();
        if (groupDTO == null || groupDTO.getId() == null) {
            throw new BadRequestAlertException("The Group in the Room must not be Null", ENTITY_NAME, "null");
        }
        roomService.createFirstRoomInGroup(roomDTO);
    }

    /**
     * {@code GET  /rooms/:id} : get the "id" room.
     *
     * @param id the id of the roomDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the roomDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/rooms/{id}")
    public ResponseEntity<RoomDTO> getRoom(@PathVariable Long id) {
        log.debug("REST request to get Room : {}", id);
        Optional<RoomDTO> roomDTO = roomService.findOne(id);
        return ResponseUtil.wrapOrNotFound(roomDTO);
    }

    /**
     * {@code GET  /rooms/logged/private/user/:id} : get the "id" room.
     *
     * @param id the id of the other user in the same room.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the roomDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/rooms/logged/private/user/{id}")
    public ResponseEntity<RoomDTO> getPrivateRoomByLoggedUserAndSelectedUser(@PathVariable Long id) {
        log.debug("REST request to get Room : {}", id);
        RoomDTO roomDTO = roomService.getPrivateRoomByLoggedUserAndSelectedUser(id);
        return ResponseUtil.wrapOrNotFound(Optional.of(roomDTO));
    }

    /**
     * {@code GET  /rooms/group/:groupId}.
     *
     * @param groupId the id of the group.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the roomDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/rooms/group/{groupId}")
    public List<RoomDTO> findAllByGroup(@PathVariable Long groupId) {
        log.debug("REST request to get all Rooms of group");
        return roomService.findAllByGroup(groupId);
    }

    /**
     * {@code GET  /rooms/logged/group/:groupId}.
     *
     * @param groupId the id of the group.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the roomDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/rooms/logged/group/{groupId}")
    public List<RoomDTO> findAllOfGroupJoinedByLoggedUser(@PathVariable Long groupId) {
        log.debug("REST request to get all rooms of a group joined by the logged user");
        return roomService.findAllOfGroupJoinedByLoggedUser(groupId);
    }

    /**
     * {@code GET  /rooms/logged} : get all the rooms joined by logged user.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of rooms in body.
     */
    @GetMapping("/rooms/logged")
    public List<RoomDTO> getAllRoomsJoinedByLoggedUser() {
        log.debug("REST request to get all Rooms Joined by logged user");
        return roomService.findAllByLoggedUser();
    }

    /**
     * {@code GET  /rooms/logged/public} : get all the public rooms joined by logged user.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of rooms in body.
     */
    @GetMapping("/rooms/logged/public")
    public List<RoomDTO> getAllPublicRoomsJoinedByLoggedUser() {
        log.debug("REST request to get all public Rooms Joined by logged user");
        return roomService.findAllByLoggedUserAndGroupNotNull();
    }

    /**
     * {@code DELETE  /rooms/:id} : delete the "id" room.
     *
     * @param id the id of the roomDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        log.debug("REST request to delete Room : {}", id);
        roomService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
