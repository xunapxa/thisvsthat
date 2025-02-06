package com.project.thisvsthat.common.repository;

import com.project.thisvsthat.common.entity.SpamFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpamFilterRepository extends JpaRepository<SpamFilter, Long> {
    // 스팸 필터 관련 추가 쿼리 메서드가 필요하면 여기에 작성
}