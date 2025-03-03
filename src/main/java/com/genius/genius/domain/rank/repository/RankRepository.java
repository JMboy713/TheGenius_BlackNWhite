package com.genius.genius.domain.rank.repository;

import com.genius.genius.domain.rank.entity.Rank;
import com.genius.genius.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RankRepository extends JpaRepository<Rank, Long> {
    Optional<Rank> findRankByUser(User user);
}
