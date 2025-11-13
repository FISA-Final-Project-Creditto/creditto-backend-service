package org.creditto.creditto_service.domain.consent.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.creditto.creditto_service.domain.consent.dto.ConsentAgreeReq;
import org.creditto.creditto_service.domain.consent.dto.ConsentDefinitionRes;
import org.creditto.creditto_service.domain.consent.dto.ConsentRecordRes;
import org.creditto.creditto_service.domain.consent.dto.ConsentWithdrawReq;
import org.creditto.creditto_service.domain.consent.service.ConsentService;
import org.creditto.creditto_service.global.response.ApiResponseUtil;
import org.creditto.creditto_service.global.response.BaseResponse;
import org.creditto.creditto_service.global.response.SuccessCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 동의서 관련 API 요청을 처리하는 컨트롤러
 * 클라이언트의 동의 및 철회, 동의서 조회 등의 기능을 제공
 */
@RestController
@RequestMapping("/api/consent")
@RequiredArgsConstructor
public class ConsentController {

    private final ConsentService consentService;

    /**
     * 모든 동의서의 최신 버전을 조회
     *
     * @return 최신 버전의 모든 동의서 목록을 포함하는 응답 엔티티
     */
    @GetMapping("/definitions")
    public ResponseEntity<BaseResponse<?>> getLatestConsentDefinitions() {
        List<ConsentDefinitionRes> definitions = consentService.getLatestConsentDefinitions();
        return ApiResponseUtil.success(SuccessCode.OK, definitions);
    }

    /**
     * 사용자가 특정 동의서에 동의
     *
     * @param req 동의 요청 정보 (동의 코드, IP 주소, 클라이언트 ID)
     * @return 생성된 동의 기록 정보를 포함하는 응답 엔티티
     */
    @PostMapping("/agree")
    public ResponseEntity<BaseResponse<?>> agreeConsent(@Valid @RequestBody ConsentAgreeReq req) {
        ConsentRecordRes record = consentService.agree(req);
        return ApiResponseUtil.success(SuccessCode.CREATED, record);
    }

    /**
     * 사용자가 동의했던 내역을 철회
     *
     * @param req 동의 철회 요청 정보 (클라이언트 ID, 동의서 정의 ID)
     * @return 철회 처리된 동의 기록 정보를 포함하는 응답 엔티티
     */
    @PostMapping("/withdraw")
    public ResponseEntity<BaseResponse<?>> withdrawConsent(@Valid @RequestBody ConsentWithdrawReq req) {
        ConsentRecordRes record = consentService.withdraw(req);
        return ApiResponseUtil.success(SuccessCode.OK, record);
    }

    /**
     * 특정 사용자의 전체 동의 내역을 조회
     *
     * @param clientId 클라이언트 ID
     * @return 해당 사용자의 전체 동의 기록 목록을 포함하는 응답 엔티티
     */
    @GetMapping("/record/{clientId}")
    public ResponseEntity<BaseResponse<?>> getConsentRecord(@PathVariable String clientId) {
        List<ConsentRecordRes> record = consentService.getConsentRecord(clientId);
        return ApiResponseUtil.success(SuccessCode.OK, record);
    }

    /**
     * 특정 사용자가 특정 동의서의 최신 버전에 동의했는지 확인
     *
     * @param clientId 클라이언트 ID
     * @param code 동의 코드
     * @return 동의 여부(true/false)를 포함하는 응답 엔티티
     */
    @GetMapping("/check/{clientId}/{code}")
    public ResponseEntity<BaseResponse<?>> checkAgreement(@PathVariable String clientId, @PathVariable String code) {
        boolean hasAgreed = consentService.checkAgreement(clientId, code);
        return ApiResponseUtil.success(SuccessCode.OK, hasAgreed);
    }
}