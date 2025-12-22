package com.wallet.service;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.wallet.DeterministicSeed;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;

import java.util.Arrays;
import java.util.List;

public class MnemonicWalletGeneratorTest {

    // BIP44 Ethereum 路径: m/44'/60'/0'/0/0
    private static final List<ChildNumber> ETH_DERIVATION_PATH = Arrays.asList(
            new ChildNumber(44, true),
            new ChildNumber(60, true),
            new ChildNumber(0, true),
            ChildNumber.ZERO,
            ChildNumber.ZERO
    );

    public static void main(String[] args) throws Exception {
        String mnemonic = "your twelve word mnemonic goes here";
        String passphrase = ""; // 一般为空字符串

        // 创建 DeterministicSeed
        DeterministicSeed seed = new DeterministicSeed(mnemonic, null, passphrase, 0L);

        // 从种子生成根密钥
        DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed.getSeedBytes());
        DeterministicHierarchy dh = new DeterministicHierarchy(rootPrivateKey);

        // 派生到目标路径 m/44'/60'/0'/0/0
        DeterministicKey key = dh.get(ETH_DERIVATION_PATH, true, true);

        // 转换为 ECKeyPair
        ECKeyPair ecKeyPair = ECKeyPair.create(key.getPrivKeyBytes());

        // 获取 Credentials 对象
        Credentials credentials = Credentials.create(ecKeyPair);

        // 输出结果
        System.out.println("地址: " + credentials.getAddress());
        System.out.println("私钥: " + credentials.getEcKeyPair().getPrivateKey().toString(16));
        System.out.println("公钥: " + credentials.getEcKeyPair().getPublicKey().toString(16));
    }
}
