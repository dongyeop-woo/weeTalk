package com.weeTalk.weeTalk.controller;

import com.weeTalk.weeTalk.dto.*;
import com.weeTalk.weeTalk.service.UserService;
import com.weeTalk.weeTalk.service.VerificationService;
import com.weeTalk.weeTalk.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private VerificationService verificationService;
    
    @Autowired
    private JwtService jwtService;
    

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        UserSignupResponse response = userService.signup(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 로그인
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest, jakarta.servlet.http.HttpServletRequest request) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            
            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "이메일과 비밀번호를 입력해주세요."));
            }
            
            Map<String, Object> result = userService.login(email, password);
            
            if ((Boolean) result.get("success")) {
                // 세션에 사용자 정보 저장
                jakarta.servlet.http.HttpSession session = request.getSession(true);
                session.setAttribute("userEmail", email);
                session.setAttribute("authenticated", true);
                
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "로그인 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 토큰 검증
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            
            if (token == null) {
                return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "토큰이 필요합니다."));
            }
            
            boolean isValid = jwtService.validateToken(token);
            
            if (isValid) {
                String email = jwtService.extractUsername(token);
                return ResponseEntity.ok(Map.of("valid", true, "email", email));
            } else {
                return ResponseEntity.ok(Map.of("valid", false, "message", "유효하지 않은 토큰입니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "토큰 검증 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 세션 검증
    @GetMapping("/validate-session")
    public ResponseEntity<Map<String, Object>> validateSession(jakarta.servlet.http.HttpServletRequest request) {
        try {
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            
            if (session != null && session.getAttribute("authenticated") != null) {
                String email = (String) session.getAttribute("userEmail");
                return ResponseEntity.ok(Map.of("valid", true, "email", email));
            } else {
                return ResponseEntity.ok(Map.of("valid", false, "message", "유효하지 않은 세션입니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "세션 검증 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 사용자 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(jakarta.servlet.http.HttpServletRequest request) {
        try {
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            
            if (session == null || session.getAttribute("authenticated") == null) {
                return ResponseEntity.status(401).body(Map.of("message", "인증이 필요합니다."));
            }
            
            String email = (String) session.getAttribute("userEmail");
            if (email == null) {
                return ResponseEntity.status(401).body(Map.of("message", "사용자 정보를 찾을 수 없습니다."));
            }
            
            Map<String, Object> profileData = userService.getUserProfile(email);
            
            if (profileData != null) {
                return ResponseEntity.ok(profileData);
            } else {
                return ResponseEntity.status(404).body(Map.of("message", "프로필을 찾을 수 없습니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "프로필 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.isEmailExists(email);
        return ResponseEntity.ok(exists);
    }

    
    
    // 이메일 인증코드 발송
    @PostMapping("/send-verification")
    public ResponseEntity<Map<String, String>> sendVerificationCode(@Valid @RequestBody EmailVerificationRequest request) {
        try {
            verificationService.sendVerificationCode(request.getEmail());
            return ResponseEntity.ok(Map.of("message", "인증코드가 발송되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "인증코드 발송에 실패했습니다: " + e.getMessage()));
        }
    }
    
    // 인증코드 검증
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, String>> verifyCode(@Valid @RequestBody CodeVerificationRequest request) {
        boolean isValid = verificationService.verifyCode(request.getEmail(), request.getCode());
        
        if (isValid) {
            return ResponseEntity.ok(Map.of("message", "인증이 완료되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "인증코드가 올바르지 않거나 만료되었습니다."));
        }
    }
    
    // 친구 목록 조회
    @GetMapping("/friends")
    public ResponseEntity<List<Map<String, Object>>> getFriends(
            @RequestParam String userEmail,
            @RequestParam(required = false, defaultValue = "personal") String profileType) {
        try {
            System.out.println("친구 목록 조회 API 호출: " + userEmail + ", profileType: " + profileType);
            List<Map<String, Object>> friends = userService.getFriends(userEmail, profileType);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            System.err.println("친구 목록 조회 API 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(List.of());
        }
    }
    
    // 프로필 업데이트
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@Valid @RequestBody ProfileUpdateDto request) {
        try {
            System.out.println("프로필 업데이트 API 호출: " + request.getEmail());
            Map<String, Object> result = userService.updateProfile(request);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
        } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            System.err.println("프로필 업데이트 API 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "프로필 업데이트 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 업무 프로필 생성
    @PostMapping("/business-profile")
    public ResponseEntity<Map<String, Object>> createBusinessProfile(@RequestBody Map<String, Object> request) {
        try {
            String userEmail = (String) request.get("userEmail");
            String businessNickname = (String) request.get("businessNickname");
            String businessTitle = (String) request.get("businessTitle");
            String businessCompany = (String) request.get("businessCompany");
            String businessDescription = (String) request.get("businessDescription");
            String businessProfileImage = (String) request.get("businessProfileImage");
            String businessWeeTalkId = (String) request.get("businessWeeTalkId");
            
            boolean success = userService.createBusinessProfile(userEmail, businessNickname, businessTitle, 
                                                              businessCompany, businessDescription, 
                                                              businessProfileImage, businessWeeTalkId);
            
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "업무 프로필이 생성되었습니다."));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "업무 프로필 생성에 실패했습니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "업무 프로필 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 업무 프로필 조회
    @GetMapping("/business-profile")
    public ResponseEntity<Map<String, Object>> getBusinessProfile(@RequestParam String userEmail) {
        try {
            Map<String, Object> businessProfile = userService.getBusinessProfile(userEmail);
            
            if (businessProfile != null) {
                return ResponseEntity.ok(businessProfile);
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "업무 프로필이 없습니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "업무 프로필 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 업무 프로필 업데이트
    @PutMapping("/business-profile")
    public ResponseEntity<Map<String, Object>> updateBusinessProfile(@RequestBody Map<String, Object> request) {
        try {
            String userEmail = (String) request.get("userEmail");
            String businessNickname = (String) request.get("businessNickname");
            String businessTitle = (String) request.get("businessTitle");
            String businessCompany = (String) request.get("businessCompany");
            String businessDescription = (String) request.get("businessDescription");
            String businessProfileImage = (String) request.get("businessProfileImage");
            String businessBackgroundImage = (String) request.get("businessBackgroundImage");
            String businessWeeTalkId = (String) request.get("businessWeeTalkId");
            
            boolean success = userService.updateBusinessProfile(userEmail, businessNickname, businessTitle, 
                                                              businessCompany, businessDescription, 
                                                              businessProfileImage, businessBackgroundImage,
                                                              businessWeeTalkId);
            
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "업무 프로필이 업데이트되었습니다."));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "업무 프로필 업데이트에 실패했습니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "업무 프로필 업데이트 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 업무 프로필 삭제
    @DeleteMapping("/business-profile")
    public ResponseEntity<Map<String, Object>> deleteBusinessProfile(@RequestParam String userEmail) {
        try {
            boolean success = userService.deleteBusinessProfile(userEmail);
            
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "업무 프로필이 삭제되었습니다."));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "업무 프로필 삭제에 실패했습니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "업무 프로필 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 업무 사용자 검색 (업무 위톡 ID로)
    @GetMapping("/business-search")
    public ResponseEntity<Map<String, Object>> searchBusinessUser(@RequestParam String businessWeeTalkId) {
        try {
            Map<String, Object> businessProfile = userService.findBusinessUserByWeeTalkId(businessWeeTalkId);
            
            if (businessProfile != null) {
                return ResponseEntity.ok(businessProfile);
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "업무 사용자를 찾을 수 없습니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "업무 사용자 검색 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 사용자 검증 (프로필 정보 포함)
    @GetMapping("/validate-user")
    public ResponseEntity<Map<String, Object>> validateUser(
            @RequestParam String email,
            @RequestParam(required = false, defaultValue = "personal") String profileType) {
        try {
            Map<String, Object> result = userService.validateUser(email, profileType);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "사용자 검증 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 위톡 ID 생성
    @PostMapping("/create-weetalk-id")
    public ResponseEntity<Map<String, Object>> createWeeTalkId(
            @RequestParam String email,
            @RequestParam String weeTalkId,
            @RequestParam(required = false, defaultValue = "personal") String profileType) {
        try {
            Map<String, Object> result = userService.createWeeTalkId(email, weeTalkId, profileType);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "위톡 ID 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 위톡 ID로 사용자 검색
    @GetMapping("/search-by-weetalk-id")
    public ResponseEntity<Map<String, Object>> searchByWeeTalkId(
            @RequestParam String weeTalkId,
            @RequestParam(required = false, defaultValue = "personal") String profileType) {
        try {
            Map<String, Object> result = userService.searchByWeeTalkId(weeTalkId, profileType);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "사용자 검색 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 친구 추가
    @PostMapping("/add-friend")
    public ResponseEntity<Map<String, Object>> addFriend(
            @RequestParam String userEmail,
            @RequestParam String friendWeeTalkId,
            @RequestParam(required = false, defaultValue = "personal") String profileType) {
        try {
            Map<String, Object> result = userService.addFriend(userEmail, friendWeeTalkId, profileType);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "친구 추가 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 사용자 차단
    @PostMapping("/block")
    public ResponseEntity<Map<String, Object>> blockUser(@RequestBody Map<String, String> request) {
        try {
            String blockerEmail = request.get("blockerEmail");
            String blockedEmail = request.get("blockedEmail");
            
            if (blockerEmail == null || blockedEmail == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "차단할 사용자 정보가 필요합니다."));
            }
            
            Map<String, Object> result = userService.blockUser(blockerEmail, blockedEmail);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "사용자 차단 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
}
