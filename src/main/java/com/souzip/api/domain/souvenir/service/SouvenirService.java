package com.souzip.api.domain.souvenir.service;

import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.file.service.FileService;
import com.souzip.api.domain.souvenir.dto.SouvenirCreateRequest;
import com.souzip.api.domain.souvenir.dto.SouvenirResponse;
import com.souzip.api.domain.souvenir.dto.SouvenirUpdateRequest;
import com.souzip.api.domain.souvenir.entity.Souvenir;
import com.souzip.api.domain.souvenir.repository.SouvenirRepository;
import com.souzip.api.domain.user.repository.UserRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SouvenirService {

    private final SouvenirRepository souvenirRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    public SouvenirResponse getSouvenir(Long souvenirId) {
        Souvenir souvenir = souvenirRepository.findByIdAndDeletedFalse(souvenirId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOUVENIR_NOT_FOUND));

        List<FileResponse> files = fileService.getFilesByEntity("Souvenir", souvenirId);
        return SouvenirResponse.from(souvenir, files);
    }

    @Transactional
    public SouvenirResponse createSouvenir(
            SouvenirCreateRequest request,
            Long userId,
            List<MultipartFile> files
    ) {

        Souvenir souvenir = Souvenir.of(
                request.name(),
                request.localPrice(),
                request.localCurrency(),
                request.krwPrice(),
                request.description(),
                request.address(),
                request.locationDetail(),
                request.latitude(),
                request.longitude(),
                request.category(),
                request.purpose(),
                userId
        );

        Souvenir savedSouvenir = souvenirRepository.save(souvenir);
        List<FileResponse> uploadedFiles = uploadFiles(savedSouvenir.getId(), userId, files);

        return SouvenirResponse.from(savedSouvenir, uploadedFiles);
    }

    @Transactional
    public SouvenirResponse updateSouvenir(
            Long id,
            SouvenirUpdateRequest request,
            Long userId,
            List<MultipartFile> files
    ) {
        Souvenir souvenir = souvenirRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOUVENIR_NOT_FOUND));

        if (!souvenir.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        souvenir.update(
                request.name(),
                request.localPrice(),
                request.localCurrency(),
                request.krwPrice(),
                request.description(),
                request.address(),
                request.locationDetail(),
                request.latitude(),
                request.longitude(),
                request.category(),
                request.purpose()
        );

        fileService.deleteFilesByEntity("Souvenir", id);
        List<FileResponse> uploadedFiles = uploadFiles(id, userId, files);

        return SouvenirResponse.from(souvenir, uploadedFiles);
    }

    @Transactional
    public void deleteSouvenir(Long id, Long userId) {
        Souvenir souvenir = souvenirRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOUVENIR_NOT_FOUND));

        if (!souvenir.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        fileService.deleteFilesByEntity("Souvenir", id);
        souvenir.delete();
    }

    private List<FileResponse> uploadFiles(Long souvenirId, Long userId, List<MultipartFile> files) {
        String uuid = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getUserId();

        return Optional.ofNullable(files)
                .orElse(List.of())
            .stream()
                .map(file -> fileService.uploadFile(
                        uuid,
                        "Souvenir",
                        souvenirId,
                        file,
                        null
                ))
                .toList();
    }
}
