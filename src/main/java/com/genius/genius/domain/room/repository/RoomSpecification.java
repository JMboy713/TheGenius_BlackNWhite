package com.genius.genius.domain.room.repository;

import com.genius.genius.domain.room.domain.Room;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class RoomSpecification {
    public static Specification<Room> searchByQuery(String query) {
        // ✅ query가 없으면 전체 검색 수행
        if (query == null || query.trim().isEmpty()) {
            return Specification.where(null); // ✅ 전체 검색
        }

        return (root, queryObj, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ✅ 방 제목 검색 (부분 일치)
            predicates.add(criteriaBuilder.like(root.get("name"), "%" + query + "%"));

            // ✅ 초성 검색
            predicates.add(criteriaBuilder.like(root.get("name"), query + "%"));

            // ✅ 방 ID 검색 (query가 숫자인 경우)
            try {
                Long roomId = Long.parseLong(query);
                predicates.add(criteriaBuilder.equal(root.get("id"), roomId));
            } catch (NumberFormatException ignored) {}

            // ✅ 특정 유저 ID가 포함된 방 검색 (query가 숫자인 경우)
            try {
                Long userId = Long.parseLong(query);
                predicates.add(criteriaBuilder.isMember(userId, root.get("users")));
            } catch (NumberFormatException ignored) {}

            // ✅ 검색 결과는 isStarted = false인 방만 반환
            predicates.add(criteriaBuilder.isFalse(root.get("isStarted")));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0])); // OR 조건으로 검색
        };
    }
}
