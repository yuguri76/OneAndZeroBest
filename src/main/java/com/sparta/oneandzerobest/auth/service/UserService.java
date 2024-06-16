package com.sparta.oneandzerobest.auth.service;

import com.sparta.oneandzerobest.auth.dto.TokenResponseDto;
import com.sparta.oneandzerobest.auth.entity.LoginRequest;
import com.sparta.oneandzerobest.auth.entity.LoginResponse;
import com.sparta.oneandzerobest.auth.entity.SignupRequest;
import com.sparta.oneandzerobest.auth.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService {
    // 회원가입
    ResponseEntity<String> signup(SignupRequest signupRequest);

    // 로그인
    LoginResponse login(LoginRequest loginRequest);

    // 로그아웃
    ResponseEntity<String> logout(String username, String accessToken, String refreshToken);

    // 탈퇴
    ResponseEntity<String> withdraw(String username, String password, String accessToken, String refreshToken);

    // 리프레시 토큰
    TokenResponseDto refresh(String refreshToken);

    // 이메일 인증
    boolean verifyEmail(String username, String verificationCode);

    // 이메일 업데이트 - 이메일이 잘못된 회원가입
    void updateEmail(SignupRequest signupRequest);

    // OAuth으로 로그인
    LoginResponse loginWithOAuth(String email);

    // 카카오 유저 정보 저장
    User saveOrUpdateKakaoUser(String userInfoJson);
    User saveOrUpdateGoogleUser(String userInfoJson);
    User saveOrUpdateGithubUser(String userInfoJson);
}