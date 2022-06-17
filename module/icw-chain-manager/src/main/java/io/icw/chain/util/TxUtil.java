/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.icw.chain.util;

import io.icw.base.RPCUtil;
import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.data.CoinData;
import io.icw.base.data.CoinFrom;
import io.icw.base.data.CoinTo;
import io.icw.base.data.Transaction;
import io.icw.chain.model.po.Asset;
import io.icw.chain.model.po.BlockChain;
import io.icw.chain.model.tx.txdatav5.TxAsset;
import io.icw.chain.model.tx.txdatav5.TxChain;
import io.icw.chain.info.CmConstants;
import io.icw.core.crypto.HexUtil;
import io.icw.core.exception.NulsException;
import io.icw.core.log.Log;
import io.icw.core.model.ArraysTool;
import io.icw.core.model.StringUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author lan
 * @description
 * @date 2019/02/20
 **/
public class TxUtil {
    public static io.icw.chain.model.tx.txdata.TxAsset parseAssetToTx(Asset asset) throws IOException {
        io.icw.chain.model.tx.txdata.TxAsset txAsset = new io.icw.chain.model.tx.txdata.TxAsset();
        txAsset.setAddress(asset.getAddress());
        txAsset.setAssetId(asset.getAssetId());
        txAsset.setChainId(asset.getChainId());
        txAsset.setDecimalPlaces(asset.getDecimalPlaces());
        txAsset.setDepositNuls(asset.getDepositNuls());
        txAsset.setDestroyNuls(asset.getDestroyNuls());
        txAsset.setInitNumber(asset.getInitNumber());
        txAsset.setName(asset.getAssetName());
        txAsset.setSymbol(asset.getSymbol());
        return txAsset;
    }

    public static io.icw.chain.model.tx.txdata.TxChain parseChainToTx(BlockChain blockChain, Asset asset) throws IOException {
        io.icw.chain.model.tx.txdata.TxChain txChain = new io.icw.chain.model.tx.txdata.TxChain();
        txChain.setAddressType(blockChain.getAddressType());
        txChain.setAddressPrefix(blockChain.getAddressPrefix());
        txChain.getDefaultAsset().setChainId(blockChain.getChainId());
        txChain.setMagicNumber(blockChain.getMagicNumber());
        txChain.setMinAvailableNodeNum(blockChain.getMinAvailableNodeNum());
        txChain.setName(blockChain.getChainName());
        txChain.setSupportInflowAsset(blockChain.isSupportInflowAsset());
        txChain.setVerifierList(blockChain.getVerifierList());
        txChain.setMaxSignatureCount(blockChain.getMaxSignatureCount());
        txChain.setSignatureByzantineRatio(blockChain.getSignatureByzantineRatio());
        txChain.getDefaultAsset().setAddress(asset.getAddress());
        txChain.getDefaultAsset().setAssetId(asset.getAssetId());
        txChain.getDefaultAsset().setSymbol(asset.getSymbol());
        txChain.getDefaultAsset().setName(asset.getAssetName());
        txChain.getDefaultAsset().setDepositNuls(asset.getDepositNuls());
        txChain.getDefaultAsset().setInitNumber(asset.getInitNumber());
        txChain.getDefaultAsset().setDestroyNuls(asset.getDestroyNuls());
        txChain.getDefaultAsset().setDecimalPlaces(asset.getDecimalPlaces());
        return txChain;
    }

    public static TxAsset parseAssetToTxV5(Asset asset) throws IOException {
        TxAsset txAsset = new TxAsset();
        txAsset.setAssetId(asset.getAssetId());
        txAsset.setChainId(asset.getChainId());
        txAsset.setDecimalPlaces(asset.getDecimalPlaces());
        txAsset.setInitNumber(asset.getInitNumber());
        txAsset.setName(asset.getAssetName());
        txAsset.setSymbol(asset.getSymbol());
        return txAsset;
    }

    public static TxChain parseChainToTxV5(BlockChain blockChain, Asset asset) throws IOException {
        TxChain txChain = new TxChain();
        txChain.setAddressType(Short.valueOf(blockChain.getAddressType()));
        txChain.setAddressPrefix(blockChain.getAddressPrefix());
        txChain.getDefaultAsset().setChainId(blockChain.getChainId());
        txChain.setMagicNumber(blockChain.getMagicNumber());
        txChain.setMinAvailableNodeNum(blockChain.getMinAvailableNodeNum());
        txChain.setName(blockChain.getChainName());
        txChain.setSupportInflowAsset(blockChain.isSupportInflowAsset());
        txChain.setVerifierList(blockChain.getVerifierList());
        txChain.setMaxSignatureCount(blockChain.getMaxSignatureCount());
        txChain.setSignatureByzantineRatio(blockChain.getSignatureByzantineRatio());
        txChain.getDefaultAsset().setAssetId(asset.getAssetId());
        txChain.getDefaultAsset().setSymbol(asset.getSymbol());
        txChain.getDefaultAsset().setName(asset.getAssetName());
        txChain.getDefaultAsset().setInitNumber(asset.getInitNumber());
        txChain.getDefaultAsset().setDecimalPlaces(asset.getDecimalPlaces());
        return txChain;
    }

    public static void fillAssetByTxAsset(Asset asset, io.icw.chain.model.tx.txdata.TxAsset txAsset) {
        asset.setAddress(txAsset.getAddress());
        asset.setAssetId(txAsset.getAssetId());
        asset.setChainId(txAsset.getChainId());
        asset.setDecimalPlaces(txAsset.getDecimalPlaces());
        asset.setDepositNuls(txAsset.getDepositNuls());
        asset.setDestroyNuls(txAsset.getDestroyNuls());
        asset.setInitNumber(txAsset.getInitNumber());
        asset.setSymbol(txAsset.getSymbol());
        asset.setAssetName(txAsset.getName());
    }

    public static void fillAssetByTxAssetV5(Asset asset, TxAsset txAsset, Transaction tx) throws NulsException {
        asset.setAssetId(txAsset.getAssetId());
        asset.setChainId(txAsset.getChainId());
        asset.setDecimalPlaces(txAsset.getDecimalPlaces());
        asset.setInitNumber(txAsset.getInitNumber());
        asset.setSymbol(txAsset.getSymbol());
        asset.setAssetName(txAsset.getName());
        byte[] stream = tx.getCoinData();
        CoinData coinData = new CoinData();
        coinData.parse(new NulsByteBuffer(stream));
        List<CoinTo> coinTos = coinData.getTo();
        List<CoinFrom> coinFroms = coinData.getFrom();
        byte[] fromAddress = null;
        BigInteger lockedNuls = BigInteger.ZERO;
        BigInteger destroyNuls = BigInteger.ZERO;
        if (coinTos.size() > 0) {
            for (CoinTo coinTo : coinTos) {
                byte[] toAddress = coinTo.getAddress();
                if (ArraysTool.arrayEquals(toAddress, CmConstants.BLACK_HOLE_ADDRESS)) {
                    destroyNuls = coinTo.getAmount();
                } else if (-1 == coinTo.getLockTime()) {
                    //永久锁定值
                    lockedNuls = coinTo.getAmount();
                }
            }
        } else {
            throw new RuntimeException();
        }
        if (coinFroms.size() > 0) {
            fromAddress = coinFroms.get(0).getAddress();
        } else {
            throw new RuntimeException();
        }
        asset.setAddress(fromAddress);
        asset.setDepositNuls(lockedNuls.add(destroyNuls));
        asset.setDestroyNuls(destroyNuls);
        asset.setTxHash(tx.getHash().toHex());
        asset.setCreateTime(tx.getTime());
    }

    public static void fillBlockChainByTxChain(BlockChain blockChain, io.icw.chain.model.tx.txdata.TxChain txChain) {
        blockChain.setAddressType(txChain.getAddressType());
        blockChain.setAddressPrefix(txChain.getAddressPrefix());
        blockChain.setChainId(txChain.getDefaultAsset().getChainId());
        blockChain.setMagicNumber(txChain.getMagicNumber());
        blockChain.setMinAvailableNodeNum(txChain.getMinAvailableNodeNum());
        blockChain.setChainName(txChain.getName());
        blockChain.setSupportInflowAsset(txChain.isSupportInflowAsset());
        blockChain.setSignatureByzantineRatio(txChain.getSignatureByzantineRatio());
        blockChain.setVerifierList(txChain.getVerifierList());
        blockChain.setMaxSignatureCount(txChain.getMaxSignatureCount());
    }

    public static void fillBlockChainByTxChainV5(BlockChain blockChain, TxChain txChain) {
        blockChain.setAddressType(String.valueOf(txChain.getAddressType()));
        blockChain.setAddressPrefix(txChain.getAddressPrefix());
        blockChain.setChainId(txChain.getDefaultAsset().getChainId());
        blockChain.setMagicNumber(txChain.getMagicNumber());
        blockChain.setMinAvailableNodeNum(txChain.getMinAvailableNodeNum());
        blockChain.setChainName(txChain.getName());
        blockChain.setSupportInflowAsset(txChain.isSupportInflowAsset());
        blockChain.setSignatureByzantineRatio(txChain.getSignatureByzantineRatio());
        blockChain.setVerifierList(txChain.getVerifierList());
        blockChain.setMaxSignatureCount(txChain.getMaxSignatureCount());
    }

    public static Asset buildAssetWithTxChain(Transaction tx) {
        try {
            io.icw.chain.model.tx.txdata.TxChain txChain = new io.icw.chain.model.tx.txdata.TxChain();
            txChain.parse(tx.getTxData(), 0);
            Asset asset = new Asset();
            io.icw.chain.model.tx.txdata.TxAsset txAsset = txChain.getDefaultAsset();
            fillAssetByTxAsset(asset, txAsset);
            asset.setTxHash(tx.getHash().toHex());
            asset.setCreateTime(tx.getTime());
            return asset;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }


    public static Asset buildAssetWithTxAsset(Transaction tx) {
        try {
            io.icw.chain.model.tx.txdata.TxAsset txAsset = new io.icw.chain.model.tx.txdata.TxAsset();
            txAsset.parse(tx.getTxData(), 0);
            Asset asset = new Asset();
            fillAssetByTxAsset(asset, txAsset);
            asset.setTxHash(tx.getHash().toHex());
            asset.setCreateTime(tx.getTime());
            return asset;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    public static BlockChain buildChainWithTxData(Transaction tx, boolean isDelete) {
        try {
            io.icw.chain.model.tx.txdata.TxChain txChain = new io.icw.chain.model.tx.txdata.TxChain();
            txChain.parse(tx.getTxData(), 0);
            BlockChain blockChain = new BlockChain();
            fillBlockChainByTxChain(blockChain, txChain);
            if (isDelete) {
                blockChain.setDelTxHash(tx.getHash().toHex());
                blockChain.setDelAddress(txChain.getDefaultAsset().getAddress());
                blockChain.setDelAssetId(txChain.getDefaultAsset().getAssetId());
            } else {
                blockChain.setRegTxHash(tx.getHash().toHex());
                blockChain.setRegAddress(txChain.getDefaultAsset().getAddress());
                blockChain.setRegAssetId(txChain.getDefaultAsset().getAssetId());
            }
            blockChain.setCreateTime(tx.getTime());
            return blockChain;
        } catch (Exception e) {
            LoggerUtil.logger().error("buildChainWithTxData error:{}", e);
            return null;
        }
    }

    public static Asset buildAssetWithTxChainV4(Transaction tx) {
        try {
            Asset asset = new Asset();
            TxChain txChain = new TxChain();
            txChain.parse(tx.getTxData(), 0);
            TxAsset txAsset = txChain.getDefaultAsset();
            fillAssetByTxAssetV5(asset, txAsset, tx);
            return asset;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    public static Asset buildAssetWithTxAssetV5(Transaction tx) {
        try {
            TxAsset txAsset = new TxAsset();
            txAsset.parse(tx.getTxData(), 0);
            Asset asset = new Asset();
            fillAssetByTxAssetV5(asset, txAsset, tx);
            return asset;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    public static BlockChain buildChainWithTxDataV4(Transaction tx, boolean isDelete) {
        try {
            TxChain txChain = new TxChain();
            txChain.parse(tx.getTxData(), 0);
            BlockChain blockChain = new BlockChain();
            fillBlockChainByTxChainV5(blockChain, txChain);
            byte[] stream = tx.getCoinData();
            CoinData coinData = new CoinData();
            coinData.parse(new NulsByteBuffer(stream));
            List<CoinFrom> coinFroms = coinData.getFrom();
            byte[] fromAddress = null;
            if (coinFroms.size() == 1) {
                fromAddress = coinFroms.get(0).getAddress();
            } else {
                throw new RuntimeException();
            }
            if (isDelete) {
                blockChain.setDelTxHash(tx.getHash().toHex());
                blockChain.setDelAddress(fromAddress);
                blockChain.setDelAssetId(txChain.getDefaultAsset().getAssetId());
            } else {
                blockChain.setRegTxHash(tx.getHash().toHex());
                blockChain.setRegAddress(fromAddress);
                blockChain.setRegAssetId(txChain.getDefaultAsset().getAssetId());
            }
            blockChain.setCreateTime(tx.getTime());
            return blockChain;
        } catch (Exception e) {
            LoggerUtil.logger().error("buildChainWithTxData error:{}", e);
            return null;
        }
    }

    public static byte[] getNonceByTxHash(String txHash) {
        byte[] out = new byte[8];
        byte[] in = HexUtil.decode(txHash);
        int copyEnd = in.length;
        System.arraycopy(in, (copyEnd - 8), out, 0, 8);
        return out;
    }

    public static Transaction buildTxData(String txHex) {
        try {
            if (StringUtils.isBlank(txHex)) {
                return null;
            }
            byte[] txStream = RPCUtil.decode(txHex);
            Transaction tx = new Transaction();
            tx.parse(new NulsByteBuffer(txStream));
            return tx;
        } catch (Exception e) {
            LoggerUtil.logger().error("transaction parse error:{}", e);
            return null;
        }
    }

    public static List<String> moveRepeatInfo(List<String> list) {
        Set<String> set = new HashSet<String>();
        for (String s : list) {
            set.add(s);
        }
        list.clear();
        list.addAll(set);
        return list;
    }

}
