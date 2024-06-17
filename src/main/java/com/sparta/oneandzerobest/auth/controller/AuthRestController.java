package com.sparta.oneandzerobest.auth.controller;


import com.sparta.oneandzerobest.auth.dto.RefreshTokenRequestDto;
import com.sparta.oneandzerobest.auth.dto.TokenResponseDto;
import com.sparta.oneandzerobest.auth.entity.LoginRequest;
import com.sparta.oneandzerobest.auth.entity.LoginResponse;
import com.sparta.oneandzerobest.auth.entity.SignupRequest;
import com.sparta.oneandzerobest.auth.service.UserService;
import com.sparta.oneandzerobest.auth.util.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증기능 컨트롤러
 * - 로그인
 * - 로그아웃
 * - 탈퇴
 * - 리프레시 토큰 재발급
 */
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthRestController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 회원가입
     * @param signupRequest
     * @return
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest signupRequest) {
        return userService.signup(signupRequest);
    }

    /**
     * 로그인
     * @param loginRequest
     * @return 헤더에 반환
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse tokens = userService.login(loginRequest); // 로그인 시도 및 토큰 생성
        String accessToken = tokens.getAccessToken();
        String refreshToken = tokens.getRefreshToken();

        // 각 토큰을 별도의 헤더에 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Refresh-Token", refreshToken);

        return new ResponseEntity<>("로그인 성공", headers, HttpStatus.OK);
    }

    /**
     * 로그아웃
     * @param username
     * @return
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String username,String accessToken,String refreshToken) {
        return userService.logout(username, accessToken, refreshToken);
    }

    /**
     *withdraw: 탈퇴
     * @param username
     * @param password
     * @return
     */
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestParam String username, @RequestParam String password,String accessToken,String refreshToken) {
        return userService.withdraw(username, password, accessToken,refreshToken);
    }

    /**
     * 리프레시 토큰 재발급
     * @param refreshTokenRequestDto
     * @return
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        String refreshToken = refreshTokenRequestDto.getRefreshToken();
        String newAccessToken = jwtUtil.refreshToken(refreshToken);
        TokenResponseDto tokenResponseDto = new TokenResponseDto(newAccessToken, refreshToken);
        return ResponseEntity.ok(tokenResponseDto);
    }

    /**
     * 이메일 인증
     * @param username 사용자 이름
     * @param verificationCode 인증 코드
     * @return 인증 성공 또는 실패 메시지
     */
    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String username, @RequestParam String verificationCode) {
        boolean isVerified = userService.verifyEmail(username, verificationCode);
        if (isVerified) {
            return ResponseEntity.ok("이메일 인증 성공");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이메일 인증 실패");
        }
    }
}
