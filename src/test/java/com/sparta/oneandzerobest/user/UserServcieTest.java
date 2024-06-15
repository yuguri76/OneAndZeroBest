package com.sparta.oneandzerobest.user;

import com.sparta.oneandzerobest.auth.config.JwtConfig;
import com.sparta.oneandzerobest.auth.email.service.EmailService;
import com.sparta.oneandzerobest.auth.entity.LoginRequest;
import com.sparta.oneandzerobest.auth.entity.LoginResponse;
import com.sparta.oneandzerobest.auth.entity.SignupRequest;
import com.sparta.oneandzerobest.auth.entity.User;
import com.sparta.oneandzerobest.auth.entity.UserStatus;
import com.sparta.oneandzerobest.auth.repository.UserRepository;
import com.sparta.oneandzerobest.auth.service.UserServiceImpl;
import com.sparta.oneandzerobest.auth.util.JwtUtil;
import com.sparta.oneandzerobest.config.MockSpringSecurityFilter;
import java.util.Date;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;



import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
//@SpringBootTest(properties = {"spring.profiles.active=test"})
@TestPropertySource(locations = "classpath:application-test.yml")
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
public class UserServcieTest {

    @Autowired
    private Environment env;

    @MockBean
    UserServiceImpl userService;
    @MockBean
    UserRepository userRepository;
    @MockBean
    PasswordEncoder passwordEncoder;
    @MockBean
    EmailService emailService;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    JwtConfig jwtConfig;

    static final String USERNAME = "Seokjoon123";
    static final String PASSWORD = "1234@123aaaa";
    static final String EMAIL = "tjrwns3428@gmail.com";

    @BeforeAll
    void setUp() {


        System.out.println(
            "env.getProperty(\"jwt.test-secret.key\") = " + env.getProperty("jwt.test-secret.key"));

        String encodedPassword = passwordEncoder.encode(PASSWORD);
        User user = new User(USERNAME, encodedPassword, USERNAME, EMAIL, UserStatus.ACTIVE);
        userRepository.save(user);
        jwtConfig.init();
        System.out.println("jwtConfig.getSecretKey() = " + jwtConfig.getSecretKey());


    }

    @Test
    void test_login() {

        LoginRequest loginRequest = new LoginRequest(USERNAME,PASSWORD);

        LoginResponse token = userService.login(loginRequest);


        //assertNotNull(token);



    }

}
