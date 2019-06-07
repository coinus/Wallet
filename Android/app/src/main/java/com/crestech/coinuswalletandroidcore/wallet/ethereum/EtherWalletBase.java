package com.crestech.coinuswalletandroidcore.wallet.ethereum;

import com.crestech.coinuswalletandroidcore.common.CLog;
import com.crestech.coinuswalletandroidcore.wallet.CoinUsServiceWallet;
import com.crestech.coinuswalletandroidcore.wallet.ethereum.web3j.CoinUsWeb3jManager;
import com.crestech.coinuswalletandroidcore.data.CoinUsResponseCallback;

import org.web3j.abi.datatypes.Function;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * Ethereum wallet base abstract class
 */
public abstract class EtherWalletBase implements CoinUsServiceWallet {

    private CoinUsWeb3jManager web3jManager;

    public EtherWalletBase() {
        web3jManager = new CoinUsWeb3jManager("USE INFURA URL");
    }

    public EtherWalletBase(String infuraApiUrl) {
        web3jManager = new CoinUsWeb3jManager(infuraApiUrl);
        CLog.d("Web3j Url : " + infuraApiUrl);
    }

    public BigInteger getNonce(String address) {
        return web3jManager.getNonce(address);
    }

    public BigInteger getEstimateGasTest(Function function, String fromAddress, String contractAddress) {
        return web3jManager.getEtherContractEstimateGasTest(function, fromAddress, contractAddress);
    }

    public String sendWeb3jTransaction(Function function, Credentials credentials, String contractAddress, BigInteger gasPrice, int gasLimit, BigInteger nonce) {
        return web3jManager.requestWeb3jTransaction(credentials, function, contractAddress, gasPrice, gasLimit, nonce);
    }

    public BigInteger getGasPrice() {
        return web3jManager.getGasPrice();
    }

    public List<org.web3j.abi.datatypes.Type> sendWeb3jCall(Function function, String fromAddress, String toAddress) {
        return web3jManager.requestWeb3jEthCall(function, fromAddress, toAddress);
    }

    public BigInteger getBalanceTest(String address) {
        return web3jManager.requestWeb3jGetBalanceTest(address);
    }

    public BigInteger getBalanceFromWeb3j(String address) {
        return web3jManager.requestWeb3jGetBalanceTest(address);
    }

    @Override
    public String getBalanceAsync(String pubAddress, CoinUsResponseCallback callback) {
        return null;
    }

    @Override
    public BigInteger getBalance(String pubAddress) {
        return getBalance_ByWEB3J( pubAddress );
    }

    private BigInteger getBalance_ByWEB3J(String pubAddress){
        if (web3jManager != null) {
            return web3jManager.requestWeb3jGetBalance(pubAddress);
        } else {
            CLog.d("web3jManager is null");
        }
        return BigInteger.ZERO;
    }

    public String sendEthBalance(ECKeyPair ecKeyPair, String receiverAddress, BigDecimal funds, BigInteger gasPrice, long gasLimit, String data) {
        if (web3jManager != null && ecKeyPair != null) {

            Credentials credentials = Credentials.create(ECKeyPair.create(ecKeyPair.getPrivateKey()));
            CLog.d("Web3j credentials - address : " + credentials.getAddress());

            CLog.d("funds : " + funds);
            CLog.d("gasPrice : " + gasPrice);

            return web3jManager.requestWeb3jSendBalance(receiverAddress, funds, gasPrice, credentials, gasLimit, data);
        } else {
            CLog.w("Web3jManager, ecKeyPair are null");
            return null;
        }
    }

    public String sendEthErc20Balance(ECKeyPair ecKeyPair, int decimal, String contractAddress, String receiverAddress, BigDecimal funds, BigInteger gasPrice, long gasLimit) {
        if (web3jManager != null && ecKeyPair != null) {

            Credentials credentials = Credentials.create(ECKeyPair.create(ecKeyPair.getPrivateKey()));
            CLog.d("Web3j credentials - address : " + credentials.getAddress());

            return web3jManager.requestWeb3jERC20SendBalance(contractAddress, receiverAddress, funds, decimal, gasPrice, credentials, gasLimit);
        } else {
            CLog.w("Web3jManager, ecKeyPair are null");
            return null;
        }
    }

    public BigInteger getEthGasPrice() {
        if (web3jManager != null) {
            return web3jManager.requestWeb3jGetEthGasPrice();
        } else {
            CLog.w("Web3jManager is null");
        }
        return null;
    }

    public BigInteger getEstimateGas(String fromAddress, String toAddress, String actualEther) {
        return web3jManager.getEtherEstimateGas(fromAddress, toAddress, actualEther);
    }

    public BigInteger getErc20EstimateGas(String fromAddress, String toAddress, String contractAddress, String actualToken, int tokenDecimals) {
        return web3jManager.getEtherContractEstimateGas(fromAddress, toAddress, contractAddress, actualToken, tokenDecimals);
    }
}

