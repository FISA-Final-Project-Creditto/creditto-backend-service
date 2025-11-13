package org.creditto.creditto_service.domain.consent.dto;

// 동의 요청 DTO
public record ConsentAgreeReq(
        String clientId,
        String consentCode,
        String ipAddress
) {
}
