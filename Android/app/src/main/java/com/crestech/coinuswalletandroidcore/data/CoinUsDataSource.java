package com.crestech.coinuswalletandroidcore.data;

public interface CoinUsDataSource {

    CoinUsDataManager.NET_TYPE getDataSourceType();

    void requestHello();

    void cancelRequest();

}
