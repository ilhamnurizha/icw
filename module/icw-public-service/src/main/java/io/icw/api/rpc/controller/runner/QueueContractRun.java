/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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
package io.icw.api.rpc.controller.runner;

import io.icw.api.ApiContext;
import io.icw.api.analysis.AnalysisHandler;
import io.icw.api.analysis.WalletRpcHandler;
import io.icw.api.db.TransactionService;
import io.icw.api.model.po.TransactionInfo;
import io.icw.api.utils.LoggerUtil;
import io.icw.base.RPCUtil;
import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.data.Transaction;
import io.icw.core.basic.Result;

import java.util.concurrent.TimeUnit;

/**
 * @author: PierreLuo
 * @date: 2020-03-31
 */
public class QueueContractRun implements Runnable {
    private int chainId;
    private String txHex;
    private TransactionService txService;

    public QueueContractRun(int chainId, String txHex, TransactionService txService) {
        this.chainId = chainId;
        this.txHex = txHex;
        this.txService = txService;
    }

    @Override
    public void run() {
        try {
            Result result = WalletRpcHandler.broadcastTx(chainId, txHex);

            if (result.isSuccess()) {
                Transaction tx = new Transaction();
                tx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                TransactionInfo txInfo = AnalysisHandler.toTransaction(chainId, tx, ApiContext.protocolVersion);
                LoggerUtil.commonLog.info("排队广播指定合约交易[{}]成功", txInfo.getHash());
                txService.saveUnConfirmTx(chainId, txInfo, txHex);
                // 休眠10秒
                TimeUnit.SECONDS.sleep(10);
                return;
            }
            LoggerUtil.commonLog.error("排队广播指定合约交易失败, 详细: {}", result.toString());
        } catch (Exception e) {
            LoggerUtil.commonLog.error("排队广播指定合约交易失败", e);
        }
    }
}
