package com.crestech.coinuswalletandroidcore.bip39;

import com.crestech.coinuswalletandroidcore.common.CLog;

import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Mnemonic {

    public Mnemonic() {

    }

    public static byte[] getEntropyByteSeeds(String seed) {
        try {
            return MnemonicCode.toSeed(convertStringToList(seed), "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String generateSeeds(String lang, final int strength) {
        String seedString = null;
        final byte[] seed = new byte[strength];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(seed);

        try {
            MnemonicCode mc = new MnemonicCode();
            List<String> seedList = mc.toMnemonic(seed);
            seedString = convertListToString(seedList);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (MnemonicException.MnemonicLengthException e) {
            e.printStackTrace();
            return null;
        }
        return seedString;
    }

    public static boolean check(final String mnemonic) {
        try {
            MnemonicCode mnemonicCode = new MnemonicCode();
            mnemonicCode.check(convertStringToList(mnemonic));

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (MnemonicException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static List<String> convertStringToList(String seed) {
        String[] splitSeed = seed.split(" ");
        return new ArrayList<>(Arrays.asList(splitSeed));
    }

    private static String convertListToString(List<String> seedList) {
        if (seedList != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < seedList.size(); i++) {
                sb.append(seedList.get(i));
                if (i < seedList.size() - 1) {
                    sb.append(" ");
                }
            }
            return sb.toString();
        }

        CLog.w("seedList is null");
        return null;
    }
}
