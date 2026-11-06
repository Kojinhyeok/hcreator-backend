package com.hcreator.creator.domain.member;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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
        Long memberId = (Long) session.getAttribute("memberId");
        String name = memberRepository.findById(memberId)
                .map(Member::getName)
                .filter(n -> n != null && !n.isBlank())
                .orElse("관리자");
        return ResponseEntity.ok(Map.of(
                "memberId", memberId,
                "position", session.getAttribute("role"),
                "name", name
        ));
    }

    public record ProfileRequest(String name, String currentPassword, String newPassword) {}

    /** 내 정보 변경: 표시 이름 / 비밀번호 */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileRequest req, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("memberId") == null) {
            return ResponseEntity.status(401).build();
        }
        Member member = memberRepository.findById((Long) session.getAttribute("memberId")).orElse(null);
        if (member == null) return ResponseEntity.status(401).build();

        if (req.name() != null && !req.name().isBlank()) {
            String name = req.name().trim();
            if (name.length() > 50) {
                return ResponseEntity.badRequest().body(Map.of("message", "이름은 50자 이내로 입력해 주세요."));
            }
            member.setName(name);
        }

        if (req.newPassword() != null && !req.newPassword().isBlank()) {
            if (req.currentPassword() == null || !passwordEncoder.matches(req.currentPassword(), member.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("message", "현재 비밀번호가 일치하지 않습니다."));
            }
            if (req.newPassword().length() < 8) {
                return ResponseEntity.badRequest().body(Map.of("message", "새 비밀번호는 8자 이상이어야 합니다."));
            }
            member.setPassword(passwordEncoder.encode(req.newPassword()));
        }

        memberRepository.save(member);
        return ResponseEntity.ok(Map.of("name", member.getName() == null ? "관리자" : member.getName()));
    }
}