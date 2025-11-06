package com.dkp.exchange.service;

import com.dkp.exchange.model.KycVerification;
import com.dkp.exchange.model.User;
import com.dkp.exchange.repository.KycVerificationRepository;
import com.dkp.exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycService {
    private final KycVerificationRepository kycRepository;
    private final UserRepository userRepository;

    @Transactional
    public KycVerification submitKycVerification(Long userId, KycVerification kycData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 기존 KYC가 있으면 업데이트
        KycVerification kyc = kycRepository.findByUserId(userId)
                .orElse(new KycVerification());

        kyc.setUser(user);
        kyc.setFullName(kycData.getFullName());
        kyc.setDateOfBirth(kycData.getDateOfBirth());
        kyc.setNationality(kycData.getNationality());
        kyc.setAddress(kycData.getAddress());
        kyc.setCity(kycData.getCity());
        kyc.setCountry(kycData.getCountry());
        kyc.setPostalCode(kycData.getPostalCode());
        kyc.setDocumentType(kycData.getDocumentType());
        kyc.setDocumentNumber(kycData.getDocumentNumber());
        kyc.setDocumentFrontImageUrl(kycData.getDocumentFrontImageUrl());
        kyc.setDocumentBackImageUrl(kycData.getDocumentBackImageUrl());
        kyc.setSelfieImageUrl(kycData.getSelfieImageUrl());
        kyc.setProofOfAddressUrl(kycData.getProofOfAddressUrl());
        kyc.setStatus(KycVerification.VerificationStatus.PENDING);
        kyc.setSubmittedAt(LocalDateTime.now());

        KycVerification saved = kycRepository.save(kyc);
        log.info("KYC verification submitted for user {}", userId);

        return saved;
    }

    @Transactional
    public void approveKyc(Long kycId, KycVerification.KycLevel level) {
        KycVerification kyc = kycRepository.findById(kycId)
                .orElseThrow(() -> new RuntimeException("KYC not found"));

        kyc.setStatus(KycVerification.VerificationStatus.APPROVED);
        kyc.setLevel(level);
        kyc.setVerifiedAt(LocalDateTime.now());

        // 만료일 설정 (2년)
        kyc.setExpiresAt(LocalDateTime.now().plusYears(2));

        kycRepository.save(kyc);
        log.info("KYC approved for user {} with level {}", kyc.getUser().getId(), level);
    }

    @Transactional
    public void rejectKyc(Long kycId, String reason) {
        KycVerification kyc = kycRepository.findById(kycId)
                .orElseThrow(() -> new RuntimeException("KYC not found"));

        kyc.setStatus(KycVerification.VerificationStatus.REJECTED);
        kyc.setRejectionReason(reason);

        kycRepository.save(kyc);
        log.info("KYC rejected for user {}: {}", kyc.getUser().getId(), reason);
    }

    @Transactional(readOnly = true)
    public KycVerification.KycLevel getUserKycLevel(Long userId) {
        return kycRepository.findByUserId(userId)
                .map(KycVerification::getLevel)
                .orElse(KycVerification.KycLevel.UNVERIFIED);
    }

    @Transactional(readOnly = true)
    public boolean isKycVerified(Long userId) {
        return kycRepository.findByUserId(userId)
                .map(kyc -> kyc.getLevel() != KycVerification.KycLevel.UNVERIFIED
                        && kyc.getStatus() == KycVerification.VerificationStatus.APPROVED)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<KycVerification> getPendingVerifications() {
        return kycRepository.findPendingVerifications();
    }
}
