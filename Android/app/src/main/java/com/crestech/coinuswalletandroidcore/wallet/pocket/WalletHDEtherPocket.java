package com.crestech.coinuswalletandroidcore.wallet.pocket;

import android.util.Base64;

import com.crestech.coinuswalletandroidcore.coins.CoinType;
import com.crestech.coinuswalletandroidcore.common.CLog;
import com.crestech.coinuswalletandroidcore.common.CoinUsConstants;
import com.crestech.coinuswalletandroidcore.common.CoinUsUtils;
import com.crestech.coinuswalletandroidcore.data.CoinUsDataManager;
import com.crestech.coinuswalletandroidcore.data.CoinUsResponseCallback;
import com.crestech.coinuswalletandroidcore.data.domain.wallet.cnus.WalletCryptoDomain;
import com.crestech.coinuswalletandroidcore.data.domain.wallet.cnus.WalletDomain;
import com.crestech.coinuswalletandroidcore.wallet.SimpleHDKeyChain;
import com.crestech.coinuswalletandroidcore.wallet.ethereum.EtherWalletBase;
import com.crestech.coinuswalletandroidcore.wallet.ethereum.web3j.CoinUsBancorAbi;

import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper class that holds Ethereum related operation
 */
public class WalletHDEtherPocket extends EtherWalletBase {

    private CoinType coinType;

    public WalletHDEtherPocket(CoinType coinType) {
        super();
        this.coinType = coinType;
        CLog.d("********************************* Pocket Wallet Generate ***********************************************");
    }

    public WalletHDEtherPocket(CoinType coinType, String infuraApiUrl) {
        super(infuraApiUrl);
        this.coinType = coinType;
        CLog.d("********************************* Pocket Wallet Generate ***********************************************");
    }

    /**
     * Returns derived ether wallet address at specified addressIndex
     *
     * @param parentKey A deterministic key is a node in a deterministic hierarchy
     * @param accountNo Account number in BIP44 path
     * @param addressIndex Address index in BIP44 path
     * @return Ethereum address of created wallet
     */
    @Override
    public String createCnusWalletAddress(DeterministicKey parentKey, int accountNo, int addressIndex) {
        CLog.e("Called");
        SimpleHDKeyChain keys = new SimpleHDKeyChain(parentKey, addressIndex);
        Credentials credentials = Credentials.create(ECKeyPair.create(keys.getECKeyPair().getPrivateKey()));
        return credentials.getAddress();
    }

    /**
     * Returns derived ether wallet address at specified addressIndex
     *
     * @param parentKey A deterministic key is a node in a deterministic hierarchy
     * @param accountNo Account number in BIP44 path
     * @param addressIndex Address index in BIP44 path
     * @return Ethereum address of created wallet
     */
    @Override
    public String getCnusWalletAddressEachCoin(DeterministicKey rootKey, int accountNo, int addressIndex) {
        CLog.e("Called");
        SimpleHDKeyChain keys = new SimpleHDKeyChain(rootKey, addressIndex);
        Credentials credentials = Credentials.create(ECKeyPair.create(keys.getECKeyPair().getPrivateKey()));

        return credentials.getAddress();
    }

    /**
     * Obtain CNUS balance of a given address
     * @param address an Ethereum address
     * @return CNUS balance of the given address
     */
    public BigDecimal requestCnusBalance(String address) {
        List<Type> results = sendWeb3jCall(
                CoinUsBancorAbi.getBalanceOf(address),
                address,
                CoinUsConstants.CNUS_TOKEN_ADDRESS
        );

        for (org.web3j.abi.datatypes.Type callResult : results) {
            if (callResult instanceof Uint) {
                return new BigDecimal(((Uint) callResult).getValue()).divide(BigDecimal.TEN.pow(18));
            }
        }
        return null;
    }

    /**
     * Obtain BNUS balance of a given address
     * @param address an Ethereum address
     * @return BNUS balance of the given address
     */
    public BigDecimal requestBnusBalance(String address) {
        List<Type> results = sendWeb3jCall(
                CoinUsBancorAbi.getBalanceOf(address),
                address,
                CoinUsConstants.BNUS_TOKEN_ADDRESS
        );

        for (org.web3j.abi.datatypes.Type callResult : results) {
            if (callResult instanceof Uint) {
                return new BigDecimal(((Uint) callResult).getValue()).divide(BigDecimal.TEN.pow(18));
            }
        }

        return null;
    }

    /**
     * Obtain estimated gas required in BNUS buy transaction
     * @param address an Ethereum address
     * @param amount the amount of CNUS to buy BNUS with in wei
     * @return estimated gas limit amount
     */
    public BigInteger requestBuyBnusEstimateGas(String address, BigInteger amount) {
        BigInteger minAmount = BigInteger.ONE;
        long timeStamp = getTimeStamp();
        Credentials signatureCredentials = getCredentialFromPrivKey(getSGKey());
        byte[] signatureData = getSignatureForBuyingBnus(amount, minAmount, timeStamp, signatureCredentials);

        return getEstimateGasTest(
                CoinUsBancorAbi.getBuyBuns(amount, BigInteger.ONE, timeStamp, signatureData),
                address,
                CoinUsConstants.BNUS_CONVERTER_ADDRESS
        );
    }

    /**
     * Obtain estimated gas required in BNUS sell transaction
     * @param address an Ethereum address
     * @param amount the amount of BNUS to sell in wei
     * @return estimated gas limit amount
     */
    public BigInteger requestSellBnusEstimateGas(String address, BigInteger amount) {
        BigInteger minAmount = BigInteger.ONE;
        long timeStamp = getTimeStamp();
        Credentials signatureCredentials = getCredentialFromPrivKey(getSGKey());
        byte[] signatureData = getSignatureForBuyingBnus(amount, minAmount, timeStamp, signatureCredentials);

        return getEstimateGasTest(
                CoinUsBancorAbi.getSellBuns(amount, BigInteger.ONE, timeStamp, signatureData),
                address,
                CoinUsConstants.BNUS_CONVERTER_ADDRESS
        );
    }

    /**
     * Approves CNUS transferFrom and requests Bancor to convert CNUS into BNUS
     * @param credentials credentials that will be used for this buyBnus transaction
     * @param amount amount of CNUS to buy BNUS with in wei
     * @param estimateGas the estimated gas limit
     * @param gasPrice gas price with which to make this transaction
     * @param gasLimitForApprove gas limit estimated for approval transaction
     * @return transaction hash if successful, and empty string if not
     */
    public String requestBuyBnus(Credentials credentials, BigInteger amount, BigInteger estimateGas, BigInteger gasPrice, BigInteger gasLimitForApprove) {
        BigInteger nonce = null;
        BigInteger minAmount = BigInteger.ONE;
        long timeStamp = getTimeStamp();
        Credentials signatureCredentials = getCredentialFromPrivKey(getSGKey());
        byte[] signatureData = getSignatureForBuyingBnus(amount, minAmount, timeStamp, signatureCredentials);

        // Get Approve Amount
        BigInteger approvedAmount = requestGetBuyBnusAllowance(credentials.getAddress());

        // Check Approve Amount
        if (approvedAmount == null || approvedAmount.compareTo(amount) < 0) {
            nonce = requestApprove(
                        credentials,
                        CoinUsConstants.CNUS_TOKEN_ADDRESS,
                        CoinUsBancorAbi.getApprove(CoinUsConstants.BNUS_CONVERTER_ADDRESS, amount),
                        gasPrice,
                        gasLimitForApprove
            );

            if (nonce == null) {
                CLog.w("requestApproveFailed");
                return null;
            }

            nonce = nonce.add(BigInteger.ONE);
        } else {
            nonce = getNonce(credentials.getAddress());
        }

        return sendWeb3jTransaction(
                CoinUsBancorAbi.getBuyBuns(amount, minAmount, timeStamp, signatureData),
                credentials,
                CoinUsConstants.BNUS_CONVERTER_ADDRESS,
                gasPrice,
                estimateGas.intValue(),
                nonce
        );
    }

    /**
     * Requests Bancor to convert BNUS into CNUS
     * @param credentials credentials that will be used for this sellBnus transaction
     * @param amount amount of BNUS to sell in wei
     * @param estimateGas the estimated gas lmiit
     * @param gasPrice gas price with which to make this transaction
     * @return transaction hash if successful, and empty string if not
     */
    public String requestSellBnus(Credentials credentials, BigInteger amount, BigInteger estimateGas, BigInteger gasPrice) {
        BigInteger minAmount = BigInteger.ONE;
        long timeStamp = getTimeStamp();
        Credentials signatureCredentials = getCredentialFromPrivKey(getSGKey());
        byte[] signatureData = getSignatureForBuyingBnus(amount, minAmount, timeStamp, signatureCredentials);


        BigInteger nonce = getNonce(credentials.getAddress());

        return sendWeb3jTransaction(
                CoinUsBancorAbi.getSellBuns(amount, minAmount, timeStamp, signatureData),
                credentials,
                CoinUsConstants.BNUS_CONVERTER_ADDRESS,
                gasPrice,
                estimateGas.intValue(),
                nonce
        );
    }

    /**
     * Obtain expected amount of CNUS for the given BNUS amount
     * @param amount amount of BNUS in wei
     * @param fromAddress an Ethereum address from which BNUS will be converted
     * @return Expected amount of CNUS in wei when given BNUS amount is converted at this time
     */
    public BigInteger requestExpectedCnus(BigInteger amount, String fromAddress) {
        List<Type> results = sendWeb3jCall(
                CoinUsBancorAbi.getExpectedCnus(amount),
                fromAddress,
                CoinUsConstants.BNUS_CONVERTER_ADDRESS
        );

        for (org.web3j.abi.datatypes.Type callResult : results) {
            if (callResult instanceof Uint) {
                CLog.d("requestExpectedCnus Result : " + ((Uint) callResult).getValue());
                return ((Uint) callResult).getValue();
            }
        }
        return null;
    }

    /**
     * Obtain expected amount of BNUS for the given CNUS amount
     * @param amount amount of CNUS in wei
     * @param fromAddress an Ethereum address from which CNUS will be converted
     * @return Expected amount of BNUS in wei when given CNUS amount is converted at this time
     */
    public BigInteger requestExpectedBnus(BigInteger amount, String fromAddress) {
        List<Type> results = sendWeb3jCall(
                CoinUsBancorAbi.getExpectedBnus(amount),
                fromAddress,
                CoinUsConstants.BNUS_CONVERTER_ADDRESS
        );

        for (org.web3j.abi.datatypes.Type callResult : results) {
            if (callResult instanceof Uint) {
                CLog.d("requestExpectedBnus Result : " + ((Uint) callResult).getValue());
                return ((Uint) callResult).getValue();
            }
        }
        return null;
    }

    /**
     * request transaction with given ABI function (only used with approval function)
     * @param ownerCredentials credential with which to make this transaction
     * @param toAddress an ethereum address to give approval to
     * @param function ABI function to be executed
     * @param gasPrice gas price with which to make this transaction
     * @param gasLimit gas limit to be used with this transaction
     * @return nonce used for this transaction or null if transaction failed
     */
    public BigInteger requestApprove(Credentials ownerCredentials, String toAddress, Function function, BigInteger gasPrice, BigInteger gasLimit) {
        BigInteger nonce = getNonce(ownerCredentials.getAddress());

        String result = sendWeb3jTransaction(
                function,
                ownerCredentials,
                toAddress,
                gasPrice,
                gasLimit.intValue(),
                nonce
        );

        if (result != null) {
            return nonce;
        } else {
            return null;
        }
    }

    /**
     * Obtain transferFrom approval amount from given address to CNUS staking contract
     * @param fromAddress an ethereum address from which CNUS token is approved for transfer
     * @return Wei amount of CNUS in allowance
     */
    public BigInteger requestGetStakingAllowance(String fromAddress) {
        List<org.web3j.abi.datatypes.Type> results = sendWeb3jCall(
                CoinUsBancorAbi.getAllowance(fromAddress, CoinUsConstants.CNUS_STAKING_ADDRESS),
                fromAddress,
                CoinUsConstants.CNUS_TOKEN_ADDRESS
        );

        for (org.web3j.abi.datatypes.Type result : results) {
            if (result instanceof Uint) {
                CLog.d("Allowance Result : " + ((Uint) result).getValue().divide(BigInteger.TEN.pow(18)));
                return ((Uint) result).getValue().divide(BigInteger.TEN.pow(18));
            }
        }
        return null;
    }

    /**
     * Obtain transferFrom approval amount from given address to CoinVerse converter contract
     * @param fromAddress an ethereum address from which CNUS token is approved for transfer
     * @return Wei amount of CNUS in allowance
     */
    public BigInteger requestGetBuyBnusAllowance(String fromAddress) {
        List<org.web3j.abi.datatypes.Type> results = sendWeb3jCall(
                CoinUsBancorAbi.getAllowance(fromAddress, CoinUsConstants.BNUS_CONVERTER_ADDRESS),
                fromAddress,
                CoinUsConstants.CNUS_TOKEN_ADDRESS
        );

        for (org.web3j.abi.datatypes.Type result : results) {
            if (result instanceof Uint) {
                CLog.d("Allowance Result : " + ((Uint) result).getValue().divide(BigInteger.TEN.pow(18)));
                return ((Uint) result).getValue();
            }
        }
        return null;
    }

    /**
     * send specified crypto balance from the wallet to the receiver address
     * @param parentKey deterministic key of owner wallet
     * @param walletDomain object that contains required owner wallet information
     * @param walletCryptoDomain object that contains required information regarding crypto currency
     * @param receiverAddress an ethereum address for receiver
     * @param funds amount in wei for transfer
     * @param gasPrice gas price with which to make this transaction
     * @param gasLimit gas limit to be used with this transaction
     * @param data data used for this transaction
     * @return transaction hash if successful, and empty string if not
     */
    @Override
    public String sendBalance(DeterministicKey parentKey, WalletDomain walletDomain, WalletCryptoDomain walletCryptoDomain, String receiverAddress, BigDecimal funds, BigInteger gasPrice, long gasLimit, String data) {
        int addressIndex = walletDomain.getWalletAddressIndex();
        CLog.d("TargetAddress Index : " + addressIndex);
        if (addressIndex < 0) {
            return null;
        }

        DeterministicHierarchy hierarchy = new DeterministicHierarchy(parentKey);
        DeterministicKey rootKey = hierarchy.get(coinType.getBip44Path(0), false, true);
        SimpleHDKeyChain keys = new SimpleHDKeyChain(rootKey, addressIndex);

        if (walletCryptoDomain.getCryptoTypeDcd().equals("1010001")) {
            return sendEthBalance(keys.getECKeyPair(), receiverAddress, funds, gasPrice, gasLimit, data);
        } else {
            return sendEthErc20Balance(keys.getECKeyPair(), walletCryptoDomain.getTokenDecimals(), walletCryptoDomain.getContractAddress(), receiverAddress, funds, gasPrice, gasLimit);
        }
    }

    /**
     * Preserved function for other crypto currency
     */
    @Override
    public String sendBalance(DeterministicKey parentKey, Object targetObject, String receiverAddress, BigDecimal funds, BigInteger gasPrice, long gasLimit, String data) {
        return null;
    }

    @Override
    public void checkBalanceHistory(DeterministicKey parentKey, final CoinUsResponseCallback<Boolean> callback) {
        int addressIndex = 0;
        CoinUsDataManager dataManager = CoinUsDataManager.getInstance();
        CLog.i("--------- Balance History Check Start ---------");
        do {
            CLog.i("Checking History...");
            SimpleHDKeyChain keys = new SimpleHDKeyChain(parentKey, addressIndex);
            Credentials credentials = Credentials.create(ECKeyPair.create(keys.getECKeyPair().getPrivateKey()));
            String walletAddress = credentials.getAddress();
            CLog.i("finding .. AddressIndex : " + addressIndex + " address : " + walletAddress);

            if (CoinUsDataManager.getInstance().getEthAddressSync(walletAddress)) {
                Map<String, Object> insertWalletParams = new HashMap<>();
                insertWalletParams.put("coinId", coinType.getCoinId());
                insertWalletParams.put("walletAddress", walletAddress);
                insertWalletParams.put("walletAddressIndex", addressIndex);
                insertWalletParams.put("walletAddressNm", "");
                CLog.d("check 5 Time : " + CoinUsUtils.dateFormat(new Date(), "HH:mm:ss"));
                CLog.i("sending.. AddressIndex : " + addressIndex + " address : " + credentials.getAddress());
                dataManager.insertWallet(insertWalletParams);
            } else {
                break;
            }

            addressIndex++;

        } while (true);
        CLog.i("--------- Balance History Check Finished ---------");
        CLog.e("check 6 Time : " + CoinUsUtils.dateFormat(new Date(), "HH:mm:ss"));
        callback.onResultFetched(true);
    }

    @Override
    public CoinType getCoinType() {
        return coinType;
    }

    @Override
    public BigInteger getFeeOnNet(boolean coinYn, WalletCryptoDomain walletCryptoDomain, String toAddress, String actualFund) {
        if (coinYn) {
            return super.getEstimateGas(walletCryptoDomain.getWalletAddress(), toAddress, actualFund);
        } else {
            return super.getErc20EstimateGas(
                    walletCryptoDomain.getWalletAddress(),
                    toAddress,
                    walletCryptoDomain.getContractAddress(),
                    actualFund,
                    walletCryptoDomain.getTokenDecimals()
            );
        }
    }

    @Override
    public BigInteger getFee(String coinNm, String symbol, int position) {
        return Convert.toWei(String.valueOf(position), Convert.Unit.GWEI).toBigInteger();
    }

    @Override
    public BigDecimal getTotalPrice(String coinNm, String symbol, int position, BigInteger fee) {
        BigDecimal returnVal = new BigDecimal(fee).multiply(Convert.toWei(String.valueOf(position), Convert.Unit.GWEI));
        return returnVal.setScale(12, RoundingMode.FLOOR);
    }

    @Override
    public String getCoinValue(String coinNm, String symbol, BigDecimal totalPrice, String format) {
        return String.format(format, Convert.fromWei(totalPrice, Convert.Unit.ETHER).doubleValue());
    }

    @Override
    public BigDecimal getSendBalanceFormat(String coinNm, String symbol, BigDecimal funds) {
        return Convert.toWei(funds, Convert.Unit.ETHER);
    }

    @Override
    public boolean checkValidAddress(String coinNm, String coinSymbol, String address) {
        boolean result = false;
        do {
            if ((false == Numeric.containsHexPrefix(address)) || address.length() != 42) { //길이, length
                break;
            }

            Pattern pattern = Pattern.compile("^[0-9a-zA-Z]*$");
            Matcher matcher = pattern.matcher(address);
            if (!matcher.matches()) {
                CLog.w("Address has irreverent character. : " + address);
                break;
            }

            result = true;
        } while (false);

        return result;
    }

    @Override
    public int getPositionWithFee(String coinNm, String symbol, String totalCoinFeeValue, BigInteger fee) {
        return Convert.fromWei(Convert.toWei(totalCoinFeeValue, Convert.Unit.ETHER).divide(new BigDecimal(fee), MathContext.DECIMAL128), Convert.Unit.GWEI).intValue();
    }


    private Credentials getCredentialFromPrivKey(String privateKey) {
        return Credentials.create(privateKey);
    }

    private byte[] getSignatureForBuyingBnus(BigInteger amount, BigInteger minAmount, long timeStamp, Credentials signatureCredentials) {
        String message = getMessageData(amount, minAmount, timeStamp);
        String soliditySha3 = Hash.sha3(message);

        byte[] bytesResult = getEthereumMessageHash(CoinUsUtils.toBytes(soliditySha3.substring(2)));
        String finalResult = Numeric.toHexString(bytesResult);

        return generateSignature(
                signatureCredentials,
                CoinUsUtils.toBytes(finalResult.substring(2))
        );
    }

    private String getMessageData(BigInteger amount, BigInteger minAmount, long timeStamp) {
        String amountEncoder = TypeEncoder.encode(new Uint(amount));
        String minAmountEncoder = TypeEncoder.encode(new Uint(minAmount));
        String timeStampEncoder = TypeEncoder.encode(new Uint(BigInteger.valueOf(timeStamp)));

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("0x");
        stringBuffer.append(amountEncoder);
        stringBuffer.append(minAmountEncoder);
        stringBuffer.append(timeStampEncoder);
        return stringBuffer.toString();
    }

    private static byte[] getEthereumMessageHash(byte[] message) {
        byte[] prefix = getEthereumMessagePrefix(message.length);
        byte[] result = new byte[prefix.length + message.length];
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(message, 0, result, prefix.length, message.length);

        return Hash.sha3(result);
    }

    private static final String MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";
    private static byte[] getEthereumMessagePrefix(int messageLength) {
        return MESSAGE_PREFIX.concat(String.valueOf(32)).getBytes();
    }

    public byte[] generateSignature(Credentials credentials, byte[] message) {
        // Get ECKeyPair
        ECKeyPair keyPair = credentials.getEcKeyPair();

        // Create Signature Key.
        Sign.SignatureData data = Sign.signMessage(message, keyPair, false);
        String r = Numeric.toHexString(data.getR());
        String s = Numeric.toHexStringNoPrefix(data.getS());
        String v = Numeric.toHexStringNoPrefix(new byte[]{data.getV()});

        // Concat SignatureData to String Type.
        StringBuilder sb = new StringBuilder();
        sb.append(r);
        sb.append(s);
        sb.append(v);

        String signatureData = sb.toString();
        return CoinUsUtils.toBytes(signatureData.substring(2));
    }

    private long getTimeStamp() {
        return Math.round((new Date()).getTime() / 1000) + 3000;
    }

    private String getSGKey() {

        String encodedHex  = new String(CoinUsUtils.toBytes("signature key encoded"));
        CLog.d("encodedHex  :: " + encodedHex );

        byte[] encrypted = Base64.decode(encodedHex, Base64.DEFAULT);
        byte[] decryptedKey = encrypted; // decryption logic hidden

        try {
            String decrypted = new String(decryptedKey, "UTF-8");
            CLog.d("decryptedKey :: " + decrypted + " address : " + Credentials.create(decrypted).getAddress());

            return decrypted;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}