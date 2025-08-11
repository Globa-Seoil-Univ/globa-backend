package org.y2k2.globa.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import org.y2k2.globa.common.annotation.ValidFile;

import java.util.List;

public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {
    private final List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png");

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext) {
        boolean isValidFile =
                file != null
                && !file.isEmpty()
                && file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty();

        boolean isValidExtension =
                isValidFile
                && allowedExtensions.stream().anyMatch(file.getOriginalFilename()::endsWith);

        return isValidFile && isValidExtension;
    }
}
