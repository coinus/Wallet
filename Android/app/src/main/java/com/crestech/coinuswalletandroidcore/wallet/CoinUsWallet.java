package com.crestech.coinuswalletandroidcore.wallet;

import android.support.annotation.Nullable;
import android.util.Base64;

import com.crestech.coinuswalletandroidcore.bip39.Mnemonic;
import com.crestech.coinuswalletandroidcore.coins.CoinType;
import com.crestech.coinuswalletandroidcore.coins.ethereum.EthereumCoins;
import com.crestech.coinuswalletandroidcore.common.CLog;
import com.crestech.coinuswalletandroidcore.common.CoinUsConstants;
import com.crestech.coinuswalletandroidcore.common.CoinUsUtils;
import com.crestech.coinuswalletandroidcore.data.CoinUsDataManager;
import com.crestech.coinuswalletandroidcore.data.CoinUsResponseCallback;
import com.crestech.coinuswalletandroidcore.data.domain.wallet.CoinUsAccount;
import com.crestech.coinuswalletandroidcore.data.domain.wallet.cnus.WalletCryptoDomain;
import com.crestech.coinuswalletandroidcore.data.domain.wallet.cnus.WalletDomain;
import com.crestech.coinuswalletandroidcore.wallet.pocket.WalletHDEtherPocket;

import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.web3j.crypto.Credentials;
import org.web3j.utils.Numeric;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Base wallet class for CoinUs Wallet. Manages all wallet related operations.
 */
public class CoinUsWallet {

    private static CoinUsWallet sInstance;
    public static String WALLET_LANG_EN = "en";

    private List<CoinType> mServiceCoinType;
    private LinkedHashMap<Long, CoinUsServiceWallet> mServiceWallets;
    private CoinUsDataManager mDataManager;

    synchronized static public CoinUsWallet getInstance() {
        if (sInstance == null) {
            sInstance = new CoinUsWallet();
        }
        return sInstance;
    }

    private CoinUsWallet() {
        init();
    }

    private void init() {
        mDataManager = CoinUsDataManager.getInstance();
        mServiceCoinType = CoinUsConstants.getServiceCoinTypes();
    }

    public boolean createWallet(int accountNo, String walletName, String walletEncryptedPassword, String language, int keyStrength, String seed) { //keyStrength - BIP39_ENTROPY_LEN_256
        boolean result = false;
        do {
            CLog.d("createWallet : called");
            // Mnemonic Create
            if (seed == null || seed.isEmpty()) {
                seed = Mnemonic.generateSeeds(language, keyStrength);
                if (seed == null) {
                    return false;
                }
            }
            CLog.d("createWallet - user seeds : " + seed);

            // Create Account
            String encryptedSeed = ""; // encryption logic hidden
            result = createAccount(accountNo, walletEncryptedPassword, encryptedSeed, walletName);
            if (!result) {
                break;
            }

            mServiceWallets = createAllServiceWallets();
            if (mServiceWallets == null || mServiceWallets.size() == 0) {
                break;
            }

        } while (false);

        return result;
    }

    @Nullable
    public String createCnusWallet(int accountNo, int addressIndex, CoinType targetCoinType) {
        CoinUsAccount account = CoinUsWallet.getInstance().getCurrentAccount();

        String seed = CoinUsWallet.getInstance().getDecryptedSeed(accountNo, account.getWalletPwd());

        for (CoinType coinType : mServiceCoinType) {
            if (targetCoinType.getCoinId() == coinType.getCoinId()) {
                DeterministicKey parentKey= getParentKey(coinType, seed, accountNo);

                if (mServiceWallets == null) {
                    mServiceWallets = createAllServiceWallets();
                }

                CoinUsServiceWallet serviceWallet = mServiceWallets.get(coinType.getCoinId());
                return serviceWallet.createCnusWalletAddress(parentKey, accountNo, addressIndex);
            }
        }

        return null;
    }

    public String getCnusWalletAddressInMnemonic(int accountNo, int addressIndex, CoinType targetCoinType) {
        CoinUsAccount account = CoinUsWallet.getInstance().getCurrentAccount();

        String seed = CoinUsWallet.getInstance().getDecryptedSeed(accountNo, account.getWalletPwd());

        for (CoinType coinType : mServiceCoinType) {
            if (targetCoinType.getCoinId() == coinType.getCoinId()) {
                DeterministicKey parentKey;
                parentKey = getParentKey(coinType, seed, accountNo);

                if(mServiceWallets == null) {
                    mServiceWallets = createAllServiceWallets();
                }

                CoinUsServiceWallet serviceWallet = mServiceWallets.get(coinType.getCoinId());
                return serviceWallet.getCnusWalletAddressEachCoin(parentKey, accountNo, addressIndex);
            }
        }
        return "";
    }

    private DeterministicKey getParentKey(CoinType coinType, String seed, int accountNo) {
        DeterministicKey parentKey;
        DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(Mnemonic.getEntropyByteSeeds(seed));
        DeterministicHierarchy hierarchy = new DeterministicHierarchy(masterKey);

        parentKey = hierarchy.get(coinType.getBip44Path(accountNo), false, true);

        return parentKey;
    }

    private boolean createAccount(int accountNo, String walletEncryptedPassword, String encryptedSeed, String walletName) {
        CoinUsAccount coinUsAccount = mDataManager.createAccount(accountNo, walletName, walletEncryptedPassword, encryptedSeed);
        return coinUsAccount != null;
    }

    public boolean updateAccount(CoinUsAccount account) {
        return mDataManager.updateAccount(account);
    }

    public boolean loadWallet(int accountNo) {
        CLog.d("called");
        mServiceWallets = loadAllServiceWallets(accountNo);
        printAccountInfo("loadWallet");
        return mServiceWallets != null && mServiceWallets.size() > 0;
    }

    public void checkBalanceHistory(CoinUsResponseCallback<Boolean> callback) {
        CLog.e("check Start Time : " + CoinUsUtils.dateFormat(new Date(), "HH:mm:ss"));
        CoinUsAccount account = CoinUsWallet.getInstance().getCurrentAccount();
        int accountNo = 0;
        CLog.e("check 1 Time : " + CoinUsUtils.dateFormat(new Date(), "HH:mm:ss"));
        String seed = CoinUsWallet.getInstance().getDecryptedSeed(0, account.getWalletPwd());

        CLog.e("check 2 Time : " + CoinUsUtils.dateFormat(new Date(), "HH:mm:ss"));
        for (CoinType coinType : mServiceCoinType) {
            DeterministicKey parentKey = null;
            parentKey = getParentKey(coinType, seed, accountNo);

            if (getWalletPocket(coinType) != null) {
                getWalletPocket(coinType).checkBalanceHistory(parentKey, callback);
            } else {
                CLog.d("getWalletPocket is null");
            }
        }
    }

    public CoinUsServiceWallet getWalletPocket(CoinType coinType) {
        mServiceWallets = loadAllServiceWallets(0);
        if (mServiceWallets != null && mServiceWallets.containsKey(coinType.getCoinId())) {
            return mServiceWallets.get(coinType.getCoinId());
        }

        return null;
    }

    public LinkedHashMap<Long, CoinUsServiceWallet> getWalletPockets() {
        if (mServiceWallets == null) {
            mServiceWallets = loadAllServiceWallets(0);
        }
        return mServiceWallets;
    }

    private LinkedHashMap<Long, CoinUsServiceWallet> createAllServiceWallets() {
        if (mServiceCoinType != null) {

            LinkedHashMap<Long, CoinUsServiceWallet> serviceWallet = new LinkedHashMap<>();
            for (CoinType coinType : mServiceCoinType) {

                CoinUsServiceWallet pocket;

                if (coinType instanceof EthereumCoins) {
                    pocket = new WalletHDEtherPocket(coinType);
                } else {
                    CLog.w(coinType.getCoinId() + " Can't involved in service ");
                    continue;
                }

                serviceWallet.put(coinType.getCoinId(), pocket);
                CLog.i(coinType.getCoinId() + " Pocket Created");
            }
            return serviceWallet;
        } else {
            CLog.w("ServiceCoinType is null");
        }

        return null;
    }

    private LinkedHashMap<Long, CoinUsServiceWallet> loadAllServiceWallets(int accountNo) {
        if (mServiceWallets == null) {
            return createAllServiceWallets();
        }

        return mServiceWallets;
    }

    private CoinUsServiceWallet createEtherPocket(int accountNo, String encryptedSeed, String walletEncryptedPassword) {

        return null;
    }

    private CoinUsServiceWallet createBitcoinPocket(int accountNo, String encryptedSeed, String walletEncryptedPassword) {

        return null;
    }

    private CoinUsServiceWallet createLitecoinPocket(int accountNo, String encryptedSeed, String walletEncryptedPassword) {

        return null;
    }

    private CoinUsServiceWallet createQtumPocket(int accountNo, String encryptedSeed, String walletEncryptedPassword) {

        return null;
    }

    public CoinUsAccount getCurrentAccount() {
        int accountNo = 0;
        if (accountNo >= 0) {
            CLog.d("Current AccountNo : " + accountNo);
            if (mDataManager != null) {
                CoinUsAccount account = mDataManager.getCurrentAccount(accountNo);
                return account;
            } else {
                CLog.w("mDataManager is null");
            }
        } else {
            CLog.w("accountNo is " + accountNo);
        }
        return null;
    }

    public String getWalletName(int accountNo) {
        if (accountNo >= 0) {
            CLog.d("Current AccountNo : " + accountNo);
            if (mDataManager != null) {
                CoinUsAccount account = mDataManager.getCurrentAccount(accountNo);
                return account.getWalletName();
            } else {
                CLog.w("mDataManager is null");
            }
        } else {
            CLog.w("accountNo is " + accountNo);
        }
        return null;
    }

    public String getDecryptedSeed(int accountNo, String hashPassword) {
        if (accountNo >= 0) {
            CLog.d("Current AccountNo : " + accountNo);
            if (mDataManager != null) {
                CoinUsAccount account = mDataManager.getCurrentAccount(accountNo);
                if (account != null) {
                    String encryptedSeed = account.getWalletSeed();
                    byte[] encrypted = Base64.decode(encryptedSeed, Base64.DEFAULT);
                    byte[] decrypted = encrypted; // encryption logic hidden
                    try {
                        return new String(decrypted, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    CLog.w("Account is null - accountNo : " + accountNo);
                }
            } else {
                CLog.w("mDataManager is null");
            }
        } else {
            CLog.w("accountNo is " + accountNo);
        }
        return null;
    }

    public String getEncryptedSeed(int accountNo) {
        if (accountNo >= 0) {
            CLog.d("Current AccountNo : " + accountNo);
            if (mDataManager != null) {
                CoinUsAccount account = mDataManager.getCurrentAccount(accountNo);
                if (account != null) {
                   return account.getWalletSeed();
                } else {
                    CLog.w("Account is null - accountNo : " + accountNo);
                }
            } else {
                CLog.w("mDataManager is null");
            }
        } else {
            CLog.w("accountNo is " + accountNo);
        }
        return null;
    }

    public boolean isWalletCreated() {
        int accountNo = 0;
        CLog.d("accountNo : " + accountNo);
        if (accountNo < 0) {
            return false;
        } else {
            return mDataManager.getCurrentAccount(accountNo) != null;
        }
    }

    public boolean isWalletBackup() {
        int accountNo = 0;
        if (accountNo >= 0) {
            CLog.d("Current AccountNo : " + accountNo);
            if (mDataManager != null) {
                CoinUsAccount account = mDataManager.getCurrentAccount(accountNo);
                if (account != null) {
                    return account.isBackup();
                } else {
                    CLog.w("Account is null - accountNo : " + accountNo);
                }
            } else {
                CLog.w("mDataManager is null");
            }
        } else {
            CLog.w("accountNo is " + accountNo);
        }
        return false;
    }

    public void setIsWalletBackup(boolean isBackup) {
        int accountNo = 0;
        if (accountNo >= 0) {
            if (mDataManager != null) {
                mDataManager.updateIsBackUp(accountNo, isBackup);
            } else {
                CLog.w("mDataManager is null");
            }
        } else {
            CLog.w("Account is null. Wrong Approach.");
        }
    }

    private void printAccountInfo(String callerFrom) {
        if (mDataManager != null) {
            CoinUsAccount account = mDataManager.getCurrentAccount(0);
            if (account != null) {
                CLog.d("print from  ." + callerFrom);
                CLog.d("Account No ." + account.getAccountNo());
                CLog.d("Account Name ." + account.getWalletName());
            } else {
                CLog.w("Can't find target Account");
            }
        } else {
            CLog.w("mDataManager is null");
        }
    }

    public String getHexAddress(String address) {
        return Numeric.prependHexPrefix(address);
    }

    public void deleteWallet(int accountNo) {
        CLog.d("called");
        mDataManager.deleteAccount(accountNo);

        CLog.d("Delete Completed.");
    }

    public String sendBalance(String encryptedSeed, WalletDomain walletDomain, WalletCryptoDomain walletCryptoDomain, final String receiverAddress, final BigDecimal funds, final BigInteger feePrice, long gasLimit, String data) {
        CLog.d("called");
        long coinId = walletDomain.getCoinId();

        for (Long key : mServiceWallets.keySet()) {
            // get master key
            // pass to pocket and get erc key in pocket then send with erc key
            if (coinId == key) {
                CoinUsAccount coinUsAccount = CoinUsDataManager.getInstance().getCurrentAccount(0);
                if (coinUsAccount != null) {
                    byte[] encrypted = Base64.decode(encryptedSeed, Base64.DEFAULT);
                    byte[] decrypted = encrypted; // decryption logic hidden
                    try {
                        String decryptedSeed = new String(decrypted, "UTF-8");
                        DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(Mnemonic.getEntropyByteSeeds(decryptedSeed));

                        CoinUsServiceWallet pocket = mServiceWallets.get(key);
                        return pocket.sendBalance(masterKey, walletDomain, walletCryptoDomain, receiverAddress, funds, feePrice, gasLimit, data);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    CLog.w("Can't find target Account Object");
                }
            }
        }
        return null;
    }

    public Credentials getCredentials(String encryptedSeed, int addressIndex, CoinType coinType) {

        // get master key
        // pass to pocket and get erc key in pocket then send with erc key
        CoinUsAccount coinUsAccount = CoinUsDataManager.getInstance().getCurrentAccount(0);
        if (coinUsAccount != null) {
            byte[] encrypted = Base64.decode(encryptedSeed, Base64.DEFAULT);
            byte[] decrypted = encrypted; // decryption logic hidden
            try {
                String decryptedSeed = new String(decrypted, "UTF-8");
                DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(Mnemonic.getEntropyByteSeeds(decryptedSeed));

                DeterministicHierarchy hierarchy = new DeterministicHierarchy(masterKey);
                DeterministicKey rootKey = hierarchy.get(coinType.getBip44Path(0), false, true);
                SimpleHDKeyChain keys = new SimpleHDKeyChain(rootKey, addressIndex);
                return Credentials.create(keys.getECKeyPair());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            CLog.w("Can't find target Account Object");
        }
        return null;
    }

    public static boolean checkMnemonicValidation(String seed) {
        CLog.d("called");
        return Mnemonic.check(seed);
    }

    public CoinType getCoinType(long coinId) {
        for (CoinType coinType : mServiceCoinType) {
            if (coinType.getCoinId() == coinId) {
                return coinType;
            }
        }
        return null;
    }
}
