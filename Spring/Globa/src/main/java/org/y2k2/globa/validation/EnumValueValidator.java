package org.y2k2.globa.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.y2k2.globa.annotation.EnumValue;

public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {
    private EnumValue annotation;

    @Override
    public void initialize(EnumValue constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        Enum<?>[] enumValues = annotation.enumClass().getEnumConstants();
        if (enumValues != null) {
            for (Enum<?> enumValue : enumValues) {
                if (value.equals(enumValue.toString())
                                || (annotation.ignoreCase() && value.equalsIgnoreCase(enumValue.toString()))) {
                    return true;
                }
            }
        }

        return false;
    }
}
