package io.icw.contract.tx.v1;

import io.icw.base.data.BlockHeader;
import io.icw.base.data.Transaction;
import io.icw.base.protocol.ProtocolGroupManager;
import io.icw.base.protocol.TransactionProcessor;
import io.icw.contract.model.bo.ContractResult;
import io.icw.contract.model.bo.ContractWrapperTransaction;
import io.icw.contract.model.dto.ContractPackageDto;
import io.icw.contract.model.tx.CallContractTransaction;
import io.icw.contract.model.txdata.CallContractData;
import io.icw.contract.config.ContractContext;
import io.icw.contract.helper.ContractHelper;
import io.icw.contract.manager.ChainManager;
import io.icw.contract.processor.CallContractTxProcessor;
import io.icw.contract.util.ContractUtil;
import io.icw.contract.util.Log;
import io.icw.contract.validator.CallContractTxValidator;
import io.icw.core.basic.Result;
import io.icw.core.constant.TxType;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("CallContractProcessor")
public class CallContractProcessor implements TransactionProcessor {

    @Autowired
    private CallContractTxProcessor callContractTxProcessor;
    @Autowired
    private CallContractTxValidator callContractTxValidator;
    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ChainManager chainManager;

    @Override
    public int getType() {
        return TxType.CALL_CONTRACT;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        ChainManager.chainHandle(chainId);
        Map<String, Object> result = new HashMap<>();
        List<Transaction> errorList = new ArrayList<>();
        result.put("txList", errorList);
        String errorCode = null;
        CallContractTransaction callTx;
        for(Transaction tx : txs) {
            callTx = new CallContractTransaction();
            callTx.copyTx(tx);
            try {
                Result validate = callContractTxValidator.validate(chainId, callTx);
                if(validate.isFailed()) {
                    errorCode = validate.getErrorCode().getCode();
                    errorList.add(tx);
                }
            } catch (NulsException e) {
                Log.error(e);
                errorCode = e.getErrorCode().getCode();
                errorList.add(tx);
            }
        }
        result.put("errorCode", errorCode);
        return result;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader header) {
        try {
            ContractPackageDto contractPackageDto = contractHelper.getChain(chainId).getBatchInfo().getContractPackageDto();
            if (contractPackageDto != null) {
                Map<String, ContractResult> contractResultMap = contractPackageDto.getContractResultMap();
                ContractResult contractResult;
                ContractWrapperTransaction wrapperTx;
                String txHash;
                for (Transaction tx : txs) {
                    txHash = tx.getHash().toString();
                    contractResult = contractResultMap.get(txHash);
                    if (contractResult == null) {
                        Log.warn("empty contract result with txHash: {}, txType: {}", txHash, tx.getType());
                        continue;
                    }
                    wrapperTx = contractResult.getTx();
                    wrapperTx.setContractResult(contractResult);
                    callContractTxProcessor.onCommit(chainId, wrapperTx);
                }
            }

            return true;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        try {
            ChainManager.chainHandle(chainId);
            CallContractData call;
            for (Transaction tx : txs) {
                if (tx.getType() == TxType.CROSS_CHAIN) {
                    // add by pierre at 2019-12-01 处理type10交易的业务回滚, 需要协议升级 done
                    if(ProtocolGroupManager.getCurrentVersion(chainId) < ContractContext.UPDATE_VERSION_V250) {
                        continue;
                    }
                    call = ContractUtil.parseCrossChainTx(tx, chainManager);
                    if (call == null) {
                        continue;
                    }
                } else {
                    call = new CallContractData();
                    call.parse(tx.getTxData(), 0);
                }
                callContractTxProcessor.onRollback(chainId, new ContractWrapperTransaction(tx, call));
            }
            return true;
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
    }
}
