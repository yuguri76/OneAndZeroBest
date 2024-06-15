package com.sparta.oneandzerobest.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.oneandzerobest.auth.controller.AuthRestController;
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
import java.security.Principal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
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
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(
    controllers = AuthRestController.class,
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = WebSecurity.class
        )
    }
)
@TestInstance(Lifecycle.PER_CLASS)
@TestPropertySource(locations = "classpath:application-test.yml")
public class AuthRestControllerTest {

    private MockMvc mvc;

    private Principal mockPrincipal;

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;


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

    final String USERNAME = "Seokjoon123";
    final String PASSWORD = "1234@123aaaa";
    final String EMAIL = "tjrwns3428@gmail.com";
    final String name = "";

    private void mockUserSetup(){

        String encodedPassword = passwordEncoder.encode(PASSWORD);
        User user = new User(USERNAME, encodedPassword, USERNAME, EMAIL, UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @BeforeAll
    void setUp(){
        mvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity(new MockSpringSecurityFilter()))
            .build();


    }

    @Test
    @DisplayName("회원가입")
    void test_signup() throws Exception {

        String username = "Seokjoon1234";
        String password = "1234@123aaaa";
        String email = "nellucia@naver.com";
        boolean isAdmin= false;
        String adminToken = "";
        SignupRequest signupRequest = new SignupRequest(username,password,email,isAdmin,adminToken);


        mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("로그인")
    void test_login() throws Exception {
        this.mockUserSetup();
        LoginRequest loginRequest = new LoginRequest(USERNAME,PASSWORD);

        mvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andDo(MockMvcResultHandlers.print());

    }


}
