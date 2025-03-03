package com.genius.genius.domain.room.repository;

import com.genius.genius.domain.room.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
