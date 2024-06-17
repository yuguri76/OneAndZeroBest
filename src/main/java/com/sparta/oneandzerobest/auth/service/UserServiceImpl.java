package com.sparta.oneandzerobest.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.oneandzerobest.auth.dto.TokenResponseDto;
import com.sparta.oneandzerobest.auth.email.service.EmailService;
import com.sparta.oneandzerobest.auth.entity.*;
import com.sparta.oneandzerobest.auth.repository.UserRepository;
import com.sparta.oneandzerobest.auth.util.JwtUtil;
import com.sparta.oneandzerobest.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;
    private final Random random = new Random();

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Value("${app.email.verification.expiry}")
    private long verificationExpiry;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, RedisTemplate<String, String> redisTemplate, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 회원가입: ID, PW, Email
     *
     * @param signupRequest
     */
    @Override
    public ResponseEntity<String> signup(SignupRequest signupRequest) {
        String authId = signupRequest.getUsername();
        String password = signupRequest.getPassword();
        String email = signupRequest.getEmail();

        if (!authId.matches("^[a-zA-Z0-9]{10,20}$")) {
            throw new IdPatternException("아이디는 최소 10자 이상, 20자 이하이며 알파벳 대소문자와 숫자로 구성되어야 합니다.");
        }

        if (!password.matches("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*]{10,}$")) {
            throw new PasswordPatternException();
        }

        // if (userRepository.findByUsername(authId).isPresent()) {
        Optional<User> existingUser = userRepository.findByUsername(authId);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getStatusCode().equals(UserStatus.UNVERIFIED)) {
                // 인증 전 상태일 때는 이메일을 업데이트하고 새로운 인증 이메일을 보냄
                updateEmail(signupRequest);
                 return ResponseEntity.ok("이메일 재 전송");
            }
            throw new InfoNotCorrectedException("중복된 사용자 ID가 존재합니다.");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new InfoNotCorrectedException("중복된 이메일이 존재합니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(authId, encodedPassword, signupRequest.getUsername(), email, UserStatus.UNVERIFIED);
        userRepository.save(user);
        sendVerificationEmail(user);
        return ResponseEntity.ok("회원가입 성공");
    }

    /**
     * 인증 코드 생성: 6자리 랜덤 숫자를 생성
     *
     * @return 인증 코드
     */
    private String generateVerificationCode() {
        return String.valueOf(100000 + random.nextInt(900000));
    }

    /**
     * 인증 이메일 발송: 이메일로 인증 코드를 발송
     *
     * @param user 회원 정보
     */
    private void sendVerificationEmail(User user) {
        String verificationCode = generateVerificationCode();
        // Redis에 인증 코드를 저장하고 3분 유지
        redisTemplate.opsForValue().set(user.getUsername(), verificationCode, 3, TimeUnit.MINUTES);
        String text = String.format("귀하의 인증 코드는 %s 입니다.", verificationCode);
        emailService.sendEmail(user.getEmail(), "이메일 인증", text);
    }

    /**
     * 로그인: ACCESS TOKEN, REFRESH TOKEN 생성
     * 비밀번호: 암호화
     * 탈퇴한 사람 분별
     *
     * @param loginRequest
     * @return
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new InfoNotCorrectedException("사용자 ID와 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("사용자 ID와 비밀번호가 일치하지 않습니다.");
        }

        if (UserStatus.WITHDRAWN.equals(user.getStatusCode())) {
            throw new InfoNotCorrectedException("탈퇴한 사용자입니다.");
        }

        if (UserStatus.UNVERIFIED.equals(user.getStatusCode())) {
            throw new InfoNotCorrectedException("이메일 인증이 필요합니다.");
        }
        String accessToken = jwtUtil.createAccessToken(user.getUsername());
        String refreshToken = jwtUtil.createRefreshToken(user.getUsername());

        user.updateRefreshToken(refreshToken);
        userRepository.save(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 로그아웃: 리프레시 토큰 삭제
     *
     * @param username
     */
    @Override
    public ResponseEntity<String> logout(String username, String accessToken, String refreshToken) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InfoNotCorrectedException("사용자를 찾을 수 없습니다."));

        user.clearRefreshToken();

        // 액세스 토큰을 블랙리스트에 추가
        jwtUtil.addblacklistToken(accessToken);
        jwtUtil.addblacklistToken(refreshToken);

        userRepository.save(user);

        return ResponseEntity.ok("로그아웃 성공");
    }

    /**
     * 회원 탈퇴 -> 리프레시 토큰 삭제, getStatusCode: 탈퇴
     *
     * @param id:       아이디
     * @param password: 비밀번호
     */
    @Override
    public ResponseEntity<String> withdraw(String id, String password, String accessToken, String refreshToken) {
        User user = userRepository.findByUsername(id)
                .orElseThrow(() -> new InfoNotCorrectedException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IncorrectPasswordException();
        }

        if ("탈퇴".equals(user.getStatusCode())) {
            throw new InfoNotCorrectedException("이미 탈퇴한 사용자입니다.");
        }

        user.withdraw();
        user.clearRefreshToken();
        jwtUtil.addblacklistToken(accessToken);
        jwtUtil.addblacklistToken(refreshToken);

        userRepository.save(user);

        return ResponseEntity.ok("회원탈퇴 성공");
    }

    /**
     * 리프레시 토큰으로 토큰 재발급
     *
     * @param refreshToken
     * @return
     */
    @Override
    public TokenResponseDto refresh(String refreshToken) {
        if (jwtUtil.isTokenBlacklisted(refreshToken)) {
            throw new IllegalArgumentException("Refresh token is blacklisted.");
        }
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InfoNotCorrectedException("사용자를 찾을 수 없습니다."));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new IncorrectPasswordException();
        }

        String newAccessToken = jwtUtil.createAccessToken(username);
        String newRefreshToken = jwtUtil.createRefreshToken(username);

        user.clearRefreshToken();
        userRepository.save(user);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }

    /**
     * 이메일 인증: 입력한 인증 코드를 검증
     *
     * @param username         사용자 이름
     * @param verificationCode 입력한 인증 코드
     * @return 인증 성공 여부
     */
    @Override
    public boolean verifyEmail(String username, String verificationCode) {
        String storedCode = redisTemplate.opsForValue().get(username);
        if (storedCode != null && storedCode.equals(verificationCode)) {
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.updateStatus(UserStatus.ACTIVE);  // 인증이 성공하면 정상
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    /**
     * 회원가입중 이메일이 잘못되었을 경우 추가
     *
     * @param signupRequest
     */
    @Override
    public void updateEmail(SignupRequest signupRequest) {
        User user = userRepository.findByUsername(signupRequest.getUsername())
                .orElseThrow(() -> new InfoNotCorrectedException("사용자를 찾을 수 없습니다."));
        user.updateEmail(signupRequest.getEmail());
        userRepository.save(user);
        sendVerificationEmail(user);
    }

    /**
     * OAuth으로 로그인하기 - 이메일
     *
     * @param email
     * @return
     */
    @Override
    public LoginResponse loginWithOAuth(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InfoNotCorrectedException("이메일을 찾을 수 없습니다."));

        String accessToken = jwtUtil.createAccessToken(user.getUsername());
        String refreshToken = jwtUtil.createRefreshToken(user.getUsername());

        user.updateRefreshToken(refreshToken);
        userRepository.save(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 카카오 로그인- 사용자 정보 저장
     *
     * @param userInfoJson
     * @return
     */
    @Override
    public User saveOrUpdateKakaoUser(String userInfoJson) {
        try {
            JsonNode userInfo = objectMapper.readTree(userInfoJson);

            long kakaoId = userInfo.path("id").asLong();

            String nickname = userInfo.path("properties").path("nickname").asText();
            String name = nickname + "-kakaoEmail";
            String email = kakaoId + "aA@naver.com";

            User user = userRepository.findByEmail(email).orElse(new User());
            user.updateKakaoUser(kakaoId, name, nickname, email, UserStatus.ACTIVE);

            // JWT 토큰 생성
            String refreshToken = jwtUtil.createRefreshToken(user.getUsername());

            user.updateRefreshToken(refreshToken);
            return userRepository.save(user);

        } catch (IOException e) {
            throw new InfoNotCorrectedException("사용자 정보 불러오기 실패");
        }
    }

    @Override
    public User saveOrUpdateGoogleUser(String userInfoJson) {
        try {
            JsonNode userInfo = objectMapper.readTree(userInfoJson);

            long googleId = userInfo.path("id").asLong();
            String email = userInfo.path("email").asText();
            String nickname = userInfo.path("name").asText();

            // email이 없으면 nickname으로 email 생성
            if (email == null || email.isEmpty()) {
                email = nickname + "@google.com";
            }

            String name = email + "-name";

            User user = userRepository.findByEmail(email).orElse(new User());
            user.updateGoogleUser(googleId, nickname, name, email, UserStatus.ACTIVE);

            String refreshToken = jwtUtil.createRefreshToken(user.getUsername());
            user.updateRefreshToken(refreshToken);

            return userRepository.save(user);

        } catch (IOException e) {
            throw new InfoNotCorrectedException("사용자 정보 불러오기 실패");
        }
    }

    @Override
    public User saveOrUpdateGithubUser(String userInfoJson) {
        try {
            JsonNode userInfo = objectMapper.readTree(userInfoJson);
            long githubId = userInfo.path("id").asLong();
            String nickname = userInfo.path("name").asText();
            String email = nickname + "@github.com";

            String name = nickname + "-githubUser";

            User user = userRepository.findByEmail(email).orElse(new User());
            user.updateGithubUser(githubId, nickname, name, email, UserStatus.ACTIVE);

            String refreshToken = jwtUtil.createRefreshToken(user.getUsername());
            user.updateRefreshToken(refreshToken);
            return userRepository.save(user);

        } catch (IOException e) {
            throw new InfoNotCorrectedException("사용자 정보 불러오기 실패");
        }
    }
}
