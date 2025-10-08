package com.weeTalk.weeTalk.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationService {

    @Autowired
    private EmailService emailService;

    // 인증코드 저장 (실제로는 Redis나 DB 사용 권장)
    private final Map<String, VerificationData> verificationCodes = new ConcurrentHashMap<>();
    
    // 인증코드 유효기간: 10분 (밀리초)
    private static final long VERIFICATION_CODE_EXPIRY_TIME = 10 * 60 * 1000; // 10분

    // 이메일 인증코드 전송
    public String sendVerificationCode(String email) {
        // 기존 인증코드 제거
        verificationCodes.remove(email);
        
        // 새 인증코드 발송
        String code = emailService.sendVerificationCode(email);
        
        // 인증코드 저장 (10분 유효)
        long expiryTime = System.currentTimeMillis() + VERIFICATION_CODE_EXPIRY_TIME;
        verificationCodes.put(email, new VerificationData(code, expiryTime));
        
        System.out.println("인증코드 발송 완료: " + email + " -> " + code + " (만료시간: " + expiryTime + ", 유효기간: 10분)");
        
        return code;
    }

    // 인증코드 검증
    public boolean verifyCode(String email, String code) {
        VerificationData data = verificationCodes.get(email);
        
        if (data == null) {
            System.out.println("인증코드 검증 실패: 인증코드가 없음 - " + email);
            return false; // 인증코드가 없음
        }
        
        long currentTime = System.currentTimeMillis();
        long remainingTime = data.getExpiryTime() - currentTime;
        
        if (currentTime > data.getExpiryTime()) {
            verificationCodes.remove(email);
            System.out.println("인증코드 검증 실패: 만료됨 - " + email + " (만료시간: " + data.getExpiryTime() + ", 현재시간: " + currentTime + ")");
            return false; // 인증코드 만료
        }
        
        if (data.getCode().equals(code)) {
            // 인증 성공 시 인증된 이메일로 표시
            verificationCodes.put(email, new VerificationData("VERIFIED", Long.MAX_VALUE));
            System.out.println("인증코드 검증 성공: " + email + " (남은시간: " + (remainingTime / 1000) + "초)");
            return true; // 인증 성공
        }
        
        System.out.println("인증코드 검증 실패: 코드 불일치 - " + email + " (입력: " + code + ", 저장: " + data.getCode() + ")");
        return false; // 인증코드 불일치
    }
    
    // 이메일 인증 상태 확인
    public boolean isEmailVerified(String email) {
        VerificationData data = verificationCodes.get(email);
        return data != null && "VERIFIED".equals(data.getCode());
    }


    // 인증 코드 생성
    private String generateVerificationCode() {
        return String.valueOf(100000 + (int)(Math.random() * 900000)); // 6자리 숫자
    }

    // 인증 데이터 클래스
    private static class VerificationData {
        private final String code;
        private final long expiryTime;

        public VerificationData(String code, long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }

        public String getCode() {
            return code;
        }

        public long getExpiryTime() {
            return expiryTime;
        }
    }
}
