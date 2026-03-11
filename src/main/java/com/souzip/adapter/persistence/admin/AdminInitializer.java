package com.souzip.adapter.persistence.admin;

import com.souzip.adapter.config.AdminProperties;
import com.souzip.application.admin.required.AdminRepository;
import com.souzip.domain.admin.Admin;
import com.souzip.domain.admin.AdminRegisterRequest;
import com.souzip.domain.admin.AdminRole;
import com.souzip.domain.admin.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AdminInitializer implements ApplicationRunner {

    private final AdminRepository adminRepository;
    private final AdminProperties adminProperties;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        adminRepository.findByUsername(adminProperties.getUsername()).ifPresentOrElse(
                admin -> log.info("초기 어드민 계정이 이미 존재합니다. username={}", admin.getUsername()),
                this::createInitialAdmin
        );
    }

    private void createInitialAdmin() {
        Admin admin = Admin.register(
                AdminRegisterRequest.of(
                        adminProperties.getUsername(),
                        adminProperties.getPassword(),
                        AdminRole.SUPER_ADMIN
                ),
                passwordEncoder
        );
        adminRepository.save(admin);
        log.info("초기 어드민 계정이 생성되었습니다. username={}", adminProperties.getUsername());
    }
}
