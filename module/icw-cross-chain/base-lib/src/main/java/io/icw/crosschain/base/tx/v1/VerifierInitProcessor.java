package io.icw.crosschain.base.tx.v1;

import io.icw.base.data.BlockHeader;
import io.icw.base.data.Transaction;
import io.icw.base.protocol.TransactionProcessor;
import io.icw.core.constant.TxType;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.crosschain.base.service.VerifierInitService;

import java.util.List;
import java.util.Map;

/**
 * 验证人初始化处理类
 * Verifier Initialization Processing Class
 *
 * @author tag
 * 2019/8/7
 */
@Component("VerifierInitProcessorV1")
public class VerifierInitProcessor implements TransactionProcessor {
    @Autowired
    private VerifierInitService verifierInitService;

    @Override
    public int getType() {
        return TxType.VERIFIER_INIT;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        return verifierInitService.validate(chainId, txs, txMap, blockHeader);
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return verifierInitService.commit(chainId, txs, blockHeader);
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return verifierInitService.rollback(chainId, txs, blockHeader);
    }
}
