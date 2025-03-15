package com.genius.genius.domain.room.controller;

import com.genius.genius.common.response.CustomResponse;
import com.genius.genius.domain.room.domain.Room;
import com.genius.genius.domain.room.dto.RoomCreateRequest;
import com.genius.genius.domain.room.dto.RoomDTO;
import com.genius.genius.domain.room.dto.RoomJoinRequest;
import com.genius.genius.domain.room.dto.UserDTO;
import com.genius.genius.domain.room.service.RoomService;
import com.genius.genius.domain.user.domain.User;
import com.genius.genius.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;


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
    public CustomResponse<RoomDTO> createRoom(@RequestBody RoomCreateRequest request) {
        Room room = roomService.createRoom(request);
        Set<UserDTO> userDTOs = room.getUserDTOs(room.getUsers());
        Set<UserDTO> readyUserDTOs = room.getReadyUserDTOs(room.getReadyUsers());
        RoomDTO roomDTO = new RoomDTO(room, userDTOs, readyUserDTOs);
        return new CustomResponse<>(HttpStatus.OK, "방 생성 완료", roomDTO);
    }

    @GetMapping("/search")
    @Operation(summary = "방 검색 요청", description = "방 검색 요청 API.")
    @ApiResponse(responseCode = "200", description = "방 검색 완료")
    public CustomResponse<Page<RoomDTO>> searchRooms(
            @RequestParam(required = false) String query, // ✅ 하나의 검색어만 받음
            @ParameterObject
            @PageableDefault(size = 10, page = 0)
            @SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Room> rooms = roomService.searchRooms(query, pageable);
        Page<RoomDTO> roomDTOs = rooms.map(room -> {
            Set<UserDTO> userDTOs = room.getUserDTOs(room.getUsers());
            Set<UserDTO> readyUserDTOs = room.getReadyUserDTOs(room.getReadyUsers());
            return new RoomDTO(room, userDTOs, readyUserDTOs);
        });
        return new CustomResponse<>(HttpStatus.OK, "검색 완료", roomDTOs);
    }

    // 방 참여 API (유저가 특정 방에 참여)
    @PostMapping("/join")
    @Operation(summary = "방 참가 요청", description = "방 참가 요청 API.")
    @ApiResponse(responseCode = "200", description = "방 참가 완료")
    public CustomResponse<RoomDTO> joinRoom(@RequestBody RoomJoinRequest req) {
        User currentUser = userService.getCurrentUser();
        Room room = roomService.joinRoom(req, currentUser.getId());
        Set<UserDTO> userDTOs = room.getUserDTOs(room.getUsers());
        Set<UserDTO> readyUserDTOs = room.getReadyUserDTOs(room.getReadyUsers());
        RoomDTO roomDTO = new RoomDTO(room, userDTOs, readyUserDTOs);
        return new CustomResponse<>(HttpStatus.OK, "방 참가 완료", roomDTO);
    }

    @PostMapping("/leave")
    @Operation(summary = "방 나가기 요청", description = "방 나가기 요청 API.")
    @ApiResponse(responseCode = "200", description = "방 나가기 완료")
    @ApiResponse(responseCode = "204", description = "방 삭제됨")
    public CustomResponse<Void> leaveRoom(@RequestParam Long roomId) {
        User currentUser = userService.getCurrentUser();
        Optional<Room> room = roomService.leaveRoom(roomId, currentUser.getId());

        if (room.isEmpty()) {
            return new CustomResponse<>(HttpStatus.NO_CONTENT, "방이 삭제되었습니다", null); // ✅ 방이 삭제된 경우 204 응답
        }
        return new CustomResponse<>(HttpStatus.OK, "방 나가기 성공", null);
    }

    @PostMapping("/ready")
    @Operation(summary = "방 준비 요청", description = "방 준비 요청 API.")
    @ApiResponse(responseCode = "200", description = "방 준비 완료")
    public CustomResponse<RoomDTO> readyRoom() {
        User currentUser = userService.getCurrentUser();
        Room room = roomService.setReady(currentUser);
        Set<UserDTO> userDTOs = room.getUserDTOs(room.getUsers());
        Set<UserDTO> readyUserDTOs = room.getReadyUserDTOs(room.getReadyUsers());
        RoomDTO roomDTO = new RoomDTO(room, userDTOs, readyUserDTOs);
        return new CustomResponse<>(HttpStatus.OK, "방 준비 완료", roomDTO);
    }

}

