package com.crestech.coinuswalletandroidcore.common;

import com.crestech.coinuswalletandroidcore.coins.CoinType;
import com.crestech.coinuswalletandroidcore.coins.ethereum.EthereumCoins;

import java.util.ArrayList;
import java.util.List;

public class CoinUsConstants {
    public static String BNUS_CONVERTER_ADDRESS = "0x13bccb947052935cc5a96d8bd761984918ccb667";
    public static String BNUS_TOKEN_ADDRESS = "0xbcf8969f0f5c5075f0b925809fed62eb04e58ecf";
    public static String CNUS_TOKEN_ADDRESS = "0x722f2f3eac7e9597c73a593f7cf3de33fbfc3308";
    public static String CNUS_STAKING_ADDRESS = "0x70d93c969ab23468b6305f0180b6f05e8afe046f";

    public static List<CoinType> getServiceCoinTypes() {
        List<CoinType> coinTypeList = new ArrayList<>();
        coinTypeList.add(new EthereumCoins());

        return coinTypeList;
    }
}
