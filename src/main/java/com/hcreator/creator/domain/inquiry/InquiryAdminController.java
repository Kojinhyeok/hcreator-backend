package com.hcreator.creator.domain.inquiry;

import com.hcreator.creator.domain.comment.ApplyCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/inquiry")
@RequiredArgsConstructor
public class InquiryAdminController {

    private static final Set<String> STATUSES = Set.of("대기", "연락 완료");

    private final InquiryRepository inquiryRepository;
    private final ApplyCommentRepository commentRepository;

    @GetMapping
    public List<Inquiry> list() {
        return inquiryRepository.findAllByOrderByCreatedAtDesc();
    }

    public record StatusRequest(String status) {}

    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestBody StatusRequest req) {
        if (req.status() == null || !STATUSES.contains(req.status())) {
            throw new IllegalArgumentException("허용되지 않는 상태입니다.");
        }
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));
        inquiry.setStatus(req.status());
        inquiryRepository.save(inquiry);
        return Map.of("status", inquiry.getStatus());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        commentRepository.deleteByTargetTypeAndTargetId("inquiry", id);
        inquiryRepository.deleteById(id);
    }
}