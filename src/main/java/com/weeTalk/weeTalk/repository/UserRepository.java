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
    
    // 전화번호로 사용자 찾기
    Optional<User> findByPhone(String phone);
    
    // UUID로 사용자 찾기
    Optional<User> findByUuid(UUID uuid);
    
    // 이메일 중복 확인
    boolean existsByEmail(String email);
    
    // 닉네임 중복 확인
    boolean existsByNickname(String nickname);
    
    // 전화번호 중복 확인
    boolean existsByPhone(String phone);
}
