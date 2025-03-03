package com.genius.genius.domain.room.repository;

import com.genius.genius.domain.room.domain.Room;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    // ✅ 특정 유저가 포함된 방 중에서 isStarted가 false인 방만 검색
    @Query("SELECT r FROM Room r JOIN r.users u WHERE u.id = :userId AND r.isStarted = false")
    List<Room> findRoomsByUserIdAndNotStarted(@Param("userId") Long userId);
}
