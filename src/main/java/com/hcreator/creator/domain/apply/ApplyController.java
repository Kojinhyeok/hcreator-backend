package com.hcreator.creator.domain.apply;

import com.hcreator.creator.domain.gif.GifApplication;
import com.hcreator.creator.domain.gif.GifApplicationRepository;
import com.hcreator.creator.domain.gif.GifApplicationRow;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/apply")
@RequiredArgsConstructor
public class ApplyController {

    private final GifApplicationRepository gifApplicationRepository;
    private final ServiceApplicationRepository serviceApplicationRepository;

    // ===== GIF =====

    public record GifRowRequest(String reportNo, String testItem, String templateNo, String subjectNo) {}

    public record GifApplyRequest(
            String companyName, String contactName, String contactPhone, String contactEmail,
            String pointColor, String bgColor, String bgEffect, String quantity, String filePath,
            String etcNote, List<GifRowRequest> rows
    ) {}

    @PostMapping("/gif")
    public Map<String, Object> applyGif(@RequestBody GifApplyRequest req) {
        GifApplication application = GifApplication.builder()
                .companyName(req.companyName()).contactName(req.contactName())
                .contactPhone(req.contactPhone()).contactEmail(req.contactEmail())
                .pointColor(req.pointColor()).bgColor(req.bgColor()).bgEffect(req.bgEffect())
                .quantity(req.quantity()).filePath(req.filePath()).etcNote(req.etcNote())
                .build();

        if (req.rows() != null) {
            req.rows().forEach(r -> application.getRows().add(
                    GifApplicationRow.builder()
                            .reportNo(r.reportNo()).testItem(r.testItem())
                            .templateNo(r.templateNo()).subjectNo(r.subjectNo())
                            .application(application)
                            .build()
            ));
        }

        gifApplicationRepository.save(application);
        return Map.of("id", application.getId());
    }

    // ===== 상세페이지 / 블로그 / 영상 =====

    public record ServiceApplyRequest(
            String serviceType, String companyName, String contactName, String contactPhone, String contactEmail,
            String productName, String brandName, String launchDate, String needsShooting, String hasPlan,
            String attachmentPath, String promoContent, String videoFormat, String videoContent,
            String etcNote, String requestNote
    ) {}

    @PostMapping("/service")
    public Map<String, Object> applyService(@RequestBody ServiceApplyRequest req) {
        ServiceApplication application = ServiceApplication.builder()
                .serviceType(req.serviceType())
                .companyName(req.companyName()).contactName(req.contactName())
                .contactPhone(req.contactPhone()).contactEmail(req.contactEmail())
                .productName(req.productName()).brandName(req.brandName())
                .launchDate(req.launchDate()).needsShooting(req.needsShooting()).hasPlan(req.hasPlan())
                .attachmentPath(req.attachmentPath()).promoContent(req.promoContent())
                .videoFormat(req.videoFormat()).videoContent(req.videoContent())
                .etcNote(req.etcNote()).requestNote(req.requestNote())
                .build();

        serviceApplicationRepository.save(application);
        return Map.of("id", application.getId());
    }
}