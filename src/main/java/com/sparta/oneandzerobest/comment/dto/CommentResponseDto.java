package com.sparta.oneandzerobest.comment.dto;

import com.sparta.oneandzerobest.comment.entity.Comment;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import lombok.Setter;

/**
 * CommentResponseDto는 댓글 조회 응답 시 반환되는 데이터를 담는 DTO
 * 이 DTO는 댓글의 ID, 뉴스피드 ID, 작성자 ID, 내용, 생성 및 수정 시간을 포함
 */
@Getter
public class CommentResponseDto {
    private Long id;  // 댓글 ID
    private Long newsfeedId;  // 뉴스피드 ID
    private Long userId;  // 댓글 작성자 ID
    private String content;  // 댓글 내용

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.newsfeedId = comment.getNewsfeedId();
        this.userId = comment.getUserId();
        this.content = comment.getContent();

    }
}
