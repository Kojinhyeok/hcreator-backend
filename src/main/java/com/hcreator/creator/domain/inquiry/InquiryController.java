package com.hcreator.creator.domain.inquiry;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryRepository inquiryRepository;
    private final PasswordEncoder passwordEncoder;

    public record InquiryRequest(
            String companyName, String contactName, String contactPhone, String contactEmail,
            String inquiryService, String title, String content, String attachmentPath, String password
    ) {}

    @PostMapping
    public Map<String, Object> create(@RequestBody InquiryRequest req) {
        Inquiry inquiry = Inquiry.builder()
                .companyName(req.companyName()).contactName(req.contactName())
                .contactPhone(req.contactPhone()).contactEmail(req.contactEmail())
                .inquiryService(req.inquiryService()).title(req.title()).content(req.content())
                .attachmentPath(req.attachmentPath())
                .password(req.password() != null && !req.password().isBlank() ? passwordEncoder.encode(req.password()) : null)
                .build();

        inquiryRepository.save(inquiry);
        return Map.of("id", inquiry.getId());
    }
}