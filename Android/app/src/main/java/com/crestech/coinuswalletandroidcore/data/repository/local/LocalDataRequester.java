package com.crestech.coinuswalletandroidcore.data.repository.local;

import com.crestech.coinuswalletandroidcore.common.CLog;
import com.crestech.coinuswalletandroidcore.data.domain.wallet.CoinUsAccount;
import com.crestech.coinuswalletandroidcore.data.repository.CoinUsRequester;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

public class LocalDataRequester implements CoinUsRequester {

    public static LocalDataRequester sInstance;

    synchronized public static LocalDataRequester getInstance() {
        if (sInstance == null) {
            sInstance = new LocalDataRequester();
        }
        return sInstance;
    }

    private LocalDataRequester() {

    }

    @Override
    public void onStop() {
    }

    /**
     * Manages local data.
     * Internal logic hidden.
     */

    public CoinUsAccount createAccount(int accountNo, String walletName, String encrytpedWalletPwd, String encrytedSeed) {
        CoinUsAccount coinUsAccount = null;

        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        // max seq, accountNo
        CoinUsAccount account = realm.createObject(CoinUsAccount.class); // insert already
        account.setWalletName(walletName);
        account.setWalletPwd(encrytpedWalletPwd);
        account.setAccountNo(accountNo);
        account.setWalletSeed(encrytedSeed);
        account.setUpdateDt(new Date());
        account.setBackup(false);

        CLog.e("WalletName : " + account.getWalletName());
        CLog.e("WalletPwd : " + account.getWalletPwd());
        CLog.e("AccountNo : " + account.getAccountNo());
        CLog.e("WalletSeed : " + account.getWalletSeed());
        CLog.e("UpdateDt : " + account.getUpdateDt());
        CLog.e("IsBackup : " + account.isBackup());

        coinUsAccount = realm.copyFromRealm(account);

        realm.commitTransaction();
        realm.close();

        return coinUsAccount;
    }

    public boolean updateAccount(CoinUsAccount account) {
        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        CoinUsAccount coinUsAccount = realm.where(CoinUsAccount.class).equalTo("accountNo", account.getAccountNo()).findFirst();
        if (coinUsAccount != null) {
            coinUsAccount.setWalletSeed(account.getWalletSeed());
            coinUsAccount.setWalletName(account.getWalletName());
            coinUsAccount.setBackup(account.isBackup());
            coinUsAccount.setSecureRnd(account.getSecureRnd());
            coinUsAccount.setUpdateDt(account.getUpdateDt());
            coinUsAccount.setWalletPwd(account.getWalletPwd());

            realm.insertOrUpdate(coinUsAccount);
        } else {
            realm.close();
            return false;
        }

        realm.commitTransaction();
        realm.close();

        return true;
    }

    public CoinUsAccount getAccount(int accountNo) {
        CoinUsAccount coinUsAccount = null;

        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        RealmResults<CoinUsAccount> account = realm.where(CoinUsAccount.class).equalTo("accountNo", accountNo).findAll();
        if (account != null && account.size() > 0) {
            CLog.d("account size : " + account.size());
            coinUsAccount = realm.copyFromRealm(account.get(0));
        }

        realm.commitTransaction();
        realm.close();

        return coinUsAccount;
    }

    public boolean getIsBackUp(int accountNo) {
        boolean isBackup = false;

        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        CoinUsAccount coinUsAccount = realm.where(CoinUsAccount.class).equalTo("accountNo", accountNo).findFirst();
        isBackup = coinUsAccount.isBackup();
        realm.commitTransaction();
        realm.close();

        return isBackup;
    }

    public void updateAccountBackup(final int accountNo, final boolean isBackup) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                CoinUsAccount coinUsAccount = realm.where(CoinUsAccount.class).equalTo("accountNo", Integer.valueOf(accountNo)).findFirst();
                if (coinUsAccount != null) {
                    coinUsAccount.setBackup(isBackup);
                    realm.insertOrUpdate(coinUsAccount);
                } else {
                    CLog.w("Account Object is null");
                }
            }
        });
        realm.close();
    }

    public boolean deleteAccount() {
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<CoinUsAccount> targetResult = realm.where(CoinUsAccount.class).findAll();
                targetResult.deleteAllFromRealm();
            }
        });

        realm.close();

        return true;
    }

}
