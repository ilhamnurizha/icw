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
package io.icw.contract.callable;

import io.icw.base.RPCUtil;
import io.icw.base.data.BlockHeader;
import io.icw.base.data.Transaction;
import io.icw.contract.model.dto.ContractPackageDto;
import io.icw.contract.model.tx.ContractReturnGasTransaction;
import io.icw.contract.model.tx.ContractTransferTransaction;
import io.icw.contract.vm.program.ProgramExecutor;
import io.icw.contract.vm.program.ProgramInvokeRegisterCmd;
import io.icw.contract.vm.program.ProgramNewTx;
import io.icw.base.data.*;
import io.icw.contract.enums.CmdRegisterMode;
import io.icw.contract.helper.ContractHelper;
import io.icw.contract.manager.ChainManager;
import io.icw.contract.model.bo.*;
import io.icw.contract.service.ResultAnalyzer;
import io.icw.contract.service.ResultHanlder;
import io.icw.contract.util.Log;
import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.model.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;


/**
 * @author: PierreLuo
 * @date: 2019-03-26
 */
public class ContractBatchEndCallable implements Callable<ContractPackageDto> {

    private int chainId;
    private int blockType;
    private long blockHeight;
    private ContractHelper contractHelper;
    private ResultAnalyzer resultAnalyzer;
    private ResultHanlder resultHanlder;

    public ContractBatchEndCallable(int chainId, int blockType, long blockHeight) {
        this.chainId = chainId;
        this.blockType = blockType;
        this.blockHeight = blockHeight;
        this.contractHelper = SpringLiteContext.getBean(ContractHelper.class);
        this.resultAnalyzer = SpringLiteContext.getBean(ResultAnalyzer.class);
        this.resultHanlder = SpringLiteContext.getBean(ResultHanlder.class);
    }

    @Override
    public ContractPackageDto call() {
        try {
            ChainManager.chainHandle(chainId, blockType);
            BatchInfo batchInfo = contractHelper.getChain(chainId).getBatchInfo();
            BlockHeader currentBlockHeader = batchInfo.getCurrentBlockHeader();
            long blockTime = currentBlockHeader.getTime();

            LinkedHashMap<String, ContractContainer> contractContainerMap = batchInfo.getContractContainerMap();
            Collection<ContractContainer> containerList = contractContainerMap.values();
            CallerResult callerResult = new CallerResult();
            List<CallableResult> resultList = callerResult.getCallableResultList();
            for (ContractContainer container : containerList) {
                container.loadFutureList();
                resultList.add(container.getCallableResult());
            }

            ProgramExecutor batchExecutor = batchInfo.getBatchExecutor();
            String preStateRoot = batchInfo.getPreStateRoot();
            // 合约执行结果归类
            AnalyzerResult analyzerResult = resultAnalyzer.analysis(callerResult.getCallableResultList());
            // 重新执行冲突合约，处理失败合约的金额退还
            List<ContractResult> contractResultList = resultHanlder.handleAnalyzerResult(chainId, batchExecutor, analyzerResult, preStateRoot);
            // 归集[外部模块调用生成的交易]和[合约内部转账交易]
            List<byte[]> offlineTxHashList = new ArrayList<>();
            List<String> resultTxList = new ArrayList<>();
            List<String> resultOrginTxList = new ArrayList<>();
            List<ContractTransferTransaction> contractTransferList;
            List<ProgramInvokeRegisterCmd> invokeRegisterCmds;
            String newTx, newTxHash, orginTxHash;
            ProgramNewTx programNewTx;
            for (ContractResult contractResult : contractResultList) {
                //if (Log.isDebugEnabled()) {
                //    Log.debug("ContractResult Address is {}, Order is {}", AddressTool.getStringAddressByBytes(contractResult.getContractAddress()), contractResult.getTxOrder());
                //}
                orginTxHash = contractResult.getHash();
                // [外部模块调用生成的交易]
                invokeRegisterCmds = contractResult.getInvokeRegisterCmds();
                for (ProgramInvokeRegisterCmd invokeRegisterCmd : invokeRegisterCmds) {
                    if (!invokeRegisterCmd.getCmdRegisterMode().equals(CmdRegisterMode.NEW_TX)) {
                        continue;
                    }
                    programNewTx = invokeRegisterCmd.getProgramNewTx();
                    if (StringUtils.isNotBlank(newTxHash = programNewTx.getTxHash())) {
                        offlineTxHashList.add(RPCUtil.decode(newTxHash));
                    }
                    if (StringUtils.isNotBlank(newTx = programNewTx.getTxString())) {
                        resultTxList.add(newTx);
                        resultOrginTxList.add(orginTxHash);
                    }
                }
                // [合约内部转账交易]
                contractTransferList = contractResult.getContractTransferList();
                for(Transaction tx : contractTransferList) {
                    newTx = RPCUtil.encode(tx.serialize());
                    contractResult.getContractTransferTxStringList().add(newTx);
                    resultTxList.add(newTx);
                    resultOrginTxList.add(orginTxHash);
                    offlineTxHashList.add(tx.getHash().getBytes());
                }
            }
            // 生成退还剩余Gas的交易
            ContractReturnGasTransaction contractReturnGasTx = contractHelper.makeReturnGasTx(contractResultList, blockTime);
            if (contractReturnGasTx != null) {
                resultTxList.add(RPCUtil.encode(contractReturnGasTx.serialize()));
            }

            ContractPackageDto dto = new ContractPackageDto(offlineTxHashList, resultTxList, resultOrginTxList);
            dto.makeContractResultMap(contractResultList);
            batchInfo.setContractPackageDto(dto);

            Log.info("[Before End Contract Execution Cost Time] BlockHeight is {}, Total Cost Time is {}", currentBlockHeader.getHeight(), System.currentTimeMillis() - batchInfo.getBeginTime());
            return dto;
        } catch (IOException e) {
            Log.error("",e);
            return null;
        } catch (InterruptedException e) {
            Log.error("", e);
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            Log.error("", e);
            return null;
        }
    }

}
