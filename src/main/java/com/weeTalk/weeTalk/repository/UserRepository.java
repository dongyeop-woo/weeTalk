package com.weeTalk.weeTalk.repository;

import com.weeTalk.weeTalk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);
    
    // 닉네임으로 사용자 찾기
    Optional<User> findByNickname(String nickname);
    
    
    // UUID로 사용자 찾기
    Optional<User> findByUuid(UUID uuid);
    
    // 이메일 중복 확인
    boolean existsByEmail(String email);
    
    // 위톡 ID 중복 확인
    boolean existsByWeeTalkId(String weeTalkId);
    
    // 위톡 ID로 사용자 찾기
    Optional<User> findByWeeTalkId(String weeTalkId);
    
    // 업무 위톡 ID 중복 확인
    boolean existsByBusinessWeeTalkId(String businessWeeTalkId);
    
    // 업무 위톡 ID로 사용자 찾기
    Optional<User> findByBusinessWeeTalkId(String businessWeeTalkId);
    
}
