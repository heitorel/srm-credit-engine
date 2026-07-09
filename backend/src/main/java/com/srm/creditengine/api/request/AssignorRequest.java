package com.srm.creditengine.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssignorRequest(
    @NotBlank(message = "Assignor name is required.") @Size(max = 255, message = "Assignor name must contain at most 255 characters.") String name,
    @Size(max = 32, message = "Assignor document must contain at most 32 characters.") String document) {}
