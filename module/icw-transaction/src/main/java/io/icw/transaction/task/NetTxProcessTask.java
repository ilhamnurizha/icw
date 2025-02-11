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

package io.icw.transaction.task;

import io.icw.base.data.Transaction;
import io.icw.core.constant.BaseConstant;
import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.exception.NulsException;
import io.icw.transaction.cache.PackablePool;
import io.icw.transaction.constant.TxConstant;
import io.icw.transaction.constant.TxErrorCode;
import io.icw.transaction.manager.TxManager;
import io.icw.transaction.model.bo.Chain;
import io.icw.transaction.model.bo.TxRegister;
import io.icw.transaction.model.po.TransactionNetPO;
import io.icw.transaction.rpc.call.LedgerCall;
import io.icw.transaction.rpc.call.NetworkCall;
import io.icw.transaction.rpc.call.TransactionCall;
import io.icw.transaction.service.TxService;
import io.icw.transaction.storage.UnconfirmedTxStorageService;
import io.icw.transaction.utils.TxDuplicateRemoval;
import io.icw.transaction.utils.TxUtil;

import java.util.*;

/**
 * Process new transactions broadcast by other nodes in the network
 *
 * @author: Charlie
 * @date: 2019/6/11
 */
public class NetTxProcessTask implements Runnable {
    private PackablePool packablePool = SpringLiteContext.getBean(PackablePool.class);
    private UnconfirmedTxStorageService unconfirmedTxStorageService = SpringLiteContext.getBean(UnconfirmedTxStorageService.class);
    private TxService txService = SpringLiteContext.getBean(TxService.class);
    private Chain chain;

    public NetTxProcessTask(Chain chain) {
        this.chain = chain;
    }

    @Override
    public void run() {
        try {
            process();
        } catch (Exception e) {
            chain.getLogger().error("NetTxProcessTask Exception");
            chain.getLogger().error(e);
        }
    }

    private void process() {
        while (true) {
            try {
                if (chain.getUnverifiedQueue().isEmpty()) {
                    Thread.sleep(3000L);
                    continue;
                }
                if (chain.getProtocolUpgrade().get()) {
                    chain.getLogger().info("Protocol upgrade pause process new tx..");
                    Thread.sleep(10000L);
                    continue;
                }
                List<TransactionNetPO> txNetList = new ArrayList<>(TxConstant.NET_TX_PROCESS_NUMBER_ONCE);
                chain.getUnverifiedQueue().drainTo(txNetList, TxConstant.NET_TX_PROCESS_NUMBER_ONCE);
                //分组 调验证器
                Map<String, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);
                Iterator<TransactionNetPO> it = txNetList.iterator();
                int packableTxMapDataSize = 0;
                for(Transaction tx : chain.getPackableTxMap().values()){
                    packableTxMapDataSize += tx.size();
                }
                while (it.hasNext()) {
                    TransactionNetPO txNetPO = it.next();
                    Transaction tx = txNetPO.getTx();
                    //待打包队列map超过预定值,则不再接受处理交易,直接转发交易完整交易
                    if (TxUtil.discardTx(chain, packableTxMapDataSize, tx)) {
                        //待打包队列map超过预定值, 不处理转发失败的情况
                        String hash = tx.getHash().toHex();
                        NetworkCall.broadcastTx(chain, tx, TxDuplicateRemoval.getExcludeNode(hash));
                        it.remove();
                        continue;
                    }
                    TxUtil.moduleGroups(chain, moduleVerifyMap, tx);
                }
                verifiction(chain, moduleVerifyMap, txNetList);
                verifyCoinData(chain, txNetList);
                if (txNetList.isEmpty()) {
                    continue;
                }
                //保存到rocksdb
                unconfirmedTxStorageService.putTxList(chain.getChainId(), txNetList);
                for (TransactionNetPO txNet : txNetList) {
                    Transaction tx = txNet.getTx();
                    if (chain.getPackaging().get()) {
                        //当节点是出块节点时, 才将交易放入待打包队列
                        packablePool.add(chain, tx);
                    }
                    //网络交易不处理转发失败的情况
                    String hash = tx.getHash().toHex();
                    NetworkCall.forwardTxHash(chain, tx.getHash(), TxDuplicateRemoval.getExcludeNode(hash));
                }
            } catch (Exception e) {
                chain.getLogger().error(e);
            }
        }
    }


    private void verifiction(Chain chain, Map<String, List<String>> moduleVerifyMap, List<TransactionNetPO> txNetList) {
        Iterator<Map.Entry<String, List<String>>> it = moduleVerifyMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            List<String> moduleList = entry.getValue();
            String moduleCode = entry.getKey();
            List<String> txHashList = null;
            try {
                txHashList = TransactionCall.txModuleValidator(chain, moduleCode, moduleList);
            } catch (NulsException e) {
                chain.getLogger().error("Net new tx verify failed -txModuleValidator Exception:{}, module-code:{}, count:{} , return count:{}",
                        BaseConstant.TX_VALIDATOR, moduleCode, moduleList.size(), txHashList.size());
                //出错则删掉整个模块的交易
                Iterator<TransactionNetPO> its = txNetList.iterator();
                while (its.hasNext()) {
                    Transaction tx = its.next().getTx();
                    TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                    if (txRegister.getModuleCode().equals(moduleCode)) {
                        its.remove();
                    }
                }
                continue;
            }
            if (null == txHashList || txHashList.isEmpty()) {
                continue;
            }
            chain.getLogger().error("[Net new tx verify failed] module:{}, module-code:{}, count:{} , return count:{}",
                    BaseConstant.TX_VALIDATOR, moduleCode, moduleList.size(), txHashList.size());
            /**冲突检测有不通过的, 执行清除和未确认回滚 从txNetList删除*/
            for (int i = 0; i < txHashList.size(); i++) {
                String hash = txHashList.get(i);
                Iterator<TransactionNetPO> its = txNetList.iterator();
                while (its.hasNext()) {
                    Transaction tx = its.next().getTx();
                    if (hash.equals(tx.getHash().toHex())) {
                        its.remove();
                    }
                }
            }
        }
    }


    private void verifyCoinData(Chain chain, List<TransactionNetPO> txNetList) throws NulsException {
        if (txNetList.isEmpty()) {
            return;
        }
        try {
            Map verifyCoinDataResult = LedgerCall.commitBatchUnconfirmedTxs(chain, txNetList);
            List<String> failHashs = (List<String>) verifyCoinDataResult.get("fail");
            List<String> orphanHashs = (List<String>) verifyCoinDataResult.get("orphan");
            if (failHashs.isEmpty() && orphanHashs.isEmpty()) {
                return;
            }

            Iterator<TransactionNetPO> it = txNetList.iterator();
            removeAndGo:
            while (it.hasNext()) {
                TransactionNetPO transactionNetPO = it.next();
                Transaction tx = transactionNetPO.getTx();
                //去除账本验证失败的交易
                for (String hash : failHashs) {
                    String hashStr = tx.getHash().toHex();
                    if (hash.equals(hashStr)) {
                        it.remove();
                        continue removeAndGo;
                    }
                }
                //去除孤儿交易, 同时把孤儿交易放入孤儿池
                for (String hash : orphanHashs) {
                    String hashStr = tx.getHash().toHex();
                    if (hash.equals(hashStr)) {
                        //孤儿交易
                        List<TransactionNetPO> chainOrphan = chain.getOrphanList();
                        //孤儿交易集合数据总大小
                        if (chain.getOrphanListDataSize().get() >= TxConstant.ORPHAN_LIST_MAX_DATA_SIZE) {
                            it.remove();
                            break;
                        } else {
                            synchronized (chainOrphan) {
                                chainOrphan.add(transactionNetPO);
                                chain.getOrphanListDataSize().addAndGet(transactionNetPO.getTx().size());
                            }
                            it.remove();
                            continue removeAndGo;
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

}
