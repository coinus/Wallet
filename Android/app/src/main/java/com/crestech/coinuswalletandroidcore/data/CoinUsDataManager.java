package com.crestech.coinuswalletandroidcore.data;

import com.crestech.coinuswalletandroidcore.data.domain.wallet.CoinUsAccount;
import com.crestech.coinuswalletandroidcore.data.repository.local.LocalDataRequester;
import com.crestech.coinuswalletandroidcore.data.repository.remote.RemoteDataRequester;

import java.util.Map;

// Singleton
public class CoinUsDataManager implements CoinUsDataSource {

    private static final Object synchronizationLock = new Object();

    public enum NET_TYPE {
        REMOTE,
        LOCAL
    }

    private NET_TYPE mCurNetType = NET_TYPE.REMOTE;

    private static CoinUsDataManager sInstance;

    private RemoteDataRequester mRemoteDataSource; // Remote

    private LocalDataRequester mLocalDataSource; // Local

    synchronized public static CoinUsDataManager getInstance() {
        if (sInstance == null) {
            sInstance = new CoinUsDataManager();
        }
        return sInstance;
    }

    private CoinUsDataManager() {
        mLocalDataSource = LocalDataRequester.getInstance();
        mRemoteDataSource = RemoteDataRequester.getInstance(mLocalDataSource);
    }

    /**
     * Manages data communication with CoinUs Server.
     * Internal logic hidden.
     */

    public CoinUsAccount createAccount(int accountNo, String walletName, String walletPwd, String recoverySeed) {
        return mLocalDataSource.createAccount(accountNo, walletName, walletPwd, recoverySeed);
    }

    public boolean updateAccount(CoinUsAccount account) {
        return mLocalDataSource.updateAccount(account);
    }

    public void deleteAccount(int accountNo) {
        mLocalDataSource.deleteAccount();
    }

    public CoinUsAccount getCurrentAccount(int accountNo) {
        return mLocalDataSource.getAccount(accountNo);
    }

    public boolean getIsBackUp(int accountNo) {
        return mLocalDataSource.getIsBackUp(accountNo);
    }

    public void updateIsBackUp(int accountNo, boolean isBackup) {
        mLocalDataSource.updateAccountBackup(accountNo, isBackup);
    }

    public long insertWallet(Map<String, Object> params) {
        return mRemoteDataSource.insertWallet(params);
    }

    public boolean getEthAddressSync(String walletAddress) {
        return false;
    }

    @Override
    public NET_TYPE getDataSourceType() {
        return mCurNetType;
    }

    @Override
    public void cancelRequest() {

    }

    @Override
    public void requestHello() {

    }
}
