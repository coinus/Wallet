package com.crestech.coinuswalletandroidcore.wallet.ethereum.web3j;

import com.crestech.coinuswalletandroidcore.common.CLog;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.exceptions.MessageDecodingException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ChainId;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.response.Callback;
import org.web3j.tx.response.QueuingTransactionReceiptProcessor;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static org.web3j.tx.TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH;

public class CoinUsWeb3jManager {

    private Web3j mWeb3;
    private final int THREAD_COUNT_LIMIT = 5;
    private ExecutorService mExecutor;

    public CoinUsWeb3jManager(String network) {
        final long TIME_OUT = 20;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout( TIME_OUT, TimeUnit.SECONDS )
                .readTimeout( TIME_OUT, TimeUnit.SECONDS )
                .writeTimeout( TIME_OUT, TimeUnit.SECONDS )
                .build();

        mWeb3 = Web3jFactory.build( new HttpService( network, client, false ) );
        mExecutor = Executors.newFixedThreadPool(THREAD_COUNT_LIMIT);

    }

    public BigInteger getNonce(final String address) {
        Request<?, EthGetTransactionCount> request = mWeb3.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING);

        try {
            EthGetTransactionCount transaction = request.send();
            if (transaction != null) {
                if (transaction.getError() != null) {
                    CLog.w("transaction.getError() : " + transaction.getError().getMessage());
                    return null;
                }

                CLog.d("pending - transaction.getTransactionCount() :: " + transaction.getTransactionCount());
                return transaction.getTransactionCount();
            } else {
                CLog.w("get transaction Failed");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessageDecodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void requestWeb3jVersion(final CallbackResult callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Future<Web3ClientVersion> futureClientVersion = mWeb3.web3ClientVersion().sendAsync();
                try {
                    Web3ClientVersion web3ClientVersion = futureClientVersion.get();
                    CLog.d("web3ClientVersion : " + web3ClientVersion.getWeb3ClientVersion());
                    if (callback != null) {
                        List<String> result = new ArrayList();
                        result.add(web3ClientVersion.getWeb3ClientVersion());
                        callback.resultFetched(result);
                    } else {
                        CLog.w("callback is null");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public org.web3j.protocol.core.methods.response.Transaction getTransactionByHash(String transactionHash) {
        Request<?, EthTransaction> ethTransaction = mWeb3.ethGetTransactionByHash(transactionHash);

        try {
            EthTransaction transaction = ethTransaction.send();
            if (transaction != null) {
                if (transaction.getError() != null) {
                    CLog.w("transaction.getError() : " + transaction.getError().getMessage());
                }
                return transaction.getTransaction();
            } else {
                CLog.w("get transaction Failed");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessageDecodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public BigInteger requestWeb3jGetBalance(final String address) {
        Request<?, EthGetBalance> balanceRequest = mWeb3.ethGetBalance(address, new DefaultBlockParameter() {
            @Override
            public String getValue() {
                return "latest";
            }
        });

        try {
            EthGetBalance ethBalance = balanceRequest.send();

            CLog.d("balance : " + ethBalance.getBalance());
            return ethBalance.getBalance();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessageDecodingException e) {
            e.printStackTrace();
        }

        return BigInteger.ZERO;
    }

    public BigInteger requestWeb3jGetBalanceTest(final String address) {
            Request<?, EthGetBalance> balanceRequest = mWeb3.ethGetBalance(address, new DefaultBlockParameter() {
//                Request<?, EthGetBalance> balanceRequest = mWeb3.ethGetBalance("0x3452932349Aac7D249640974C8D38948205ba728", new DefaultBlockParameter() {

                @Override
                public String getValue() {
                    return "latest";
                }
            });
            try {
                EthGetBalance ethBalance = balanceRequest.send();
                return ethBalance.getBalance();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (MessageDecodingException e) {
                e.printStackTrace();
            }

            return null;
    }

    public BigInteger requestWeb3jGetEthGasPrice() {
        try {
            Request<?, EthGasPrice> ethGasPriceRequest = mWeb3.ethGasPrice();
            EthGasPrice ethGasPrice = ethGasPriceRequest.send();
            BigInteger gas = ethGasPrice.getGasPrice();
            CLog.d("gas : " + Convert.fromWei(new BigDecimal(gas), Convert.Unit.GWEI).toBigInteger());
            return Convert.fromWei(new BigDecimal(gas), Convert.Unit.GWEI).toBigInteger();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final long POLLING_FREQUENCY = 15000;
    private static final int COUNT = 1;  // don't set too high if using a real Ethereum network

    public String requestWeb3jSendBalance(final String receiverAddress,
                                          final BigDecimal fundsOnWei,
                                          final BigInteger gasPrice,
                                          final Credentials credentials,
                                          final long gasLimit,
                                          final String data) {

        CLog.e("receiverAddress : " + receiverAddress);
        CLog.e("fundsOnWei : " + fundsOnWei);
        CLog.e("gasPrice : " + gasPrice);
        CLog.e("gasLimit : " + gasLimit);
        CLog.e("data : " + data);

        Map<String, Object> pendingTransactions = new ConcurrentHashMap<>();
        final ConcurrentLinkedQueue<TransactionReceipt> transactionReceipts =
                new ConcurrentLinkedQueue<>();

        FastRawTransactionManager transactionManager = new FastRawTransactionManager(
                mWeb3, credentials,

                new QueuingTransactionReceiptProcessor(mWeb3, new Callback() {
                    @Override
                    public void accept(TransactionReceipt transactionReceipt) {
                        if (transactionReceipt != null) {
                            CLog.d ("transactionReceipt.getTransactionHash() : " + transactionReceipt.getTransactionHash());
                        }
                        transactionReceipts.add(transactionReceipt);
                    }

                    @Override
                    public void exception(Exception exception) {

                    }
                }, DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH, POLLING_FREQUENCY));

        try {
            EthSendTransaction transactionResponse = transactionManager.sendTransaction(gasPrice, new BigInteger(Long.toString(gasLimit)), receiverAddress, data != null ? data : "", fundsOnWei.toBigInteger());
            String transactionHash = transactionResponse.getTransactionHash();
            if (transactionHash == null) {
                Response.Error error = transactionResponse.getError();
                CLog.w("Error Message : " + error.getMessage());
                CLog.w("tra nsactionHash is null");
                return null;
            } else {
                return transactionHash;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private RemoteCall<TransactionReceipt> createTransaction(Transfer transfer, final BigDecimal fundsOnWei, String receiveAddress, BigInteger gasPrice, long gasLimit) {
        return transfer.sendFunds(
                receiveAddress, fundsOnWei, Convert.Unit.WEI,
                gasPrice, new BigInteger(Long.toString(gasLimit)));
    }

    public String requestWeb3jERC20SendBalance(final String contractAddress, final String receiverAddress, final BigDecimal tokenValue, final int decimal, final BigInteger gasPrice, final Credentials credentials, final long gasLimit) {
        CLog.d("requestWeb3jTokenSendBalance called");

        Map<String, Object> pendingTransactions = new ConcurrentHashMap<>();
        final ConcurrentLinkedQueue<TransactionReceipt> transactionReceipts =
                new ConcurrentLinkedQueue<>();

        FastRawTransactionManager transactionManager = new FastRawTransactionManager(
                mWeb3, credentials,

                new QueuingTransactionReceiptProcessor(mWeb3, new Callback() {
                    @Override
                    public void accept(TransactionReceipt transactionReceipt) {
                        if (transactionReceipt != null) {
                            CLog.d ("transactionReceipt.getTransactionHash() : " + transactionReceipt.getTransactionHash());
                        }
                        transactionReceipts.add(transactionReceipt);
                    }

                    @Override
                    public void exception(Exception exception) {

                    }
                }, DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH, POLLING_FREQUENCY));

        try {
            CLog.d("decimal : " + decimal + " contractAddress : " + contractAddress + " receiverAddress : " + receiverAddress + " tokenValue : " + tokenValue + " gasPrice : " + gasPrice + " chainId : " + ChainId.MAINNET);
            BigInteger tokenFund = tokenValue.toBigInteger();

            Function function = new Function("transfer", Arrays.<Type>asList(new Address(receiverAddress), new Uint256(tokenFund)), Collections.<TypeReference<?>>emptyList());
            String encodedFunction = FunctionEncoder.encode(function);
            CLog.d("encodedFunction : " + encodedFunction);

            EthSendTransaction transactionResponse = transactionManager.sendTransaction(gasPrice, new BigInteger(Long.toString(gasLimit)), contractAddress, encodedFunction, BigInteger.ZERO);

            String transactionHash = transactionResponse.getTransactionHash();
            CLog.w("transactionHash is : " + transactionHash);

            if (transactionHash == null) {
                Response.Error error = transactionResponse.getError();
                CLog.w("Error Message : " + error.getMessage());
                CLog.w("tra nsactionHash is null");
                return null;
            } else {
                return transactionHash;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Bancor Method Start
     */
    public boolean requestWeb3jERC20Approve(final String contractAddress, final String receiverAddress, final BigDecimal tokenValue, final int decimal, final BigInteger gasPrice, final Credentials credentials, final long gasLimit) {
        CLog.d("requestWeb3jTokenSendBalance called");

        final ConcurrentLinkedQueue<TransactionReceipt> transactionReceipts =
                new ConcurrentLinkedQueue<>();

        FastRawTransactionManager transactionManager = new FastRawTransactionManager(
                mWeb3, credentials,

                new QueuingTransactionReceiptProcessor(mWeb3, new Callback() {
                    @Override
                    public void accept(TransactionReceipt transactionReceipt) {
                        if (transactionReceipt != null) {
                            CLog.d ("transactionReceipt.getTransactionHash() : " + transactionReceipt.getTransactionHash());
                        }
                        transactionReceipts.add(transactionReceipt);
                    }

                    @Override
                    public void exception(Exception exception) {

                    }
                }, DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH, POLLING_FREQUENCY));

        try {
            CLog.d("decimal : " + decimal + " contractAddress : " + contractAddress + " receiverAddress : " + receiverAddress + " tokenValue : " + tokenValue + " gasPrice : " + gasPrice + " chainId : " + ChainId.MAINNET);
            BigInteger tokenFund = tokenValue.toBigInteger();

            Function function = new Function("approve", Arrays.<Type>asList(new Address(receiverAddress), new Uint256(tokenFund)), Collections.<TypeReference<?>>emptyList());
            String encodedFunction = FunctionEncoder.encode(function);
            CLog.d("encodedFunction : " + encodedFunction);

            EthSendTransaction transactionResponse = transactionManager.sendTransaction(gasPrice, new BigInteger(Long.toString(gasLimit)), contractAddress, encodedFunction, BigInteger.ZERO);

            String transactionHash = transactionResponse.getTransactionHash();
            CLog.w("transactionHash is : " + transactionHash);

            if (transactionHash == null) {
                Response.Error error = transactionResponse.getError();
                CLog.w("Error Message : " + error.getMessage());
                CLog.w("tra nsactionHash is null");
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String requestWeb3jTransaction(final Credentials credentials, Function function, final String contractAddress, final BigInteger gasPrice, final long gasLimit, final BigInteger nonce) {
        String encodedFunction = FunctionEncoder.encode(function);

        final ConcurrentLinkedQueue<TransactionReceipt> transactionReceipts =
                new ConcurrentLinkedQueue<>();

        FastRawTransactionManager transactionManager = new FastRawTransactionManager(
                mWeb3, credentials,

                new QueuingTransactionReceiptProcessor(mWeb3, new Callback() {
                    @Override
                    public void accept(TransactionReceipt transactionReceipt) {
                        if (transactionReceipt != null) {
                            CLog.d ("transactionReceipt.getTransactionHash() : " + transactionReceipt.getTransactionHash());
                        }
                        transactionReceipts.add(transactionReceipt);
                    }

                    @Override
                    public void exception(Exception exception) {

                    }
                }, DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH, POLLING_FREQUENCY));

        try {
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce,
                    gasPrice,
                    new BigInteger(Long.toString(gasLimit)),
                    contractAddress,
                    BigInteger.ZERO,
                    encodedFunction);

            EthSendTransaction transactionResponse = transactionManager.signAndSend(rawTransaction);

            String transactionHash = transactionResponse.getTransactionHash();
            CLog.w("transactionHash is : " + transactionHash);

            if (transactionHash == null) {
                Response.Error error = transactionResponse.getError();
                CLog.w("Error Message : " + error.getMessage());
                CLog.w("transactionHash is null");
                return null;
            } else {
                return transactionHash;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Type> requestWeb3jEthCall(Function function, String fromAddress, String contractAddress) {
        String encodedFunction = FunctionEncoder.encode(function);

        try {
            org.web3j.protocol.core.methods.response.EthCall ethCall = mWeb3.ethCall(
                    Transaction.createEthCallTransaction(
                            fromAddress, contractAddress, encodedFunction),
                    DefaultBlockParameterName.LATEST)
                    .send();

            String value = ethCall.getValue();
            CLog.w("requestWeb3jEthCall :: " + value);

            Response.Error error = ethCall.getError();
            if (error != null) {
                CLog.w("error.getMessage() :: " + error.getMessage());
            }

            return FunctionReturnDecoder.decode(value, function.getOutputParameters());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public BigInteger getEtherContractEstimateGasTest(Function function, String fromAddress, String contractAddress) {

        String encodedFunction = FunctionEncoder.encode(function);
        try {
            Transaction contractTransaction = new Transaction(
                    fromAddress,
                    null,
                    null,
                    null,
                    contractAddress,
                    BigInteger.ZERO,
                    encodedFunction);

            Future<EthEstimateGas> ethEstimateGasResponse = mWeb3.ethEstimateGas(contractTransaction).sendAsync();
            EthEstimateGas ethEstimateGas = ethEstimateGasResponse.get();
            if (ethEstimateGas != null) {
                if (!ethEstimateGas.hasError()) {
                    CLog.e("contract address :: " + contractAddress);
                    CLog.e("estimateGas contract :: " + ethEstimateGas.getAmountUsed());
                    return ethEstimateGas.getAmountUsed();
                } else {
                    CLog.e(ethEstimateGas.getError().getMessage());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BigInteger getGasPrice() {

        try {
            Future<EthGasPrice> ethEstimateGasResponse = mWeb3.ethGasPrice().sendAsync();
            EthGasPrice ethEstimateGas = ethEstimateGasResponse.get();
            if (ethEstimateGas != null) {
                if (!ethEstimateGas.hasError()) {
                    CLog.e("getGasPrice :: " + ethEstimateGas.getGasPrice());
                    return ethEstimateGas.getGasPrice();
                } else {
                    CLog.e(ethEstimateGas.getError().getMessage());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BigInteger getTokenUnit256(String tokenValue, int decimal) {
        BigDecimal value = new BigDecimal(tokenValue);
        BigDecimal tokenDecimal = BigDecimal.TEN.pow(decimal);
        return value.multiply(tokenDecimal).toBigInteger();
    }

    /**
     *
     * @param fromAddress ether Address
     * @param toAddress ether Address
     * @param actualEther String Ether Unit.
     * @return
     */
    public BigInteger getEtherEstimateGas(String fromAddress, String toAddress, String actualEther) {
        try {
            CLog.w("actualEther :: " + actualEther);
            final BigDecimal fundsOnWei = Convert.toWei(actualEther, Convert.Unit.ETHER);

            Transaction transaction = new Transaction(fromAddress, null, null, null, toAddress, fundsOnWei.toBigInteger(), null);
            Request<?, EthEstimateGas> ethEstimateGas = mWeb3.ethEstimateGas(transaction);
            Future<EthEstimateGas> estimateGas = ethEstimateGas.sendAsync();
            if (estimateGas != null) {
                EthEstimateGas eg = estimateGas.get();
                if (!eg.hasError()) {
                    CLog.i("estimateGas :: " + eg.getAmountUsed());
                    return eg.getAmountUsed();
                } else {
                    CLog.w(eg.getError().getMessage());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public BigInteger getEtherContractEstimateGas(String fromAddress, String toAddress, String contractAddress, String actualToken, int tokenDecimals) {
        BigInteger tokenFund = getTokenUnit256(String.valueOf(actualToken), tokenDecimals);

        Function function = new Function("transfer", Arrays.<Type>asList(new Address(toAddress), new Uint256(tokenFund)), Collections.<TypeReference<?>>emptyList());
        String encodedFunction = FunctionEncoder.encode(function);

        try {
            Transaction contractTransaction = new Transaction(
                    fromAddress,
                    null,
                    null,
                    null,
                    contractAddress,
                    BigInteger.ZERO,
                    encodedFunction);

            Future<EthEstimateGas> ethEstimateGasResponse = mWeb3.ethEstimateGas(contractTransaction).sendAsync();
            EthEstimateGas ethEstimateGas = ethEstimateGasResponse.get();
            if (ethEstimateGas != null) {
                if (!ethEstimateGas.hasError()) {
                    CLog.e("contract address :: " + contractAddress);
                    CLog.e("estimateGas contract :: " + ethEstimateGas.getAmountUsed());
                    return ethEstimateGas.getAmountUsed();
                } else {
                    CLog.e(ethEstimateGas.getError().getMessage());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface CallbackResult<T> {
        void resultFetched(List<T> value);
    }
}
