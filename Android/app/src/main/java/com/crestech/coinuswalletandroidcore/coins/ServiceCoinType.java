package com.crestech.coinuswalletandroidcore.coins;

public enum ServiceCoinType {

    BITCOIN(10002),
    ETHER(10001),
    ;

    public final long mCoinId;

    ServiceCoinType(long coinId) {
        mCoinId = coinId;
    }

    public String toString() {
        return String.valueOf(mCoinId);
    }

    public long getCoinId() {
        return mCoinId;
    }
}
