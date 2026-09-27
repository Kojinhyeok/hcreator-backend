package com.hcreator.creator.config;

import com.hcreator.creator.domain.member.Member;
import com.hcreator.creator.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.init.username}")
    private String initUsername;

    @Value("${admin.init.password}")
    private String initPassword;

    @Override
    public void run(String... args) {
        if (memberRepository.findByUsername(initUsername).isEmpty()) {
            Member admin = Member.builder()
                    .username(initUsername)
                    .password(passwordEncoder.encode(initPassword))
                    .role("ADMIN")
                    .build();
            memberRepository.save(admin);
            System.out.println(">>> 관리자 계정 생성됨: " + initUsername);
        }
    }
}