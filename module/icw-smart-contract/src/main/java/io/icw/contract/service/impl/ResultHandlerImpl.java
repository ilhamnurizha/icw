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
package io.icw.contract.service.impl;

import io.icw.base.RPCUtil;
import io.icw.base.data.*;
import io.icw.base.data.*;
import io.icw.contract.helper.ContractHelper;
import io.icw.contract.helper.ContractTransferHandler;
import io.icw.contract.manager.ContractTempBalanceManager;
import io.icw.contract.model.bo.AnalyzerResult;
import io.icw.contract.model.bo.ContractBalance;
import io.icw.contract.model.bo.ContractResult;
import io.icw.contract.model.bo.ContractWrapperTransaction;
import io.icw.contract.model.tx.ContractTransferTransaction;
import io.icw.contract.model.txdata.ContractData;
import io.icw.contract.model.txdata.ContractTransferData;
import io.icw.contract.service.ContractCaller;
import io.icw.contract.service.ResultHanlder;
import io.icw.contract.util.CompareTxOrderAsc;
import io.icw.contract.util.Log;
import io.icw.contract.vm.program.ProgramExecutor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static io.icw.contract.config.ContractContext.ASSET_ID;
import static io.icw.contract.config.ContractContext.CHAIN_ID;
import static io.icw.core.constant.TxType.CALL_CONTRACT;

/**
 * @author: PierreLuo
 * @date: 2018/11/23
 */
@Component
public class ResultHandlerImpl implements ResultHanlder {

    @Autowired
    private ContractCaller contractCaller;
    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ContractTransferHandler contractTransferHandler;

    @Override
    public List<ContractResult> handleAnalyzerResult(int chainId, ProgramExecutor batchExecutor, AnalyzerResult analyzerResult, String preStateRoot) {
        try {
            BlockHeader currentBlockHeader = contractHelper.getBatchInfoCurrentBlockHeader(chainId);
            long blockTime = currentBlockHeader.getTime();
            // 得到重新执行的合约结果
            List<ContractResult> reCallResultList = this.reCall(batchExecutor, analyzerResult, chainId, preStateRoot);
            // 处理调用失败的合约，把需要退还的NULS 生成一笔合约内部转账交易，退还给调用者
            this.handleFailedContract(chainId, analyzerResult, blockTime);
            // 组装所有的合约结果
            List<ContractResult> finalResultList = new ArrayList<>();
            finalResultList.addAll(analyzerResult.getSuccessList());
            finalResultList.addAll(analyzerResult.getFailedSet());
            finalResultList.addAll(reCallResultList);
            // 按接收交易的顺序升序排序
            return finalResultList.stream().sorted(CompareTxOrderAsc.getInstance()).collect(Collectors.toList());
        } catch (IOException e) {
            Log.error(e);
            return Collections.emptyList();
        } catch (NulsException e) {
            Log.error(e);
            return Collections.emptyList();
        }
    }

    private void handleFailedContract(int chainId, AnalyzerResult analyzerResult, long blockTime) throws IOException, NulsException {
        ContractTempBalanceManager tempBalanceManager = contractHelper.getBatchInfoTempBalanceManager(chainId);
        int assetsId = contractHelper.getChain(chainId).getConfig().getAssetId();

        Set<ContractResult> failedSet = analyzerResult.getFailedSet();
        for (ContractResult contractResult : failedSet) {
            ContractWrapperTransaction orginTx = contractResult.getTx();
            if (orginTx.getType() != CALL_CONTRACT) {
                continue;
            }
            ContractData contractData = orginTx.getContractData();
            BigInteger value = contractData.getValue();
            if (value.compareTo(BigInteger.ZERO) > 0) {

                byte[] contractAddress = contractData.getContractAddress();
                ContractTransferData txData = new ContractTransferData(orginTx.getHash(), contractAddress);

                CoinData coinData = new CoinData();
                ContractBalance balance = tempBalanceManager.getBalance(contractAddress, CHAIN_ID, ASSET_ID).getData();
                byte[] nonceBytes = RPCUtil.decode(balance.getNonce());

                CoinFrom coinFrom = new CoinFrom(contractAddress, chainId, assetsId, value, nonceBytes, (byte) 0);
                coinData.getFrom().add(coinFrom);
                CoinTo coinTo = new CoinTo(contractData.getSender(), chainId, assetsId, value, 0L);
                coinData.getTo().add(coinTo);

                ContractTransferTransaction tx = new ContractTransferTransaction();
                tx.setCoinDataObj(coinData);
                tx.setTxDataObj(txData);
                // 合约内部转账交易的时间的偏移量，用于排序
                //tx.setTime(blockTime + orginTx.getOrder());
                tx.setTime(blockTime);

                tx.serializeData();
                NulsHash hash = NulsHash.calcHash(tx.serializeForHash());
                byte[] hashBytes = hash.getBytes();
                byte[] currentNonceBytes = Arrays.copyOfRange(hashBytes, hashBytes.length - 8, hashBytes.length);
                balance.setNonce(RPCUtil.encode(currentNonceBytes));
                tx.setHash(hash);
                contractResult.getContractTransferList().add(tx);
                contractResult.setMergedTransferList(contractTransferHandler.contractTransfer2mergedTransfer(orginTx, contractResult.getContractTransferList()));
                contractResult.setMergerdMultyAssetTransferList(contractTransferHandler.contractMultyAssetTransfer2mergedTransfer(orginTx, contractResult.getContractTransferList()));
            }
        }
    }

    private List<ContractResult> reCall(ProgramExecutor batchExecutor, AnalyzerResult analyzerResult, int chainId, String preStateRoot) throws NulsException {
        // 重新执行合约
        List<ContractResult> list = analyzerResult.getReCallTxList();
        List<ContractWrapperTransaction> collectTxs = list.stream().sorted(CompareTxOrderAsc.getInstance()).map(c -> c.getTx()).collect(Collectors.toList());
        List<ContractResult> resultList = contractCaller.reCallTx(batchExecutor, collectTxs, chainId, preStateRoot);
        return resultList;
    }
}
