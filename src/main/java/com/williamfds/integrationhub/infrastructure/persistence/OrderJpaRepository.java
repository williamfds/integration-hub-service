package com.williamfds.integrationhub.infrastructure.persistence;

import com.williamfds.integrationhub.domain.model.OrderStatus;
import com.williamfds.integrationhub.domain.model.Platform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    Optional<OrderEntity> findByPlatformAndExternalId(Platform platform, String externalId);

    @Query("""
        SELECT o FROM OrderEntity o
        WHERE (:platform IS NULL OR o.platform = :platform)
          AND (:status   IS NULL OR o.status   = :status)
        """)
    Page<OrderEntity> search(@Param("platform") Platform platform,
                             @Param("status") OrderStatus status,
                             Pageable pageable);
}
