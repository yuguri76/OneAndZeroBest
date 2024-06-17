package com.sparta.oneandzerobest.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sparta.oneandzerobest.auth.entity.User;
import com.sparta.oneandzerobest.auth.entity.UserStatus;
import com.sparta.oneandzerobest.auth.repository.UserRepository;
import com.sparta.oneandzerobest.profile.dto.ProfileRequestDto;
import com.sparta.oneandzerobest.s3.entity.Image;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
public class UserTest {

    @Autowired
    private UserRepository userRepository;

    final String USERNAME = "test_username";
    final String PASSWORD = "test_password";
    final String NAME = "test_name";
    final String EMAIL = "test_email@test.com";
    final UserStatus STATUS = UserStatus.ACTIVE;

    @BeforeAll
    void setUp() {
        User user = new User(USERNAME, PASSWORD, NAME, EMAIL, STATUS);
        userRepository.save(user);
    }


    @Test
    void test_setProfileImage() {
        String name = "testname";
        String url = "testUrl";

        Image image = new Image(name,url);
        User user = userRepository.findByUsername(USERNAME).orElse(null);

        user.setProfileImage(image);

        assertEquals(user.getImage().getName(),name);
        assertEquals(user.getImage().getUrl(),url);

    }

    @Test
    void test_update() {
        String name = "profileName";
        String introduction ="profileIntroduction";

        ProfileRequestDto profileRequestDto = new ProfileRequestDto();
        profileRequestDto.setName(name);
        profileRequestDto.setIntroduction(introduction);

        User user = userRepository.findByUsername(USERNAME).orElse(null);
        user.update(profileRequestDto);

        assertEquals(user.getName(), name);
        assertEquals(user.getIntroduction(),introduction);
    }

    @Test
    void test_updateRefreshToken() {
        String testToken = "testToken";

        User user = userRepository.findByUsername(USERNAME).orElse(null);
        user.updateRefreshToken(testToken);

        assertEquals(user.getRefreshToken(),testToken);
    }

    @Test
    void test_withdraw() {
        User user = userRepository.findByUsername(USERNAME).orElse(null);
        user.withdraw();

        assertEquals(user.getStatusCode(),UserStatus.WITHDRAWN);
        assertNull(user.getRefreshToken());

    }

    @Test
    void test_updateEmail() {
        String email = "newEmail@test.com";

        User user = userRepository.findByUsername(USERNAME).orElse(null);
        user.updateEmail(email);

        assertEquals(user.getEmail(),email);
    }

    @Test
    void test_updateStatus() {
        UserStatus status = UserStatus.WITHDRAWN;

        User user = userRepository.findByUsername(USERNAME).orElse(null);
        user.updateStatus(status);

        assertEquals(user.getStatusCode(),status);
    }

    @Test
    void test_updateKakaoUser() {
        User user = userRepository.findByUsername(USERNAME).orElse(null);
        String username ="kakaoUsername";
        String nickname ="kakakoNickname";
        String email = "kakaoEmail";
        UserStatus status = UserStatus.ACTIVE;
        Long id = user.getId();

        user.updateKakaoUser(id,username,nickname,email,status);

        assertEquals(user.getId(), id);
        assertEquals(user.getUsername(), nickname);
        assertEquals(user.getPassword(), "kakao");
        assertEquals(user.getEmail(), email);
        assertEquals(user.getStatusCode(),UserStatus.ACTIVE);
    }

    @Test
    void test_updateGoogleUser() {
        User user = userRepository.findByUsername(USERNAME).orElse(null);
        Long id = user.getId();
        String nickname = "googleNickname";
        String name = "googleName";
        String email = "googleEmail";
        UserStatus userStatus = UserStatus.ACTIVE;

        user.updateGoogleUser(id, nickname, name, email, userStatus);

        assertEquals(user.getId(), id);
        assertEquals(user.getUsername(), nickname);
        assertEquals(user.getName(), name);
        assertEquals(user.getEmail(), email);
        assertEquals(user.getStatusCode(),UserStatus.ACTIVE);
        assertEquals(user.getPassword(),"google");
    }

    @Test
    void test_updateGithubUser() {
        User user = userRepository.findByUsername(USERNAME).orElse(null);
        Long id = user.getId();
        String nickname = "githubNickname";
        String name = "githubName";
        String email = "githubEmail";
        UserStatus status = UserStatus.ACTIVE;

        user.updateGithubUser(id, nickname, name, email, status);

        assertEquals(user.getId(), id);
        assertEquals(user.getUsername(), nickname);
        assertEquals(user.getName(), name);
        assertEquals(user.getEmail(), email);
        assertEquals(user.getStatusCode(), UserStatus.ACTIVE);
        assertEquals(user.getPassword(),"github");
    }

    @Test
    void test_clearRefreshToken() {
        User user = userRepository.findByUsername(USERNAME).orElse(null);
        user.clearRefreshToken();

        assertNull(user.getRefreshToken());

    }

    @Test
    void test_updatePasswrod() {
        String password = "newPassword";

        User user = userRepository.findByUsername(USERNAME).orElse(null);
        user.updatePassword(password);

        assertEquals(user.getPassword(),password);
    }


}
