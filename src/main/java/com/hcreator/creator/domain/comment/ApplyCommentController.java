package com.hcreator.creator.domain.comment;

import com.hcreator.creator.domain.member.Member;
import com.hcreator.creator.domain.member.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
public class ApplyCommentController {

    private static final Set<String> TYPES = Set.of("gif", "inquiry");
    private static final int MAX_LENGTH = 5000;

    private final ApplyCommentRepository commentRepository;
    private final MemberRepository memberRepository;

    public record CommentView(Long id, String authorName, String content,
                              LocalDateTime createdAt, LocalDateTime updatedAt, boolean mine) {}

    public record CreateRequest(String type, Long id, String content) {}

    public record UpdateRequest(String content) {}

    @GetMapping
    public List<CommentView> list(@RequestParam String type, @RequestParam Long id, HttpServletRequest request) {
        checkType(type);
        Long me = currentMemberId(request);
        return commentRepository.findByTargetTypeAndTargetIdOrderByCreatedAtAscIdAsc(type, id).stream()
                .map(c -> toView(c, me))
                .toList();
    }

    @PostMapping
    public CommentView create(@RequestBody CreateRequest req, HttpServletRequest request) {
        checkType(req.type());
        if (req.id() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대상이 없습니다.");
        String content = cleanContent(req.content());

        Long me = currentMemberId(request);
        Member member = memberRepository.findById(me)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        String name = member.getName() != null && !member.getName().isBlank() ? member.getName() : member.getUsername();

        ApplyComment saved = commentRepository.save(ApplyComment.builder()
                .targetType(req.type()).targetId(req.id())
                .authorId(me).authorName(name)
                .content(content)
                .build());
        return toView(saved, me);
    }

    @PutMapping("/{commentId}")
    public CommentView update(@PathVariable Long commentId, @RequestBody UpdateRequest req, HttpServletRequest request) {
        Long me = currentMemberId(request);
        ApplyComment comment = findOwn(commentId, me);
        comment.setContent(cleanContent(req.content()));
        comment.setUpdatedAt(LocalDateTime.now());
        return toView(commentRepository.save(comment), me);
    }

    @DeleteMapping("/{commentId}")
    public void delete(@PathVariable Long commentId, HttpServletRequest request) {
        Long me = currentMemberId(request);
        commentRepository.delete(findOwn(commentId, me));
    }

    // ---- helpers ----

    private ApplyComment findOwn(Long commentId, Long me) {
        ApplyComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));
        if (comment.getAuthorId() == null || !comment.getAuthorId().equals(me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 댓글만 수정/삭제할 수 있습니다.");
        }
        return comment;
    }

    private Long currentMemberId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object id = session != null ? session.getAttribute("memberId") : null;
        if (!(id instanceof Long)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return (Long) id;
    }

    private void checkType(String type) {
        if (type == null || !TYPES.contains(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 대상입니다.");
        }
    }

    private String cleanContent(String content) {
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "내용을 입력해 주세요.");
        }
        String trimmed = content.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "내용은 " + MAX_LENGTH + "자 이내로 입력해 주세요.");
        }
        return trimmed;
    }

    private CommentView toView(ApplyComment c, Long me) {
        return new CommentView(c.getId(), c.getAuthorName(), c.getContent(), c.getCreatedAt(), c.getUpdatedAt(),
                c.getAuthorId() != null && c.getAuthorId().equals(me));
    }
}