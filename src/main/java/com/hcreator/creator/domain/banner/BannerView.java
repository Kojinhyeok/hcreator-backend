package com.hcreator.creator.domain.banner;

import lombok.Builder;
import lombok.Getter;

/** 화면(Thymeleaf)에서 쓰는 값. 기본값과 관리자가 바꾼 값을 합친 결과. */
@Getter
@Builder
public class BannerView {
    private final String key;
    private final String label;
    private final String title;
    private final String titleHtml;        // 줄바꿈을 <br>로 바꾼 안전한 HTML
    private final String description;
    private final String descriptionHtml;
    private final String buttonText;
    private final String buttonLink;
    private final String button2Text;
    private final String button2Link;
    private final String videoUrl;
    private final String imageUrl;

    public boolean getHasVideo() {
        return videoUrl != null && !videoUrl.isBlank();
    }

    public boolean getHasImage() {
        return imageUrl != null && !imageUrl.isBlank();
    }

    public boolean getHasLabel() {
        return label != null && !label.isBlank();
    }

    public boolean getHasDescription() {
        return description != null && !description.isBlank();
    }

    public boolean getHasButton() {
        return buttonText != null && !buttonText.isBlank();
    }

    public boolean getHasButton2() {
        return button2Text != null && !button2Text.isBlank();
    }
}