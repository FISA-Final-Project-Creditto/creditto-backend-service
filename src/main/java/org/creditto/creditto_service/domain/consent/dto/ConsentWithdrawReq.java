package org.creditto.creditto_service.domain.consent.dto;

// 철회 요청 DTO
public record ConsentWithdrawReq(
        String clientId,
        Long definitionId
) {
}
