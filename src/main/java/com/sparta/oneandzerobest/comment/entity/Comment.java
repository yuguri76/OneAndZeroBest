package com.sparta.oneandzerobest.comment.entity;

import com.sparta.oneandzerobest.timestamp.TimeStamp;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

/**
 * Comment 엔티티는 뉴스피드에 대한 댓글을 나타낸다.
 * 이 클래스는 댓글의 ID, 뉴스피드 ID, 사용자 ID, 댓글 내용을 관리!
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Comment extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;          // 댓글의 고유 ID
    private Long newsfeedId;  // 댓글이 속한 뉴스피드의 ID
    private Long userId;      // 댓글을 작성한 사용자의 ID
    private String content;   // 댓글 내용

    /**
     * 댓글 생성자
     * @param newsfeedId 뉴스피드의 ID
     * @param userId 댓글 작성자의 사용자 ID
     * @param content 댓글의 내용
     */
    public Comment(Long newsfeedId, Long userId, String content) {
        this.newsfeedId = newsfeedId;
        this.userId = userId;
        this.content = content;
    }
}
