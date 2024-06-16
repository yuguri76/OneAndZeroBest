package com.sparta.oneandzerobest.newsfeed;

import com.sparta.oneandzerobest.auth.entity.User;
import com.sparta.oneandzerobest.auth.entity.UserStatus;
import com.sparta.oneandzerobest.auth.repository.UserRepository;
import com.sparta.oneandzerobest.auth.service.UserServiceImpl;
import com.sparta.oneandzerobest.auth.util.JwtUtil;
import com.sparta.oneandzerobest.config.TestJwtConfig;
import com.sparta.oneandzerobest.newsfeed.dto.NewsfeedRequestDto;
import com.sparta.oneandzerobest.newsfeed.dto.NewsfeedResponseDto;
import com.sparta.oneandzerobest.newsfeed.entity.Newsfeed;
import com.sparta.oneandzerobest.newsfeed.repository.NewsfeedRepository;
import com.sparta.oneandzerobest.newsfeed.service.NewsfeedService;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.assertj.core.api.Assertions;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"spring.profiles.active=test"})
@TestPropertySource(locations = "classpath:application-test.yml")
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
@Import({TestJwtConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NewsfeedServiceTest {

    @MockBean
    NewsfeedRepository newsfeedRepository;
    @MockBean
    JwtUtil jwtUtil;
    @Autowired
    TestJwtConfig testJwtConfig;
    @MockBean
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    static final String USERNAME = "Seokjoon123";
    static final String PASSWORD = "1234@123aaaa";
    static final String EMAIL = "tjrwns3428@gmail.com";

    NewsfeedService newsfeedService;


    @BeforeAll
    void setUp() {
        JwtUtil.init(testJwtConfig);

        newsfeedService = new NewsfeedService(newsfeedRepository, jwtUtil, userRepository);
    }

    @Test
    @DisplayName("게시글 작성 성공")
    @Order(1)
    void postContent_success() {
        // given
        String token = jwtUtil.createAccessToken(USERNAME);
        String content = "newsfeed content";
        NewsfeedRequestDto newsfeedRequestDto = new NewsfeedRequestDto();
        newsfeedRequestDto.setContent(content);

        String encodedPassword = passwordEncoder.encode(PASSWORD);
        User user = new User(USERNAME, encodedPassword, USERNAME, EMAIL, UserStatus.ACTIVE);

        given(userRepository.findByUsername(USERNAME)).willReturn(Optional.of(user));

        // when
        ResponseEntity<NewsfeedResponseDto> response = newsfeedService.postContent(token,
            newsfeedRequestDto);

        // then
        assertEquals(Objects.requireNonNull(response.getBody()).getContent(), content);
    }

    @Test
    @DisplayName("게시글 조회 성공")
    @Order(2)
    void getAllContents_success() {
        // given
        int page = 0;
        int size = 10;
        boolean isAsc = false;
        boolean like = false;
        LocalDateTime startTime = LocalDateTime.MIN;
        LocalDateTime endTime = LocalDateTime.MAX;

        Sort.Direction direction = isAsc ? Direction.ASC : Direction.DESC;
        Sort sort = like ? Sort.by(direction, "likeCount") : Sort.by(direction, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        List<Newsfeed> newsfeedList = new ArrayList<>();
        newsfeedList.add(new Newsfeed(1L, "content 1"));
        newsfeedList.add(new Newsfeed(2L, "content 2"));
        newsfeedList.add(new Newsfeed(3L, "content 3"));

        Page<Newsfeed> newsfeeds = new PageImpl<>(newsfeedList, pageable, newsfeedList.size());
        given(newsfeedRepository.findAll(pageable)).willReturn(newsfeeds);
        given(newsfeedRepository.findAllByCreateAtBetween(startTime, endTime, pageable)).willReturn(
            newsfeeds);

        // when
        ResponseEntity<Page<NewsfeedResponseDto>> response = newsfeedService.getAllContents(page,
            size, isAsc, like, startTime, endTime);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getTotalElements());
        assertEquals("content 1", response.getBody().getContent().get(0).getContent());
    }

    @Test
    @DisplayName("게시글 수정 성공")
    @Order(3)
    void putContent_success(){
        // given
        String token = jwtUtil.createAccessToken(USERNAME);
        Long id = 1L; // contentid
        String content = "newsfeed Content";
        NewsfeedRequestDto requestDto = new NewsfeedRequestDto();
        requestDto.setContent(content);

        Newsfeed newsfeed = new Newsfeed(1L,content);
        User user = createMockUser(1L, USERNAME, PASSWORD, EMAIL, UserStatus.ACTIVE);
        given(newsfeedRepository.findById(id)).willReturn(Optional.of(newsfeed));
        given(userRepository.findByUsername(USERNAME)).willReturn(Optional.of(user));

        // when
        ResponseEntity<NewsfeedResponseDto> response = newsfeedService.putContent(token, id,
            requestDto);

        // then
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(response.getBody().getContent(),content);

    }

    // User 객체를 생성하고 ID 필드를 설정하는 메서드
    // Setter를 사용하지 않고 , private 필드를 설정해줘서 더 복잡하다
    private User createMockUser(Long id, String username, String password, String email, UserStatus status) {
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

    // Newsfeed 목 개체 생성
    private Newsfeed createMockNewsfeed(Long id,Long userId,String content){
        Newsfeed newsfeed = new Newsfeed(userId,content);
        try{
            Field idField = Newsfeed.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(newsfeed,id);
        }
        catch ( NoSuchFieldException | IllegalArgumentException e){

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return newsfeed;
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    @Order(4)
    void deleteContent_success(){
        // given
        String token = jwtUtil.createAccessToken(USERNAME);
        Long contentId = 1L;

        Newsfeed newsfeed = createMockNewsfeed(1L,1L,"Newsfeed Content");
        User user = createMockUser(1L, USERNAME, PASSWORD, EMAIL, UserStatus.ACTIVE);
        given(newsfeedRepository.findById(contentId)).willReturn(Optional.of(newsfeed));
        given(userRepository.findByUsername(USERNAME)).willReturn(Optional.of(user));

        // when
        ResponseEntity<Long> response = newsfeedService.deleteContent(token, contentId);

        // then
        assertEquals(response.getStatusCode(),HttpStatus.OK);
        assertEquals(response.getBody(),contentId);


    }

}
