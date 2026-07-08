package com.srm.creditengine.api.response;

import java.util.UUID;

public record AssignorResponse(
        UUID id,
        String name,
        String document
) {
}
