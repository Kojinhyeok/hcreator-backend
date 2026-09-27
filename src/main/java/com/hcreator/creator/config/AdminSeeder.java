package com.hcreator.creator.config;

import com.hcreator.creator.domain.member.Member;
import com.hcreator.creator.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (memberRepository.findByUsername("admin").isEmpty()) {
            Member admin = Member.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("changeme1234"))
                    .role("ADMIN")
                    .build();
            memberRepository.save(admin);
            System.out.println(">>> 관리자 계정 생성됨: admin / changeme1234 (꼭 나중에 바꾸세요)");
        }
    }
}