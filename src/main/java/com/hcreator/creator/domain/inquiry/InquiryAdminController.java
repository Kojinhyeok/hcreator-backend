package com.hcreator.creator.domain.inquiry;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inquiry")
@RequiredArgsConstructor
public class InquiryAdminController {

    private final InquiryRepository inquiryRepository;

    @GetMapping
    public List<Inquiry> list() {
        return inquiryRepository.findAllByOrderByCreatedAtDesc();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        inquiryRepository.deleteById(id);
    }
}