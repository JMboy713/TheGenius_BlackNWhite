package com.genius.genius.domain.room.controller;

import com.genius.genius.common.response.CustomResponse;
import com.genius.genius.domain.room.domain.Room;
import com.genius.genius.domain.room.dto.RoomCreateRequest;
import com.genius.genius.domain.room.dto.RoomJoinRequest;
import com.genius.genius.domain.room.service.RoomService;
import com.genius.genius.domain.user.domain.User;
import com.genius.genius.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("api/v1/room")
@Tag(name = "Room", description = "방 API")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;
    private final UserService userService;

    @PostMapping("create")
    @Operation(summary = "방 생성 요청", description = "방 생성 요청 API.")
    @ApiResponse(responseCode = "200", description = "방 생성 완료")
    public CustomResponse<Room> createRoom(@RequestBody RoomCreateRequest request) {
        Room room = roomService.createRoom(request);
        return new CustomResponse<>(HttpStatus.OK, "검색 완료", room);
    }

    @GetMapping("/search")
    @Operation(summary = "방 검색 요청", description = "방 검색 요청 API.")
    @ApiResponse(responseCode = "200", description = "방 검색 완료")
    public CustomResponse<Page<Room>> searchRooms(
            @RequestParam(required = false) String query, // ✅ 하나의 검색어만 받음
            Pageable pageable // ✅ Pagination 지원
    ) {
        Page<Room> rooms = roomService.searchRooms(query, pageable);
        return new CustomResponse<>(HttpStatus.OK, "검색 완료", rooms);
    }

    // 방 참여 API (유저가 특정 방에 참여)
    @PostMapping("/join")
    @Operation(summary = "방 참가 요청", description = "방 참가 요청 API.")
    @ApiResponse(responseCode = "200", description = "방 참가 완료")
    public CustomResponse<Room> joinRoom(@RequestBody RoomJoinRequest req) {
        User currentUser = userService.getCurrentUser();
        Room room = roomService.joinRoom(req, currentUser.getId());
        return new CustomResponse<>(HttpStatus.OK, "방 참가 완료", room);
    }

    @PostMapping("/leave")
    @Operation(summary = "방 나가기 요청", description = "방 나가기 요청 API.")
    @ApiResponse(responseCode = "200", description = "방 나가기 완료")
    @ApiResponse(responseCode = "204", description = "방 삭제됨")
    public CustomResponse<Room> leaveRoom(@RequestParam long roomId) {
        User currentUser = userService.getCurrentUser();
        Optional<Room> room = roomService.leaveRoom(roomId, currentUser.getId());

        if (room.isEmpty()) {
            return new CustomResponse<>(HttpStatus.NO_CONTENT, "방이 삭제되었습니다", null); // ✅ 방이 삭제된 경우 204 응답
        }
        return new CustomResponse<>(HttpStatus.OK, "방 나가기 성공", room.get());
    }

}


