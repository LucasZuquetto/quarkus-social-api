package org.acme.quarkussocial.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "name is required")
    private String name;
    @NotNull(message = "age is required")
    private Integer age;
}
