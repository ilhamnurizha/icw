package io.icw.contract.tx.v4;

import io.icw.base.data.BlockHeader;
import io.icw.base.data.Transaction;
import io.icw.base.protocol.TransactionProcessor;
import io.icw.contract.constant.ContractErrorCode;
import io.icw.contract.manager.ChainManager;
import io.icw.core.constant.TxType;
import io.icw.core.core.annotation.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("ContractReturnGasProcessor")
public class ContractReturnGasProcessor implements TransactionProcessor {

    @Override
    public int getType() {
        return TxType.CONTRACT_RETURN_GAS;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        ChainManager.chainHandle(chainId);
        Map<String, Object> result = new HashMap<>();
        List<Transaction> errorList = new ArrayList<>();
        String errorCode = null;
        result.put("txList", errorList);
        if(txs != null && txs.size() > 1) {
            result.put("txList", txs);
            errorCode = ContractErrorCode.DUPLICATE_CONTRACT_RETURN_GAS_TX.getCode();
        }
        result.put("errorCode", errorCode);
        return result;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader header) {
        // nothing to do
        return true;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        // nothing to do
        return true;
    }
}
