package com.crestech.coinuswalletandroidcore.data.domain.wallet.cnus;

import java.io.Serializable;

import lombok.Data;

@Data
public class WalletCryptoDomain implements Serializable {

    private long cryptoSeq;
    private long wno;
    private String walletAddress;
    private String cryptoTypeDcd;
    private String cryptoType;
    private long cryptoId;
    private String cryptoImgPath;
    private String cryptoSymbol;
    private String cryptoNm;
    private String tokenTypeDcd;
    private String tokenType;
    private int tokenDecimals;
    private String contractAddress;
    private String balance;
    private WalletCryptoMarketCapDomain marketCap;
    private int cryptoOrd;
    private String cryptoActiveYn;

    private long createDt;
    private long updateDt;

}