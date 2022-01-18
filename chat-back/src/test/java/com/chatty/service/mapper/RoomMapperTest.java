package com.chatty.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoomMapperTest {

    private RoomMapper roomMapper;

    @BeforeEach
    public void setUp() {
        roomMapper = new RoomMapperImpl();
    }
}
