package com.hcreator.creator.domain.apply;

import com.hcreator.creator.domain.comment.ApplyCommentRepository;
import com.hcreator.creator.domain.gif.GifApplication;
import com.hcreator.creator.domain.gif.GifApplicationRepository;
import com.hcreator.creator.domain.gif.GifApplicationRow;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/apply")
@RequiredArgsConstructor
public class ApplyAdminController {

    private static final Set<String> GIF_STATUSES = Set.of("대기중", "작업중", "전달완료");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final GifApplicationRepository gifApplicationRepository;
    private final ServiceApplicationRepository serviceApplicationRepository;
    private final ApplyCommentRepository commentRepository;

    @GetMapping("/gif")
    @Transactional(readOnly = true)
    public List<GifApplication> listGif() {
        return gifApplicationRepository.findAllWithRows();
    }

    public record RowMemo(Long id, String memo) {}

    public record GifUpdateRequest(String status, String nasPath, List<RowMemo> rowMemos) {}

    @PutMapping("/gif/{id}")
    @Transactional
    public Map<String, Object> updateGif(@PathVariable Long id, @RequestBody GifUpdateRequest req) {
        GifApplication application = gifApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("신청 내역을 찾을 수 없습니다."));

        if (req.status() != null) {
            if (!GIF_STATUSES.contains(req.status())) throw new IllegalArgumentException("허용되지 않는 상태입니다.");
            application.setStatus(req.status());
        }
        if (req.nasPath() != null) {
            String nas = req.nasPath().trim();
            application.setNasPath(nas.length() > 1000 ? nas.substring(0, 1000) : nas);
        }
        if (req.rowMemos() != null) {
            for (RowMemo rm : req.rowMemos()) {
                if (rm.id() == null) continue;
                for (GifApplicationRow row : application.getRows()) {
                    if (rm.id().equals(row.getId())) {
                        String memo = rm.memo() == null ? "" : rm.memo().trim();
                        row.setMemo(memo.length() > 1000 ? memo.substring(0, 1000) : memo);
                    }
                }
            }
        }
        gifApplicationRepository.save(application);
        return Map.of("id", application.getId());
    }

    @DeleteMapping("/gif/{id}")
    @Transactional
    public void deleteGif(@PathVariable Long id) {
        commentRepository.deleteByTargetTypeAndTargetId("gif", id);
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

    // ===== 엑셀 다운로드 (서비스 신청 내역 전체) =====

    @GetMapping("/export")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        List<GifApplication> gifs = gifApplicationRepository.findAllWithRows();
        List<ServiceApplication> services = serviceApplicationRepository.findAllByOrderByCreatedAtDesc();

        // GIF 신청
        List<List<String>> gifRows = new ArrayList<>();
        int total = gifs.size();
        for (int i = 0; i < total; i++) {
            GifApplication g = gifs.get(i);
            StringBuilder list = new StringBuilder();
            int n = 1;
            for (GifApplicationRow r : g.getRows()) {
                if (list.length() > 0) list.append('\n');
                list.append(n++).append(". ")
                        .append(nz(r.getReportNo())).append(" / ")
                        .append(nz(r.getTestItem())).append(" / ")
                        .append(nz(r.getTemplateNo())).append(" / ")
                        .append(nz(r.getSubjectNo()));
                if (r.getMemo() != null && !r.getMemo().isBlank()) list.append(" (메모: ").append(r.getMemo()).append(')');
            }
            gifRows.add(List.of(
                    String.valueOf(total - i), fmt(g.getCreatedAt()), nz(g.getCompanyName()), nz(g.getContactName()),
                    nz(g.getContactPhone()), nz(g.getContactEmail()), nz(g.getClinicalAmount()),
                    nz(g.getStatus()), nz(g.getNasPath()), nz(g.getPointColor()), nz(g.getBgColor()),
                    nz(g.getBgEffect()), nz(g.getQuantity()), nz(g.getFilePath()), nz(g.getEtcNote()), list.toString()
            ));
        }
        XlsxWriter.Sheet gifSheet = new XlsxWriter.Sheet("GIF 신청", List.of(
                "번호", "접수일시", "회사명", "담당자", "연락처", "이메일", "임상시험 금액", "상태", "NAS 경로",
                "포인트컬러", "배경색", "배경 효과", "용량", "첨부파일", "기타사항", "신청 목록"), gifRows);

        // 상세페이지 / 블로그 / 영상
        List<List<String>> svcRows = new ArrayList<>();
        int st = services.size();
        for (int i = 0; i < st; i++) {
            ServiceApplication s = services.get(i);
            svcRows.add(List.of(
                    String.valueOf(st - i), fmt(s.getCreatedAt()), typeLabel(s.getServiceType()),
                    nz(s.getCompanyName()), nz(s.getContactName()), nz(s.getContactPhone()), nz(s.getContactEmail()),
                    nz(s.getProductName()), nz(s.getBrandName()), nz(s.getLaunchDate()),
                    nz(s.getNeedsShooting()), nz(s.getHasPlan()), nz(s.getAttachmentPath()),
                    nz(s.getPromoContent()), nz(s.getVideoFormat()), nz(s.getVideoContent()),
                    nz(s.getEtcNote()), nz(s.getRequestNote())
            ));
        }
        XlsxWriter.Sheet svcSheet = new XlsxWriter.Sheet("상세페이지·블로그·영상", List.of(
                "번호", "접수일시", "구분", "회사명", "담당자", "연락처", "이메일", "제품명", "브랜드명", "출시 예정일",
                "제품컷 촬영", "기획안 유무", "첨부파일", "홍보 내용", "영상 형식", "영상 내용", "기타사항", "요청사항"), svcRows);

        byte[] bytes = XlsxWriter.build(List.of(gifSheet, svcSheet));
        String filename = "서비스신청내역_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(bytes.length)
                .body(bytes);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String fmt(LocalDateTime t) {
        return t == null ? "" : t.format(DATE_TIME);
    }

    private static String typeLabel(String type) {
        if (type == null) return "";
        return switch (type) {
            case "detail-page" -> "상세페이지";
            case "blog" -> "블로그";
            case "video" -> "영상";
            default -> type;
        };
    }
}