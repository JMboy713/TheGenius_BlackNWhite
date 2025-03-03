package com.genius.genius.domain.room.service;

import com.genius.genius.common.exception.ApiException;
import com.genius.genius.common.exception.ExceptionEnum;
import com.genius.genius.domain.room.domain.Room;
import com.genius.genius.domain.room.dto.RoomCreateRequest;
import com.genius.genius.domain.room.dto.RoomJoinRequest;
import com.genius.genius.domain.room.repository.RoomRepository;
import com.genius.genius.domain.room.repository.RoomSpecification;
import com.genius.genius.domain.user.domain.User;
import com.genius.genius.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Room createRoom(RoomCreateRequest request) {

        // 비밀번호가 입력되지 않으면 null 저장
        Integer password = (request.getPassword() == null ? null : request.getPassword());

        User user = userService.getCurrentUser();

        // 처음에는 방 생성자(user1)만 포함, user2 없음 (List<User> 사용)
        List<User> users = List.of(user);

        List<Room> rooms = roomRepository.findRoomsByUserIdAndNotStarted(user.getId());

        if (!rooms.isEmpty()) {
            throw new ApiException(ExceptionEnum.IN_OTHER_ROOM);
        }


        Room room = Room.builder().name(request.getName()).password(password).isStarted(false)  // ✅ 기본값: 방이 시작되지 않음
                .users(users)      // ✅ 처음에는 user1만 포함
                .build();

        return roomRepository.save(room);
    }

    @Override
    @Transactional
    public Room joinRoom(RoomJoinRequest req, Long userId) {
        Room room = roomRepository.findById(req.getRoomId()).orElseThrow(() -> new ApiException(ExceptionEnum.ROOM_NOT_FOUND));

        User user = userService.findById(userId).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));

        List<Room> rooms = roomRepository.findRoomsByUserIdAndNotStarted(userId);
        if (!rooms.isEmpty()) {
            throw new ApiException(ExceptionEnum.IN_OTHER_ROOM);
        }

        // 유저가 이미 방에 존재하지 않을 경우만 추가, 1명일 경우에만 추가
        if (!room.getUsers().contains(user) && room.getUsers().size() == 1 && room.getPassword() == req.getPassword()) {
            room.getUsers().add(user);
            return roomRepository.save(room);
        } else if (room.getUsers().contains(user)) {
            return room;
        } else if (room.getUsers().size() >= 2) {
            throw new ApiException(ExceptionEnum.ROOM_FULL);
        } else if (room.getPassword() != req.getPassword()) {
            throw new ApiException(ExceptionEnum.DIFFERENT_PASSWORD);
        }

        return room;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Room> searchRooms(String query, Pageable pageable) {
        // ✅ Specification.where() 사용하여 전체 검색 가능하도록 처리
        Specification<Room> spec = Specification.where(RoomSpecification.searchByQuery(query));
        return roomRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<Room> leaveRoom(long roomId, Long userId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ApiException(ExceptionEnum.ROOM_NOT_FOUND));
        User user = userService.findById(userId).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));

        if (!room.getUsers().contains(user)) {
            throw new ApiException(ExceptionEnum.NOT_IN_ROOM);
        }

        room.getUsers().remove(user);

        if (room.getUsers().size() == 0) {
            roomRepository.delete(room);
            return Optional.empty();
        }
        return Optional.of(roomRepository.save(room));
    }

}




