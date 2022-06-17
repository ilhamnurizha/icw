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

package io.icw.contract.validator;

import io.icw.base.basic.AddressTool;
import io.icw.base.basic.TransactionFeeCalculator;
import io.icw.base.data.CoinData;
import io.icw.base.data.CoinFrom;
import io.icw.base.data.CoinTo;
import io.icw.base.signture.SignatureUtil;
import io.icw.contract.constant.ContractErrorCode;
import io.icw.contract.model.bo.Chain;
import io.icw.contract.model.tx.CallContractTransaction;
import io.icw.contract.model.txdata.CallContractData;
import io.icw.contract.helper.ContractHelper;
import io.icw.contract.util.ContractLedgerUtil;
import io.icw.contract.util.ContractUtil;
import io.icw.contract.util.Log;
import io.icw.core.basic.Result;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;
import io.icw.core.model.BigIntegerUtils;

import java.math.BigInteger;
import java.util.*;

import static io.icw.contract.config.ContractContext.ASSET_ID;
import static io.icw.contract.config.ContractContext.CHAIN_ID;
import static io.icw.contract.constant.ContractConstant.MININUM_TRANSFER_AMOUNT;
import static io.icw.contract.util.ContractUtil.getSuccess;

/**
 * @author: PierreLuo
 * @date: 2019-03-07
 */
@Component
public class CallContractTxValidator {

    @Autowired
    private ContractHelper contractHelper;

    public Result validate(int chainId, CallContractTransaction tx) throws NulsException {

        CoinData coinData = tx.getCoinDataInstance();
        List<CoinFrom> fromList = coinData.getFrom();
        List<CoinTo> toList = coinData.getTo();
        CallContractData txData = tx.getTxDataObj();
        byte[] sender = txData.getSender();
        boolean existSender = false;
        Chain chain = contractHelper.getChain(chainId);
        int assetsId = chain.getConfig().getAssetId();
        for(CoinFrom from : fromList) {
            if(from.getAssetsChainId() != chainId || from.getAssetsId() != assetsId) {
                Log.error("contract call error: The chain id or assets id of coin from is error.");
                return Result.getFailed(ContractErrorCode.CONTRACT_COIN_ASSETS_ERROR);
            }
            if(!existSender && Arrays.equals(from.getAddress(), sender)) {
                existSender = true;
            }
        }
        Set<String> addressSet = SignatureUtil.getAddressFromTX(tx, chainId);
        if (!existSender || !addressSet.contains(AddressTool.getStringAddressByBytes(sender))) {
            Log.error("contract call error: The contract caller is not the transaction creator.");
            return Result.getFailed(ContractErrorCode.CONTRACT_CALLER_ERROR);
        }
        if (!ContractUtil.checkPrice(txData.getPrice())) {
            Log.error("contract call error: The gas price is error.");
            return Result.getFailed(ContractErrorCode.CONTRACT_MINIMUM_PRICE_ERROR);
        }
        if (!ContractUtil.checkGasLimit(txData.getGasLimit())) {
            Log.error("contract call error: The value of gas limit ranges from 1 to 10,000,000.");
            return Result.getFailed(ContractErrorCode.CONTRACT_GAS_LIMIT_ERROR);
        }
        BigInteger transferValue = txData.getValue();
        byte[] contractAddress = txData.getContractAddress();


        if (!ContractLedgerUtil.isExistContractAddress(chainId, contractAddress)) {
            Log.error("contract call error: The contract does not exist.");
            return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST);
        }

        BigInteger contractReceivedValue = BigInteger.ZERO;
        for (CoinTo coin : toList) {
            if(coin.getAssetsChainId() != chainId || coin.getAssetsId() != assetsId) {
                Log.error("contract call error: The chain id or assets id of coin to is error.");
                return Result.getFailed(ContractErrorCode.CONTRACT_COIN_ASSETS_ERROR);
            }
            if (coin.getLockTime() != 0) {
                Log.error("contract call error: Transfer amount cannot be locked.");
                return Result.getFailed(ContractErrorCode.AMOUNT_LOCK_ERROR);
            }
            byte[] owner = coin.getAddress();
            if (addressSet.contains(AddressTool.getStringAddressByBytes(owner))) {
                continue;
            }
            if (!Arrays.equals(owner, contractAddress)) {
                Log.error("contract call error: The receiver is not the contract address.");
                return Result.getFailed(ContractErrorCode.CONTRACT_RECEIVER_ERROR);
            } else {
                contractReceivedValue = contractReceivedValue.add(coin.getAmount());
            }

            if (coin.getAmount().compareTo(MININUM_TRANSFER_AMOUNT) < 0) {
                Log.error("contract call error: The amount of the transfer is too small.");
                return Result.getFailed(ContractErrorCode.TOO_SMALL_AMOUNT);
            }
        }
        if (contractReceivedValue.compareTo(transferValue) < 0) {
            Log.error("contract call error: Insufficient balance to transfer to the contract address.");
            return Result.getFailed(ContractErrorCode.INSUFFICIENT_BALANCE_TO_CONTRACT);
        }

        BigInteger realFee = tx.getFee();
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(tx.size()).add(BigInteger.valueOf(txData.getGasLimit()).multiply(BigInteger.valueOf(txData.getPrice())));
        if (realFee.compareTo(fee) >= 0) {
            return getSuccess();
        } else {
            Log.error("contract call error: The contract transaction fee is not right.");
            return Result.getFailed(ContractErrorCode.FEE_NOT_RIGHT);
        }
    }

    public Result validateV8(int chainId, CallContractTransaction tx) throws NulsException {

        CoinData coinData = tx.getCoinDataInstance();
        List<CoinFrom> fromList = coinData.getFrom();
        List<CoinTo> toList = coinData.getTo();
        CallContractData txData = tx.getTxDataObj();
        byte[] sender = txData.getSender();

        Set<String> signatureAddressSet = SignatureUtil.getAddressFromTX(tx, chainId);
        if (!signatureAddressSet.contains(AddressTool.getStringAddressByBytes(sender))) {
            Log.error("contract call error: The contract caller is not the transaction signer.");
            return Result.getFailed(ContractErrorCode.CONTRACT_CALLER_SIGN_ERROR);
        }
        if (!ContractUtil.checkGasLimit(txData.getGasLimit())) {
            Log.error("contract call error: The value of gas limit ranges from 25 to 10,000,000.");
            return Result.getFailed(ContractErrorCode.CONTRACT_GAS_LIMIT_ERROR);
        }

        byte[] contractAddress = txData.getContractAddress();

        if (!ContractLedgerUtil.isExistContractAddress(chainId, contractAddress)) {
            Log.error("contract call error: The contract does not exist.");
            return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST);
        }

        Map<String, BigInteger> multyAssetMap = new HashMap<>();
        Set<String> multyAssetKeys = new HashSet<>();
        int assetChainId, assetId;
        String assetKey;
        BigInteger nulsValue = BigInteger.ZERO;
        for(CoinFrom from : fromList) {
            assetChainId = from.getAssetsChainId();
            assetId = from.getAssetsId();
            assetKey = assetChainId + "_" + assetId;
            if (CHAIN_ID == assetChainId && ASSET_ID == assetId) {
                nulsValue = nulsValue.add(from.getAmount());
            } else {
                multyAssetKeys.add(assetKey);
                BigInteger multyAssetValue = multyAssetMap.getOrDefault(assetKey + "from", BigInteger.ZERO);
                multyAssetMap.put(assetKey + "from", multyAssetValue.add(from.getAmount()));
            }
        }

        int toSize = toList.size();
        BigInteger transferNulsValue = txData.getValue();
        BigInteger contractReceivedNulsValue = BigInteger.ZERO;
        if (toSize > 0) {
            for (CoinTo coin : toList) {
                if (coin.getLockTime() != 0) {
                    Log.error("contract call error: Transfer amount cannot be locked.");
                    return Result.getFailed(ContractErrorCode.AMOUNT_LOCK_ERROR);
                }
                byte[] owner = coin.getAddress();
                if (!Arrays.equals(owner, contractAddress)) {
                    Log.error("contract call error: The receiver is not the contract address.");
                    return Result.getFailed(ContractErrorCode.CONTRACT_RECEIVER_ERROR);
                }
                assetChainId = coin.getAssetsChainId();
                assetId = coin.getAssetsId();
                boolean mainAsset = assetChainId == CHAIN_ID && assetId == ASSET_ID;
                if (!mainAsset) {
                    if (coin.getAmount().compareTo(BigInteger.ZERO) == 0) {
                        Log.error("contract call error: Transfer amount cannot be zero.");
                        return Result.getFailed(ContractErrorCode.TOO_SMALL_AMOUNT);
                    }
                    assetKey = assetChainId + "_" + assetId;
                    multyAssetKeys.add(assetKey);
                    BigInteger multyAssetValue = multyAssetMap.getOrDefault(assetKey + "to", BigInteger.ZERO);
                    multyAssetMap.put(assetKey + "to", multyAssetValue.add(coin.getAmount()));
                    continue;
                }
                if (coin.getAmount().compareTo(MININUM_TRANSFER_AMOUNT) < 0) {
                    Log.error("contract call error: The amount of the transfer is too small.");
                    return Result.getFailed(ContractErrorCode.TOO_SMALL_AMOUNT);
                }
                contractReceivedNulsValue = contractReceivedNulsValue.add(coin.getAmount());
            }

        }

        // 其他资产校验
        BigInteger assetKeyFrom, assetKeyTo;
        for (String multyAssetKey : multyAssetKeys) {
            assetKeyFrom = multyAssetMap.get(multyAssetKey + "from");
            assetKeyTo = multyAssetMap.get(multyAssetKey + "to");
            if(null == assetKeyFrom){
                Log.error("contract call error: Illegal coinFrom in the contract.");
                return Result.getFailed(ContractErrorCode.CONTRACT_COIN_FROM_ERROR);
            }
            if (null == assetKeyTo) {
                Log.error("contract call error: Illegal coinTo in the contract.");
                return Result.getFailed(ContractErrorCode.CONTRACT_COIN_TO_ERROR);
            }
            if (!BigIntegerUtils.isEqual(assetKeyFrom, assetKeyTo)) {
                Log.error("contract call error: The amount of coin data is error.");
                return Result.getFailed(ContractErrorCode.CONTRACT_COIN_ASSETS_ERROR);
            }
        }

        // 主资产校验
        if (contractReceivedNulsValue.compareTo(transferNulsValue) < 0) {
            Log.error("contract call error: Insufficient balance of nuls to transfer to the contract address.");
            return Result.getFailed(ContractErrorCode.INSUFFICIENT_BALANCE_TO_CONTRACT);
        }

        if (transferNulsValue.compareTo(BigInteger.ZERO) > 0) {
            // 手续费账户也能支出，向合约转资产
            if (nulsValue.compareTo(transferNulsValue) < 0) {
                Log.error("contract call error: Insufficient balance to transfer to the contract address.");
                return Result.getFailed(ContractErrorCode.INSUFFICIENT_BALANCE_TO_CONTRACT);
            }
        }

        BigInteger realFee = coinData.getFeeByAsset(CHAIN_ID, ASSET_ID);
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(tx.size()).add(BigInteger.valueOf(txData.getGasLimit()).multiply(BigInteger.valueOf(txData.getPrice())));
        if (realFee.compareTo(fee) >= 0) {
            return getSuccess();
        } else {
            Log.error("contract call error: The contract transaction fee is not right.");
            return Result.getFailed(ContractErrorCode.FEE_NOT_RIGHT);
        }
    }
}
