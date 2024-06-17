package com.sparta.oneandzerobest.newsfeed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sparta.oneandzerobest.newsfeed.entity.Newsfeed;
import com.sparta.oneandzerobest.newsfeed.repository.NewsfeedRepository;
import com.sparta.oneandzerobest.newsfeed_like.entity.NewsfeedLike;
import com.sparta.oneandzerobest.s3.entity.Image;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
public class NewsfeedTest {

    @Autowired
    private NewsfeedRepository newsfeedRepository;

    final Long USER_ID = 1L;
    final String CONTENT = "Newsfeed Content";

    @BeforeAll
    void setUp() {
        newsfeedRepository.deleteAll();  // 데이터 초기화
        Newsfeed newsfeed = new Newsfeed(USER_ID, CONTENT);
        newsfeedRepository.save(newsfeed);
    }

    @Test
    @Transactional
    void test_setImage() {
        String imageName = "imageName";
        String imageUrl = "imageUrl";
        Image image = new Image(imageName, imageUrl);

        Newsfeed newsfeed = newsfeedRepository.findById(1L).orElse(null);
        newsfeed.setImage(image);

        assertEquals(newsfeed.getImageList().get(0).getName(), imageName);
        assertEquals(newsfeed.getImageList().get(0).getUrl(), imageUrl);
    }

    @Test
    @Transactional
    void test_setNewsfeedLike() {
        Newsfeed newsfeed = newsfeedRepository.findById(1L).orElse(null);
        Long otherUserid = 2L;
        NewsfeedLike newsfeedLike = new NewsfeedLike(otherUserid, newsfeed);

        newsfeed.setNewsfeedLike(newsfeedLike);

        assertEquals(newsfeed.getNewsfeedLikeList().get(0), newsfeedLike);
        assertEquals(newsfeed.getLikeCount(), 1);
    }

    @Test
    @Transactional
    void test_removeNewsfeedLike() {
        Newsfeed newsfeed = newsfeedRepository.findById(1L).orElse(null);
        Long otherUserid = 2L;
        NewsfeedLike newsfeedLike = new NewsfeedLike(otherUserid, newsfeed);

        // newsfeed 좋아요
        newsfeed.setNewsfeedLike(newsfeedLike);

        assertEquals(newsfeed.getNewsfeedLikeList().get(0), newsfeedLike);
        assertEquals(newsfeed.getLikeCount(), 1);

        // newsfeed 좋아요 취소
        newsfeed.removeNewsfeedLike(newsfeedLike);

        assertTrue(newsfeed.getNewsfeedLikeList().isEmpty());
        assertEquals(newsfeed.getLikeCount(), 0);
    }
}
