package com.project.thisvsthat.myPage.DAO;

import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Transactional
public class MyPageDAO {
    @Autowired
    EntityManager em;

//    public Long findUserId(String userId) {
//        String sql = "SELECT u FROM User u " +
//                "WHERE u.userId = '" + userId + "'";
//        Query query = em.createQuery(sql);
//        User user = (User) query.getSingleResult();
//        Long id = user.getUserId();
//        System.out.println(id);
//        return id;
//    }

    // 안전한 쿼리 실행을 위해 파라미터 바인딩 사용
    public Long findUserId(String userId) {
        String jpql = "SELECT u FROM User u WHERE u.userId = :userId"; // userId가 Long인지 String인지 확인 필요
        Query query = em.createQuery(jpql);

        // userId가 Long 타입이라면 변환 필요
        query.setParameter("userId", Long.parseLong(userId));

        try {
            User user = (User) query.getSingleResult();
            return user.getUserId();
        } catch (NoResultException e) {
            return null;
        }
    }

    //em.find() 메서드를 사용해 특정 User 조회
    public User getOneUser(Long id) {
        return em.find(User.class, id);
    }

    //새 사용자 저장 or 기존 사용자 업데이트
    public void save(User user) {
        em.merge(user); //이미 존재하는 entity일 경우 업데이트
    }

    //내가 올린 게시물 조회
    public List<Post> findMyPosts(Long userId) {
        String jpql = "SELECT p FROM p WHERE p.userId = :userId ORDER BY p.createdAt DESC";
        return em.createQuery(jpql, Post.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    //내가 투표한 게시물 조회
    public List<Post> findVotedPosts(Long userId) {
        String jpql = "SELECT v.post FROM Vote v WHERE v.user.userId = :userId";
        return em.createQuery(jpql, Post.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
