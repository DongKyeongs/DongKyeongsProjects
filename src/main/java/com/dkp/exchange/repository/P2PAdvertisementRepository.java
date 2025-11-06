package com.dkp.exchange.repository;

import com.dkp.exchange.model.P2PAdvertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface P2PAdvertisementRepository extends JpaRepository<P2PAdvertisement, Long> {
    List<P2PAdvertisement> findByStatus(P2PAdvertisement.AdvertisementStatus status);

    @Query("SELECT a FROM P2PAdvertisement a WHERE a.asset = :asset AND a.fiatCurrency = :fiatCurrency AND a.type = :type AND a.status = 'ACTIVE'")
    List<P2PAdvertisement> findActiveByAssetAndCurrencyAndType(@Param("asset") String asset,
                                                                @Param("fiatCurrency") String fiatCurrency,
                                                                @Param("type") P2PAdvertisement.TradeType type);

    List<P2PAdvertisement> findByMerchantIdAndStatus(Long merchantId, P2PAdvertisement.AdvertisementStatus status);
}
