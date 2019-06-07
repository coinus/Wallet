package com.crestech.coinuswalletandroidcore.wallet;

import com.google.common.collect.ImmutableList;

import org.bitcoinj.core.BloomFilter;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDUtils;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcoinj.wallet.EncryptableKeyChain;
import org.bitcoinj.wallet.KeyBag;
import org.bitcoinj.wallet.Protos;
import org.bitcoinj.wallet.RedeemData;
import org.bitcoinj.wallet.listeners.KeyChainEventListener;
import org.spongycastle.crypto.params.KeyParameter;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.util.List;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

// prefixed m/44H/ H/ H/
// get Key of EX or IN  (m/44H/ H/ H/ EX or IN/ )
// last index addressIndex

public class SimpleHDKeyChain implements EncryptableKeyChain, KeyBag {

    private DeterministicKey rootKey;
    private final String PATH_FORMAT = "%d/%d";

    // Paths through the key tree. External keys are ones that are communicated to other parties. Internal keys are
    // keys created for change addresses, coinbases, mixing, etc - anything that isn't communicated. The distinction
    // is somewhat arbitrary but can be useful for audits.
    public static final ChildNumber EXTERNAL_PATH_NUM = ChildNumber.ZERO;
    public static final ImmutableList<ChildNumber> EXTERNAL_PATH = ImmutableList.of(EXTERNAL_PATH_NUM);

    // The parent keys for external keys (handed out to other people) and internal keys (used for change addresses).
    private DeterministicKey externalKey, internalKey;

    public SimpleHDKeyChain(DeterministicKey rootkey, int addressIndex) {
        DeterministicHierarchy hierarchy = new DeterministicHierarchy(rootkey);
        externalKey = hierarchy.get(HDUtils.parsePath(String.format(PATH_FORMAT, 0, addressIndex)), true, true);
    }

    @Nullable
    public DeterministicKey getDeterministicKey() {
        return externalKey;
    }

    public String getAddress(KeyCrypter crypter, KeyParameter key) {
        return Numeric.prependHexPrefix(Keys.getAddress(getECKeyPair()));
    }

    public String getBitCoinAddress(AbstractBitcoinNetParams params) {
        return externalKey.toAddress(params).toString();
    }

    public ECKeyPair getECKeyPair() {
        return ECKeyPair.create(externalKey.getPrivKey());
    }

    @Override
    public SimpleHDKeyChain toEncrypted(CharSequence password) {
        return null;
    }

    @Override
    public SimpleHDKeyChain toEncrypted(KeyCrypter keyCrypter, KeyParameter aesKey) {
        if (!externalKey.isEncrypted()) {
            externalKey = externalKey.encrypt(keyCrypter, aesKey, null);
        }
        return this;
    }

    @Override
    public SimpleHDKeyChain toDecrypted(CharSequence password) {
        return null;
    }

    @Override
    public SimpleHDKeyChain toDecrypted(KeyParameter aesKey) {
        if (externalKey.isEncrypted()) {
            externalKey = externalKey.decrypt(externalKey.getKeyCrypter(), aesKey);
        }
        return this;
    }

    @Override
    public boolean checkPassword(CharSequence password) {
        return false;
    }

    @Override
    public boolean checkAESKey(KeyParameter aesKey) {
        return false;
    }

    @Nullable
    @Override
    public KeyCrypter getKeyCrypter() {
        return externalKey.getKeyCrypter();
    }

    @Nullable
    @Override
    public ECKey findKeyFromPubHash(byte[] pubkeyHash) {
        return null;
    }

    @Nullable
    @Override
    public ECKey findKeyFromPubKey(byte[] pubkey) {
        return null;
    }

    @Nullable
    @Override
    public RedeemData findRedeemDataFromScriptHash(byte[] scriptHash) {
        return null;
    }

    @Override
    public boolean hasKey(ECKey key) {
        return false;
    }

    @Override
    public List<? extends ECKey> getKeys(KeyPurpose purpose, int numberOfKeys) {
        return null;
    }

    @Override
    public ECKey getKey(KeyPurpose purpose) {
        return null;
    }

    @Override
    public List<Protos.Key> serializeToProtobuf() {
        return null;
    }

    @Override
    public void addEventListener(KeyChainEventListener listener) {

    }

    @Override
    public void addEventListener(KeyChainEventListener listener, Executor executor) {

    }

    @Override
    public boolean removeEventListener(KeyChainEventListener listener) {
        return false;
    }

    @Override
    public int numKeys() {
        return 0;
    }

    @Override
    public int numBloomFilterEntries() {
        return 0;
    }

    @Override
    public long getEarliestKeyCreationTime() {
        return 0;
    }

    @Override
    public BloomFilter getFilter(int size, double falsePositiveRate, long tweak) {
        return null;
    }
}
