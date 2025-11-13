package org.creditto.creditto_service.domain.consent.entity;

import jakarta.persistence.*;
import lombok.*;
import org.creditto.creditto_service.global.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsentRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definition_id")
    private ConsentDefinition consentDefinition;

    @Enumerated(EnumType.STRING)
    private ConsentStatus consentStatus;

    private Integer consentRecVer; // 사용자가 동의한 동의서 버전

    private LocalDateTime consentDate; // 동의 시점
    private LocalDateTime withdrawalDate; // 철회 시점

    private String ipAddress;

    private String clientId; // 외부 클라이언트 ID

    public static ConsentRecord of(ConsentDefinition consentDefinition, ConsentStatus consentStatus, Integer consentRecVer, LocalDateTime consentDate, LocalDateTime withdrawalDate, String ipAddress, String clientId) {
        return ConsentRecord.builder()
                .consentDefinition(consentDefinition)
                .consentStatus(consentStatus)
                .consentRecVer(consentRecVer)
                .consentDate(consentDate)
                .withdrawalDate(withdrawalDate)
                .ipAddress(ipAddress)
                .clientId(clientId)
                .build();
    }

    // 동의 -> 철회 변경
    public void withdraw() {
        this.consentStatus = ConsentStatus.WITHDRAW;
        this.withdrawalDate = LocalDateTime.now();
    }
}
