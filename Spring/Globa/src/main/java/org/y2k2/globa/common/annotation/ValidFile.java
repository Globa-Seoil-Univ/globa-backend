package org.y2k2.globa.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.y2k2.globa.common.validation.FileValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileValidator.class)
public @interface ValidFile {
    String message() default "파일은 반드시 필요합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
