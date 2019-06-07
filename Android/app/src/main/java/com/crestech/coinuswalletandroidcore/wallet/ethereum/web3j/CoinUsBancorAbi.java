package com.crestech.coinuswalletandroidcore.wallet.ethereum.web3j;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

/**
 * Wrapper class for CoinVerse Bancor ABI
 */
public class CoinUsBancorAbi {

    public static Function getApprove(String toAddress, BigInteger token) {
        return new Function(
                "approve",
                Arrays.<org.web3j.abi.datatypes.Type>asList(new Address(toAddress), new Uint256(token)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {})
        );
    }

    public static Function getIncreaseApproval(String toAddress, BigInteger token) {
        return new Function(
                "increaseApproval",
                Arrays.<org.web3j.abi.datatypes.Type>asList(new Address(toAddress), new Uint256(token)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {})
        );
    }

    public static Function getDecreaseApproval(String toAddress, BigInteger token) {
        return new Function(
                "decreaseApproval",
                Arrays.<org.web3j.abi.datatypes.Type>asList(new Address(toAddress), new Uint256(token)),
                Collections.<TypeReference<?>>emptyList()
        );
    }

    public static Function getAllowance(String owner, String spender) {
        return new Function(
                "allowance",
                Arrays.<org.web3j.abi.datatypes.Type>asList(new Address(owner), new Address(spender)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint>() {})
        );
    }

    public static Function getBalanceOf(String address) {
        return new Function(
                "balanceOf",
                Arrays.<org.web3j.abi.datatypes.Type>asList(new Address(address)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint>() {})
        );
    }

    /**
     * Bnus Converter
     */
    public static Function getBuyBuns(BigInteger depositAmount, BigInteger minReturn, long expiration, byte[] signature) {
        return new Function(
                "buyBnus",
                Arrays.<org.web3j.abi.datatypes.Type>asList(new Uint(depositAmount), new Uint(minReturn), new Uint(BigInteger.valueOf(expiration)), new DynamicBytes(signature)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint>() {})
        );
    }

    public static Function getSellBuns(BigInteger sellAmount, BigInteger minReturn, long expiration, byte[] signature) {
        return new Function(
                "sellBnus",
                Arrays.<org.web3j.abi.datatypes.Type>asList(new Uint(sellAmount), new Uint(minReturn), new Uint(BigInteger.valueOf(expiration)), new DynamicBytes(signature)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint>() {})
        );
    }

    public static Function getExpectedBnus(BigInteger cnusAmount) {
        return new Function(
                "getExpectedBnus",
                Arrays.<org.web3j.abi.datatypes.Type>asList(new Uint(cnusAmount)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint>() {})
        );
    }

    public static Function getExpectedCnus(BigInteger bnusAmount) {
        return new Function(
                "getExpectedCnus",
                Arrays.<org.web3j.abi.datatypes.Type>asList(new Uint(bnusAmount)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint>() {})
        );
    }

    public static Function getTotalSupply() {
        return new Function(
                "totalSupply",
                Arrays.<org.web3j.abi.datatypes.Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint>() {})
        );
    }

    /**
     * CnusPoolForStaking
     */
    public static Function getStake(BigInteger amount, long expiration, byte[] signature) {
        return new Function(
                "stake",
//                Arrays.<org.web3j.abi.datatypes.Type>asList(new Uint(cAmount), new Uint(BigInteger.valueOf(expiration)), new DynamicBytes(signature)),
                Arrays.<org.web3j.abi.datatypes.Type>asList(new Uint(amount), new Uint(BigInteger.valueOf(expiration)), new DynamicBytes(signature)),
                Collections.<TypeReference<?>>emptyList()
        );
    }

    public static Function getWithdraw(BigInteger amount, byte[] udid, long expiration, byte[] signature) {
        return new Function(
                "withdraw",
                Arrays.<org.web3j.abi.datatypes.Type>asList(new Uint(amount), new DynamicBytes(udid), new Uint(BigInteger.valueOf(expiration)), new DynamicBytes(signature)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint>() {})
        );
    }

    public static Function getStakedAmount() {
        return new Function(
                "getStakedAmount",
                Arrays.<org.web3j.abi.datatypes.Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint>() {})
        );
    }

    // Owner Only. (For DEV Only.)
    public static Function setCoinUsAccount(String account) {
        return new Function(
                "setCoinUsAccount",
                Arrays.<org.web3j.abi.datatypes.Type>asList(new Address(account)),
                Collections.<TypeReference<?>>emptyList()
        );
    }

    /**
     * Token Pool
     */
    public static Function getBnusBalance() {
        return new Function(
                "getBnusBalance",
                Arrays.<org.web3j.abi.datatypes.Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint>() {})
        );
    }

    public static Function getCnusBalance() {
        return new Function(
                "getCnusBalance",
                Arrays.<org.web3j.abi.datatypes.Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint>() {})
        );
    }



}
