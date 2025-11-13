package org.creditto.creditto_service.domain.consent.dto;

import org.creditto.creditto_service.domain.consent.entity.ConsentRecord;
import org.creditto.creditto_service.domain.consent.entity.ConsentStatus;

import java.time.LocalDateTime;

public record ConsentRecordRes(
        Long recordId,
        Long definitionId,
        String consentCode,
        Integer consentRecVer,
        ConsentStatus consentStatus,
        LocalDateTime consentDate,
        LocalDateTime withdrawalDate,
        String ipAddress,
        String clientId
) {

    public static ConsentRecordRes from(ConsentRecord r) {
        return new ConsentRecordRes(
                r.getId(),
                r.getConsentDefinition().getId(),
                r.getConsentDefinition().getConsentCode(),
                r.getConsentRecVer(),
                r.getConsentStatus(),
                r.getConsentDate(),
                r.getWithdrawalDate(),
                r.getIpAddress(),
                r.getClientId()
        );
    }
}
