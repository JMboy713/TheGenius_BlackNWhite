package com.genius.genius.domain.rank.service;

import com.genius.genius.common.exception.ApiException;
import com.genius.genius.common.exception.ExceptionEnum;
import com.genius.genius.domain.rank.entity.Rank;
import com.genius.genius.domain.rank.repository.RankRepository;
import com.genius.genius.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RankServiceImpl implements RankService {

    private final RankRepository rankRepository;
    @Override
    public void save(Rank rank) {
        rankRepository.save(rank);
    }

    @Override
    public Rank findByUser(User user) {
        return rankRepository.findRankByUser(user).orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_USER));
    }

    /**
     * TODO 게임 마무리 되고 결과 저장해야됨
     */
}
