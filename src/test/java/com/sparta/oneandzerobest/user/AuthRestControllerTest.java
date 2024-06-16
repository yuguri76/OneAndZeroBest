package com.sparta.oneandzerobest.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.oneandzerobest.auth.controller.AuthRestController;
import com.sparta.oneandzerobest.auth.dto.RefreshTokenRequestDto;
import com.sparta.oneandzerobest.auth.email.service.EmailService;
import com.sparta.oneandzerobest.auth.entity.LoginRequest;
import com.sparta.oneandzerobest.auth.entity.LoginResponse;
import com.sparta.oneandzerobest.auth.entity.SignupRequest;
import com.sparta.oneandzerobest.auth.entity.User;
import com.sparta.oneandzerobest.auth.entity.UserStatus;
import com.sparta.oneandzerobest.auth.repository.UserRepository;
import com.sparta.oneandzerobest.auth.service.UserDetailsServiceImpl;
import com.sparta.oneandzerobest.auth.service.UserServiceImpl;
import com.sparta.oneandzerobest.auth.util.JwtUtil;
import com.sparta.oneandzerobest.config.MockSpringSecurityFilter;
import com.sparta.oneandzerobest.config.TestJwtConfig;
import java.security.Principal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.BDDMockito.*;

@WebMvcTest(
    controllers = AuthRestController.class,
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = WebSecurity.class
        )
    }
)
@TestPropertySource(locations = "classpath:application-test.yml")
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
@Import(TestJwtConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthRestControllerTest {

    private MockMvc mvc;

    private Principal mockPrincipal;

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;


    @MockBean
    AuthRestController authRestController;
    @MockBean
    UserDetailsServiceImpl userDetailsService;
    @MockBean
    UserServiceImpl userService;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    RedisTemplate<String, String> redisTemplate;
    @MockBean
    EmailService emailService;
    @MockBean
    PasswordEncoder passwordEncoder;
    @MockBean
    UserRepository userRepository;
    @Autowired
    TestJwtConfig testJwtConfig;

    final String USERNAME = "Seokjoon123";
    final String PASSWORD = "1234@123aaaa";
    final String EMAIL = "tjrwns3428@gmail.com";


    @BeforeAll
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity(new MockSpringSecurityFilter()))
            .build();

        JwtUtil.init(testJwtConfig);
        this.setupUser();
    }

    void setupUser(){
        String encodedPassword = passwordEncoder.encode(PASSWORD);
        User user = new User(USERNAME, encodedPassword, USERNAME, EMAIL, UserStatus.ACTIVE);

        userRepository.save(user);
    }

    @Test
    @DisplayName("회원가입")
    @Order(1)
    void test_signup() throws Exception {

        //given
        String username = "Seokjoon1234";
        String password = "1234@123aaaa";
        String email = "nellucia@naver.com";
        boolean isAdmin = false;
        String adminToken = "";
        SignupRequest signupRequest = new SignupRequest(username, password, email, isAdmin,
            adminToken);

        //when then
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("로그인")
    @Order(2)
    void test_login() throws Exception {
        //given


        LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD);

        //when then
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andDo(MockMvcResultHandlers.print());

    }

    @Test
    @DisplayName("로그아웃")
    @Order(3)
    void test_logout() throws Exception {
        // given

        String accessToken = jwtUtil.createAccessToken(USERNAME);
        String refreshToken = jwtUtil.createRefreshToken(USERNAME);

        MultiValueMap<String, String> paramRequestMap = new LinkedMultiValueMap<>();
        paramRequestMap.add("username", USERNAME);
        paramRequestMap.add("accessToken", accessToken);
        paramRequestMap.add("refreshToken", refreshToken);

        //when -then
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/logout")
                .contentType("application/json")
                .params(paramRequestMap))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("회원탈퇴")
    @Order(4)
    void test_withdraw() throws Exception {
        // given
        String username= USERNAME;
        String password = PASSWORD;
        String accessToken = jwtUtil.createAccessToken(USERNAME);
        String refreshToken = jwtUtil.createRefreshToken(USERNAME);

        MultiValueMap<String,String> paramRequestMap = new LinkedMultiValueMap<>();
        paramRequestMap.add("username", username);
        paramRequestMap.add("password", password);
        paramRequestMap.add("accessToken", accessToken);
        paramRequestMap.add("refreshToekn", refreshToken);

        // when -then

        mvc.perform(MockMvcRequestBuilders.post("/api/auth/withdraw")
            .params(paramRequestMap))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andDo(MockMvcResultHandlers.print());

    }

    @Test
    @DisplayName("리프레시 토큰 재발급")
    @Order(5)
    void test_refresh() throws Exception {
        //given
        String refreshToken = jwtUtil.createRefreshToken(USERNAME);
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto();
        requestDto.setRefreshToken(refreshToken);

        //when -then
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/refresh")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestDto)))
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("이메일 인증")
    @Order(6)
    void test_verifyEmail(){
        //given
        String username = USERNAME;
        String verificationCode = "";

    }
}
