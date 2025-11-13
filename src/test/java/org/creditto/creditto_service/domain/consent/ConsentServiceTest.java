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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConsentServiceTest {

    @InjectMocks
    private ConsentService consentService;

    @Mock
    private ConsentDefinitionRepository definitionRepository;

    @Mock
    private ConsentRecordRepository recordRepository;

    @Test
    @DisplayName("동의하기 - 성공")
    void agree_Success() {
        // Given
        ConsentAgreeReq req = new ConsentAgreeReq("client123", "CODE1", "127.0.0.1");
        ConsentDefinition definition = ConsentDefinition.of("CODE1", "Title", "Desc", ConsentCategory.MARKETING, 2, LocalDateTime.now(), null);

        given(definitionRepository.findLatestByCode("CODE1")).willReturn(Optional.of(definition));

        // When
        ConsentRecordRes result = consentService.agree(req);

        // Then
        verify(recordRepository).save(any(ConsentRecord.class));
        assertThat(result.consentCode()).isEqualTo("CODE1");
        assertThat(result.consentStatus()).isEqualTo(ConsentStatus.AGREE);
        assertThat(result.consentRecVer()).isEqualTo(2);
        assertThat(result.clientId()).isEqualTo("client123");
    }

    @Test
    @DisplayName("철회하기 - 성공")
    void withdraw_Success() {
        // Given
        ConsentWithdrawReq req = new ConsentWithdrawReq("client123", 1L);
        ConsentDefinition definition = ConsentDefinition.of("CODE1", "Title", "Desc", ConsentCategory.MARKETING, 1, LocalDateTime.now(), null);
        ConsentRecord record = ConsentRecord.of(definition, ConsentStatus.AGREE, 1, LocalDateTime.now(), null, "127.0.0.1", "client123");

        given(recordRepository.findLatestByClientAndDefinition(req.clientId(), req.definitionId())).willReturn(Optional.of(record));

        // When
        ConsentRecordRes result = consentService.withdraw(req);

        // Then
        assertThat(result.consentStatus()).isEqualTo(ConsentStatus.WITHDRAW);
        assertThat(result.withdrawalDate()).isNotNull();
    }

    @Test
    @DisplayName("모든 최신 동의서 조회")
    void getLatestConsentDefinitions_Success() {
        // Given
        ConsentDefinition def1 = ConsentDefinition.of("CODE1", "Title1", "Desc1", ConsentCategory.MARKETING, 2, LocalDateTime.now(), null);
        ConsentDefinition def2 = ConsentDefinition.of("CODE2", "Title2", "Desc2", ConsentCategory.SERVICE, 1, LocalDateTime.now(), null);
        given(definitionRepository.findLatestForAllCodes()).willReturn(List.of(def1, def2));

        // When
        List<ConsentDefinitionRes> results = consentService.getLatestConsentDefinitions();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).consentCode()).isEqualTo("CODE1");
        assertThat(results.get(1).consentCode()).isEqualTo("CODE2");
    }

    @Test
    @DisplayName("사용자 동의 내역 조회")
    void getConsentHistory_Success() {
        // Given
        String clientId = "client123";
        ConsentDefinition def1 = ConsentDefinition.of("CODE1", "Title1", "Desc1", ConsentCategory.MARKETING, 1, LocalDateTime.now(), null);
        ConsentRecord rec1 = ConsentRecord.of(def1, ConsentStatus.AGREE, 1, LocalDateTime.now(), null, "127.0.0.1", clientId);
        given(recordRepository.findAllByClientIdWithDefinition(clientId)).willReturn(List.of(rec1));

        // When
        List<ConsentRecordRes> results = consentService.getConsentRecord(clientId);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).clientId()).isEqualTo(clientId);
        assertThat(results.get(0).consentCode()).isEqualTo("CODE1");
    }

    @Test
    @DisplayName("최신 버전 동의 여부 확인 - 동의함")
    void checkAgreed_Agreed() {
        // Given
        String clientId = "client123";
        String code = "CODE1";
        ConsentDefinition definition = ConsentDefinition.of(code, "Title", "Desc", ConsentCategory.MARKETING, 2, LocalDateTime.now(), null);
        ConsentRecord record = ConsentRecord.of(definition, ConsentStatus.AGREE, 2, LocalDateTime.now(), null, "127.0.0.1", clientId);

        given(definitionRepository.findLatestByCode(code)).willReturn(Optional.of(definition));
        given(recordRepository.findLatestByClientAndCode(clientId, code)).willReturn(Optional.of(record));

        // When
        boolean hasAgreed = consentService.checkAgreement(clientId, code);

        // Then
        assertThat(hasAgreed).isTrue();
    }

    @Test
    @DisplayName("최신 버전 동의 여부 확인 - 이전 버전에 동의함")
    void checkAgreed_AgreedToOlderVersion() {
        // Given
        String clientId = "client123";
        String code = "CODE1";
        ConsentDefinition latestDefinition = ConsentDefinition.of(code, "Title", "Desc", ConsentCategory.MARKETING, 2, LocalDateTime.now(), null);
        ConsentDefinition oldDefinition = ConsentDefinition.of(code, "Title", "Desc", ConsentCategory.MARKETING, 1, LocalDateTime.now(), null);
        ConsentRecord record = ConsentRecord.of(oldDefinition, ConsentStatus.AGREE, 1, LocalDateTime.now(), null, "127.0.0.1", clientId);

        given(definitionRepository.findLatestByCode(code)).willReturn(Optional.of(latestDefinition));
        given(recordRepository.findLatestByClientAndCode(clientId, code)).willReturn(Optional.of(record));

        // When
        boolean hasAgreed = consentService.checkAgreement(clientId, code);

        // Then
        assertThat(hasAgreed).isFalse();
    }

    @Test
    @DisplayName("최신 버전 동의 여부 확인 - 철회함")
    void checkAgreed_Withdrawn() {
        // Given
        String clientId = "client123";
        String code = "CODE1";
        ConsentDefinition definition = ConsentDefinition.of(code, "Title", "Desc", ConsentCategory.MARKETING, 2, LocalDateTime.now(), null);
        ConsentRecord record = ConsentRecord.of(definition, ConsentStatus.WITHDRAW, 2, LocalDateTime.now(), LocalDateTime.now(), "127.0.0.1", clientId);

        given(definitionRepository.findLatestByCode(code)).willReturn(Optional.of(definition));
        given(recordRepository.findLatestByClientAndCode(clientId, code)).willReturn(Optional.of(record));

        // When
        boolean hasAgreed = consentService.checkAgreement(clientId, code);

        // Then
        assertThat(hasAgreed).isFalse();
    }

    @Test
    @DisplayName("최신 버전 동의 여부 확인 - 기록 없음")
    void checkAgreed_NoRecord() {
        // Given
        String clientId = "client123";
        String code = "CODE1";
        ConsentDefinition definition = ConsentDefinition.of(code, "Title", "Desc", ConsentCategory.MARKETING, 1, LocalDateTime.now(), null);

        given(definitionRepository.findLatestByCode(code)).willReturn(Optional.of(definition));
        given(recordRepository.findLatestByClientAndCode(clientId, code)).willReturn(Optional.empty());

        // When
        boolean hasAgreed = consentService.checkAgreement(clientId, code);

        // Then
        assertThat(hasAgreed).isFalse();
    }
}