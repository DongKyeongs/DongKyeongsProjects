package com.dkp.exchange.service;

import com.dkp.exchange.model.ExchangeSettings;
import com.dkp.exchange.model.User;
import com.dkp.exchange.model.UserRole;
import com.dkp.exchange.repository.ExchangeSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeServiceTest {

    @Mock
    private ExchangeSettingsRepository exchangeSettingsRepository;

    @InjectMocks
    private FeeService feeService;

    private User testUser;
    private ExchangeSettings testSettings;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(UserRole.USER);

        testSettings = new ExchangeSettings();
        testSettings.setUser(testUser);
        testSettings.setMakerFee(new BigDecimal("0.001"));
        testSettings.setTakerFee(new BigDecimal("0.002"));
    }

    @Test
    void calculateMakerFee_ShouldReturnCorrectFee() {
        // Given
        BigDecimal tradeAmount = new BigDecimal("1000");
        when(exchangeSettingsRepository.findByUserId(any())).thenReturn(Optional.of(testSettings));

        // When
        BigDecimal fee = feeService.calculateMakerFee(testUser, tradeAmount);

        // Then
        assertEquals(new BigDecimal("1.00000000"), fee);
    }

    @Test
    void calculateTakerFee_ShouldReturnCorrectFee() {
        // Given
        BigDecimal tradeAmount = new BigDecimal("1000");
        when(exchangeSettingsRepository.findByUserId(any())).thenReturn(Optional.of(testSettings));

        // When
        BigDecimal fee = feeService.calculateTakerFee(testUser, tradeAmount);

        // Then
        assertEquals(new BigDecimal("2.00000000"), fee);
    }

    @Test
    void calculateWithdrawalFee_BTC_ShouldReturnCorrectFee() {
        // When
        BigDecimal fee = feeService.calculateWithdrawalFee("BTC", BigDecimal.ONE);

        // Then
        assertEquals(new BigDecimal("0.0005"), fee);
    }

    @Test
    void calculateWithdrawalFee_ETH_ShouldReturnCorrectFee() {
        // When
        BigDecimal fee = feeService.calculateWithdrawalFee("ETH", BigDecimal.ONE);

        // Then
        assertEquals(new BigDecimal("0.005"), fee);
    }

    @Test
    void calculateWithdrawalFee_USDT_ShouldReturnCorrectFee() {
        // When
        BigDecimal fee = feeService.calculateWithdrawalFee("USDT", BigDecimal.ONE);

        // Then
        assertEquals(new BigDecimal("1.0"), fee);
    }
}
