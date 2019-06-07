package com.crestech.coinuswalletandroidcore.data;


public interface CoinUsResponseCallback<T> {

    void onResultFetched(T response);

    void onResultFailed(int code, String message);

}
