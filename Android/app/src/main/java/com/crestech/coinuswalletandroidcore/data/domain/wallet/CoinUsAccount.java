package com.crestech.coinuswalletandroidcore.data.domain.wallet;

import java.util.Date;

import io.realm.RealmObject;
import lombok.Data;

@Data
public class CoinUsAccount extends RealmObject {

    private int accountNo;
    private String walletName;
    private String walletPwd;
    private String walletSeed;
    private boolean isBackup;
    private Date updateDt;
    private byte[] secureRnd;

}