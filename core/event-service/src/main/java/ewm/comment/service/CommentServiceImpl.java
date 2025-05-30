package ewm.comment.service;

import ewm.comment.dto.CommentDto;
import ewm.comment.dto.InputCommentDto;
import ewm.comment.dto.UpdateCommentDto;
import ewm.comment.mapper.CommentMapper;
import ewm.comment.model.Comment;
import ewm.comment.repository.CommentRepository;
import ewm.event.feignClient.RequestClient;
import ewm.event.feignClient.UserClient;
import ewm.event.model.Event;
import ru.yandex.practicum.dto.event.EventState;
import ewm.event.repository.EventRepository;
import ewm.exception.EntityNotFoundException;
import ewm.exception.InitiatorRequestException;
import ewm.exception.ValidationException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.user.UserDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;
    private final UserClient userClient;
    private final RequestClient requestClient;


    @Override
    public CommentDto privateAdd(Long userId, Long eventId, InputCommentDto inputCommentDto) {
        Event event = findEvent(eventId);
        if (event.getId().equals(userId)) {
            throw new ValidationException(Comment.class, " Нельзя оставлять комментарии к своему событию.");
        }
        if (requestClient.findByRequestIdAndEventId(userId, eventId).isEmpty()) {
            throw new ValidationException(Comment.class, " Пользователь с ID - " + userId + ", не заявился на событие с ID - " + eventId + ".");
        }
        UserDto author = findUser(userId);

        Comment comment = commentMapper.toComment(inputCommentDto, author, event);
        comment.setCreated(LocalDateTime.now());
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }


    @Override
    public void privateDelete(Long userId, Long commentId) {
        findUser(userId);
        Comment comment = findComment(commentId);
        if (!comment.getAuthorId().equals(userId)) {
            throw new InitiatorRequestException(" Нельзя удалить комментарий другого пользователя.");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public void adminDelete(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public CommentDto privateUpdate(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        findUser(userId);
        Comment comment = findComment(commentId);
        if (!comment.getAuthorId().equals(userId)) {
            throw new InitiatorRequestException(" Нельзя редактировать комментарий другого пользователя.");
        }
        comment.setText(updateCommentDto.getText());
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto adminUpdate(Long id, UpdateCommentDto updateCommentDto) {

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Comment.class, "Комментарий c ID - " + id + ", не найден."));

        if (updateCommentDto.getText() != null) {
            comment.setText(updateCommentDto.getText());
        }
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> findCommentsByEventId(Long eventId, Integer from, Integer size) {
        findEvent(eventId);
        Pageable pageable = PageRequest.of(from, size);
        return commentMapper.toCommentDtos(commentRepository.findAllByEventId(eventId, pageable));
    }

    @Override
    public CommentDto findCommentById(Long commentId) {
        Comment comment = findComment(commentId);
        return commentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentDto> findCommentsByEventIdAndUserId(Long eventId, Long userId, Integer from, Integer size) {
        findUser(userId);
        findEvent(eventId);
        Pageable pageable = PageRequest.of(from, size);
        return commentMapper.toCommentDtos(commentRepository.findAllByEventIdAndAuthorId(eventId, userId, pageable));
    }

    @Override
    public List<CommentDto> findCommentsByUserId(Long eventId, Integer from, Integer size) {
        UserDto user = findUser(eventId);
        Pageable pageable = PageRequest.of(from, size);
        return commentMapper.toCommentDtos(commentRepository.findAllByAuthorId(user.getId(), pageable));
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new EntityNotFoundException(
                        Event.class, "Событие c ID - " + eventId + ", не найдено или ещё не опубликовано")
                );
    }

    private UserDto findUser(Long userId) {
        try {
            return userClient.getUserById(userId);
        } catch (FeignException e) {
            throw new EntityNotFoundException(UserDto.class, "Пользователь c ID - " + userId + ", не найден.");
        }
    }

    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(Comment.class, "Комментарий c ID - " + commentId + ", не найден."));
    }
}