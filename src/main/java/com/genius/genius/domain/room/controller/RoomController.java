package com.genius.genius.domain.room.controller;

import com.genius.genius.domain.room.domain.Room;
import com.genius.genius.domain.room.dto.RoomRequest;
import com.genius.genius.domain.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/room")
@Tag(name = "Room", description = "방 API")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @PostMapping("create")
    @Operation(summary = "방 생성 요청", description = "방 생성 요청 API.")
    @ApiResponse(responseCode = "200", description = "방 생성 완료")
    public ResponseEntity<Room> createRoom(@RequestBody RoomRequest request) {
        Room room = roomService.createRoom(request);
        return ResponseEntity.ok(room);
    }

}
