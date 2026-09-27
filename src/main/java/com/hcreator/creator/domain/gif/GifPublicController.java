package com.hcreator.creator.domain.gif;

import lombok.RequiredArgsConstructor;
import com.hcreator.creator.repository.gif.GifTemplateRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gif")
@RequiredArgsConstructor
public class GifPublicController {

    private final GifTemplateRepository gifTemplateRepository;

    @GetMapping("/{id}")
    public GifTemplate detail(@PathVariable Long id) {
        return gifTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("템플릿을 찾을 수 없습니다."));
    }
}