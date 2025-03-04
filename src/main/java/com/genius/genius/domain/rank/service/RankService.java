package com.genius.genius.domain.rank.service;

import com.genius.genius.domain.rank.entity.Rank;
import com.genius.genius.domain.user.domain.User;

import java.util.Optional;

public interface RankService {
    void save(Rank rank);

    Rank findByUser(User user);
}
