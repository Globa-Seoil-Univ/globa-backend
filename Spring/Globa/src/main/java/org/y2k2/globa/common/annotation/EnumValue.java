package org.y2k2.globa.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.y2k2.globa.common.validation.EnumValueValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = EnumValueValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValue {
    String message() default "Enum에 정의된 값이 아닙니다.";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
    Class<? extends Enum<?>> enumClass();
    boolean ignoreCase() default true;
}
