package com.hcreator.creator.domain.portfolio;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// 공개 포트폴리오 상세 내용 조회 (카드 클릭 시 모달에서 지연 로딩)
@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioPublicController {

    private final PortfolioItemRepository itemRepository;

    public record PortfolioDetailResponse(Long id, String name, String detailContent) {}

    @GetMapping("/items/{id}")
    public PortfolioDetailResponse detail(@PathVariable Long id) {
        PortfolioItem item = itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "항목을 찾을 수 없습니다."));
        return new PortfolioDetailResponse(item.getId(), item.getName(), item.getDetailContent());
    }
}