package com.crestech.coinuswalletandroidcore.coins;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.HDUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * CoinType superclass that all coin types must extend. Holds common operation schema for all crypto coins.
 */
public abstract class CoinType implements Serializable {

    private static final String BIP_44_KEY_PATH = "44H/%dH/%dH";
    private static final String BIP_32_KEY_PATH = "%dH/%dH";

    public long coinId;
    public String coinDcd;
    public String name;
    public String symbol;
    public int bip44Index;
    public String contractAddress;
    public int decimal;
    public BigInteger fee;
    public String minUnitNm;

    public abstract String getMinCoinUnit();

    public abstract BigInteger getFee(int position);

    public abstract BigDecimal getTotalPrice(int position);

    public abstract String getCoinValue(BigDecimal totalPrice, String format);

    public abstract BigDecimal getSendBalanceFormat(BigDecimal funds);

    public abstract int getPositionWithFeeValue(String totalCoinFeeValue);

    public abstract boolean checkAddressValidation(String address);

    public long getCoinId() {
        return coinId;
    }

    public void setCoinId(long coinId) {
        this.coinId = coinId;
    }

    public String getCoinDcd() {
        return coinDcd;
    }

    public void setCoinDcd(String coinDcd) {
        this.coinDcd = coinDcd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getBip44Index() {
        return bip44Index;
    }

    public void setBip44Index(int bip44Index) {
        this.bip44Index = bip44Index;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public int getDecimal() {
        return decimal;
    }

    public void setDecimal(int decimal) {
        this.decimal = decimal;
    }

    public BigInteger getFee() {
        return fee;
    }

    public void setFee(BigInteger fee) {
        this.fee = fee;
    }

    public List<ChildNumber> getBip44Path(int account) {
        String path = String.format(BIP_44_KEY_PATH, bip44Index, account);
        return HDUtils.parsePath(path);
    }

    public List<ChildNumber> getBip32Path(int account) {
        String path = String.format(BIP_32_KEY_PATH, bip44Index, account);
        return HDUtils.parsePath(path);
    }

    @Override
    public String toString() {
        return "Coin {" +
                "coinId='" + coinId + "\'" +
                "name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", bip44Index=" + bip44Index +
                '}';
    }


}
