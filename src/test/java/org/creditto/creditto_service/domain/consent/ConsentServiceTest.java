package org.creditto.creditto_service.domain.consent;

import org.creditto.creditto_service.domain.consent.dto.ConsentAgreeReq;
import org.creditto.creditto_service.domain.consent.dto.ConsentDefinitionRes;
import org.creditto.creditto_service.domain.consent.dto.ConsentRecordRes;
import org.creditto.creditto_service.domain.consent.dto.ConsentWithdrawReq;
import org.creditto.creditto_service.domain.consent.entity.ConsentCategory;
import org.creditto.creditto_service.domain.consent.entity.ConsentDefinition;
import org.creditto.creditto_service.domain.consent.entity.ConsentRecord;
import org.creditto.creditto_service.domain.consent.entity.ConsentStatus;
import org.creditto.creditto_service.domain.consent.repository.ConsentDefinitionRepository;
import org.creditto.creditto_service.domain.consent.repository.ConsentRecordRepository;
import org.creditto.creditto_service.domain.consent.service.ConsentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ConsentServiceTest {

    @Autowired
    private ConsentService consentService;

    @Autowired
    private ConsentDefinitionRepository definitionRepository;

    @Autowired
    private ConsentRecordRepository recordRepository;

    @AfterEach
    void tearDown() {
        recordRepository.deleteAllInBatch();
        definitionRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("동의하기 - 성공")
    void agree_Success() {
        // Given
        ConsentDefinition definition = ConsentDefinition.of("CODE1", "Title", "Desc", ConsentCategory.MARKETING, 2, LocalDateTime.now(), null);
        definitionRepository.save(definition);
        ConsentAgreeReq req = new ConsentAgreeReq("client123", "CODE1", "127.0.0.1");

        // When
        ConsentRecordRes result = consentService.agree(req);

        // Then
        List<ConsentRecord> records = recordRepository.findAll();
        assertThat(records).hasSize(1);
        ConsentRecord savedRecord = records.get(0);

        assertThat(result.consentCode()).isEqualTo("CODE1");
        assertThat(result.consentStatus()).isEqualTo(ConsentStatus.AGREE);
        assertThat(result.consentRecVer()).isEqualTo(2);
        assertThat(result.clientId()).isEqualTo("client123");

        assertThat(savedRecord.getConsentDefinition().getConsentCode()).isEqualTo("CODE1");
        assertThat(savedRecord.getConsentStatus()).isEqualTo(ConsentStatus.AGREE);
    }

    @Test
    @DisplayName("철회하기 - 성공")
    void withdraw_Success() {
        // Given
        ConsentDefinition definition = definitionRepository.save(ConsentDefinition.of("CODE1", "Title", "Desc", ConsentCategory.MARKETING, 1, LocalDateTime.now(), null));
        ConsentRecord record = recordRepository.save(ConsentRecord.of(definition, ConsentStatus.AGREE, 1, LocalDateTime.now(), null, "127.0.0.1", "client123"));
        ConsentWithdrawReq req = new ConsentWithdrawReq("client123", definition.getId());

        // When
        ConsentRecordRes result = consentService.withdraw(req);

        // Then
        ConsentRecord withdrawnRecord = recordRepository.findById(record.getId()).get();
        assertThat(result.consentStatus()).isEqualTo(ConsentStatus.WITHDRAW);
        assertThat(result.withdrawalDate()).isNotNull();
        assertThat(withdrawnRecord.getConsentStatus()).isEqualTo(ConsentStatus.WITHDRAW);
    }

    @Test
    @DisplayName("모든 최신 동의서 조회")
    void getLatestConsentDefinitions_Success() {
        // Given
        definitionRepository.save(ConsentDefinition.of("CODE1", "Title1", "Desc1", ConsentCategory.MARKETING, 1, LocalDateTime.now(), null));
        definitionRepository.save(ConsentDefinition.of("CODE1", "Title1 v2", "Desc1 v2", ConsentCategory.MARKETING, 2, LocalDateTime.now(), null));
        definitionRepository.save(ConsentDefinition.of("CODE2", "Title2", "Desc2", ConsentCategory.SERVICE, 1, LocalDateTime.now(), null));

        // When
        List<ConsentDefinitionRes> results = consentService.getLatestConsentDefinitions();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(ConsentDefinitionRes::consentCode).containsExactlyInAnyOrder("CODE1", "CODE2");
        // CODE1의 최신 버전인 2가 조회되어야 함
        ConsentDefinitionRes code1Result = results.stream().filter(r -> r.consentCode().equals("CODE1")).findFirst().get();
        assertThat(code1Result.consentDefVer()).isEqualTo(2);
    }

    @Test
    @DisplayName("사용자 동의 내역 조회")
    void getConsentRecord_Success() {
        // Given
        String clientId = "client123";
        ConsentDefinition def1 = definitionRepository.save(ConsentDefinition.of("CODE1", "Title1", "Desc1", ConsentCategory.MARKETING, 1, LocalDateTime.now(), null));
        ConsentDefinition def2 = definitionRepository.save(ConsentDefinition.of("CODE2", "Title2", "Desc2", ConsentCategory.SERVICE, 1, LocalDateTime.now(), null));
        recordRepository.save(ConsentRecord.of(def1, ConsentStatus.AGREE, 1, LocalDateTime.now(), null, "127.0.0.1", clientId));
        recordRepository.save(ConsentRecord.of(def2, ConsentStatus.WITHDRAW, 1, LocalDateTime.now(), LocalDateTime.now(), "127.0.0.1", clientId));

        // When
        List<ConsentRecordRes> results = consentService.getConsentRecord(clientId);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(ConsentRecordRes::clientId).containsOnly(clientId);
    }

    @Test
    @DisplayName("최신 버전 동의 여부 확인 - 동의함")
    void checkAgreement_Agreed() {
        // Given
        String clientId = "client123";
        String code = "CODE1";
        ConsentDefinition definition = definitionRepository.save(ConsentDefinition.of(code, "Title", "Desc", ConsentCategory.MARKETING, 2, LocalDateTime.now(), null));
        recordRepository.save(ConsentRecord.of(definition, ConsentStatus.AGREE, 2, LocalDateTime.now(), null, "127.0.0.1", clientId));

        // When
        boolean hasAgreed = consentService.checkAgreement(clientId, code);

        // Then
        assertThat(hasAgreed).isTrue();
    }

    @Test
    @DisplayName("최신 버전 동의 여부 확인 - 이전 버전에 동의함")
    void checkAgreement_AgreedToOlderVersion() {
        // Given
        String clientId = "client123";
        String code = "CODE1";
        definitionRepository.save(ConsentDefinition.of(code, "Title", "Desc", ConsentCategory.MARKETING, 2, LocalDateTime.now(), null));
        ConsentDefinition oldDefinition = definitionRepository.save(ConsentDefinition.of(code, "Title", "Desc", ConsentCategory.MARKETING, 1, LocalDateTime.now(), null));
        recordRepository.save(ConsentRecord.of(oldDefinition, ConsentStatus.AGREE, 1, LocalDateTime.now(), null, "127.0.0.1", clientId));

        // When
        boolean hasAgreed = consentService.checkAgreement(clientId, code);

        // Then
        assertThat(hasAgreed).isFalse();
    }

    @Test
    @DisplayName("최신 버전 동의 여부 확인 - 철회함")
    void checkAgreement_Withdrawn() {
        // Given
        String clientId = "client123";
        String code = "CODE1";
        ConsentDefinition definition = definitionRepository.save(ConsentDefinition.of(code, "Title", "Desc", ConsentCategory.MARKETING, 2, LocalDateTime.now(), null));
        recordRepository.save(ConsentRecord.of(definition, ConsentStatus.WITHDRAW, 2, LocalDateTime.now(), LocalDateTime.now(), "127.0.0.1", clientId));

        // When
        boolean hasAgreed = consentService.checkAgreement(clientId, code);

        // Then
        assertThat(hasAgreed).isFalse();
    }

    @Test
    @DisplayName("최신 버전 동의 여부 확인 - 기록 없음")
    void checkAgreement_NoRecord() {
        // Given
        String clientId = "client123";
        String code = "CODE1";
        definitionRepository.save(ConsentDefinition.of(code, "Title", "Desc", ConsentCategory.MARKETING, 1, LocalDateTime.now(), null));

        // When
        boolean hasAgreed = consentService.checkAgreement(clientId, code);

        // Then
        assertThat(hasAgreed).isFalse();
    }
}
