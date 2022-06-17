/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.icw.ledger.rpc.cmd;

import io.icw.base.RPCUtil;
import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.data.Transaction;
import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.exception.NulsException;
import io.icw.core.model.StringUtils;
import io.icw.core.rpc.cmd.BaseCmd;
import io.icw.core.rpc.model.message.Response;
import io.icw.ledger.manager.LedgerChainManager;
import io.icw.ledger.utils.LoggerUtil;
import io.icw.ledger.constant.LedgerErrorCode;

import java.util.List;

/**
 * @author lan
 * @description
 * @date 2019/03/13
 **/
public class BaseLedgerCmd extends BaseCmd {
    boolean chainHanlder(int chainId) {
        //链判断？判断是否是有效的.
        //进行初始化
        try {
            SpringLiteContext.getBean(LedgerChainManager.class).addChain(chainId);
        } catch (Exception e) {
            LoggerUtil.logger(chainId).error(e);
            return false;
        }
        return true;
    }

    Response parseTxs(List<String> txStrList, List<Transaction> txList, int chainId) {
        for (String txStr : txStrList) {
            Transaction tx = RPCUtil.getInstanceRpcStr(txStr, Transaction.class);
            if (null == tx) {
                return failed(LedgerErrorCode.TX_IS_WRONG);
            } else {
                txList.add(tx);
            }
        }
        return success();
    }

    Transaction parseTxs(String txStr, int chainId) {
        if (StringUtils.isBlank(txStr)) {
            return null;
        }
        byte[] txStream = RPCUtil.decode(txStr);
        Transaction tx = new Transaction();
        try {
            tx.parse(new NulsByteBuffer(txStream));
        } catch (NulsException e) {
            LoggerUtil.logger(chainId).error("transaction parse error", e);
            return null;
        }
        return tx;
    }

}
