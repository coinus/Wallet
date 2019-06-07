package com.crestech.coinuswalletandroidcore.data.repository.remote;

import com.crestech.coinuswalletandroidcore.data.repository.CoinUsRequester;
import com.crestech.coinuswalletandroidcore.data.repository.local.LocalDataRequester;

import java.util.Map;

// CoinUs Requester to CoinUs Server.
public class RemoteDataRequester implements CoinUsRequester {

    private static RemoteDataRequester sInstance;
    private LocalDataRequester mLocalRequester;

    synchronized public static RemoteDataRequester getInstance(LocalDataRequester localRequester) {
        if (sInstance == null) {
            sInstance = new RemoteDataRequester(localRequester);
        }

        return sInstance;
    }

    private RemoteDataRequester(LocalDataRequester localRequester) {
        mLocalRequester = localRequester;
    }

    @Override
    public void onStop() {

    }

    /**
     * Manages remote data communication with CoinUs Server.
     * Internal logic hidden.
     */

    public long insertWallet(Map<String, Object> params) {
        return -1;
    }
}
