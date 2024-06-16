package com.sparta.oneandzerobest.user;

import com.sparta.oneandzerobest.auth.dto.TokenResponseDto;
import com.sparta.oneandzerobest.auth.email.service.EmailService;
import com.sparta.oneandzerobest.auth.entity.LoginRequest;
import com.sparta.oneandzerobest.auth.entity.LoginResponse;
import com.sparta.oneandzerobest.auth.entity.SignupRequest;
import com.sparta.oneandzerobest.auth.entity.User;
import com.sparta.oneandzerobest.auth.entity.UserStatus;
import com.sparta.oneandzerobest.auth.repository.UserRepository;
import com.sparta.oneandzerobest.auth.service.UserServiceImpl;
import com.sparta.oneandzerobest.auth.util.JwtUtil;
import com.sparta.oneandzerobest.config.TestJwtConfig;
import com.sparta.oneandzerobest.s3.service.ImageService;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;


@SpringBootTest(properties = {"spring.profiles.active=test"})
@TestPropertySource(locations = "classpath:application-test.yml")
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
@Import({TestJwtConfig.class })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class UserServcieTest {


    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    EmailService emailService;
    @MockBean
    JwtUtil jwtUtil;
    @Autowired
    ImageService imageService;

    @Autowired
    TestJwtConfig testJwtConfig;
    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Mock
    Random random;

    static final String USERNAME = "Seokjoon123";
    static final String PASSWORD = "1234@123aaaa";
    static final String EMAIL = "tjrwns3428@gmail.com";
    String verificationCode;
    UserServiceImpl userService;

    @BeforeAll
    void setUp() {
        verificationCode = String.valueOf(100000 + random.nextInt(900000));
        JwtUtil.init(testJwtConfig);
        userService = new UserServiceImpl(userRepository, passwordEncoder,
            emailService, redisTemplate, jwtUtil);
        String encodedPassword = passwordEncoder.encode(PASSWORD);
        User user = new User(USERNAME, encodedPassword, USERNAME, EMAIL, UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Test
    @DisplayName("회원가입 성공")
    @Order(1)
    void signup_success() {
        // given
        String username = "Seokjoon1234";
        String password = "1234@123aaaa";
        String email = "nellucia@naver.com";
        boolean isAdmin = false;
        String adminToken = "";

        SignupRequest signupRequest = new SignupRequest(username, password, email, isAdmin,
            adminToken);

        // when

        ResponseEntity<String> resultEntity = userService.signup(signupRequest);

        // then
        assertEquals(resultEntity.getStatusCode(), HttpStatus.OK);
        assertEquals(resultEntity.getBody(), "회원가입 성공");
    }

    @Test
    @DisplayName("로그인 성공")
    @Order(2)
    void login_success() {
        //given
        LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD);
        String createdAccessToken = jwtUtil.createAccessToken(USERNAME);
        String createdRefreshToken = jwtUtil.createRefreshToken(USERNAME);

        //when
        LoginResponse loginResponse = userService.login(loginRequest);

        // then
        assertEquals(loginResponse.getAccessToken(), createdAccessToken);
        assertEquals(loginResponse.getRefreshToken(), createdRefreshToken);
    }

    @Test
    @DisplayName("로그아웃 성공")
    @Order(5)
    void logout_success() {
        // given
        String username = USERNAME;
        String accessToken = jwtUtil.createAccessToken(username);
        String refreshToken = jwtUtil.createRefreshToken(username);

        // when
        ResponseEntity<String> logoutResponse = userService.logout(username, accessToken,
            refreshToken);

        // then

        assertEquals(logoutResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(logoutResponse.getBody(), "로그아웃 성공");
    }

    @Test
    @DisplayName("회월탈퇴 성공")
    @Order(4)
    void withdraw_success() {
        // given
        String username = USERNAME;
        String password = PASSWORD;
        String accessToken = jwtUtil.createAccessToken(username);
        String refreshToken = jwtUtil.createRefreshToken(username);


        // when
        ResponseEntity<String> withdrawResponse = userService.withdraw(username, password,
            accessToken, refreshToken);

        // then
        assertEquals(withdrawResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(withdrawResponse.getBody(), "회원탈퇴 성공");
    }

    @Test
    @DisplayName("리프레시 토큰 재발급 성공")
    @Order(3)
    void refresh_success() {
        // given
        String accessToken = jwtUtil.createAccessToken(USERNAME);
        String refreshToken = jwtUtil.createRefreshToken(USERNAME);

        User user = userRepository.findByUsername(USERNAME).orElse(null);
        user.updateRefreshToken(refreshToken);

        // when
        TokenResponseDto refreshResponse = userService.refresh(refreshToken);

        // then
        assertEquals(refreshResponse.getRefreshToken(), refreshToken);
        assertEquals(refreshResponse.getAccessToken(), accessToken);
    }

    @Test
    @DisplayName("이메일 인증 성공")
    @Order(6)
    void verifyEmail_success() {
        // given
        String verificationCode = String.valueOf(100000 + random.nextInt(900000));
        redisTemplate.opsForValue().set(USERNAME, verificationCode, 3, TimeUnit.MINUTES);

        // when
        boolean response = userService.verifyEmail(USERNAME, verificationCode);

        // then
        assertTrue(response);
    }

    @Test
    @DisplayName("이메일 업데이트 성공")
    @Order(7)
    void updateEmail_success() {
        // given

        String username = USERNAME;
        String password = PASSWORD;
        String email = EMAIL;
        boolean isAdmin = false;
        String adminToken = "";

        SignupRequest signupRequest = new SignupRequest(username, password, email, isAdmin,
            adminToken);

        // when
        userService.updateEmail(signupRequest);
    }

    @Test
    @DisplayName("OAuth 로그인 성공")
    @Order(8)
    void loginWithOAuth_success() {
        // given
        String email = EMAIL;
        String accessToken = jwtUtil.createAccessToken(USERNAME);
        String refreshToken = jwtUtil.createRefreshToken(USERNAME);

        // when
        LoginResponse response = userService.loginWithOAuth(email);

        // then
        assertEquals(response.getAccessToken(), accessToken);
        assertEquals(response.getRefreshToken(), refreshToken);
    }


}
