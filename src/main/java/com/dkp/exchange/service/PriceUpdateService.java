package com.dkp.exchange.service;

import com.dkp.exchange.dto.PriceUpdateDTO;
import com.dkp.exchange.model.Coin;
import com.dkp.exchange.repository.CoinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PriceUpdateService {
    private final SimpMessagingTemplate messagingTemplate;
    private final CoinRepository coinRepository;
    private final Random random = new Random();

    @Scheduled(fixedRate = 1000) // 1초마다 실행
    public void updatePrices() {
        List<Coin> coins = coinRepository.findAll();
        
        for (Coin coin : coins) {
            // 실제 구현에서는 외부 API를 호출하여 가격을 가져와야 합니다
            // 여기서는 데모를 위해 랜덤 가격 변동을 시뮬레이션합니다
            BigDecimal currentPrice = coin.getCurrentPrice();
            BigDecimal priceChange = currentPrice.multiply(BigDecimal.valueOf(random.nextDouble() * 0.02 - 0.01))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal newPrice = currentPrice.add(priceChange);
            
            // 24시간 거래량도 업데이트
            BigDecimal volumeChange = coin.getVolume24h().multiply(BigDecimal.valueOf(random.nextDouble() * 0.1))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal newVolume = coin.getVolume24h().add(volumeChange);

            // 코인 정보 업데이트
            coin.setCurrentPrice(newPrice);
            coin.setVolume24h(newVolume);
            coinRepository.save(coin);

            // WebSocket을 통해 클라이언트에게 업데이트 전송
            PriceUpdateDTO update = new PriceUpdateDTO(
                coin.getSymbol(),
                newPrice,
                newVolume,
                priceChange,
                LocalDateTime.now()
            );
            
            messagingTemplate.convertAndSend("/topic/price/" + coin.getSymbol(), update);
        }
    }
} 