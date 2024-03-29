package ru.hits.timeflowapi.util.validation.annotation;

import ru.hits.timeflowapi.util.validation.validator.UniquePostRoleValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Валидирующая аннотация. Она нужна для проверки на уникальность
 * названия роли, которая относится к должности сотрудника.
 */
@Target({FIELD, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = UniquePostRoleValidator.class)
public @interface UniquePostRoleValidation {

    String message() default "Должность с таким названием роли уже существует.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};


}
