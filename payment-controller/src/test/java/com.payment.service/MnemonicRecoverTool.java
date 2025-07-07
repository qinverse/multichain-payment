package com.payment.service;

import org.web3j.crypto.*;
import org.bitcoinj.crypto.*;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class MnemonicRecoverTool {

    private static final String INFURA_PROJECT_ID = "23599b1b46fa40c99a81c4a376c8fdba";  // 替换成你自己的
    private static final Web3j web3 = Web3j.build(new HttpService("https://mainnet.infura.io/v3/" + INFURA_PROJECT_ID));

    private static final List<String> WORD_LIST = new ArrayList<>();
    private static final int HARDENED = 0x80000000;

    static {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                MnemonicRecoverTool.class.getResourceAsStream("/bip39.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                WORD_LIST.add(line.trim());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load BIP39 word list", e);
        }
    }

    public static void main(String[] args) {
        // 示例输入：压缩字符串（无空格）且模糊位置用 ? 表示
        String compressedInput = "n?98989eajlksjalksa??uittoasts?sdsdsdsdswqeqw?";

        List<List<String>> candidates = splitWithWildcards(compressedInput);

        System.out.println("可能的助记词组合（合法 + 常见链地址）：");
        for (List<String> sequence : candidates) {
            if (isValidLength(sequence)) {
                String mnemonic = String.join(" ", sequence);
                System.out.println("\n助记词: " + mnemonic);
                deriveAddresses(mnemonic);
            }
        }
    }

    private static List<List<String>> splitWithWildcards(String input) {
        List<List<String>> result = new ArrayList<>();
        backtrack(input, 0, new ArrayList<>(), result);
        return result;
    }

    private static void backtrack(String input, int index, List<String> path, List<List<String>> result) {
        if (index == input.length()) {
            result.add(new ArrayList<>(path));
            return;
        }
        for (int i = index + 1; i <= input.length(); i++) {
            String piece = input.substring(index, i);
            if (piece.contains("?")) {
                for (String word : WORD_LIST) {
                    if (word.length() == piece.length() && matchWildcard(word, piece)) {
                        path.add(word);
                        backtrack(input.substring(i), 0, path, result);
                        path.remove(path.size() - 1);
                    }
                }
            } else if (WORD_LIST.contains(piece)) {
                path.add(piece);
                backtrack(input, i, path, result);
                path.remove(path.size() - 1);
            }
        }
    }

    private static boolean matchWildcard(String word, String pattern) {
        for (int i = 0; i < word.length(); i++) {
            if (pattern.charAt(i) != '?' && word.charAt(i) != pattern.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidLength(List<String> words) {
        return words.size() == 12 || words.size() == 24;
    }

    private static void deriveAddresses(String mnemonic) {
        try {
            byte[] seed = MnemonicUtils.generateSeed(mnemonic, null);
            Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);

            // ETH/BSC/Polygon
            int[] ethPath = {44 | HARDENED, 60 | HARDENED, 0 | HARDENED, 0, 0};
            Bip32ECKeyPair ethKey = Bip32ECKeyPair.deriveKeyPair(master, ethPath);
            Credentials credentials = Credentials.create(ethKey);
            System.out.println("ETH/BSC Address: " + credentials.getAddress());
            BigInteger ethBalance = getETHBalance(credentials.getAddress());
            System.out.println("ETH 余额 (wei): " + ethBalance);

//            // TRON
//            int[] tronPath = {44 | HARDENED, 195 | HARDENED, 0 | HARDENED, 0, 0};
//            Bip32ECKeyPair tronKey = Bip32ECKeyPair.deriveKeyPair(master, tronPath);
//            String privateKey = tronKey.getPrivateKey().toString(16);
//            String tronAddress = TronUtil.getTronAddress(privateKey);
//            System.out.println("TRON Address:    " + tronAddress);

            // BTC
            DeterministicSeed ds = new DeterministicSeed(mnemonic, null, "", 0);
            DeterministicKeyChain chain = DeterministicKeyChain.builder().seed(ds).build();
            DeterministicKey key = chain.getKeyByPath(HDUtils.parsePath("M/44H/0H/0H/0/0"), true);
            NetworkParameters params = MainNetParams.get();
            String btcAddress = LegacyAddress.fromKey(params, key).toString();
            System.out.println("BTC Address:     " + btcAddress);

            long balance = getBTCBalanceSatoshi(btcAddress);
            System.out.println("BTC 余额 (聪): " + balance);

            if (balance > 0) {
                System.out.println(">>> 发现有余额地址，对应助记词可能正确！");
            }

        } catch (Exception e) {
            System.out.println("地址派生失败: " + e.getMessage());
        }
    }

    // 新增
    private static String deriveETHAddress(String mnemonic) {
        try {
            byte[] seed = MnemonicUtils.generateSeed(mnemonic, null);
            Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);
            int[] path = {44 | HARDENED, 60 | HARDENED, 0 | HARDENED, 0, 0};
            Bip32ECKeyPair keypair = Bip32ECKeyPair.deriveKeyPair(master, path);
            Credentials credentials = Credentials.create(keypair);
            return credentials.getAddress();
        } catch (Exception e) {
            return "地址派生失败: " + e.getMessage();
        }
    }

    private static BigInteger getETHBalance(String ethAddress) {
        try {
            EthGetBalance ethGetBalance = web3.ethGetBalance(ethAddress, DefaultBlockParameterName.LATEST).send();
            return ethGetBalance.getBalance();
        } catch (Exception e) {
            System.out.println("ETH余额查询失败: " + e.getMessage());
            return null;
        }
    }

    private static long getBTCBalanceSatoshi(String btcAddress) {
        try {
            String apiUrl = "https://blockstream.info/api/address/" + btcAddress;
            URL url = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            // 简单解析 JSON，寻找 chain_stats.funded_txo_sum 和 chain_stats.spent_txo_sum
            String json = response.toString();
            long funded = extractJsonLong(json, "\"funded_txo_sum\":");
            long spent = extractJsonLong(json, "\"spent_txo_sum\":");
            return funded - spent;
        } catch (Exception e) {
            System.out.println("余额查询失败: " + e.getMessage());
            return 0;
        }
    }

    private static long extractJsonLong(String json, String key) {
        int idx = json.indexOf(key);
        if (idx == -1) return 0;
        int start = idx + key.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        try {
            return Long.parseLong(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
