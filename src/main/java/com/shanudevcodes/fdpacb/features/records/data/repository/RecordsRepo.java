package com.shanudevcodes.fdpacb.features.records.data.repository;

import com.shanudevcodes.fdpacb.features.records.data.entity.RecordsModel;
import com.shanudevcodes.fdpacb.features.records.data.enums.Category;
import com.shanudevcodes.fdpacb.features.records.data.enums.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecordsRepo extends JpaRepository<RecordsModel, UUID> {
    Optional<RecordsModel> findByIdAndIsDeletedFalse(UUID id);

    @Query("""
    SELECT r FROM RecordsModel r
    WHERE (:userIds IS NULL OR r.user.id IN :userIds)
    AND r.isDeleted = false
    AND (:type IS NULL OR r.type = :type)
    AND (:category IS NULL OR r.category = :category)
""")
    Page<RecordsModel> findAllByFilters(
            List<UUID> userIds,
            RecordType type,
            Category category,
            Pageable pageable
    );

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM RecordsModel r
        WHERE r.user.id IN :userIds
        AND r.type = :type
        AND r.isDeleted = false
    """)
    BigDecimal sumByType(List<UUID> userIds, RecordType type);

    @Query("""
        SELECT r.category, SUM(r.amount)
        FROM RecordsModel r
        WHERE r.user.id IN :userIds
        AND r.isDeleted = false
        GROUP BY r.category
    """)
    List<Object[]> categoryBreakdown(List<UUID> userIds);

    @Query("""
        SELECT r.type, SUM(r.amount)
        FROM RecordsModel r
        WHERE r.user.id IN :userIds
        AND r.isDeleted = false
        GROUP BY r.type
    """)
    List<Object[]> typeBreakdown(List<UUID> userIds);

    @Query("""
        SELECT r.status, SUM(r.amount)
        FROM RecordsModel r
        WHERE r.user.id IN :userIds
        AND r.isDeleted = false
        GROUP BY r.status
    """)
    List<Object[]> statusBreakdown(List<UUID> userIds);

    @Query("""
        SELECT r
        FROM RecordsModel r
        WHERE r.user.id IN :userIds
        AND r.isDeleted = false
        ORDER BY r.transactionDate DESC
        LIMIT 5
    """)
    List<RecordsModel> findRecent(List<UUID> userIds);

    @Query("""
    SELECT u.id FROM UserModel u
    WHERE u.assignedAnalyst.id = :analystId
""")
    List<UUID> findViewerIdsByAnalyst(UUID analystId);
}