package com.souzip.api.domain.admin.application;

import com.souzip.api.domain.admin.application.command.InviteAdminCommand;
import com.souzip.api.domain.admin.exception.AdminErrorCode;
import com.souzip.api.domain.admin.exception.AdminException;
import com.souzip.api.domain.admin.exception.AdminNotFoundException;
import com.souzip.api.domain.admin.infrastructure.encoder.AdminPasswordEncoderImpl;
import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.AdminRole;
import com.souzip.api.domain.admin.repository.AdminRepository;
import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.city.repository.CityRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AdminManagementService {

    private final AdminRepository adminRepository;
    private final AdminPasswordEncoderImpl passwordEncoder;
    private final CityRepository cityRepository;

    public AdminPageResult getAdmins(int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<Admin> admins = adminRepository.findAllExcludingSuperAdmin(offset, pageSize);
        long total = adminRepository.countExcludingSuperAdmin();
        int totalPages = (int) Math.ceil((double) total / pageSize);

        return new AdminPageResult(admins, pageNo, pageSize, total, totalPages);
    }

    @Transactional
    public Admin inviteAdmin(InviteAdminCommand command) {
        validateNotSuperAdmin(command.role());
        validateUsernameNotDuplicated(command.username());

        Admin admin = createAdmin(command);
        return adminRepository.save(admin);
    }

    @Transactional
    public void deleteAdmin(UUID adminId, UUID requesterId) {
        Admin adminToDelete = adminRepository.findById(adminId)
            .orElseThrow(AdminNotFoundException::new);

        adminRepository.delete(adminToDelete);
    }

    @Transactional
    public void updateCityPriority(Long cityId, Integer newPriority) {
        City city = findCityByIdWithLock(cityId);
        Integer oldPriority = city.getPriority();
        Long countryId = city.getCountry().getId();

        adjustPriorities(oldPriority, newPriority, countryId);
        city.updatePriority(newPriority);
    }

    private void adjustPriorities(Integer oldPriority, Integer newPriority, Long countryId) {
        pullOldPriorityIfExists(oldPriority, countryId);
        pushNewPriorityIfExists(newPriority, countryId);
    }

    private void pullOldPriorityIfExists(Integer oldPriority, Long countryId) {
        if (hasPriority(oldPriority)) {
            cityRepository.pullPriorityFrom(oldPriority, countryId);
        }
    }

    private void pushNewPriorityIfExists(Integer newPriority, Long countryId) {
        if (hasPriority(newPriority)) {
            cityRepository.shiftPriorityFrom(newPriority, countryId);
        }
    }

    private boolean hasPriority(Integer priority) {
        return priority != null;
    }

    private City findCityByIdWithLock(Long cityId) {
        return cityRepository.findByIdWithLock(cityId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "도시를 찾을 수 없습니다."));
    }

    private void validateNotSuperAdmin(AdminRole role) {
        if (isSuperAdmin(role)) {
            throw new AdminException(AdminErrorCode.CANNOT_INVITE_SUPER_ADMIN);
        }
    }

    private boolean isSuperAdmin(AdminRole role) {
        return role == AdminRole.SUPER_ADMIN;
    }

    private void validateUsernameNotDuplicated(String username) {
        if (isUsernameDuplicated(username)) {
            throw new AdminException(AdminErrorCode.ADMIN_USERNAME_DUPLICATED);
        }
    }

    private boolean isUsernameDuplicated(String username) {
        return adminRepository.existsByUsername(username);
    }

    private Admin createAdmin(InviteAdminCommand command) {
        return Admin.create(
            command.username(),
            command.password(),
            command.role(),
            passwordEncoder
        );
    }

    public record AdminPageResult(
        List<Admin> admins,
        int pageNo,
        int pageSize,
        long total,
        int totalPages
    ) {}
}
