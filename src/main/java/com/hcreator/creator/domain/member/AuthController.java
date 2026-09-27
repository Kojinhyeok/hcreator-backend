package com.hcreator.creator.domain.member;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    public record LoginRequest(String username, String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        Member member = memberService.authenticate(req.username(), req.password());

        HttpSession session = request.getSession(true);
        session.setAttribute("memberId", member.getId());
        session.setAttribute("role", member.getRole());

        return ResponseEntity.ok(Map.of("role", member.getRole()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.ok().build();
    }

    // admin/js/common.js가 이 경로+필드명을 그대로 기대함
    @GetMapping("/current-user")
    public ResponseEntity<?> currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("memberId") == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(Map.of(
                "memberId", session.getAttribute("memberId"),
                "position", session.getAttribute("role"),
                "name", "관리자"
        ));
    }
}