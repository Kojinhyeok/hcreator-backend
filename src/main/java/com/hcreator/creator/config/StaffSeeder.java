package com.hcreator.creator.config;

import com.hcreator.creator.domain.member.Member;
import com.hcreator.creator.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 직원용 관리자 계정(hc_staff1~7)을 최초 1회 생성한다. (이미 있으면 건너뜀 → 비밀번호/이름을 바꿔도 덮어쓰지 않음)
 * 비밀번호는 BCrypt 해시로만 들어 있다.
 */
@Component
@RequiredArgsConstructor
public class StaffSeeder implements CommandLineRunner {

    private final MemberRepository memberRepository;

    // {아이디, 표시 이름, BCrypt 해시}
    private static final String[][] STAFF = {
            {"hc_staff1", "담당자1", "$2a$10$d4Vj9UKH/51WlH0VeDyRROR3RDsRgzEZjINc66uDXjFkl4vasGo7a"},
            {"hc_staff2", "담당자2", "$2a$10$Vvwmv9tJFW8SAqO1l6nqhu.fC0cIX9xBUe62bv0FasTjoiR6v1Hae"},
            {"hc_staff3", "담당자3", "$2a$10$nSuqAp9u6Z5VGrckflMXruc7dFFtM7FUeK1FQKmg3mSpxc.tCDzZG"},
            {"hc_staff4", "담당자4", "$2a$10$dwXZLOUe2CCQZOKGFMWWdO31Jsc42WS.UXJKVzz3WzN29Q93r/w.i"},
            {"hc_staff5", "담당자5", "$2a$10$TQMr2MXVJnfmguN4akWf6ODNgluA7Azw59oJvKi1B87hW3c5JAf1C"},
            {"hc_staff6", "담당자6", "$2a$10$px1jpkaPwHU7u2vsNSk4XuZ0lo2G7Ts62y5wQtGHM./CD5bX82gVO"},
            {"hc_staff7", "담당자7", "$2a$10$5no5lO/iHpVvMZEKl4bMQehGBPSeJktvkwaYSDNZCvBr8CKIca.Oa"}
    };

    @Override
    public void run(String... args) {
        for (String[] s : STAFF) {
            if (memberRepository.findByUsername(s[0]).isEmpty()) {
                memberRepository.save(Member.builder()
                        .username(s[0])
                        .name(s[1])
                        .password(s[2])
                        .role("ADMIN")
                        .build());
                System.out.println(">>> 직원 계정 생성됨: " + s[0]);
            }
        }
    }
}