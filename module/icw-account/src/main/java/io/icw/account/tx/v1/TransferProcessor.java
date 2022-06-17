package io.icw.account.tx.v1;

import io.icw.account.model.bo.Chain;
import io.icw.account.util.manager.ChainManager;
import io.icw.account.constant.AccountConstant;
import io.icw.account.constant.AccountErrorCode;
import io.icw.account.service.TransactionService;
import io.icw.account.util.LoggerUtil;
import io.icw.base.data.BlockHeader;
import io.icw.base.data.Transaction;
import io.icw.base.protocol.TransactionProcessor;
import io.icw.core.basic.Result;
import io.icw.core.constant.TxType;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("TransferProcessorV1")
public class TransferProcessor implements TransactionProcessor {
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TransactionService transactionService;
    @Override
    public int getType() {
        return TxType.TRANSFER;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Map<String, Object> result = null;
        Chain chain = null;
        try {
            chain = chainManager.getChain(chainId);
            result = new HashMap<>(AccountConstant.INIT_CAPACITY_4);
            String errorCode = null;
            if (chain == null) {
                errorCode = AccountErrorCode.CHAIN_NOT_EXIST.getCode();
                chain.getLogger().error("chain is not exist, -chainId:{}", chainId);
                result.put("txList", txs);
                result.put("errorCode", errorCode);
                return result;
            }
            List<Transaction> txList = new ArrayList<>();
            for (Transaction tx : txs) {
                try {
                    Result rs =  transactionService.transferTxValidate(chain, tx);
                    if (rs.isFailed()) {
                        errorCode = rs.getErrorCode().getCode();
                        txList.add(tx);
                    }
                } catch (NulsException e) {
                    chain.getLogger().error(e);
                    errorCode = e.getErrorCode().getCode();
                    txList.add(tx);
                }
            }
            result.put("txList", txList);
            result.put("errorCode", errorCode);
        } catch (Exception e) {
            errorLogProcess(chain, e);
            result.put("txList", txs);
            result.put("errorCode", AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return result;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return true;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return true;
    }


    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LoggerUtil.LOG.error(e);
        } else {
            chain.getLogger().error(e);
        }
    }
}
