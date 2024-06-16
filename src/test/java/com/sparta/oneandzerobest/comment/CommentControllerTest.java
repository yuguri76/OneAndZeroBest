package com.sparta.oneandzerobest.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.oneandzerobest.auth.entity.User;
import com.sparta.oneandzerobest.auth.entity.UserStatus;
import com.sparta.oneandzerobest.auth.repository.UserRepository;
import com.sparta.oneandzerobest.auth.service.UserDetailsServiceImpl;
import com.sparta.oneandzerobest.auth.util.JwtUtil;
import com.sparta.oneandzerobest.comment.controller.CommentController;
import com.sparta.oneandzerobest.comment.dto.CommentRequestDto;
import com.sparta.oneandzerobest.comment.entity.Comment;
import com.sparta.oneandzerobest.comment.repository.CommentRepository;
import com.sparta.oneandzerobest.comment.service.CommentService;
import com.sparta.oneandzerobest.config.MockSpringSecurityFilter;
import com.sparta.oneandzerobest.config.TestJwtConfig;
import com.sparta.oneandzerobest.newsfeed.entity.Newsfeed;
import com.sparta.oneandzerobest.newsfeed.repository.NewsfeedRepository;
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
    controllers = CommentController.class,
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
public class CommentControllerTest {

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    PasswordEncoder passwordEncoder;
    @MockBean
    UserDetailsServiceImpl userDetailsService;
    @MockBean
    CommentController commentController;
    @MockBean
    CommentService commentService;
    @MockBean
    CommentRepository commentRepository;
    @MockBean
    UserRepository userRepository;
    @MockBean
    NewsfeedRepository newsfeedRepository;
    @MockBean
    JwtUtil jwtUtil;

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

        String encodedPassword = passwordEncoder.encode(PASSWORD);
        User user = new User(USERNAME, encodedPassword, USERNAME, EMAIL, UserStatus.ACTIVE);
        userRepository.save(user);

        Newsfeed newsfeed = new Newsfeed(1L, "Newsfeed Content");
        newsfeedRepository.save(newsfeed);

        Comment comment = new Comment(1L, 1L, "Comment Content");
        commentRepository.save(comment);

    }

    @Test
    @DisplayName("댓글 생성")
    @Order(1)
    void test_createComment() throws Exception {
        // given
        String token = "Bearer " + jwtUtil.createAccessToken(USERNAME);

        Long id = 1L; // 뉴스피드 id
        String content = "Comment Content";
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setNewsfeedId(id);
        commentRequestDto.setContent(content);

        // when - then
        mvc.perform(MockMvcRequestBuilders.post("/newsfeed/{newsfeedId}/comment", id)
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(commentRequestDto)))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("댓글 조회")
    @Order(2)
    void test_getAllComments() throws Exception {
        // given
        Long id = 1L; // 뉴스피드 id
        String token = "Bearer " + jwtUtil.createAccessToken(USERNAME);

        // when - then

        mvc.perform(MockMvcRequestBuilders.get("/newsfeed/{newsfeedID}/comment", id)
                .header("Authorization", token))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("댓글 수정")
    @Order(3)
    void test_updateCommnet() throws Exception {
        // given
        Long newsfeedId = 1L; // 뉴스피드 id
        Long commentId = 1L; // 코멘트 id
        String content = "update Comment Content";

        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setNewsfeedId(newsfeedId);
        requestDto.setContent(content);

        String token = "Bearer " + jwtUtil.createAccessToken(USERNAME);

        // when - then
        mvc.perform(
                MockMvcRequestBuilders.put("/newsfeed/{newsfeedId}/comment/{commentId}", newsfeedId,
                        commentId)
                    .header("Authorization", token)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("댓글 삭제")
    @Order(4)
    void test_deleteComment() throws Exception {
        // given
        Long newsfeedId = 1L;
        Long commentId = 1L;

        String token = "Bearer " + jwtUtil.createAccessToken(USERNAME);
        // when
        mvc.perform(
                MockMvcRequestBuilders.delete("/newsfeed/{newsfeedId}/comment/{commentId}", newsfeedId,
                        commentId)
                    .header("Authorization", token))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andDo(MockMvcResultHandlers.print());
    }
}
