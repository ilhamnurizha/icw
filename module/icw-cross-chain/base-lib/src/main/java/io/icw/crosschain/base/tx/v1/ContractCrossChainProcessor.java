package io.icw.crosschain.base.tx.v1;

import io.icw.base.data.BlockHeader;
import io.icw.base.data.Transaction;
import io.icw.base.protocol.TransactionProcessor;
import io.icw.core.constant.TxType;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.crosschain.base.service.CrossChainService;

import java.util.List;
import java.util.Map;

/**
 * 智能合约跨链交易处理类
 * Cross chain transaction Processing Class
 *
 * @author tag
 * 2019/10/24
 */

@Component("ContractCrossChainProcessorV1")
public class ContractCrossChainProcessor implements TransactionProcessor {
    @Autowired
    private CrossChainService crossChainService;

    @Override
    public int getType() {
        return TxType.CONTRACT_TOKEN_CROSS_TRANSFER;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        return crossChainService.crossTxBatchValid(chainId, txs, txMap, blockHeader);
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return crossChainService.commitCrossTx(chainId, txs, blockHeader);
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return crossChainService.rollbackCrossTx(chainId, txs, blockHeader);
    }
}
