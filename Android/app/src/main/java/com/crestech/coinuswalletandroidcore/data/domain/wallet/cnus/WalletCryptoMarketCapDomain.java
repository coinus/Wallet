package com.crestech.coinuswalletandroidcore.data.domain.wallet.cnus;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class WalletCryptoMarketCapDomain implements Serializable {

    private long cmcId;
    private String cmcSlug;
    private String cmcUrls;
    private String cmcActiveYn;
    private BigDecimal priceBtc;
    private BigDecimal priceUsd;
    private BigDecimal priceKrw;
    private BigDecimal priceCny;
    private BigDecimal priceJpy;
    private String currency;
    private String currencyPrice;

}