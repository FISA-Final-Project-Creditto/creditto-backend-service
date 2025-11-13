package org.creditto.creditto_service.domain.consent.dto;

import org.creditto.creditto_service.domain.consent.entity.ConsentCategory;
import org.creditto.creditto_service.domain.consent.entity.ConsentDefinition;

import java.time.LocalDateTime;

public record ConsentDefinitionRes(
        Long definitionId,
        String consentCode,
        String consentTitle,
        String consentDesc,
        ConsentCategory consentCategory,
        Integer consentDefVer,
        LocalDateTime validFrom,
        LocalDateTime validTo
) {
    public static ConsentDefinitionRes from(ConsentDefinition d) {
        return new ConsentDefinitionRes(
                d.getId(),
                d.getConsentCode(),
                d.getConsentTitle(),
                d.getConsentDesc(),
                d.getConsentCategory(),
                d.getConsentDefVer(),
                d.getValidFrom(),
                d.getValidTo()
        );
    }
}
