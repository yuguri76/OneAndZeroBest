package com.sparta.oneandzerobest.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;

import com.sparta.oneandzerobest.auth.entity.User;
import com.sparta.oneandzerobest.auth.entity.UserStatus;
import com.sparta.oneandzerobest.auth.repository.UserRepository;
import com.sparta.oneandzerobest.auth.util.JwtUtil;
import com.sparta.oneandzerobest.comment.dto.CommentRequestDto;
import com.sparta.oneandzerobest.comment.dto.CommentResponseDto;
import com.sparta.oneandzerobest.comment.entity.Comment;
import com.sparta.oneandzerobest.comment.repository.CommentRepository;
import com.sparta.oneandzerobest.comment.service.CommentService;
import com.sparta.oneandzerobest.config.TestJwtConfig;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(properties = {"spring.profiles.active=test"})
@TestPropertySource(locations = "classpath:application-test.yml")
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
@Import({TestJwtConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentServiceTest {

    @MockBean
    CommentRepository commentRepository;
    @MockBean
    UserRepository userRepository;
    @MockBean
    JwtUtil jwtUtil;

    @Autowired
    TestJwtConfig testJwtConfig;

    static final String USERNAME = "Seokjoon123";
    static final String PASSWORD = "1234@123aaaa";
    static final String EMAIL = "tjrwns3428@gmail.com";

    CommentService commentService;

    @BeforeAll
    void setUp() {
        JwtUtil.init(testJwtConfig);

        commentService = new CommentService(commentRepository, userRepository, jwtUtil);
    }

    private User createMockUser(Long id, String username, String password, String email,
        UserStatus status) {
        User user = new User(username, password, username, email, status);
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return user;
    }

    private Comment createMockComment(Long id, Long newsfeedId, Long userId, String content) {
        Comment comment = new Comment(newsfeedId, userId, content);
        try {
            Field idField = Comment.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(comment, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return comment;
    }

    @Test
    @DisplayName("댓글 추가 성공")
    @Order(1)
    void addComment_success() {
        //given
        Long newsfeedId = 1L;
        String content = "comment Content";
        String token = "Bearer " + jwtUtil.createAccessToken(USERNAME);

        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setNewsfeedId(newsfeedId);
        requestDto.setContent(content);

        User user = createMockUser(1L, USERNAME, PASSWORD, EMAIL, UserStatus.ACTIVE);

        Comment savedComment = createMockComment(1L, newsfeedId, user.getId(), content);

        given(userRepository.findByUsername(USERNAME)).willReturn(Optional.of(user));
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when
        CommentResponseDto response = commentService.addComment(newsfeedId, requestDto, token);

        // then
        assertEquals(savedComment.getId(), response.getId());
        assertEquals(content, response.getContent());
    }

    @Test
    @DisplayName("댓글 조회 성공")
    @Order(2)
    void getAllComments_success() {
        // given
        Long newsfeedId = 1L;
        String token = "Bearer " + jwtUtil.createAccessToken(USERNAME);

        User user = createMockUser(1L, USERNAME, PASSWORD, EMAIL, UserStatus.ACTIVE);
        given(userRepository.findByUsername(USERNAME)).willReturn(Optional.of(user));

        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment(newsfeedId, user.getId(), "Comment content 1"));
        comments.add(new Comment(newsfeedId, user.getId(), "Comment content 2"));
        comments.add(new Comment(newsfeedId, user.getId(), "Comment content 3"));
        given(commentRepository.findByNewsfeedId(newsfeedId)).willReturn(comments);

        // when
        List<CommentResponseDto> response = commentService.getAllComments(newsfeedId, token);

        // then
        assertEquals(response.size(), comments.size());
        assertEquals(response.get(0).getContent(), "Comment content 1");
    }

    @Test
    @DisplayName("댓글 수정 성공")
    @Order(3)
    void updateComment_success() {
        // given
        Long newsfeedId = 1L;
        Long commentId = 1L;
        String content = "comment content";
        String token = "Bearer " + jwtUtil.createAccessToken(USERNAME);

        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setContent(content);
        requestDto.setNewsfeedId(newsfeedId);

        User user = createMockUser(1L, USERNAME, PASSWORD, EMAIL, UserStatus.ACTIVE);
        given(userRepository.findByUsername(USERNAME)).willReturn(Optional.of(user));

        Comment comment = createMockComment(1L, newsfeedId, user.getId(), content);
        given(commentRepository.findByIdAndNewsfeedIdAndUserId(commentId, newsfeedId,
            user.getId())).willReturn(Optional.of(comment));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        // when
        CommentResponseDto response = commentService.updateComment(newsfeedId, commentId,
            requestDto, token);

        // then
        assertEquals(response.getContent(), content);
        assertEquals(response.getUserId(), user.getId());
        assertEquals(response.getNewsfeedId(), newsfeedId);
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    @Order(4)
    void deleteComment_success() {
        // given
        Long newsfeedId = 1L;
        Long commentId = 1L;
        String token = "Bearer " + jwtUtil.createAccessToken(USERNAME);

        User user = createMockUser(1L, USERNAME, PASSWORD, EMAIL, UserStatus.ACTIVE);
        given(userRepository.findByUsername(USERNAME)).willReturn(Optional.of(user));
        Comment comment = createMockComment(commentId, newsfeedId, user.getId(), "content");
        given(
            commentRepository.findByIdAndNewsfeedIdAndUserId(commentId, newsfeedId,
                user.getId())).willReturn(Optional.of(comment));

        // when - then
        commentService.deleteComment(newsfeedId, commentId, token);
    }
}
