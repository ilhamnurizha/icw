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
package io.icw.contract.processor;


import io.icw.base.basic.AddressTool;
import io.icw.base.data.BlockHeader;
import io.icw.contract.model.bo.ContractResult;
import io.icw.contract.model.bo.ContractWrapperTransaction;
import io.icw.contract.model.dto.CallContractDataDto;
import io.icw.contract.model.dto.ContractResultDto;
import io.icw.contract.model.po.ContractAddressInfoPo;
import io.icw.contract.model.po.ContractTokenTransferInfoPo;
import io.icw.contract.model.txdata.CallContractData;
import io.icw.contract.model.txdata.ContractData;
import io.icw.contract.vm.program.ProgramStatus;
import io.icw.contract.helper.ContractHelper;
import io.icw.contract.service.ContractService;
import io.icw.contract.storage.ContractTokenTransferStorageService;
import io.icw.contract.util.ContractUtil;
import io.icw.contract.util.Log;
import io.icw.core.basic.Result;
import io.icw.core.basic.VarInt;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.parse.JSONUtils;
import org.bouncycastle.util.Arrays;

import static io.icw.contract.util.ContractUtil.getFailed;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/8
 */
@Component
public class CallContractTxProcessor {

    @Autowired
    private ContractHelper contractHelper;

    @Autowired
    private ContractTokenTransferStorageService contractTokenTransferStorageService;

    @Autowired
    private ContractService contractService;

    public Result onCommit(int chainId, ContractWrapperTransaction tx) {
        try {
            BlockHeader blockHeader = contractHelper.getBatchInfoCurrentBlockHeader(chainId);
            byte[] stateRoot = blockHeader.getStateRoot();
            long blockHeight = blockHeader.getHeight();
            ContractResult contractResult = tx.getContractResult();
            contractResult.setBlockHeight(blockHeight);

            // 保存代币交易
            ContractData callContractData = tx.getContractData();
            byte[] contractAddress = callContractData.getContractAddress();

            Result<ContractAddressInfoPo> contractAddressInfoPoResult = contractHelper.getContractAddressInfo(chainId, contractAddress);
            ContractAddressInfoPo contractAddressInfoPo = contractAddressInfoPoResult.getData();
            contractResult.setNrc20(contractAddressInfoPo.isNrc20());
            tx.setBlockHeight(blockHeight);
            // 获取合约当前状态
            ProgramStatus status = contractHelper.getContractStatus(chainId, stateRoot, contractAddress);
            boolean isTerminatedContract = ContractUtil.isTerminatedContract(status.ordinal());

            // 处理合约执行失败 - 没有transferEvent的情况, 直接从数据库中获取, 若是本地创建的交易，获取到修改为失败交易
            if (isTerminatedContract || !contractResult.isSuccess()) {
                if (contractAddressInfoPo != null && contractAddressInfoPo.isNrc20() && ContractUtil.isTransferMethod(callContractData.getMethodName())) {
                    byte[] txHashBytes = tx.getHash().getBytes();
                    byte[] infoKey = Arrays.concatenate(callContractData.getSender(), txHashBytes, new VarInt(0).encode());
                    Result<ContractTokenTransferInfoPo> infoResult = contractTokenTransferStorageService.getTokenTransferInfo(chainId, infoKey);
                    ContractTokenTransferInfoPo po = infoResult.getData();
                    if (po != null) {
                        po.setStatus((byte) 2);
                        contractTokenTransferStorageService.saveTokenTransferInfo(chainId, infoKey, po);

                        // 刷新token余额
                        if (isTerminatedContract) {
                            // 终止的合约，回滚token余额
                            contractHelper.rollbackContractToken(chainId, po);
                            contractResult.setError(true);
                            contractResult.setErrorMessage("this contract has been terminated");
                        } else {

                            if (po.getFrom() != null) {
                                contractHelper.refreshTokenBalance(chainId, stateRoot, blockHeight, contractAddressInfoPo, AddressTool.getStringAddressByBytes(po.getFrom()), po.getContractAddress());
                            }
                            if (po.getTo() != null) {
                                contractHelper.refreshTokenBalance(chainId, stateRoot, blockHeight, contractAddressInfoPo, AddressTool.getStringAddressByBytes(po.getTo()), po.getContractAddress());
                            }
                        }
                    }
                }
            }

            if (!isTerminatedContract) {
                // 处理合约事件
                contractHelper.dealNrc20Events(chainId, stateRoot, tx, contractResult, contractAddressInfoPo);
            }

            // 保存合约执行结果
            return contractService.saveContractExecuteResult(chainId, tx.getHash(), contractResult);
        } catch (Exception e) {
            Log.error("save call contract tx error.", e);
            return getFailed();
        }
    }

    public Result onRollback(int chainId, ContractWrapperTransaction tx) {
        try {
            // 回滚代币转账交易
            ContractResult contractResult = tx.getContractResult();
            if (contractResult == null) {
                contractResult = contractService.getContractExecuteResult(chainId, tx.getHash());
            }
            if (contractResult == null) {
                return ContractUtil.getSuccess();
            }
            try {
                CallContractData contractData = (CallContractData) tx.getContractData();
                Log.info("rollback call tx, contract data is {}, result is {}", JSONUtils.obj2json(new CallContractDataDto(contractData)), JSONUtils.obj2json(new ContractResultDto(chainId, contractResult, contractData.getGasLimit())));
            } catch (Exception e) {
                Log.warn("failed to trace call rollback log, error is {}", e.getMessage());
            }
            contractHelper.rollbackNrc20Events(chainId, tx, contractResult);
            // 删除合约执行结果
            return contractService.deleteContractExecuteResult(chainId, tx.getHash());
        } catch (Exception e) {
            Log.error("rollback call contract tx error.", e);
            return getFailed();
        }
    }

    public Result onCommitV8(int chainId, ContractWrapperTransaction tx) {
        try {
            BlockHeader blockHeader = contractHelper.getBatchInfoCurrentBlockHeaderV8(chainId);
            byte[] stateRoot = blockHeader.getStateRoot();
            long blockHeight = blockHeader.getHeight();
            ContractResult contractResult = tx.getContractResult();
            contractResult.setBlockHeight(blockHeight);

            // 保存代币交易
            ContractData callContractData = tx.getContractData();
            byte[] contractAddress = callContractData.getContractAddress();

            Result<ContractAddressInfoPo> contractAddressInfoPoResult = contractHelper.getContractAddressInfo(chainId, contractAddress);
            ContractAddressInfoPo contractAddressInfoPo = contractAddressInfoPoResult.getData();
            contractResult.setNrc20(contractAddressInfoPo.isNrc20());
            tx.setBlockHeight(blockHeight);
            // 获取合约当前状态
            ProgramStatus status = contractHelper.getContractStatus(chainId, stateRoot, contractAddress);
            boolean isTerminatedContract = ContractUtil.isTerminatedContract(status.ordinal());

            // 处理合约执行失败 - 没有transferEvent的情况, 直接从数据库中获取, 若是本地创建的交易，获取到修改为失败交易
            if (isTerminatedContract || !contractResult.isSuccess()) {
                if (contractAddressInfoPo != null && contractAddressInfoPo.isNrc20() && ContractUtil.isTransferMethod(callContractData.getMethodName())) {
                    byte[] txHashBytes = tx.getHash().getBytes();
                    byte[] infoKey = Arrays.concatenate(callContractData.getSender(), txHashBytes, new VarInt(0).encode());
                    Result<ContractTokenTransferInfoPo> infoResult = contractTokenTransferStorageService.getTokenTransferInfo(chainId, infoKey);
                    ContractTokenTransferInfoPo po = infoResult.getData();
                    if (po != null) {
                        po.setStatus((byte) 2);
                        contractTokenTransferStorageService.saveTokenTransferInfo(chainId, infoKey, po);

                        // 刷新token余额
                        if (isTerminatedContract) {
                            // 终止的合约，回滚token余额
                            contractHelper.rollbackContractToken(chainId, po);
                            contractResult.setError(true);
                            contractResult.setErrorMessage("this contract has been terminated");
                        } else {

                            if (po.getFrom() != null) {
                                contractHelper.refreshTokenBalance(chainId, stateRoot, blockHeight, contractAddressInfoPo, AddressTool.getStringAddressByBytes(po.getFrom()), po.getContractAddress());
                            }
                            if (po.getTo() != null) {
                                contractHelper.refreshTokenBalance(chainId, stateRoot, blockHeight, contractAddressInfoPo, AddressTool.getStringAddressByBytes(po.getTo()), po.getContractAddress());
                            }
                        }
                    }
                }
            }

            if (!isTerminatedContract) {
                // 处理合约事件
                contractHelper.dealNrc20Events(chainId, stateRoot, tx, contractResult, contractAddressInfoPo);
            }

            // 保存合约执行结果
            return contractService.saveContractExecuteResult(chainId, tx.getHash(), contractResult);
        } catch (Exception e) {
            Log.error("save call contract tx error.", e);
            return getFailed();
        }
    }

    public Result onRollbackV8(int chainId, ContractWrapperTransaction tx) {
        try {
            // 回滚代币转账交易
            ContractResult contractResult = tx.getContractResult();
            if (contractResult == null) {
                contractResult = contractService.getContractExecuteResult(chainId, tx.getHash());
            }
            if (contractResult == null) {
                return ContractUtil.getSuccess();
            }
            try {
                CallContractData contractData = (CallContractData) tx.getContractData();
                Log.info("rollback call tx, contract data is {}, result is {}", JSONUtils.obj2json(new CallContractDataDto(contractData)), JSONUtils.obj2json(new ContractResultDto(chainId, contractResult, contractData.getGasLimit())));
            } catch (Exception e) {
                Log.warn("failed to trace call rollback log, error is {}", e.getMessage());
            }
            contractHelper.rollbackNrc20Events(chainId, tx, contractResult);
            // 删除合约执行结果
            return contractService.deleteContractExecuteResult(chainId, tx.getHash());
        } catch (Exception e) {
            Log.error("rollback call contract tx error.", e);
            return getFailed();
        }
    }


}
