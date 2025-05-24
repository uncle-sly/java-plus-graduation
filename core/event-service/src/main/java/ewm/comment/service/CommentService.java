package ewm.comment.service;

import ewm.comment.dto.CommentDto;
import ewm.comment.dto.InputCommentDto;
import ewm.comment.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto privateAdd(Long userId, Long eventId, InputCommentDto inputCommentDto);

    CommentDto privateUpdate(Long userId, Long commentId, UpdateCommentDto updateCommentDto);

    CommentDto adminUpdate(Long id, UpdateCommentDto updateCommentDto);

    void adminDelete(Long id);

    void privateDelete(Long userId, Long commentId);

    List<CommentDto> findCommentsByEventId(Long eventId, Integer from, Integer size);

    List<CommentDto> findCommentsByEventIdAndUserId(Long eventId, Long userId, Integer from, Integer size);

    List<CommentDto> findCommentsByUserId(Long userId, Integer from, Integer size);

    CommentDto findCommentById(Long commentId);
}