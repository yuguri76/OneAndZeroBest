package com.sparta.oneandzerobest.newsfeed_like.entity;

import com.sparta.oneandzerobest.newsfeed.entity.Newsfeed;
import com.sparta.oneandzerobest.timestamp.TimeStamp;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

/**
 * NewsfeedLike 엔티티는 뉴스피드 좋아요 정보를 저장하는 엔티티 클래스입니다.
 */
@Entity
@Table(name = "newsfeed_likes", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "newsfeed_id"})})
@Getter
@NoArgsConstructor
public class NewsfeedLike extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 좋아요 ID (Primary Key)

    @Column(name = "user_id", nullable = false)
    private Long userId; // 좋아요를 누른 사용자 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "newsfeed_id", nullable = false)
    private Newsfeed newsfeed; // 좋아요가 눌린 뉴스피드

    /**
     * NewsfeedLike 생성자
     * @param userId 사용자 ID
     * @param newsfeed 뉴스피드 엔티티
     */
    public NewsfeedLike(Long userId, Newsfeed newsfeed) {
        this.userId = userId;
        this.newsfeed = newsfeed;
    }
}