package com.sparta.oneandzerobest.comment.service;

import com.sparta.oneandzerobest.auth.entity.User;
import com.sparta.oneandzerobest.auth.repository.UserRepository;
import com.sparta.oneandzerobest.auth.util.JwtUtil;
import com.sparta.oneandzerobest.comment.dto.CommentRequestDto;
import com.sparta.oneandzerobest.comment.dto.CommentResponseDto;
import com.sparta.oneandzerobest.comment.entity.Comment;
import com.sparta.oneandzerobest.comment.repository.CommentRepository;
import com.sparta.oneandzerobest.exception.CommentNotFoundException;
import com.sparta.oneandzerobest.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CommentService는 댓글 관련 비즈니스 로직을 처리
 */
@Service
@RequiredArgsConstructor
public class CommentService {


    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * 뉴스피드에 댓글을 추가
     *
     * @param newsfeedId 뉴스피드 ID
     * @param requestDto 댓글 작성 요청 DTO
     * @param token      JWT 토큰
     * @return 생성된 댓글의 응답 DTO
     */
    @Transactional
    public CommentResponseDto addComment(Long newsfeedId, CommentRequestDto requestDto,
        String token) {
        Long userId = validateToken(token);
        if(requestDto.getContent().isEmpty())
            throw new IllegalArgumentException();

        Comment comment = new Comment(newsfeedId, userId, requestDto.getContent());
        comment = commentRepository.save(comment);
        return new CommentResponseDto(comment);
    }

    /**
     * 뉴스피드의 모든 댓글을 조회
     *
     * @param newsfeedId 뉴스피드 ID
     * @param token      JWT 토큰
     * @return 댓글 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getAllComments(Long newsfeedId, String token) {
        validateToken(token);
        List<Comment> comments = commentRepository.findByNewsfeedId(newsfeedId);
        return comments.stream()
            .map(CommentResponseDto::new)
            .collect(Collectors.toList());
    }

    /**
     * 특정 댓글을 수정
     *
     * @param newsfeedId 뉴스피드 ID
     * @param commentId  댓글 ID
     * @param requestDto 댓글 수정 요청 DTO
     * @param token      JWT 토큰
     * @return 수정된 댓글의 응답 DTO
     */
    @Transactional
    public CommentResponseDto updateComment(Long newsfeedId, Long commentId,
        CommentRequestDto requestDto, String token) {
        Long userId = validateToken(token);
        Comment comment = commentRepository.findByIdAndNewsfeedIdAndUserId(commentId, newsfeedId,
                userId)
            .orElseThrow(() -> new CommentNotFoundException("해당 댓글이 존재하지 않거나 권한이 없습니다."));
        comment.setContent(requestDto.getContent());
        comment = commentRepository.save(comment);
        return new CommentResponseDto(comment);
    }

    /**
     * 특정 댓글을 삭제
     *
     * @param newsfeedId 뉴스피드 ID
     * @param commentId  댓글 ID
     * @param token      JWT 토큰
     */
    @Transactional
    public void deleteComment(Long newsfeedId, Long commentId, String token) {
        Long userId = validateToken(token);
        Comment comment = commentRepository.findByIdAndNewsfeedIdAndUserId(commentId, newsfeedId,
                userId)
            .orElseThrow(() -> new CommentNotFoundException("해당 댓글이 존재하지 않거나 권한이 없습니다."));
        commentRepository.delete(comment);
    }

    /**
     * 토큰을 검증하고 사용자 ID를 반환하는 메소드
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    private Long validateToken(String token) {
        String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UnauthorizedException("인증 정보가 유효하지 않습니다."));
        return user.getId();
    }
}