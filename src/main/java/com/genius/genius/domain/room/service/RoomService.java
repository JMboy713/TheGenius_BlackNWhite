package com.genius.genius.domain.room.service;

import com.genius.genius.domain.room.domain.Room;
import com.genius.genius.domain.room.dto.RoomCreateRequest;
import com.genius.genius.domain.room.dto.RoomJoinRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


public interface RoomService {
    Room createRoom(RoomCreateRequest req);
    Room joinRoom(RoomJoinRequest req, Long userId);
    Page<Room> searchRooms(String query, Pageable pageable);
    Optional<Room> leaveRoom(Long roomId, Long userid);
}
