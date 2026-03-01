package com.souzip.domain.admin.infrastructure.init;

import com.souzip.domain.admin.model.Admin;
import com.souzip.domain.admin.model.AdminPasswordEncoder;
import com.souzip.domain.admin.model.AdminRole;
import com.souzip.domain.admin.model.Username;
import com.souzip.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(AdminProperties.class)
@Component
public class AdminInitializer implements ApplicationRunner {

    private final AdminRepository adminRepository;
    private final AdminProperties adminProperties;
    private final AdminPasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        Username username = new Username(adminProperties.getUsername());

        adminRepository.findByUsername(username).ifPresentOrElse(
            admin -> log.info("초기 어드민 계정이 이미 존재합니다. username={}", username.value()),
            () -> createInitialAdmin(username)
        );
    }

    private void createInitialAdmin(Username username) {
        Admin admin = Admin.create(
            username.value(),
            adminProperties.getPassword(),
            AdminRole.SUPER_ADMIN,
            passwordEncoder
        );

        adminRepository.save(admin);
        log.info("초기 어드민 계정이 생성되었습니다. username={}", username.value());
    }
}
