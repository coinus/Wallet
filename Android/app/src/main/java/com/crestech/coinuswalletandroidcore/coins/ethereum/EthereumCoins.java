package com.crestech.coinuswalletandroidcore.coins.ethereum;

import com.crestech.coinuswalletandroidcore.common.CLog;
import com.crestech.coinuswalletandroidcore.coins.CoinType;

import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CoinType defined for Ethereum coin
 */
public class EthereumCoins extends CoinType {

    public String publicAddress;
    public String gasFeeWei = "21000";

    public EthereumCoins() {
        coinId = 10001;
        coinDcd = "1001027";
        name = "Ethereum";
        symbol = "ETH";
        bip44Index = 60;
        fee = new BigInteger(gasFeeWei);
        minUnitNm = "wei";
    }

    @Override
    public String getMinCoinUnit() {
        return minUnitNm;
    }

    @Override
    public BigInteger getFee(int position) {
        return Convert.toWei(String.valueOf(position), Convert.Unit.GWEI).toBigInteger();
    }

    @Override
    public BigDecimal getTotalPrice(int position) {
        return new BigDecimal(fee).multiply(Convert.toWei(String.valueOf(position), Convert.Unit.GWEI));
    }

    @Override
    public String getCoinValue(BigDecimal totalPrice, String format) {
        return String.format(format, Convert.fromWei(totalPrice, Convert.Unit.ETHER).floatValue());
    }

    @Override
    public int getPositionWithFeeValue(String totalCoinFeeValue) {
        return Convert.fromWei(Convert.toWei(totalCoinFeeValue, Convert.Unit.ETHER).divide(new BigDecimal(getFee()), MathContext.DECIMAL128), Convert.Unit.GWEI).intValue();
    }

    @Override
    public BigDecimal getSendBalanceFormat(BigDecimal funds) {
        return Convert.toWei(funds, Convert.Unit.ETHER);
    }

    @Override
    public boolean checkAddressValidation(String address) {
        boolean result = false;
        do {
            if (Numeric.containsHexPrefix(address) && address.length() != 42) {
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

    public String getPublicAddress() {
        return publicAddress;
    }

    public void setPublicAddress(String publicAddress) {
        this.publicAddress = publicAddress;
    }
}
