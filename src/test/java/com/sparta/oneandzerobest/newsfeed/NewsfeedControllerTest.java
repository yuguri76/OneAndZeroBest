package com.sparta.oneandzerobest.newsfeed;

import com.amazonaws.services.s3.AmazonS3Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.oneandzerobest.auth.controller.AuthRestController;
import com.sparta.oneandzerobest.auth.email.service.EmailService;
import com.sparta.oneandzerobest.auth.entity.User;
import com.sparta.oneandzerobest.auth.entity.UserStatus;
import com.sparta.oneandzerobest.auth.repository.UserRepository;
import com.sparta.oneandzerobest.auth.service.UserDetailsServiceImpl;
import com.sparta.oneandzerobest.auth.service.UserServiceImpl;
import com.sparta.oneandzerobest.auth.util.JwtUtil;
import com.sparta.oneandzerobest.config.MockSpringSecurityFilter;
import com.sparta.oneandzerobest.config.TestJwtConfig;
import com.sparta.oneandzerobest.newsfeed.controller.NewsfeedController;
import com.sparta.oneandzerobest.newsfeed.dto.NewsfeedRequestDto;
import com.sparta.oneandzerobest.newsfeed.entity.Newsfeed;
import com.sparta.oneandzerobest.newsfeed.repository.NewsfeedRepository;
import com.sparta.oneandzerobest.newsfeed.service.NewsfeedService;
import com.sparta.oneandzerobest.s3.service.ImageService;
import java.security.Principal;
import java.util.Random;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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

import static org.mockito.BDDMockito.*;

@WebMvcTest(
    controllers = NewsfeedController.class,
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
public class NewsfeedControllerTest {

    private MockMvc mvc;


    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    NewsfeedController newsfeedController;
    @MockBean
    NewsfeedRepository newsfeedRepository;
    @MockBean
    UserDetailsServiceImpl userDetailsService;
    @MockBean
    UserServiceImpl userService;
    @MockBean
    JwtUtil jwtUtil;
    @MockBean
    PasswordEncoder passwordEncoder;
    @MockBean
    UserRepository userRepository;
    @MockBean
    Random random;
    @MockBean
    NewsfeedService newsfeedService;
    @Autowired
    TestJwtConfig testJwtConfig;

    @MockBean
    ImageService imageService;
    @MockBean
    AmazonS3Client amazonS3Client;

    final String USERNAME = "Seokjoon123";
    final String PASSWORD = "1234@123aaaa";
    final String EMAIL = "tjrwns3428@gmail.com";

    @BeforeAll
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity(new MockSpringSecurityFilter()))
            .build();

        JwtUtil.init(testJwtConfig);

        String encodedPassword = passwordEncoder.encode(PASSWORD);
        User user = new User(USERNAME, encodedPassword, USERNAME, EMAIL, UserStatus.ACTIVE);
        userRepository.save(user);

        Newsfeed newsfeed = new Newsfeed(1L,"Newsfeed Content");
        newsfeedRepository.save(newsfeed);
    }


    @Test
    @DisplayName("뉴스피드 생성")
    @Order(1)
    void test_postNewsfeed() throws Exception{
        //given
        String token ="Bearer "+jwtUtil.createAccessToken(USERNAME);

        String content = "newsfeeed Content";
        NewsfeedRequestDto newsfeedRequestDto = new NewsfeedRequestDto();
        newsfeedRequestDto.setContent(content);

        //when - then
        mvc.perform(MockMvcRequestBuilders.post("/newsfeed")
            .header("Authorization", token)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(newsfeedRequestDto)))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("뉴스피드 조회")
    @Order(2)
    void test_getAllNewsfeed() throws Exception{
        // given
        int page = 0;
        int size = 10;
        boolean isASC = false;
        boolean like = false;

        String token ="Bearer "+jwtUtil.createAccessToken(USERNAME);

        // when - then
        mvc.perform(MockMvcRequestBuilders.get("/newsfeed")
            .param("page",String.valueOf(page))
            .param("size",String.valueOf(size))
            .param("isASC",String.valueOf(isASC))
            .param("like",String.valueOf(like))
            .header("Authorization",token))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("뉴스피드 수정")
    @Order(3)
    void test_putNewwsfeed() throws Exception{
        // given
        Long id = 1L;
        String token ="Bearer "+jwtUtil.createAccessToken(USERNAME);

        String content = "newsfeeed Content";
        NewsfeedRequestDto newsfeedRequestDto = new NewsfeedRequestDto();
        newsfeedRequestDto.setContent(content);

        // when - then
        mvc.perform(MockMvcRequestBuilders.put("/newsfeed/{id}", id)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(newsfeedRequestDto))
                .header("Authorization", token))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("뉴스피드 삭제")
    @Order(4)
    void test_deleteNewsfeed() throws Exception{
        // given
        Long id = 1L;
        String token ="Bearer "+jwtUtil.createAccessToken(USERNAME);

        // when - then
        mvc.perform(MockMvcRequestBuilders.delete("/newsfeed/{id}", id)
                .header("Authorization", token))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("사진 업로드")
    @Order(5)
    void test_uploadImageToNewsfeed() throws  Exception{
        // given
        Long id = 1L;
        String token ="Bearer "+jwtUtil.createAccessToken(USERNAME);

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        // when - then
        mvc.perform(MockMvcRequestBuilders.multipart("/newsfeed/media")
                .file(file)
                .param("id", String.valueOf(id))
                .header("Authorization", token))
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("사진 수정")
    @Order(6)
    void test_updateImageToNewsfeed() throws Exception{
        // given
        Long id = 1L;
        Long fileId = 1L;
        String token ="Bearer "+jwtUtil.createAccessToken(USERNAME);

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-image-update.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        // when - then
        mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/newsfeed/media")
                .file(file)
                .param("id", String.valueOf(id))
                .param("fileid", String.valueOf(fileId))
                .header("Authorization", token))
            .andDo(MockMvcResultHandlers.print());
    }

}
