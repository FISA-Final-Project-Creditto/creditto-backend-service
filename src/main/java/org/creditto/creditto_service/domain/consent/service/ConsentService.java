package org.creditto.creditto_service.domain.consent.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.creditto.creditto_service.domain.consent.dto.ConsentAgreeReq;
import org.creditto.creditto_service.domain.consent.dto.ConsentDefinitionRes;
import org.creditto.creditto_service.domain.consent.dto.ConsentRecordRes;
import org.creditto.creditto_service.domain.consent.dto.ConsentWithdrawReq;
import org.creditto.creditto_service.domain.consent.entity.ConsentDefinition;
import org.creditto.creditto_service.domain.consent.entity.ConsentRecord;
import org.creditto.creditto_service.domain.consent.entity.ConsentStatus;
import org.creditto.creditto_service.domain.consent.repository.ConsentDefinitionRepository;
import org.creditto.creditto_service.domain.consent.repository.ConsentRecordRepository;
import org.creditto.creditto_service.global.response.error.ErrorBaseCode;
import org.creditto.creditto_service.global.response.exception.CustomBaseException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 동의서 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class ConsentService {

    private final ConsentDefinitionRepository definitionRepository;
    private final ConsentRecordRepository recordRepository;


    /**
     * 사용자의 동의를 기록
     *
     * @param req 동의 요청 정보 (동의 코드, IP 주소, 클라이언트 ID)
     * @return 생성된 동의 기록 정보
     * @throws CustomBaseException 해당 동의 코드의 최신 동의서 정의를 찾을 수 없을 때 발생
     */
    @Transactional
    public ConsentRecordRes agree(ConsentAgreeReq req) {

        ConsentDefinition latestDefinition = getLatestDefinition(req.consentCode());

        ConsentRecord newRecord = ConsentRecord.of(
                latestDefinition,
                ConsentStatus.AGREE,
                latestDefinition.getConsentDefVer(),
                LocalDateTime.now(),
                null,
                req.ipAddress(),
                req.clientId()
        );

        recordRepository.save(newRecord);

        return ConsentRecordRes.from(newRecord);
    }

    /**
     * 사용자의 동의를 철회
     *
     * @param req 동의 철회 요청 정보 (클라이언트 ID, 동의서 정의 ID)
     * @return 철회 처리된 동의 기록 정보
     * @throws CustomBaseException 철회할 동의 기록을 찾을 수 없을 때 발생
     */
    @Transactional
    public ConsentRecordRes withdraw(ConsentWithdrawReq req) {

        ConsentRecord findRecord = recordRepository.findLatestByClientAndDefinition(req.clientId(), req.definitionId())
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_RECORD));

        findRecord.withdraw();

        return ConsentRecordRes.from(findRecord);
    }

    /**
     * 같은 코드의 모든 동의서 최신 버전을 조회
     *
     * @return 최신 버전의 모든 동의서 목록
     */
    public List<ConsentDefinitionRes> getLatestConsentDefinitions() {
        return definitionRepository.findLatestForAllCodes().stream()
                .map(ConsentDefinitionRes::from)
                .collect(Collectors.toList());
    }

    // 특정 코드의 동의서 최신 버전 조회
    private ConsentDefinition getLatestDefinition(String consentCode) {
        return definitionRepository.findTopByConsentCodeOrderByConsentDefVerDesc(consentCode)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_DEFINITION));
    }

    /**
     * 특정 사용자의 전체 동의 기록을 조회
     * N+1 문제를 방지하기 위해 Fetch Join을 사용
     *
     * @param clientId 클라이언트 ID
     * @return 해당 사용자의 전체 동의 기록 목록
     */
    public List<ConsentRecordRes> getConsentRecord(String clientId) {
        return recordRepository.findAllByClientIdWithDefinition(clientId).stream()
                .map(ConsentRecordRes::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자가 특정 동의 코드의 최신 버전에 동의했는지 확인
     *
     * @param clientId 클라이언트 ID
     * @param code 동의 코드
     * @return 최신 버전에 동의한 경우 true, 그렇지 않으면 false
     */
    public boolean checkAgreement(String clientId, String code) {

        ConsentDefinition latestDefinition = getLatestDefinition(code);

        return recordRepository.findLatestByClientAndCode(clientId, code)
                .map(r ->
                        r.getConsentRecVer().equals(latestDefinition.getConsentDefVer()) &&
                        r.getConsentStatus() == ConsentStatus.AGREE
                ).orElse(false);
    }
}
