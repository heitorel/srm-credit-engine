package com.srm.creditengine.infrastructure.query;

import com.srm.creditengine.application.statement.SettlementStatementFilter;
import com.srm.creditengine.application.statement.SettlementStatementRow;
import com.srm.creditengine.infrastructure.persistence.AssignorEntity;
import com.srm.creditengine.infrastructure.persistence.SettlementEntity;
import com.srm.creditengine.infrastructure.persistence.SettlementItemEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class SettlementStatementQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<SettlementStatementRow> findStatement(SettlementStatementFilter filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<SettlementStatementRowProjection> contentQuery = cb.createQuery(SettlementStatementRowProjection.class);
        Root<SettlementEntity> root = contentQuery.from(SettlementEntity.class);
        var assignorJoin = root.join("assignor");

        List<Predicate> predicates = buildPredicates(filter, cb, contentQuery, root, assignorJoin);
        contentQuery.select(cb.construct(
                SettlementStatementRowProjection.class,
                root.get("id"),
                assignorJoin.get("id"),
                assignorJoin.get("name"),
                assignorJoin.get("document"),
                root.get("sourceCurrency").get("code"),
                root.get("paymentCurrency").get("code"),
                root.get("status"),
                root.get("itemCount"),
                root.get("totalFaceValue"),
                root.get("totalPresentValue"),
                root.get("totalPaymentValue"),
                root.get("settledAt")
        ));
        contentQuery.where(predicates.toArray(Predicate[]::new));
        contentQuery.orderBy(resolveOrders(filter.pageable(), cb, root));

        TypedQuery<SettlementStatementRowProjection> typedQuery = entityManager.createQuery(contentQuery);
        typedQuery.setFirstResult((int) filter.pageable().getOffset());
        typedQuery.setMaxResults(filter.pageable().getPageSize());

        List<SettlementStatementRow> content = typedQuery.getResultList().stream()
                .map(this::toStatementRow)
                .toList();

        long total = count(filter);
        return new PageImpl<>(content, filter.pageable(), total);
    }

    private long count(SettlementStatementFilter filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<SettlementEntity> root = countQuery.from(SettlementEntity.class);
        var assignorJoin = root.join("assignor");

        List<Predicate> predicates = buildPredicates(filter, cb, countQuery, root, assignorJoin);
        countQuery.select(cb.count(root));
        countQuery.where(predicates.toArray(Predicate[]::new));

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private <T> List<Predicate> buildPredicates(
            SettlementStatementFilter filter,
            CriteriaBuilder cb,
            CriteriaQuery<T> query,
            Root<SettlementEntity> root,
            jakarta.persistence.criteria.Join<?, ?> assignorJoin
    ) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.settledFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("settledAt"), filter.settledFrom()));
        }
        if (filter.settledTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("settledAt"), filter.settledTo()));
        }
        if (filter.assignorId() != null) {
            predicates.add(cb.equal(root.get("assignor").get("id"), filter.assignorId().toString()));
        }
        if (filter.assignorDocument() != null) {
            predicates.add(cb.equal(assignorJoin.get("document"), filter.assignorDocument()));
        }
        if (filter.paymentCurrency() != null) {
            predicates.add(cb.equal(root.get("paymentCurrency").get("code"), filter.paymentCurrency()));
        }
        if (filter.sourceCurrency() != null) {
            predicates.add(cb.equal(root.get("sourceCurrency").get("code"), filter.sourceCurrency()));
        }
        if (filter.status() != null) {
            predicates.add(cb.equal(root.get("status"), filter.status()));
        }
        if (filter.receivableType() != null) {
            Subquery<String> receivableTypeSubquery = query.subquery(String.class);
            Root<SettlementItemEntity> itemRoot = receivableTypeSubquery.from(SettlementItemEntity.class);
            receivableTypeSubquery.select(itemRoot.get("id"));
            receivableTypeSubquery.where(
                    cb.equal(itemRoot.get("settlement").get("id"), root.get("id")),
                    cb.equal(itemRoot.get("receivableType").get("code"), filter.receivableType())
            );
            predicates.add(cb.exists(receivableTypeSubquery));
        }

        return predicates;
    }

    private List<Order> resolveOrders(Pageable pageable, CriteriaBuilder cb, Root<SettlementEntity> root) {
        List<Order> orders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            if ("settledAt".equals(order.getProperty())) {
                orders.add(order.isAscending() ? cb.asc(root.get("settledAt")) : cb.desc(root.get("settledAt")));
            }
            if ("id".equals(order.getProperty())) {
                orders.add(order.isAscending() ? cb.asc(root.get("id")) : cb.desc(root.get("id")));
            }
        }
        return orders;
    }

    private SettlementStatementRow toStatementRow(SettlementStatementRowProjection row) {
        return new SettlementStatementRow(
                UUID.fromString(row.settlementId()),
                UUID.fromString(row.assignorId()),
                row.assignorName(),
                row.assignorDocument(),
                row.sourceCurrency(),
                row.paymentCurrency(),
                row.status(),
                row.itemCount(),
                row.totalFaceValue(),
                row.totalPresentValue(),
                row.totalPaymentValue(),
                row.settledAt().atOffset(ZoneOffset.UTC).toInstant()
        );
    }
}
