package com.werewolf.client.validation;

import com.werewolf.validation.ValidationResult;

public interface ClientValidator<T> {
    ValidationResult validate(T input);
}
