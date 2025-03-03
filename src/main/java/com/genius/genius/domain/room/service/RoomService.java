package com.genius.genius.domain.room.service;

import com.genius.genius.domain.room.domain.Room;
import com.genius.genius.domain.room.dto.RoomRequest;
import com.genius.genius.domain.room.repository.RoomRepository;


public interface RoomService {
    Room createRoom(RoomRequest req);
}
