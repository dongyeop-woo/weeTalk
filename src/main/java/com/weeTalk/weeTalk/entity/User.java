package com.weeTalk.weeTalk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String nickname;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, unique = true)
    private String email;
    

    @Column(nullable = false, unique = true)
    private UUID uuid;
    
    @Column(columnDefinition = "LONGTEXT")
    private String profileImage;
    
    @Column(columnDefinition = "LONGTEXT")
    private String backgroundImage;
    
    @Column(unique = true, length = 50)
    private String weeTalkId;
    
    // 업무 프로필 필드들
    @Column(length = 20)
    private String businessNickname;
    
    @Column(columnDefinition = "LONGTEXT")
    private String businessProfileImage;
    
    @Column(columnDefinition = "LONGTEXT")
    private String businessBackgroundImage;
    
    @Column(unique = true, length = 50)
    private String businessWeeTalkId;
    
    @Column(length = 100)
    private String businessTitle; // 직책/직업
    
    @Column(length = 100)
    private String businessCompany; // 회사명
    
    @Column(columnDefinition = "TEXT")
    private String businessDescription; // 업무 소개
    
    @Column(nullable = false)
    private boolean hasBusinessProfile = false; // 업무 프로필 존재 여부
    
    // 생성자 (회원가입용)
    public User(String nickname, String password, String email) {
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.uuid = UUID.randomUUID();
    }
    
    // 생성자 (프로필 사진 포함)
    public User(String nickname, String password, String email, String profileImage) {
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.profileImage = profileImage;
        this.uuid = UUID.randomUUID();
    }
    
    // 생성자 (위톡 ID 포함)
    public User(String nickname, String password, String email, String profileImage, String weeTalkId) {
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.profileImage = profileImage;
        this.weeTalkId = weeTalkId;
        this.uuid = UUID.randomUUID();
    }
}
