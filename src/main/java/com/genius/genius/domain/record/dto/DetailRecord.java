package com.genius.genius.domain.record.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Builder
public class DetailRecord {
    private long myId;
    private long opponentId;
    private ArrayList<Integer> myRecord;
    private ArrayList<Integer> opponentRecord;
    private ArrayList<Long> winnerId;

    public DetailRecord(long myId, long opponentId, ArrayList<Integer> myRecord, ArrayList<Integer> opponentRecord, ArrayList<Long> winnerId) {
        this.myId = myId;
        this.opponentId = opponentId;
        this.myRecord = myRecord;
        this.opponentRecord = opponentRecord;
        this.winnerId = winnerId;
    }
}
