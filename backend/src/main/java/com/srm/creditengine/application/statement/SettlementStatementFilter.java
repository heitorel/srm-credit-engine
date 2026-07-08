package com.srm.creditengine.application.statement;

import com.srm.creditengine.api.request.SettlementStatementRequest;
import com.srm.creditengine.application.reference.ListReferenceDataService;
import com.srm.creditengine.domain.receivable.ReceivableType;
import com.srm.creditengine.domain.settlement.SettlementStatus;
import com.srm.creditengine.domain.shared.BadRequestException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record SettlementStatementFilter(
        LocalDateTime settledFrom,
        LocalDateTime settledTo,
        UUID assignorId,
        String assignorDocument,
        String paymentCurrency,
        String sourceCurrency,
        String receivableType,
        String status,
        Pageable pageable
) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT = "settledAt,desc";
    private static final LocalTime END_OF_DAY = LocalTime.of(23, 59, 59, 999_999_000);

    public static SettlementStatementFilter from(
            SettlementStatementRequest request,
            ListReferenceDataService referenceDataService
    ) {
        if (request.from() != null && request.to() != null && request.from().isAfter(request.to())) {
            throw new InvalidStatementDateRangeException();
        }

        Integer page = request.page() == null ? DEFAULT_PAGE : request.page();
        Integer size = request.size() == null ? DEFAULT_SIZE : request.size();

        if (page < 0) {
            throw new InvalidPaginationException("page", "Page must be greater than or equal to zero.");
        }
        if (size <= 0) {
            throw new InvalidPaginationException("size", "Size must be greater than zero.");
        }
        if (size > MAX_SIZE) {
            throw new InvalidPaginationException("size", "Size must be less than or equal to 100.");
        }

        String paymentCurrency = normalize(request.paymentCurrency());
        if (paymentCurrency != null) {
            paymentCurrency = referenceDataService.requireSupportedCurrency(paymentCurrency, "paymentCurrency").value();
        }

        String sourceCurrency = normalize(request.sourceCurrency());
        if (sourceCurrency != null) {
            sourceCurrency = referenceDataService.requireSupportedCurrency(sourceCurrency, "sourceCurrency").value();
        }

        String receivableType = normalize(request.receivableType());
        if (receivableType != null) {
            receivableType = ReceivableType.from(receivableType).name();
        }

        String status = normalize(request.status());
        if (status != null) {
            status = parseSettlementStatus(status);
        }

        return new SettlementStatementFilter(
                request.from() == null ? null : request.from().atStartOfDay(),
                request.to() == null ? null : request.to().atTime(END_OF_DAY),
                request.assignorId(),
                normalizeFreeText(request.assignorDocument()),
                paymentCurrency,
                sourceCurrency,
                receivableType,
                status,
                PageRequest.of(page, size, resolveSort(request.sort()))
        );
    }

    private static Sort resolveSort(String rawSort) {
        String sort = normalizeFreeText(rawSort);
        if (sort == null) {
            sort = DEFAULT_SORT;
        }

        String[] parts = sort.split(",");
        if (parts.length != 2) {
            throw new BadRequestException("Unsupported sort field: " + sort + ".");
        }

        String field = parts[0].trim();
        String direction = parts[1].trim().toLowerCase(Locale.ROOT);

        if (!"settledAt".equals(field)) {
            throw new BadRequestException("Unsupported sort field: " + field + ".");
        }
        if (!"asc".equals(direction) && !"desc".equals(direction)) {
            throw new BadRequestException("Unsupported sort direction: " + direction + ".");
        }

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        return Sort.by(
                new Sort.Order(sortDirection, "settledAt"),
                new Sort.Order(Sort.Direction.ASC, "id")
        );
    }

    private static String parseSettlementStatus(String rawStatus) {
        try {
            return SettlementStatus.valueOf(rawStatus).name();
        } catch (IllegalArgumentException exception) {
            throw new UnsupportedSettlementStatusException(rawStatus);
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private static String normalizeFreeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
