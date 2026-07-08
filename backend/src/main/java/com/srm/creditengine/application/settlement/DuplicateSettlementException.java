package com.srm.creditengine.application.settlement;

import com.srm.creditengine.api.error.ApiErrorDetail;
import com.srm.creditengine.domain.shared.ConflictException;
import java.util.List;

public class DuplicateSettlementException extends ConflictException {

    public DuplicateSettlementException(String externalReference, String assignorIdentifier) {
        super(
                "Receivable %s for assignor %s has already been settled.".formatted(
                        externalReference,
                        assignorIdentifier
                ),
                List.of(new ApiErrorDetail(
                        "receivables.externalReference",
                        "This receivable has already been settled."
                ))
        );
    }

    public DuplicateSettlementException(String externalReference, int itemIndex, String assignorIdentifier) {
        super(
                "Receivable %s for assignor %s has already been settled.".formatted(
                        externalReference,
                        assignorIdentifier
                ),
                List.of(new ApiErrorDetail(
                        "receivables[%d].externalReference".formatted(itemIndex),
                        "This receivable has already been settled."
                ))
        );
    }

    public DuplicateSettlementException() {
        super(
                "Settlement could not be created because one or more receivables were already settled.",
                List.of(new ApiErrorDetail(
                        "receivables",
                        "One or more receivables were already settled."
                ))
        );
    }
}
