package com.chatty.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.chatty.IntegrationTest;
import com.chatty.domain.Room;
import com.chatty.repository.RoomRepository;
import com.chatty.service.RoomService;
import com.chatty.service.dto.RoomDTO;
import com.chatty.service.mapper.RoomMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link RoomResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class RoomResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_IS_ACTIVATED = false;
    private static final Boolean UPDATED_IS_ACTIVATED = true;

    private static final Boolean DEFAULT_ALLOW_IMAGE_MESSAGE = false;
    private static final Boolean UPDATED_ALLOW_IMAGE_MESSAGE = true;

    private static final Boolean DEFAULT_ALLOW_VOICE_MESSAGE = false;
    private static final Boolean UPDATED_ALLOW_VOICE_MESSAGE = true;

    private static final Boolean DEFAULT_ALLOW_STICKER_MESSAGE = false;
    private static final Boolean UPDATED_ALLOW_STICKER_MESSAGE = true;

    private static final String DEFAULT_KEY = "AAAAAAAAAA";
    private static final String UPDATED_KEY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/rooms";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private RoomRepository roomRepository;

    @Mock
    private RoomRepository roomRepositoryMock;

    @Autowired
    private RoomMapper roomMapper;

    @Mock
    private RoomService roomServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRoomMockMvc;

    private Room room;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Room createEntity(EntityManager em) {
        Room room = new Room()
            .name(DEFAULT_NAME)
            .isActivated(DEFAULT_IS_ACTIVATED)
            .allowImageMessage(DEFAULT_ALLOW_IMAGE_MESSAGE)
            .allowVoiceMessage(DEFAULT_ALLOW_VOICE_MESSAGE)
            .allowStickerMessage(DEFAULT_ALLOW_STICKER_MESSAGE);
        return room;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Room createUpdatedEntity(EntityManager em) {
        Room room = new Room()
            .name(UPDATED_NAME)
            .isActivated(UPDATED_IS_ACTIVATED)
            .allowImageMessage(UPDATED_ALLOW_IMAGE_MESSAGE)
            .allowVoiceMessage(UPDATED_ALLOW_VOICE_MESSAGE)
            .allowStickerMessage(UPDATED_ALLOW_STICKER_MESSAGE);
        return room;
    }

    @BeforeEach
    public void initTest() {
        room = createEntity(em);
    }

    @Test
    @Transactional
    void createRoom() throws Exception {
        int databaseSizeBeforeCreate = roomRepository.findAll().size();
        // Create the Room
        RoomDTO roomDTO = roomMapper.toDto(room);
        restRoomMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(roomDTO)))
            .andExpect(status().isCreated());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeCreate + 1);
        Room testRoom = roomList.get(roomList.size() - 1);
        assertThat(testRoom.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testRoom.getIsActivated()).isEqualTo(DEFAULT_IS_ACTIVATED);
        assertThat(testRoom.getAllowImageMessage()).isEqualTo(DEFAULT_ALLOW_IMAGE_MESSAGE);
        assertThat(testRoom.getAllowVoiceMessage()).isEqualTo(DEFAULT_ALLOW_VOICE_MESSAGE);
        assertThat(testRoom.getAllowStickerMessage()).isEqualTo(DEFAULT_ALLOW_STICKER_MESSAGE);
    }

    @Test
    @Transactional
    void createRoomWithExistingId() throws Exception {
        // Create the Room with an existing ID
        room.setId(1L);
        RoomDTO roomDTO = roomMapper.toDto(room);

        int databaseSizeBeforeCreate = roomRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restRoomMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(roomDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllRooms() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList
        restRoomMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(room.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].isActivated").value(hasItem(DEFAULT_IS_ACTIVATED.booleanValue())))
            .andExpect(jsonPath("$.[*].allowImageMessage").value(hasItem(DEFAULT_ALLOW_IMAGE_MESSAGE.booleanValue())))
            .andExpect(jsonPath("$.[*].allowVoiceMessage").value(hasItem(DEFAULT_ALLOW_VOICE_MESSAGE.booleanValue())))
            .andExpect(jsonPath("$.[*].allowStickerMessage").value(hasItem(DEFAULT_ALLOW_STICKER_MESSAGE.booleanValue())))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_KEY)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllRoomsWithEagerRelationshipsIsEnabled() throws Exception {
        when(roomServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restRoomMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(roomServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllRoomsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(roomServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restRoomMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(roomServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    void getRoom() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get the room
        restRoomMockMvc
            .perform(get(ENTITY_API_URL_ID, room.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(room.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.isActivated").value(DEFAULT_IS_ACTIVATED.booleanValue()))
            .andExpect(jsonPath("$.allowImageMessage").value(DEFAULT_ALLOW_IMAGE_MESSAGE.booleanValue()))
            .andExpect(jsonPath("$.allowVoiceMessage").value(DEFAULT_ALLOW_VOICE_MESSAGE.booleanValue()))
            .andExpect(jsonPath("$.allowStickerMessage").value(DEFAULT_ALLOW_STICKER_MESSAGE.booleanValue()))
            .andExpect(jsonPath("$.key").value(DEFAULT_KEY));
    }

    @Test
    @Transactional
    void getNonExistingRoom() throws Exception {
        // Get the room
        restRoomMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewRoom() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        int databaseSizeBeforeUpdate = roomRepository.findAll().size();

        // Update the room
        Room updatedRoom = roomRepository.findById(room.getId()).get();
        // Disconnect from session so that the updates on updatedRoom are not directly saved in db
        em.detach(updatedRoom);
        updatedRoom
            .name(UPDATED_NAME)
            .isActivated(UPDATED_IS_ACTIVATED)
            .allowImageMessage(UPDATED_ALLOW_IMAGE_MESSAGE)
            .allowVoiceMessage(UPDATED_ALLOW_VOICE_MESSAGE)
            .allowStickerMessage(UPDATED_ALLOW_STICKER_MESSAGE);
        RoomDTO roomDTO = roomMapper.toDto(updatedRoom);

        restRoomMockMvc
            .perform(
                put(ENTITY_API_URL_ID, roomDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(roomDTO))
            )
            .andExpect(status().isOk());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
        Room testRoom = roomList.get(roomList.size() - 1);
        assertThat(testRoom.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRoom.getIsActivated()).isEqualTo(UPDATED_IS_ACTIVATED);
        assertThat(testRoom.getAllowImageMessage()).isEqualTo(UPDATED_ALLOW_IMAGE_MESSAGE);
        assertThat(testRoom.getAllowVoiceMessage()).isEqualTo(UPDATED_ALLOW_VOICE_MESSAGE);
        assertThat(testRoom.getAllowStickerMessage()).isEqualTo(UPDATED_ALLOW_STICKER_MESSAGE);
    }

    @Test
    @Transactional
    void putNonExistingRoom() throws Exception {
        int databaseSizeBeforeUpdate = roomRepository.findAll().size();
        room.setId(count.incrementAndGet());

        // Create the Room
        RoomDTO roomDTO = roomMapper.toDto(room);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRoomMockMvc
            .perform(
                put(ENTITY_API_URL_ID, roomDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(roomDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchRoom() throws Exception {
        int databaseSizeBeforeUpdate = roomRepository.findAll().size();
        room.setId(count.incrementAndGet());

        // Create the Room
        RoomDTO roomDTO = roomMapper.toDto(room);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRoomMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(roomDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamRoom() throws Exception {
        int databaseSizeBeforeUpdate = roomRepository.findAll().size();
        room.setId(count.incrementAndGet());

        // Create the Room
        RoomDTO roomDTO = roomMapper.toDto(room);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRoomMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(roomDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateRoomWithPatch() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        int databaseSizeBeforeUpdate = roomRepository.findAll().size();

        // Update the room using partial update
        Room partialUpdatedRoom = new Room();
        partialUpdatedRoom.setId(room.getId());

        partialUpdatedRoom
            .isActivated(UPDATED_IS_ACTIVATED)
            .allowImageMessage(UPDATED_ALLOW_IMAGE_MESSAGE)
            .allowVoiceMessage(UPDATED_ALLOW_VOICE_MESSAGE);

        restRoomMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRoom.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRoom))
            )
            .andExpect(status().isOk());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
        Room testRoom = roomList.get(roomList.size() - 1);
        assertThat(testRoom.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testRoom.getIsActivated()).isEqualTo(UPDATED_IS_ACTIVATED);
        assertThat(testRoom.getAllowImageMessage()).isEqualTo(UPDATED_ALLOW_IMAGE_MESSAGE);
        assertThat(testRoom.getAllowVoiceMessage()).isEqualTo(UPDATED_ALLOW_VOICE_MESSAGE);
        assertThat(testRoom.getAllowStickerMessage()).isEqualTo(DEFAULT_ALLOW_STICKER_MESSAGE);
    }

    @Test
    @Transactional
    void fullUpdateRoomWithPatch() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        int databaseSizeBeforeUpdate = roomRepository.findAll().size();

        // Update the room using partial update
        Room partialUpdatedRoom = new Room();
        partialUpdatedRoom.setId(room.getId());

        partialUpdatedRoom
            .name(UPDATED_NAME)
            .isActivated(UPDATED_IS_ACTIVATED)
            .allowImageMessage(UPDATED_ALLOW_IMAGE_MESSAGE)
            .allowVoiceMessage(UPDATED_ALLOW_VOICE_MESSAGE)
            .allowStickerMessage(UPDATED_ALLOW_STICKER_MESSAGE);

        restRoomMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRoom.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRoom))
            )
            .andExpect(status().isOk());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
        Room testRoom = roomList.get(roomList.size() - 1);
        assertThat(testRoom.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRoom.getIsActivated()).isEqualTo(UPDATED_IS_ACTIVATED);
        assertThat(testRoom.getAllowImageMessage()).isEqualTo(UPDATED_ALLOW_IMAGE_MESSAGE);
        assertThat(testRoom.getAllowVoiceMessage()).isEqualTo(UPDATED_ALLOW_VOICE_MESSAGE);
        assertThat(testRoom.getAllowStickerMessage()).isEqualTo(UPDATED_ALLOW_STICKER_MESSAGE);
    }

    @Test
    @Transactional
    void patchNonExistingRoom() throws Exception {
        int databaseSizeBeforeUpdate = roomRepository.findAll().size();
        room.setId(count.incrementAndGet());

        // Create the Room
        RoomDTO roomDTO = roomMapper.toDto(room);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRoomMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, roomDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(roomDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchRoom() throws Exception {
        int databaseSizeBeforeUpdate = roomRepository.findAll().size();
        room.setId(count.incrementAndGet());

        // Create the Room
        RoomDTO roomDTO = roomMapper.toDto(room);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRoomMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(roomDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamRoom() throws Exception {
        int databaseSizeBeforeUpdate = roomRepository.findAll().size();
        room.setId(count.incrementAndGet());

        // Create the Room
        RoomDTO roomDTO = roomMapper.toDto(room);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRoomMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(roomDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteRoom() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        int databaseSizeBeforeDelete = roomRepository.findAll().size();

        // Delete the room
        restRoomMockMvc
            .perform(delete(ENTITY_API_URL_ID, room.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
