package org.creditto.creditto_service.domain.consent.entity;

import lombok.Getter;

@Getter
public enum ConsentStatus {

    AGREE("동의"),
    WITHDRAW("철회");

    private final String description;

    ConsentStatus(String description) {
        this.description = description;
    }
}
