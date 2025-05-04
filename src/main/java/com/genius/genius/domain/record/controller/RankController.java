package com.genius.genius.domain.record.controller;

import com.genius.genius.common.response.CustomResponse;
import com.genius.genius.domain.record.dto.DetailRecord;
import com.genius.genius.domain.record.service.GameRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/rank")
@RequiredArgsConstructor
public class RankController {
    private GameRecordService gameRecordService;

    // 유저의 각 게임과 라운드별 기록을 보여줌
    @GetMapping("/detailRecord/{userId}")
    public CustomResponse<ArrayList<DetailRecord>> detailRecord(@RequestParam("userId") long userId) {
        ArrayList<DetailRecord> detailRecords = gameRecordService.detailRecord(userId);
        return new CustomResponse<>(HttpStatus.OK, "데이터 조회 성공", detailRecords);
    }

}
