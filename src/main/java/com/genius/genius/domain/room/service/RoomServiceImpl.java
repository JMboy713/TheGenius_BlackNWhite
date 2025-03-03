package com.genius.genius.domain.room.service;

import com.genius.genius.domain.room.domain.Room;
import com.genius.genius.domain.room.dto.RoomRequest;
import com.genius.genius.domain.room.repository.RoomRepository;
import com.genius.genius.domain.user.domain.User;
import com.genius.genius.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Room createRoom(RoomRequest request) {
        User user1 = userRepository.findById(request.getUser1Id())
                .orElseThrow(() -> new IllegalArgumentException("User1 not found"));
        User user2 = userRepository.findById(request.getUser2Id())
                .orElseThrow(() -> new IllegalArgumentException("User2 not found"));

        // 비밀번호가 입력되지 않으면 null 저장
        String password = (request.getPassword() == null || request.getPassword().isBlank()) ? null : request.getPassword();
        List<User> users = List.of(user1, user2);

        Room room = Room.builder()
                .name(request.getName())
                .password(password)
                .isStarted(false)  // 기본값: 방이 시작되지 않음
                .users(users)
                .build();

        return roomRepository.save(room);
    }
}
