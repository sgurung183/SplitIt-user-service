package com.SplitIt.user_service.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        validatorFactory.close();
    }

    @Test
    void validRequest_hasNoViolations() {
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest());

        assertThat(violations).isEmpty();
    }

    @Test
    void blankFirstName_isRejected() {
        RegisterRequest request = validRequest();
        request.setFirstName(" ");

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void blankLastName_isRejected() {
        RegisterRequest request = validRequest();
        request.setLastName("");

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void blankEmail_isRejected() {
        RegisterRequest request = validRequest();
        request.setEmail(" ");

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void malformedEmail_isRejected() {
        RegisterRequest request = validRequest();
        request.setEmail("not-an-email");

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void blankPhoneNumber_isRejected() {
        RegisterRequest request = validRequest();
        request.setPhoneNumber("");

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void blankPassword_isRejected() {
        RegisterRequest request = validRequest();
        request.setPassword("");

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void passwordShorterThanEightCharacters_isRejected() {
        RegisterRequest request = validRequest();
        request.setPassword("short1");

        assertThat(validator.validate(request)).isNotEmpty();
    }

    private RegisterRequest validRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");
        request.setPhoneNumber("5551234567");
        request.setPassword("password123");
        return request;
    }
}
