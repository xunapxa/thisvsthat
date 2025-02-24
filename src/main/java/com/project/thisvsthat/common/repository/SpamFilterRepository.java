package com.project.thisvsthat.common.repository;

import com.project.thisvsthat.common.entity.SpamFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SpamFilterRepository extends JpaRepository<SpamFilter, Long> {
    // 스팸 필터 관련 추가 쿼리 메서드가 필요하면 여기에 작성

    @Query("SELECT s.filterValue FROM SpamFilter s") // JPQL로 수정
    List<String> findAllFilterValues();

    // 키워드 일괄 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM SpamFilter s WHERE s.filterId IN :filterIds")
    void deleteByFilterIdIn(@Param("filterIds") List<Long> filterIds);

    // 중복 검사
    boolean existsByFilterValue(String filterValue);
}