package org.creditto.creditto_service.domain.consent.repository;

import org.creditto.creditto_service.domain.consent.entity.ConsentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, Long> {

    // client + 동의서 code 기준 최신 기록 조회
    @Query("""
            SELECT r
            FROM ConsentRecord r
            JOIN r.consentDefinition d
            WHERE r.clientId = :clientId
                AND d.consentCode = :code
            ORDER BY r.consentDate DESC
            LIMIT 1
            """)
    Optional<ConsentRecord> findLatestByClientAndCode(@Param("clientId") String clientId, @Param("code") String code);

    // client + 동의서 ID 기준 최신 기록 조회
    @Query("""
            SELECT r
            FROM ConsentRecord r
            WHERE r.clientId = :clientId
                AND r.consentDefinition.id = :definitionId
            ORDER BY r.consentDate DESC
            """)
    Optional<ConsentRecord> findLatestByClientAndDefinition(@Param("clientId") String clientId, @Param("definitionId") Long definitionId);


    @Query("SELECT r FROM ConsentRecord r JOIN FETCH r.consentDefinition WHERE r.clientId = :clientId")
    List<ConsentRecord> findAllByClientIdWithDefinition(@Param("clientId") String clientId);
}
