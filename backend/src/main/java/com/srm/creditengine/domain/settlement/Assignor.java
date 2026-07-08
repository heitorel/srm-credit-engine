package com.srm.creditengine.domain.settlement;

import java.util.UUID;

public record Assignor(
        UUID id,
        String name,
        String document
) {
}
