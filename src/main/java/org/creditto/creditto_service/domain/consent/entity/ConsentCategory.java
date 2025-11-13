package org.creditto.creditto_service.domain.consent.entity;

import lombok.Getter;

@Getter
public enum ConsentCategory {

    REQUIRED("필수"),
    OPTIONAL("선택"),
    MARKETING("마케팅"),
    FINANCIAL("전자금융거래"),
    PRIVACY("개인정보 처리"),
    SERVICE("서비스"),
    LOCATION("위치기반 서비스");

    private final String description;

    ConsentCategory(String description) {
        this.description = description;
    }
}
