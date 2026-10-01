package com.hcreator.creator.domain.apply;

import com.hcreator.creator.domain.gif.GifApplication;
import com.hcreator.creator.domain.gif.GifApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/apply")
@RequiredArgsConstructor
public class ApplyAdminController {

    private final GifApplicationRepository gifApplicationRepository;
    private final ServiceApplicationRepository serviceApplicationRepository;

    @GetMapping("/gif")
    @Transactional(readOnly = true)
    public List<GifApplication> listGif() {
        return gifApplicationRepository.findAllWithRows();
    }

    public record GifStatusRequest(String status, String memo) {}

    @PutMapping("/gif/{id}")
    public GifApplication updateGif(@PathVariable Long id, @RequestBody GifStatusRequest req) {
        GifApplication application = gifApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("신청 내역을 찾을 수 없습니다."));
        if (req.status() != null) application.setStatus(req.status());
        if (req.memo() != null) application.setMemo(req.memo());
        return gifApplicationRepository.save(application);
    }

    @DeleteMapping("/gif/{id}")
    public void deleteGif(@PathVariable Long id) {
        gifApplicationRepository.deleteById(id);
    }

    @GetMapping("/service")
    public List<ServiceApplication> listService() {
        return serviceApplicationRepository.findAllByOrderByCreatedAtDesc();
    }

    @DeleteMapping("/service/{id}")
    public void deleteService(@PathVariable Long id) {
        serviceApplicationRepository.deleteById(id);
    }
}