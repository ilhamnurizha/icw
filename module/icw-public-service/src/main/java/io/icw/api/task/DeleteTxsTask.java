package io.icw.api.task;

import io.icw.api.db.mongo.MongoTransactionServiceImpl;
import io.icw.api.db.TransactionService;
import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.log.Log;

public class DeleteTxsTask implements Runnable {

    private int chainId;

    private TransactionService transactionService;

    public DeleteTxsTask(int chainId) {
        this.chainId = chainId;
    }

    @Override
    public void run() {
        try {
            transactionService = SpringLiteContext.getBean(MongoTransactionServiceImpl.class);
            transactionService.deleteTxs(chainId);
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
