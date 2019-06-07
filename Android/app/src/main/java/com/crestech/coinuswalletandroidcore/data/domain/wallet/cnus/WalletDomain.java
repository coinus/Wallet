package com.crestech.coinuswalletandroidcore.data.domain.wallet.cnus;

import java.io.Serializable;

import lombok.Data;

@Data
public class WalletDomain implements Serializable {

    private long wno;
    private long uid;
    private int coinId;
    private String walletAddress;
    private int walletAddressIndex;
    private String walletAddressNm;
    private String coinDcd;
    private String coin;
    private String coinImgPath;
    private String coinSymbol;
    private String coinNm;
    private String displayYn;

    public WalletDomain() {
    }

    public WalletDomain(WalletDomain original) {
        this.wno = original.wno;
        this.uid = original.uid;
        this.coinId = original.coinId;
        this.walletAddress = original.walletAddress;
        this.walletAddressIndex = original.walletAddressIndex;
        this.walletAddressNm = original.walletAddressNm;
        this.coinDcd = original.coinDcd;
        this.coin = original.coin;
        this.coinImgPath = original.coinImgPath;
        this.coinSymbol = original.coinSymbol;
        this.coinNm = original.coinNm;
        this.displayYn = original.displayYn;
    }

}