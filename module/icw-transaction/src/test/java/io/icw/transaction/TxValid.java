/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.icw.transaction;

import io.icw.base.RPCUtil;
import io.icw.base.basic.AddressTool;
import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.data.*;
import io.icw.transaction.token.AccountData;
import io.icw.transaction.token.AccountDataBetaOne;
import io.icw.transaction.token.TestJSONObj;
import io.icw.transaction.tx.Transfer;
import io.icw.base.data.*;
import io.icw.base.data.po.BlockHeaderPo;
import io.icw.base.signture.P2PHKSignature;
import io.icw.base.signture.TransactionSignature;
import io.icw.core.crypto.HexUtil;
import io.icw.core.exception.NulsException;
import io.icw.core.log.Log;
import io.icw.core.model.BigIntegerUtils;
import io.icw.core.model.DateUtils;
import io.icw.core.model.StringUtils;
import io.icw.core.parse.JSONUtils;
import io.icw.core.rpc.info.Constants;
import io.icw.core.rpc.info.HostInfo;
import io.icw.core.rpc.info.NoUse;
import io.icw.core.rpc.model.ModuleE;
import io.icw.core.rpc.model.message.Response;
import io.icw.core.rpc.netty.processor.ResponseMessageProcessor;
import io.icw.core.rpc.util.NulsDateUtils;
import io.icw.core.thread.ThreadUtils;
import io.icw.core.thread.commom.NulsThreadFactory;
import io.icw.transaction.constant.TxConstant;
import io.icw.transaction.model.bo.Chain;
import io.icw.transaction.model.bo.config.ConfigBean;
import io.icw.transaction.model.dto.CoinDTO;
import io.icw.transaction.rpc.call.LedgerCall;
import io.icw.transaction.rpc.call.TransactionCall;
import io.icw.transaction.utils.TxUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static io.icw.transaction.utils.LoggerUtil.LOG;
import static org.junit.Assert.assertNotNull;

/**
 * @author: Charlie
 * @date: 2019/3/11
 */
public class TxValid {

    static String address20 = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    static String address21 = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
    static String address22 = "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24";
    static String address23 = "tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD";
    static String address24 = "tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL";
    static String address25 = "tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL";
    static String address26 = "tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm";
    static String address27 = "tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1";
    static String address28 = "tNULSeBaMoNnKitV28JeuUdBaPSR6n1xHfKLj2";
    static String address29 = "tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn";

    /**
     * 空地址
     * tNULSeBaMm8Kp5u7WU5xnCJqLe8fRFD49aZQdK
     * tNULSeBaMigwBrvikwVwbhAgAxip8cTScwcaT8
     */
    private Chain chain;
//    static int chainId = 12;
    static int chainId = 2;
    static int assetChainId = 2;
    static int assetId = 1;
    static String version = "1.0";

    static String password = "nuls123456";//"nuls123456";


    @Before
    public void before() throws Exception {
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":7771");
        chain = new Chain();
        chain.setConfig(new ConfigBean(chainId, assetId, 1024 * 1024, 1000, 20, 20000, 60000));
    }

    private ExecutorService signExecutor = ThreadUtils.createThreadPool(Runtime.getRuntime().availableProcessors(), Integer.MAX_VALUE, new NulsThreadFactory("THREAD_VERIFIY_BLOCK_TXS_TEST"));


    @Test
    public void importPriKeyTest() {
        importPriKey("b54db432bba7e13a6c4a28f65b925b18e63bcb79143f7b894fa735d5d3d09db5", password);//种子出块地址 tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp
//        importPriKey("188b255c5a6d58d1eed6f57272a22420447c3d922d5765ebb547bc6624787d9f", password);//种子出块地址 tNULSeBaMoGr2RkLZPfJeS5dFzZeNj1oXmaYNe
        importPriKey("9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b", password);//20 tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG
        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", password);//21 tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
        importPriKey("8212e7ba23c8b52790c45b0514490356cd819db15d364cbe08659b5888339e78", password);//22 tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24
        importPriKey("4100e2f88c3dba08e5000ed3e8da1ae4f1e0041b856c09d35a26fb399550f530", password);//23 tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD
        importPriKey("bec819ef7d5beeb1593790254583e077e00f481982bce1a43ea2830a2dc4fdf7", password);//24 tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL
        importPriKey("ddddb7cb859a467fbe05d5034735de9e62ad06db6557b64d7c139b6db856b200", password);//25 tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL
        importPriKey("4efb6c23991f56626bc77cdb341d64e891e0412b03cbcb948aba6d4defb4e60a", password);//26 tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm
        importPriKey("3dadac00b523736f38f8c57deb81aa7ec612b68448995856038bd26addd80ec1", password);//27 tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1
        importPriKey("27dbdcd1f2d6166001e5a722afbbb86a845ef590433ab4fcd13b9a433af6e66e", password);//28 tNULSeBaMoNnKitV28JeuUdBaPSR6n1xHfKLj2
        importPriKey("76b7beaa98db863fb680def099af872978209ed9422b7acab8ab57ad95ab218b", password);//29 tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn
    }

    /**
     * 合约与普通交易混发
     * @throws Exception
     */
    @Test
    public void transferAndContractPixelTest() throws Exception {
        String code = Files.readString(Path.of("E:\\ContractTest", "pixel.txt"));
        int size = 0;
        for (int i = 0; i < 1000; i++) {
            size++;
            String hash = createTransfer(address21, address29, new BigInteger("100000000"));
            //String hash = createCtxTransfer();
            System.out.println("transfer: " + hash);
            System.out.println("contract: " + TestCommonUtil.createContract(address21, TestCommonUtil.PASSWORD, code, new Object[]{size % 50 + 1}));
//            Thread.sleep(100L);
        }
    }

    @Test
    public void getTxs() throws Exception {
//        getTxCfmClient("31f65fb2cc5e468b203f692291ea94f8559dca30878f9e1648c11601bf0cf7e1");
        String txStr = (String)(getTxCfmClient("6905e41243be43ba7550a25626e2dbe3b6f0ac6a472b7599464ad056ddf66c82").get("tx"));
        Transaction tx = TxUtil.getInstance(txStr, Transaction.class);//最后一条
        TxUtil.txInformationDebugPrint(tx);
    }

    @Test
    public void scTestTransfer() throws Exception { //测试合约发钱
        importPriKey("38464747051e948965d667bd24696ffcce8fb680b2faa843273aa2d609a4d40b", password);
        importPriKey("55eea92427e97fb26ab965087aba1326e44b3251923a8b1c4891eff58da43fc1", password);
        importPriKey("1c47826a81bf7ee46092e4c76c5f6a6fdf5d6a193946270b157c8f9d625baaab", password);
        importPriKey("41f2de5344e238a65b8e4f15afcab46465d641eea020d79761aa356144d1b7df", password);
        importPriKey("5302cb0d14207abcbf2c2ffcacb2c8de2f6e522bfcfbbf0d9326828b4e1d7203", password);
        importPriKey("107c590e2a33df44218dac70177b52c814992b035f2f8b8d529470612a16fc93", password);
        importPriKey("5060502d157c03e46ac7dd3ced2e4c456e55cc7b924c94c6203e4c3d7f46a6c4", password);
        importPriKey("3fd3381c647612a4cf19eac516d8c582e61eb99d8532ea11d0bae605b542f7ac", password);
        importPriKey("1b6fffdb9eb61749b7b8466bc7996da9f889e2ca1fd332895ace170914dcf958", password);
        importPriKey("368c32b90f810cc4d2f83b374785bda919a5dfda88a1a34c56757107d8b1a28b", password);
        importPriKey("368c32b90f810cc4d2f83b374785bda919a5dfda88a1a34c56757107d8b1a28b", password);
        importPriKey("1660e4773adf225f167c1834085afc31aaea52e5b28fb76d251eab6e92c73116", password);
        createTransfer(address20, "tNULSeBaMmcSM2rzbZ4HQzv1ge4KT828W3PFKx", new BigInteger("20000000000000"));
        createTransfer(address20, "tNULSeBaMvTdRV28tTLxn6T6nKShUfMvdEqtFP", new BigInteger("20000000000000"));
        createTransfer(address20, "tNULSeBaMjtpBCk5953nc81jr28rvLgsKENcQy", new BigInteger("20000000000000"));
        createTransfer(address20, "tNULSeBaMrp9a1QMF3S6gR2JZnmEZq68sqgTLA", new BigInteger("20000000000000"));
        createTransfer(address20, "tNULSeBaMvAj8L9ET1haM7FgWHsBPcNend21fS", new BigInteger("20000000000000"));
        createTransfer(address20, "tNULSeBaMjhfQ1G2shvFF4QbCZeTJ5b23iTR2h", new BigInteger("20000000000000"));
        createTransfer(address20, "tNULSeBaMuAQZMiWXmvRhpJzC4aY4DEwqQrucQ", new BigInteger("20000000000000"));
        createTransfer(address20, "tNULSeBaMph1RpK3FAJZcW9QoRa1G4jvC4VSA2", new BigInteger("20000000000000"));
        createTransfer(address20, "tNULSeBaMf9iLnMzHWtvn3JGQ7r55ysbtWDUE1", new BigInteger("20000000000000"));
        createTransfer(address20, "tNULSeBaMmc8tip5c4pob9cd41CpnZDYS6beCm", new BigInteger("20000000000000"));
        createTransfer(address20, "tNULSeBaMszoF91eiFTb4UWU6q78jsG5jDbd91", new BigInteger("20000000000000"));
        createTransfer(address20, "tNULSeBaMg9PdWwZfugBd1NJ7qxNGjWFc2XJtg", new BigInteger("20000000000000"));
    }
    /**
     * 多个地址转账
     */
    @Test
    public void mAddressTransfer() throws Exception {
        int count = 9999;
        Log.info("创建转账账户...");
        List<String> list = createAddress(count);
        //给新生成账户转账
        NulsHash hash = null;

        Log.info("交易账户余额初始化...");
        for (int i = 0; i < count; i++) {
            String address = list.get(i);
            Map transferMap = this.createTransferTx(address23, address, new BigInteger("100000000000"));
            Transaction tx = assemblyTransaction((int) transferMap.get(Constants.CHAIN_ID), (List<CoinDTO>) transferMap.get("inputs"),
                    (List<CoinDTO>) transferMap.get("outputs"), (String) transferMap.get("remark"), hash);
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("tx", RPCUtil.encode(tx.serialize()));
            hash = tx.getHash();
//            Log.debug("hash:" + hash.toHex());
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
//            Log.debug("count:" + (i + 1));
//            Thread.sleep(1L);
        }
        //睡30秒
        Thread.sleep(30000L);
        Log.info("创建接收账户...");
        List<String> listTo = createAddress(count);

        //新生成账户各执行一笔转账
        Log.debug("{}", System.currentTimeMillis());
        int countTx = 0;
        Map<String, NulsHash> preHashMap = new HashMap<>();
        for (int x = 0; x < 1000; x++) {
            Log.info("start Transfer {} 笔,  * 第 {} 次", count, x + 1);
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                String address = list.get(i);
                String addressTo = listTo.get(i);
                Map transferMap = this.createTransferTx(address, addressTo, new BigInteger("100000"));
                Transaction tx = assemblyTransaction((int) transferMap.get(Constants.CHAIN_ID), (List<CoinDTO>) transferMap.get("inputs"),
                        (List<CoinDTO>) transferMap.get("outputs"), (String) transferMap.get("remark"), preHashMap.get(address));
                Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
                params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
                params.put(Constants.CHAIN_ID, chainId);
                params.put("tx", RPCUtil.encode(tx.serialize()));
//                Log.debug("hash:" + tx.getHash().toHex());
                HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
                preHashMap.put(address, tx.getHash());
                countTx++;
            }
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            Log.info("tx count:{} - execution time:{} milliseconds,  about≈:{}seconds", count, executionTime, executionTime / 1000);
            Log.info("");
        }
        Log.info("全部完成時間：{}, - total count:{}",
                DateUtils.timeStamp2DateStr(NulsDateUtils.getCurrentTimeMillis()), countTx);
    }


    private List<String> doAccountsCreateAndGiveMoney(int addrCount, BigInteger amount, String richAddr) throws Exception {
        List<String> list = createAddress(addrCount);
        //给新生成账户转账
        NulsHash hash = null;
        Log.info("交易账户余额初始化...");
        for (int i = 0; i < addrCount; i++) {
            String address = list.get(i);
            Map transferMap = this.createTransferTx(richAddr, address, amount);
            Transaction tx = assemblyTransaction((int) transferMap.get(Constants.CHAIN_ID), (List<CoinDTO>) transferMap.get("inputs"),
                    (List<CoinDTO>) transferMap.get("outputs"), (String) transferMap.get("remark"), hash);
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("tx", RPCUtil.encode(tx.serialize()));
            hash = tx.getHash();
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
        }
        //睡30秒
        Thread.sleep(30000L);
        return list;
    }

    private long doTrans(Map<String, NulsHash> preHashMap, List<String> list1, List<String> list2, int count) throws Exception {
        long countTx = 0;
        for (int i = 0; i < count; i++) {
            String address = list1.get(i);
            String addressTo = list2.get(i);
            Map transferMap = this.createTransferTx(address, addressTo, new BigInteger("1000000"));
            Transaction tx = assemblyTransaction((int) transferMap.get(Constants.CHAIN_ID), (List<CoinDTO>) transferMap.get("inputs"),
                    (List<CoinDTO>) transferMap.get("outputs"), (String) transferMap.get("remark"), preHashMap.get(address));
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("tx", RPCUtil.encode(tx.serialize()));
//                Log.debug("hash:" + tx.getHash().toHex());
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
            preHashMap.put(address, tx.getHash());
            countTx++;
        }
        return countTx;

    }

    @Test
    public void mAddressTransferLjs() throws Exception {
        int count = 10000;
        Log.info("创建转账账户...");
        List<String> list1 = doAccountsCreateAndGiveMoney(count, new BigInteger("500000000"), address21);
        List<String> list2 = doAccountsCreateAndGiveMoney(count, new BigInteger("500000000"), address21);
        //睡30秒
        Thread.sleep(30000L);
        //新生成账户各执行一笔转账
        Log.debug("{}", System.currentTimeMillis());
        long countTx = 0;
        Map<String, NulsHash> preHashMap = new HashMap<>();
        long x = 0;
        while (true) {
            x++;
            Log.info("start Transfer {} 笔,  * 第 {} 次", countTx, x);
            long startTime = System.currentTimeMillis();
            countTx = countTx + doTrans(preHashMap, list1, list2, count);
            countTx = countTx + doTrans(preHashMap, list2, list1, count);
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            Log.info("tx count:{} - execution time:{} milliseconds,  about≈:{}seconds", countTx, executionTime, executionTime / 1000);
        }
    }

    @Test
    public void multiThreadingTransfer() throws Exception {
        /** 每个线程发起交易的数量 */
        int txCount = 7500;
        long startTime = System.currentTimeMillis();
        Transfer transfer1 = new Transfer(address25, address21, txCount);
        Thread thread1 = new Thread(transfer1);
        thread1.start();

        Transfer transfer2 = new Transfer(address26, address22, txCount);
        Thread thread2 = new Thread(transfer2);
        thread2.start();

        Transfer transfer3 = new Transfer(address27, address23, txCount);
        Thread thread3 = new Thread(transfer3);
        thread3.start();

        Transfer transfer4 = new Transfer(address28, address24, txCount);
        Thread thread4 = new Thread(transfer4);
        thread4.start();

        Transfer transfer5 = new Transfer(address29, address24, txCount);
        Thread thread5 = new Thread(transfer5);
        thread5.start();

//        Transfer transfer6 = new Transfer(address20, address24, txCount);
//        Thread thread6 = new Thread(transfer6);
//        thread6.start();
//        Log.info("{}线程执行中...", thread6.getName());

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
        thread5.join();
//        thread6.join();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        Log.info("全部完成時間：{}, - total execution time:{} milliseconds,  about≈:{}seconds",
                DateUtils.timeStamp2DateStr(NulsDateUtils.getCurrentTimeMillis()), executionTime, executionTime / 1000);
    }


    /**
     * 设置别名
     *
     * @throws Exception
     */
    @Test
    public void aliasTest() throws Exception {
        String alias = "charlie233";
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        params.put("password", password);
        params.put("alias", alias);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setAlias", params);
        System.out.println("ac_setAlias result:" + JSONUtils.obj2json(cmdResp));
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setAlias");
        String txHash = (String) result.get("txHash");
        Log.debug("alias-txHash:{}", txHash);
        /*****************************************************************/
        for (int i = 0; i < 1; i++) {
            String hash = createTransfer(address26, address20, new BigInteger("1000000000"));
            //String hash = createCtxTransfer();
            System.out.println("hash:" + hash);
            System.out.println("count:" + (i + 1));
            System.out.println("");
//            Thread.sleep(500L);
        }
    }

    @Test
    public void transfer() throws Exception {
        for (int i = 0; i < 1; i++) {
            String hash = createTransfer("tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm", "tNULSeBaNDW6r3RCaxLtBzo4QLPKPdWmDWgxAs", new BigInteger("200000000000000"));
            //String hash = createCtxTransfer();
            System.out.println("hash:" + hash);
            System.out.println("count:" + (i + 1));
            System.out.println("");
//            Thread.sleep(500L);
        }
    }


    @Test
    public void transferLocal() throws Exception {
        NulsHash hash = null;
        for (int i = 0; i < 3; i++) {
            Map transferMap = this.createTransferTx(address20, address21, new BigInteger("10000000"));

            Transaction tx = assemblyTransaction((int) transferMap.get(Constants.CHAIN_ID), (List<CoinDTO>) transferMap.get("inputs"),
                    (List<CoinDTO>) transferMap.get("outputs"), (String) transferMap.get("remark"), hash);
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("tx", RPCUtil.encode(tx.serialize()));
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
            hash = tx.getHash();
            System.out.println("hash:" + hash.toHex());
            System.out.println("hash:" + hash);
            System.out.println("count:" + (i + 1));
            System.out.println("");
//            Thread.sleep(500L);
        }
    }

    @Test
    public void transferLocal2() throws Exception {
        NulsHash hash = null;
        int[] sysTypeTx = {1, 7, 8, 18, 19, 20, 21, 22, 23};
        for (int i = 0; i < sysTypeTx.length; i++) {
            try {
                Map transferMap = this.createTransferTx(address20, address21, new BigInteger("10000000"));
                Transaction tx = assemblyTransaction((int) transferMap.get(Constants.CHAIN_ID), sysTypeTx[i], (List<CoinDTO>) transferMap.get("inputs"),
                        (List<CoinDTO>) transferMap.get("outputs"), (String) transferMap.get("remark"), hash);
                Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
                params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
                params.put(Constants.CHAIN_ID, chainId);
                params.put("tx", RPCUtil.encode(tx.serialize()));
                HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
                hash = tx.getHash();
                System.out.println("hash:" + hash.toHex());
                System.out.println("hash:" + hash);
                System.out.println("count:" + (i + 1));
                System.out.println("");
            } catch (NulsException e) {
                System.out.println("type:" +sysTypeTx[i] + "; error: " + e.getErrorCode().getCode());
            }
//            Thread.sleep(500L);
        }
    }



    @Test
    public void getBalance() throws Exception {

        BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress("tNULSeBaNDW6r3RCaxLtBzo4QLPKPdWmDWgxAs"), assetChainId, assetId);
        System.out.println(JSONUtils.obj2PrettyJson(balance));

    }

    @Test
    public void testSign() throws Exception {
        List<Transaction> list = createTx();
        sign(list);
        signMThread(list);

    }

    /**
     * 多线程
     **/
    private void signMThread(List<Transaction> list) throws Exception {
        //tx_baseValidateTx
        long s1 = System.currentTimeMillis();
        List<Future<Boolean>> futures = new ArrayList<>();
        for (Transaction tx : list) {
            Future<Boolean> res = signExecutor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    //不在未确认中就进行基础验证
                    try {
                         /*validateTxSignature(tx,null,chain);
                         return true;*/
                        //只验证单个交易的基础内容(TX模块本地验证)
                        Map<String, Object> params = new HashMap<>();
                        params.put(Constants.VERSION_KEY_STR, "1.0");
                        params.put(Constants.CHAIN_ID, chainId);
                        params.put("tx", RPCUtil.encode(tx.serialize()));
                        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_baseValidateTx", params);
                        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("tx_baseValidateTx");
                        boolean rs = (boolean) result.get("value");
                        return rs;
                    } catch (Exception e) {
                        return false;
                    }
                }
            });
            futures.add(res);
        }

        try {
            //多线程处理结果
            for (Future<Boolean> future : futures) {
                if (!future.get()) {
                    Log.debug("failed, single tx verify failed");
//                   break;
                }
            }
        } catch (InterruptedException e) {
            Log.debug("batchVerify failed, single tx verify failed");
            Log.error(e);
        } catch (ExecutionException e) {
            Log.debug("batchVerify failed, single tx verify failed");
            Log.error(e);
        }
        Log.debug("{}笔交易多线程执行时间:{}", list.size(), System.currentTimeMillis() - s1);
    }

    /**
     * 单线程
     **/
    private void sign(List<Transaction> list) throws Exception {
        long s1 = System.currentTimeMillis();
        for (Transaction tx : list) {
            //不在未确认中就进行基础验证
            try {
                /*validateTxSignature(tx,null,chain);*/
                //只验证单个交易的基础内容(TX模块本地验证)
                Map<String, Object> params = new HashMap<>();
                params.put(Constants.VERSION_KEY_STR, "1.0");
                params.put(Constants.CHAIN_ID, chainId);
                params.put("tx", RPCUtil.encode(tx.serialize()));
                Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_baseValidateTx", params);
                HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("tx_baseValidateTx");
                boolean rs = (boolean) result.get("value");
                if (!rs) {
                    Log.debug("验签名 failed type:{}, -hash:{}", tx.getType(), tx.getHash().toHex());
                    break;
                }
            } catch (Exception e) {
                Log.debug("验签名 failed type:{}, -hash:{}", tx.getType(), tx.getHash().toHex());
            }
        }
        Log.debug("{}笔交易单线程执行时间:{}", list.size(), System.currentTimeMillis() - s1);
    }


    private List<String> createAddress(int count) throws Exception {
        List<String> addressList = new ArrayList<>();
        if (100 <= count) {
            int c1 = count / 100;
            for (int i = 0; i < c1; i++) {
                List<String> list = createAccount(chainId, 100, password);
                addressList.addAll(list);
            }
            int c2 = count % 100;
            if (c2 > 0) {
                List<String> list = createAccount(chainId, c2, password);
                addressList.addAll(list);
            }
        } else if (100 > count) {
            List<String> list = createAccount(chainId, count, password);
            addressList.addAll(list);
        }
        return addressList;
    }


    /**
     * 多个地址转账
     * 坚哥版
     */
    @Test
    public void mAddressTransfer2() throws Exception {
        int count = 10000;
        List<String> list = createAddress(count);
        //给新生成账户转账
        NulsHash hash = null;
        for (int i = 0; i < count; i++) {
            String address = list.get(i);
            Map transferMap = this.createTransferTx(address23, address, new BigInteger("10000000000"));
            Transaction tx = assemblyTransaction((int) transferMap.get(Constants.CHAIN_ID), (List<CoinDTO>) transferMap.get("inputs"),
                    (List<CoinDTO>) transferMap.get("outputs"), (String) transferMap.get("remark"), hash);
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("tx", RPCUtil.encode(tx.serialize()));
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.TX.abbr, "tx_newTx_test", params);
            hash = tx.getHash();
            Log.debug("hash:" + hash.toHex());

            Log.debug("count:" + (i + 1));
            Thread.sleep(1L);
        }

        List<String> listTo = createAddress(count);

        //新生成账户各执行一笔转账
        Log.debug("{}", System.currentTimeMillis());
        int countTx = 0;
        Map<String, NulsHash> preHashMap = new HashMap<>();
        for (int x = 0; x < 10000; x++) {
            long value = 10000000000L - 1000000 * (x + 1);
            for (int i = 0; i < count; i++) {
                String address = list.get(i);
                String addressTo = listTo.get(i);
                Map transferMap = this.createTransferTx(address, addressTo, new BigInteger("" + value));
                Transaction tx = assemblyTransaction((int) transferMap.get(Constants.CHAIN_ID), (List<CoinDTO>) transferMap.get("inputs"),
                        (List<CoinDTO>) transferMap.get("outputs"), (String) transferMap.get("remark"), preHashMap.get(address));
                Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
                params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
                params.put(Constants.CHAIN_ID, chainId);
                params.put("tx", RPCUtil.encode(tx.serialize()));
                HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.TX.abbr, "tx_newTx_test", params);
                Log.debug("hash:" + tx.getHash().toHex());
                Log.debug("count:" + (i + 1));
                preHashMap.put(address, tx.getHash());
                countTx++;
            }
            Log.debug("***********************");
            removeAccountList(list);
            list = listTo;
            listTo = createAddress(count);
            Thread.sleep(10000L);
        }
        Log.debug("{}", System.currentTimeMillis());
        Log.debug("count:{}", countTx);

    }


    private void removeAccountList(List<String> list) throws Exception {
        for (String address : list) {
            this.removeAccount(address, this.password);
        }
    }

    @Test
    public void mixedTransfer() throws Exception {
        String agentHash = null;
        String depositHash = null;
        String withdrawHash = null;
        String stopAgent = null;
        for (int i = 0; i < 20000; i++) {
            String hash = createTransfer(address25, address21, new BigInteger("10000000000"));
            switch (i) {
                case 0:
                    //创建节点
                    agentHash = createAgent(address29, address27);
                    break;
                case 4000:
                    //委托
                    depositHash = deposit(address29, agentHash);
                    break;
                case 16000:
                    //取消委托
                    withdrawHash = withdraw(address29, depositHash);
                    break;
                case 19000:
                    //停止节点
                    stopAgent = stopAgent(address29);
                    break;
            }

            System.out.println("count:" + (i + 1));
            System.out.println("");

        }
    }

    @Test
    public void getTx() throws Exception {
//        getTxCfmClient("31f65fb2cc5e468b203f692291ea94f8559dca30878f9e1648c11601bf0cf7e1");
        String txStr = (String)(getTxCfmClient("0af0e6bb75df48ca1fa397d16bccfc9091e2ac8fa83713e72e30e6c8a7867be0").get("tx"));
        Transaction tx = TxUtil.getInstance(txStr, Transaction.class);//最后一条
        txInformationDebugPrint(tx);
    }


    private void getTx(String hash) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("txHash", hash);
        //调用接口
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getTx", params);
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("tx_getTx"));
        Assert.assertTrue(null != result);
        Log.debug("{}", JSONUtils.obj2PrettyJson(result));
       /* String txStr = (String) result.get("tx");
        LOG.debug("getTx -hash:{}", ((Transaction)TxUtil.getInstanceRpcStr(txStr, Transaction.class)).getHash().toHex());*/
    }


    @Test
    public void createAgentTx() throws Exception {
        createAgent(address20, "tNULSeBaMoRVvrr9noCDWwNNe3ZAbCvRWEPtij");
    }

    private String createAgent(String address, String packing) throws Exception {
        //组装创建节点交易
        Map agentTxMap = this.createAgentTx(address, packing);
        //调用接口
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", agentTxMap);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_createAgent"));
        Assert.assertTrue(null != result);
        String hash = (String) result.get("txHash");
        Log.debug("createAgent-txHash:{}", hash);
        return hash;
    }

    /**
     * 委托节点
     */
    @Test
    public void depositToAgent() throws Exception {
        //组装委托节点交易
        String hash = deposit(address20, "3f686219f3d2b5eed48c7bbc553a77a0bc5c59bef16f2a7cf7835a157c3c2d96");
    }

    private String deposit(String address, String agentHash) throws Exception {
        Map<String, Object> dpParams = new HashMap<>();
        dpParams.put(Constants.CHAIN_ID, chainId);
        dpParams.put("address", address);
        dpParams.put("password", password);
        dpParams.put("agentHash", agentHash);
        dpParams.put("deposit", 200000 * 100000000L);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", dpParams);
        HashMap dpResult = (HashMap) ((HashMap) dpResp.getResponseData()).get("cs_depositToAgent");
        String txHash = (String) dpResult.get("txHash");
        Log.debug("deposit-txHash:{}", txHash);
        return txHash;
    }

    /**
     * 取消委托
     *
     * @throws Exception
     */
    @Test
    public void withdraw() throws Exception {
        String hash = withdraw(address25, "0020b8a42eb4c70196189e607e9434fe09b595d5753711f21819113f40d64a1c82c1");
        Log.debug("withdraw-txHash:{}", hash);
    }

    private String withdraw(String address, String depositHash) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        params.put("password", password);
        params.put("txHash", depositHash);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_withdraw", params);
        HashMap dpResult = (HashMap) ((HashMap) cmdResp.getResponseData()).get("cs_withdraw");
        String hash = (String) dpResult.get("txHash");
        Log.debug("withdraw-txHash:{}", hash);
        return hash;
    }

    @Test
    public void stopAgentTx() throws Exception {
        String hash = stopAgent(address20);
        Log.debug("stopAgent-txHash:{}", hash);
    }

    private String stopAgent(String address) throws Exception {
        Map<String, Object> txMap = new HashMap();
        txMap.put("chainId", chainId);
        txMap.put("address", address);
        txMap.put("password", password);
        //调用接口
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgent", txMap);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_stopAgent"));
        String txHash = (String) result.get("txHash");
        Log.debug("stopAgent-txHash:{}", txHash);
        return txHash;
    }

    /**
     * 设置别名
     *
     * @throws Exception
     */
    @Test
    public void alias() throws Exception {
        String alias = "charlie";
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", "tNULSeBaMigwBrvikwVwbhAgAxip8cTScwcaT8");
        params.put("password", password);
        params.put("alias", alias);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setAlias", params);
        System.out.println("ac_setAlias result:" + JSONUtils.obj2json(cmdResp));
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setAlias");
        String txHash = (String) result.get("txHash");
        Log.debug("alias-txHash:{}", txHash);
    }

    private List<Transaction> createTx() throws Exception {
        List<Transaction> list = new ArrayList<>();
        NulsHash hash = null;
        System.out.println("satring......");
        for (int i = 0; i < 1; i++) {
            Map transferMap = this.createTransferTx(address25, address21, new BigInteger("1000000000"));

            Transaction tx = assemblyTransaction((int) transferMap.get(Constants.CHAIN_ID), (List<CoinDTO>) transferMap.get("inputs"),
                    (List<CoinDTO>) transferMap.get("outputs"), (String) transferMap.get("remark"), hash);
            //String hash = createCtxTransfer();
            hash = tx.getHash();
            list.add(tx);
            System.out.println("hash:" + hash.toHex());
            System.out.println("txHex:" + RPCUtil.encode(tx.serialize()));
        }

        System.out.println(list.size());
        return list;
    }

    /**
     * 查交易
     */
    @Test
    public void getTxRecord() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address20);//tNULSeBaMk4LBr1y1tsneiHvy5H2Rc3Lns4QuN
        params.put("assetChainId", null);
        params.put("assetId", null);
        params.put("type", null);
        params.put("pageSize", null);
        params.put("pageNumber", null);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getTxs", params);
        Map record = (Map) dpResp.getResponseData();
        Log.debug("Page<TransactionPO>:{}", JSONUtils.obj2PrettyJson(record));
    }

    /**
     * 查交易
     */
    private void getTxClient(String hash) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("txHash", hash);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getTxClient", params);
        Map record = (Map) dpResp.getResponseData();
        Log.debug("{}", JSONUtils.obj2PrettyJson(record));
    }

    /**
     * 查交易
     */
    private Map<String, Object> getTxCfmClient(String hash) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("txHash", hash);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getConfirmedTxClient", params);
        Map record = (Map) dpResp.getResponseData();
        Log.debug(JSONUtils.obj2PrettyJson(record));
        return (Map)record.get("tx_getConfirmedTxClient");
    }

    /**
     * 查区块
     * 高度或者hash
     */
    @Test
    public void getBlocByHeight() throws Exception {
        getBlockHeaderPoByHeight(2L);
    }
    private void getBlockHeaderPoByHeight(long param) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("height", param);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, "getBlockHeaderPoByHeight", params);
        Map record = (Map) dpResp.getResponseData();
        Map rs = (Map)record.get("getBlockHeaderPoByHeight");
        BlockHeaderPo blockHeaderPo = new BlockHeaderPo();
        blockHeaderPo.parse(new NulsByteBuffer(RPCUtil.decode((String)rs.get("value"))));
        Log.debug(JSONUtils.obj2PrettyJson(blockHeaderPo));
        for(NulsHash nulsHash : blockHeaderPo.getTxHashList()){
            Log.debug("txHash:{}", nulsHash.toHex());
        }
    }


    @Test
    public void getPriKeyByAddress() throws Exception {
        String prk = getPriKeyByAddress("tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL");
        System.out.println(prk);
    }

    /**
     * 导入keystore
     */
    @Test
    public void importAccountByKeystoreFile() {
        String address = importAccountByKeystoreFile("C:/Users/Administrator/Desktop/2.0测试配置和内容/mainnet.keystore", "nuls1234");
//        String address2 = importAccountByKeystoreFile("C:/Users/Administrator/Desktop/2.0测试配置和内容/testnet.keystore", "nuls1234");
//        String address3 = importAccountByKeystoreFile("C:/Users/Administrator/Desktop/2.0测试配置和内容/tNULSeBaMm8Kp5u7WU5xnCJqLe8fRFD49aZQdK.keystore", "nuls123456");
        Log.info("address:{}", address);
//        Log.info("address:{}", address2);
//        Log.info("address:{}", address3);
    }

    /**
     * 删除账户
     */
    @Test
    public void removeAccountTest() throws Exception {
        removeAccount("tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp", password);
//        removeAccount(address20, password);
    }

    private String createTransfer(String addressFrom, String addressTo, BigInteger amount) throws Exception {
        Map transferMap = this.createTransferTx(addressFrom, addressTo, amount);
        //调用接口
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
        if (!cmdResp.isSuccess()) {
            return "fail";
        }
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer"));
        Assert.assertTrue(null != result);
        String hash = (String) result.get("value");
//        Log.debug("{}", hash);
        return hash;
    }

    private Transaction getCreateTransferTx(String addressFrom, String addressTo, BigInteger amount) throws Exception {
        Map transferMap = this.createTransferTx(addressFrom, addressTo, amount);
        //调用接口
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer_tx", transferMap);
        if (!cmdResp.isSuccess()) {
            return null;
        }
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer_tx"));
        Assert.assertTrue(null != result);
        String str = (String) result.get("value");
        Transaction tx = new Transaction();
        tx.parse(new NulsByteBuffer(RPCUtil.decode(str)));
        return tx;
    }

    private String getPriKeyByAddress(String address) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("password", password);
        params.put("address", address);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getPriKeyByAddress", params);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getPriKeyByAddress");
        String priKey = (String) result.get("priKey");
        Log.debug("{}", priKey);
        return priKey;
    }

    public static String importAccountByKeystoreFile(String filePath, String password) {
        String address = null;
        try {
            File file = new File(filePath);
            byte[] bytes = copyToByteArray(file);
            String keyStoreStr = new String(bytes, "UTF-8");

            //AccountKeyStoreDto accountKeyStoreDto = JSONUtils.json2pojo(new String(HexUtil.decode(keyStoreHexStr)), AccountKeyStoreDto.class);

            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("keyStore", RPCUtil.encode(bytes));
            params.put("password", password);
            params.put("overwrite", true);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByKeystore", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByKeystore");
            address = (String) result.get("address");
            //assertEquals(accountList.get(0), address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return address;
    }

    public static byte[] copyToByteArray(File in) throws IOException {
        if (in == null) {
            return new byte[0];
        }
        InputStream input = null;
        try {
            input = new FileInputStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
            int byteCount = 0;
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = input.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
            return out.toByteArray();
        } finally {
            try {
                input.close();
            } catch (Exception e) {
            }
        }
    }

    public void importPriKey(String priKey, String pwd) {
        try {
            //账户已存在则覆盖 If the account exists, it covers.
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);

            params.put("priKey", priKey);
            params.put("password", pwd);
            params.put("overwrite", true);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByPriKey");
            String address = (String) result.get("address");
            Log.debug("importPriKey success! address-{}", address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeAccount(String address, String password) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        params.put("password", password);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_removeAccount", params);
        Log.debug("{}", JSONUtils.obj2json(cmdResp.getResponseData()));
    }


    //    @Test
    public void packableTxs() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        long endTime = System.currentTimeMillis() + 10000L;
        System.out.println("endTime: " + endTime);
        params.put("endTimestamp", endTime);
        params.put("maxTxDataSize", 2 * 1024 * 1024L);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_packableTxs", params);
        Assert.assertTrue(null != response.getResponseData());
        Map map = (HashMap) ((HashMap) response.getResponseData()).get("tx_packableTxs");
        Assert.assertTrue(null != map);
        List<String> list = (List) map.get("list");
        Log.debug("packableTxs:");
        for (String s : list) {
            Log.debug(s);
        }
    }

    private List<CoinDTO> createFromCoinDTOList() {
        CoinDTO coinDTO = new CoinDTO();
        coinDTO.setAssetsId(assetId);
        coinDTO.setAssetsChainId(assetChainId);
        coinDTO.setAddress(address25);
        coinDTO.setAmount(new BigInteger("200000000"));
        coinDTO.setPassword(password);

        CoinDTO coinDTO2 = new CoinDTO();
        coinDTO2.setAssetsId(assetId);
        coinDTO2.setAssetsChainId(assetChainId);
        coinDTO2.setAddress(address26);
        coinDTO2.setAmount(new BigInteger("100000000"));
        coinDTO2.setPassword(password);
        List<CoinDTO> listFrom = new ArrayList<>();
        listFrom.add(coinDTO);
        listFrom.add(coinDTO2);
        return listFrom;
    }


    private List<CoinDTO> createToCoinDTOList() {
        CoinDTO coinDTO = new CoinDTO();
        coinDTO.setAssetsId(assetId);
        coinDTO.setAssetsChainId(23);
        coinDTO.setAddress("NULSd6Hgfj4PyqSuYBEJth3zEG32sjYsUGsVA");
        coinDTO.setAmount(new BigInteger("200000000"));

        CoinDTO coinDTO2 = new CoinDTO();
        coinDTO2.setAssetsId(assetId);
        coinDTO2.setAssetsChainId(23);
        coinDTO2.setAddress("NULSd6HggkGBiHrUAL4YGErUFiMb2DkB5QQus");
        coinDTO2.setAmount(new BigInteger("100000000"));
        List<CoinDTO> listTO = new ArrayList<>();
        listTO.add(coinDTO);
        listTO.add(coinDTO2);
        return listTO;
    }
    public Transaction assemblyTransaction(int chainId, List<CoinDTO> fromList, List<CoinDTO> toList, String remark, NulsHash hash) throws NulsException {
        return assemblyTransaction(chainId, null, fromList, toList, remark, hash);
    }

    /**
     * 组装交易
     */
    public Transaction assemblyTransaction(int chainId, Integer type, List<CoinDTO> fromList, List<CoinDTO> toList, String remark, NulsHash hash) throws NulsException {
        if(null == type){
            type = 2;
        }
        Transaction tx = new Transaction(type);
        tx.setTime(NulsDateUtils.getCurrentTimeMillis() / 1000);
        tx.setRemark(StringUtils.bytes(remark));
        try {
            //组装CoinData中的coinFrom、coinTo数据
            assemblyCoinData(tx, chainId, fromList, toList, hash);
            //计算交易数据摘要哈希
            byte[] bytes = tx.serializeForHash();
            tx.setHash(NulsHash.calcHash(bytes));
            //创建ECKey用于签名
//            List<ECKey> signEcKeys = new ArrayList<>();
            TransactionSignature transactionSignature = new TransactionSignature();
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            for (CoinDTO from : fromList) {
//                P2PHKSignature p2PHKSignature = AccountCall.signDigest(from.getAddress(), from.getPassword(), tx.getHash().getBytes());

                Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
                params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
                params.put(Constants.CHAIN_ID, chainId);
                params.put("address", from.getAddress());
                params.put("password", password);
                params.put("data", RPCUtil.encode(tx.getHash().getBytes()));
                HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.AC.abbr, "ac_signDigest", params);
                String signatureStr = (String) result.get("signature");

                P2PHKSignature signature = new P2PHKSignature(); // TxUtil.getInstanceRpcStr(signatureStr, P2PHKSignature.class);
                signature.parse(new NulsByteBuffer(RPCUtil.decode(signatureStr)));

                p2PHKSignatures.add(signature);
            }
            //交易签名
            transactionSignature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(transactionSignature.serialize());
            return tx;

        } catch (Exception e) {
        }
        return tx;
    }

    private Transaction assemblyCoinData(Transaction tx, int chainId, List<CoinDTO> fromList, List<CoinDTO> toList, NulsHash hash) throws NulsException {
        try {
            //组装coinFrom、coinTo数据
            List<CoinFrom> coinFromList = assemblyCoinFrom(chainId, fromList, hash);
            List<CoinTo> coinToList = assemblyCoinTo(chainId, toList);
            //来源地址或转出地址为空
            if (coinFromList.size() == 0 || coinToList.size() == 0) {
                return null;
            }
            //交易总大小=交易数据大小+签名数据大小
            int txSize = tx.size() + getSignatureSize(coinFromList);
            //组装coinData数据
            CoinData coinData = getCoinData(chainId, coinFromList, coinToList, txSize);
            tx.setCoinData(coinData.serialize());
        } catch (Exception e) {
        }
        return tx;
    }

    private CoinData getCoinData(int chainId, List<CoinFrom> listFrom, List<CoinTo> listTo, int txSize) throws NulsException {
        CoinData coinData = new CoinData();
        coinData.setFrom(listFrom);
        coinData.setTo(listTo);
        return coinData;
    }

    /**
     * 通过coinfrom计算签名数据的size
     * 如果coinfrom有重复地址则只计算一次；如果有多签地址，只计算m个地址的size
     *
     * @param coinFroms
     * @return
     */
    private int getSignatureSize(List<CoinFrom> coinFroms) {
        int size = 0;
        Set<String> commonAddress = new HashSet<>();
        for (CoinFrom coinFrom : coinFroms) {
            String address = AddressTool.getStringAddressByBytes(coinFrom.getAddress());
            commonAddress.add(address);
        }
        size += commonAddress.size() * P2PHKSignature.SERIALIZE_LENGTH;
        return size;
    }

    /**
     * 组装coinFrom数据
     * assembly coinFrom data
     *
     * @param listFrom Initiator set coinFrom
     * @return List<CoinFrom>
     * @throws NulsException
     */
    private List<CoinFrom> assemblyCoinFrom(int chainId, List<CoinDTO> listFrom, NulsHash hash) throws NulsException {
        List<CoinFrom> coinFroms = new ArrayList<>();
        for (CoinDTO coinDto : listFrom) {
            String address = coinDto.getAddress();
            byte[] addressByte = AddressTool.getAddress(address);
            //检查该链是否有该资产
            int assetChainId = coinDto.getAssetsChainId();
            int assetId = coinDto.getAssetsId();

            //检查对应资产余额是否足够
            BigInteger amount = coinDto.getAmount();
            //查询账本获取nonce值
            byte[] nonce = getNonceByPreHash(createChain(), address, hash);
            CoinFrom coinFrom = new CoinFrom(addressByte, assetChainId, assetId, amount, nonce, (byte) 0);
            coinFroms.add(coinFrom);
        }
        return coinFroms;
    }

    private Chain createChain() {
        Chain chain = new Chain();
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(chainId);
        configBean.setAssetId(assetId);
        chain.setConfig(configBean);
        return chain;
    }

    public static byte[] getNonceByPreHash(Chain chain, String address, NulsHash hash) throws NulsException {
        if (hash == null) {
            return LedgerCall.getNonce(chain, address, assetChainId, assetId);
        }
        byte[] out = new byte[8];
        byte[] in = hash.getBytes();
        int copyEnd = in.length;
        System.arraycopy(in, (copyEnd - 8), out, 0, 8);
        String nonce8BytesStr = HexUtil.encode(out);
        return HexUtil.decode(nonce8BytesStr);
    }


    /**
     * 组装coinTo数据
     * assembly coinTo data
     * 条件：to中所有地址必须是同一条链的地址
     *
     * @param listTo Initiator set coinTo
     * @return List<CoinTo>
     * @throws NulsException
     */
    private List<CoinTo> assemblyCoinTo(int chainId, List<CoinDTO> listTo) throws NulsException {
        List<CoinTo> coinTos = new ArrayList<>();
        for (CoinDTO coinDto : listTo) {
            String address = coinDto.getAddress();
            byte[] addressByte = AddressTool.getAddress(address);
            //转账交易转出地址必须是本链地址
            if (!AddressTool.validAddress(chainId, address)) {
                Log.debug("failed");
            }
            //检查该链是否有该资产
            int assetsChainId = coinDto.getAssetsChainId();
            int assetId = coinDto.getAssetsId();
            //检查金额是否小于0
            BigInteger amount = coinDto.getAmount();
            if (BigIntegerUtils.isLessThan(amount, BigInteger.ZERO)) {
                Log.debug("failed");
            }
            CoinTo coinTo = new CoinTo();
            coinTo.setAddress(addressByte);
            coinTo.setAssetsChainId(assetsChainId);
            coinTo.setAssetsId(assetId);
            coinTo.setAmount(coinDto.getAmount());
            coinTos.add(coinTo);
        }
        return coinTos;
    }

    /**
     * 创建普通转账交易
     *
     * @return
     */
    public Map createTransferTx(String addressFrom, String addressTo, BigInteger amount) {
        Map transferMap = new HashMap();
        transferMap.put("chainId", chainId);
        transferMap.put("remark", "abc");
        List<CoinDTO> inputs = new ArrayList<>();
        List<CoinDTO> outputs = new ArrayList<>();
        CoinDTO inputCoin1 = new CoinDTO();
        inputCoin1.setAddress(addressFrom);
        inputCoin1.setPassword(password);
        inputCoin1.setAssetsChainId(chainId);
        inputCoin1.setAssetsId(assetId);
        inputCoin1.setAmount(new BigInteger("100000").add(amount));
        inputs.add(inputCoin1);

        CoinDTO outputCoin1 = new CoinDTO();
        outputCoin1.setAddress(addressTo);
        outputCoin1.setPassword(password);
        outputCoin1.setAssetsChainId(chainId);
        outputCoin1.setAssetsId(assetId);
        outputCoin1.setAmount(amount);
        outputs.add(outputCoin1);

        transferMap.put("inputs", inputs);
        transferMap.put("outputs", outputs);
        return transferMap;
    }

    //    String txt = txt2String(new File("E:\\remark.txt"));
    public static String txt2String(File file) {
        StringBuilder result = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            String s = null;
            while ((s = br.readLine()) != null) {//使用readLine方法，一次读一行
                result.append(System.lineSeparator() + s);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    /**
     * 创建节点
     */
    public Map createAgentTx(String agentAddr, String packingAddr) {
        Map<String, Object> params = new HashMap<>();
        params.put("agentAddress", agentAddr);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("deposit", 20000 * 100000000L);
        params.put("commissionRate", 10);
        params.put("packingAddress", packingAddr);
        params.put("password", password);
        params.put("rewardAddress", agentAddr);
        return params;
    }


    public static List<String> createAccount(int chainId, int count, String password) {
        List<String> accountList = null;
        Response cmdResp = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("count", count);
            params.put("password", password);
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params, 60000L);
            accountList = (List<String>) ((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createAccount")).get("list");
        } catch (Exception e) {
            Log.error("cmdResp:{}", cmdResp);
            e.printStackTrace();
        }
        return accountList;
    }


    /**
     * alpah2 发放测试币
     **/
    @Test
    public void accountToken() throws Exception {
        TestJSONObj testJSONObj = new TestJSONObj();
        List<AccountData> accountDataList = testJSONObj.readStream();
        createTransfer(address20, address29, new BigInteger("999999900000000"));
        createTransfer(address21, address29, new BigInteger("999999900000000"));
        createTransfer(address22, address29, new BigInteger("999999900000000"));
        createTransfer(address23, address29, new BigInteger("999999900000000"));
        createTransfer(address24, address29, new BigInteger("999999900000000"));
        createTransfer(address25, address29, new BigInteger("999999900000000"));
        createTransfer(address26, address29, new BigInteger("999999900000000"));
        createTransfer(address27, address29, new BigInteger("999999900000000"));
        createTransfer(address28, address29, new BigInteger("999999900000000"));
        Thread.sleep(20000L);
        for (AccountData ac : accountDataList) {
            String hash = createTransfer(address29, ac.getAddress(), new BigInteger(String.valueOf(ac.getTotalBalance())));
            System.out.println(ac.getAddress() + " : " + ac.getTotalBalance() + " : " + hash);
        }
        Thread.sleep(20000L);
        BigInteger balanceTotal = LedgerCall.getBalance(chain, AddressTool.getAddress(address29), assetChainId, assetId);
        createTransfer(address29, "tNULSeBaMfwpGBmn8xuKABPWUbdtsM2cMoinnn", balanceTotal.subtract(new BigInteger("2000000000")));
        Thread.sleep(20000L);
        BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress("tNULSeBaMfwpGBmn8xuKABPWUbdtsM2cMoinnn"), assetChainId, assetId);
        System.out.println(balance);

    }


    /**
     * beta1/beta2 发放测试币
     **/
    @Test
    public void accountTokenBeta() throws Exception {
        TestJSONObj testJSONObj = new TestJSONObj();
        List<AccountDataBetaOne> accountDataList = testJSONObj.readStreamBetaOne();
        createTransfer(address20, address29, new BigInteger("999999900000000"));
        createTransfer(address21, address29, new BigInteger("999999900000000"));
        createTransfer(address22, address29, new BigInteger("999999900000000"));
        createTransfer(address23, address29, new BigInteger("999999900000000"));
        createTransfer(address24, address29, new BigInteger("999999900000000"));
        createTransfer(address25, address29, new BigInteger("999999900000000"));
        createTransfer(address26, address29, new BigInteger("999999900000000"));
        createTransfer(address27, address29, new BigInteger("999999900000000"));
        createTransfer(address28, address29, new BigInteger("999999900000000"));
        createTransfer(address29, "tNULSeBaMtGu7vkqGzmcLHSPo2nTZQw4j8uzgr", new BigInteger("100000000000000"));
        Thread.sleep(20000L);
        for (AccountDataBetaOne ac : accountDataList) {
            String hash = createTransfer(address29, ac.getAddressV2(), new BigInteger(String.valueOf(ac.getBalance())));
            System.out.println(ac.getAddressV2() + " : " + ac.getBalance() + " : " + hash);
        }
        Thread.sleep(20000L);
        BigInteger balanceTotal = LedgerCall.getBalance(chain, AddressTool.getAddress(address29), assetChainId, assetId);
        String hash = createTransfer(address29, "tNULSeBaMoG1oaW1JZnh6Ly65Ttp6raeTFBfCG", balanceTotal.subtract(new BigInteger("200000000")));
        System.out.println(hash);
        Thread.sleep(20000L);
        BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress("tNULSeBaMoG1oaW1JZnh6Ly65Ttp6raeTFBfCG"), assetChainId, assetId);
        System.out.println(balance);
        balance = LedgerCall.getBalance(chain, AddressTool.getAddress("tNULSeBaMh39UuZCPDffqiM6SHqesh5aKnm4LR"), assetChainId, assetId);
        System.out.println(balance);
        balance = LedgerCall.getBalance(chain, AddressTool.getAddress("tNULSeBaMrLc5oxqm7kd5mNZCc7366ojE4QR59"), assetChainId, assetId);
        System.out.println(balance);

    }



    public void txInformationDebugPrint(Transaction tx) {
        if (tx.getType() == 1) {
            return;
        }
        LOG.debug("");
        LOG.debug("**************************************************");
        LOG.debug("Transaction information");
        LOG.debug("type: {}", tx.getType());
        LOG.debug("txHash: {}", tx.getHash().toHex());
        LOG.debug("time: {}", NulsDateUtils.timeStamp2DateStr(tx.getTime()));
        LOG.debug("size: {}B,  -{}KB, -{}MB",
                String.valueOf(tx.getSize()), String.valueOf(tx.getSize() / 1024), String.valueOf(tx.getSize() / 1024 / 1024));
        byte[] remark = tx.getRemark();
        try {
            String remarkStr = remark == null ? "" : new String(tx.getRemark(), "UTF-8");
            LOG.debug("remark: {}", remarkStr);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e);
        }

        CoinData coinData = null;
        try {
            if (tx.getCoinData() != null) {
                coinData = tx.getCoinDataInstance();
            }
        } catch (NulsException e) {
            LOG.error(e);
        }
        if (coinData != null) {
            LOG.debug("coinData:");
            List<CoinFrom> coinFromList = coinData.getFrom();
            if (coinFromList == null) {
                LOG.debug("\tcoinFrom: null");
            } else if (coinFromList.size() == 0) {
                LOG.debug("\tcoinFrom: size 0");
            } else {
                LOG.debug("\tcoinFrom: ");
                for (int i = 0; i < coinFromList.size(); i++) {
                    CoinFrom coinFrom = coinFromList.get(i);
                    LOG.debug("\tFROM_{}:", i);
                    LOG.debug("\taddress: {}", AddressTool.getStringAddressByBytes(coinFrom.getAddress()));
                    LOG.debug("\tamount: {}", coinFrom.getAmount());
                    LOG.debug("\tassetChainId: [{}]", coinFrom.getAssetsChainId());
                    LOG.debug("\tassetId: [{}]", coinFrom.getAssetsId());
                    LOG.debug("\tnonce: {}", HexUtil.encode(coinFrom.getNonce()));
                    LOG.debug("\tlocked(0普通交易，-1解锁金额交易（退出共识，退出委托)): [{}]", coinFrom.getLocked());
                    LOG.debug("");
                }
            }

            List<CoinTo> coinToList = coinData.getTo();
            if (coinToList == null) {
                LOG.debug("\tcoinTo: null");
            } else if (coinToList.size() == 0) {
                LOG.debug("\tcoinTo: size 0");
            } else {
                LOG.debug("\tcoinTo: ");
                for (int i = 0; i < coinToList.size(); i++) {
                    CoinTo coinTo = coinToList.get(i);
                    LOG.debug("\tTO_{}:", i);
                    LOG.debug("\taddress: {}", AddressTool.getStringAddressByBytes(coinTo.getAddress()));
                    LOG.debug("\tamount: {}", coinTo.getAmount());
                    LOG.debug("\tassetChainId: [{}]", coinTo.getAssetsChainId());
                    LOG.debug("\tassetId: [{}]", coinTo.getAssetsId());
                    LOG.debug("\tlocked(解锁高度或解锁时间，-1为永久锁定): [{}]", coinTo.getLockTime());
                    LOG.debug("");
                }
            }

        } else {
            LOG.debug("coinData: null");
        }
        LOG.debug("**************************************************");
        LOG.debug("");
    }
}
