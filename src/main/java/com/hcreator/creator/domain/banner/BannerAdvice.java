package com.hcreator.creator.domain.banner;

import com.hcreator.creator.controller.PageController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

/** 공개 페이지(PageController) 모든 화면에 "banners" 를 넣어 준다. 템플릿에서 ${banners['index']} 로 사용 */
@Slf4j
@ControllerAdvice(assignableTypes = PageController.class)
@RequiredArgsConstructor
public class BannerAdvice {

    private final BannerService bannerService;

    @ModelAttribute("banners")
    public Map<String, BannerView> banners() {
        return bannerService.allViews();
    }
}