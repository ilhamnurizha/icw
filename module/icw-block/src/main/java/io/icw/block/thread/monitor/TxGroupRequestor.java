/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.icw.block.thread.monitor;

import io.icw.base.data.*;
import io.icw.base.data.*;
import io.icw.block.manager.ContextManager;
import io.icw.block.message.HashListMessage;
import io.icw.block.model.CachedSmallBlock;
import io.icw.block.model.ChainContext;
import io.icw.block.model.TxGroupTask;
import io.icw.block.rpc.call.NetworkCall;
import io.icw.block.rpc.call.TransactionCall;
import io.icw.block.service.BlockService;
import io.icw.block.utils.BlockUtil;
import io.icw.block.utils.SmallBlockCacher;
import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.log.logback.NulsLogger;
import io.icw.core.model.CollectionUtils;
import io.icw.core.model.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.stream.Collectors;

import static io.icw.block.constant.BlockForwardEnum.ERROR;
import static io.icw.block.constant.CommandConstant.GET_TXGROUP_MESSAGE;

/**
 * 区块广播过程中,获取本地没有的交易
 *
 * @author captain
 * @version 1.0
 * @date 19-3-28 下午3:54
 */
public class TxGroupRequestor extends BaseMonitor {

    private BlockService blockService;

    private TxGroupRequestor() {
        blockService = SpringLiteContext.getBean(BlockService.class);
    }

    private static Map<Integer, Map<String, DelayQueue<TxGroupTask>>> map = new HashMap<>();

    private static final TxGroupRequestor INSTANCE = new TxGroupRequestor();

    public static TxGroupRequestor getInstance() {
        return INSTANCE;
    }

    public static void init(int chainId) {
        byte smallBlockCache = ContextManager.getContext(chainId).getParameters().getSmallBlockCache();
        Map<String, DelayQueue<TxGroupTask>> cMap = CollectionUtils.getSynSizedMap(smallBlockCache);
        map.put(chainId, cMap);
    }

    public static void addTask(int chainId, String hash, TxGroupTask task) {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        DelayQueue<TxGroupTask> txGroupTasks = map.get(chainId).get(hash);
        if (txGroupTasks == null) {
            txGroupTasks = new DelayQueue<>();
            map.get(chainId).put(hash, txGroupTasks);
        }
        boolean add = txGroupTasks.add(task);
//        logger.debug("TxGroupRequestor add TxGroupTask, hash-" + hash + ", task-" + task + ", result-" + add);
    }

    public static void removeTask(int chainId, NulsHash hash) {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        DelayQueue<TxGroupTask> remove = map.get(chainId).remove(hash.toHex());
//        logger.debug("TxGroupRequestor remove TxGroupTask, hash-" + hash + ", size-" + (remove == null ? 0 : remove.size()));
    }

    @Override
    protected void process(int chainId, ChainContext context, NulsLogger logger) {
        Map<String, DelayQueue<TxGroupTask>> delayQueueMap = map.get(chainId);
        List<String> del = new ArrayList<>();
        for (Map.Entry<String, DelayQueue<TxGroupTask>> entry : delayQueueMap.entrySet()) {
            String blockHash = entry.getKey();
            TxGroupTask task = entry.getValue().poll();
            if (task != null) {
                HashListMessage hashListMessage = task.getRequest();
                List<NulsHash> hashList = hashListMessage.getTxHashList();
                int original = hashList.size();
//                logger.debug("TxGroupRequestor send getTxgroupMessage, original hashList size-" + original + ", blockHash-" + blockHash);
                List<Transaction> existTransactions = TransactionCall.getTransactions(chainId, hashList, false);
                List<NulsHash> existHashes = existTransactions.stream().map(Transaction::getHash).collect(Collectors.toList());
                hashList = CollectionUtils.removeAll(hashList, existHashes);
                int filtered = hashList.size();
//                logger.debug("TxGroupRequestor send getTxgroupMessage, filtered hashList size-" + filtered + ", blockHash-" + blockHash);
                CachedSmallBlock cachedSmallBlock = SmallBlockCacher.getCachedSmallBlock(chainId, NulsHash.fromHex(blockHash));
                if (cachedSmallBlock == null) {
                    continue;
                }
                if (filtered == 0) {
                    SmallBlock smallBlock = cachedSmallBlock.getSmallBlock();
                    BlockHeader header = smallBlock.getHeader();
                    Map<NulsHash, Transaction> txMap = cachedSmallBlock.getTxMap();
                    for (Transaction tx : existTransactions) {
                        txMap.put(tx.getHash(), tx);
                    }

                    Block block = BlockUtil.assemblyBlock(header, txMap, smallBlock.getTxHashList());
                    block.setNodeId(cachedSmallBlock.getNodeId());
                    TxGroupRequestor.removeTask(chainId, header.getHash());
                    logger.debug("record recv block, block create time-" + DateUtils.timeStamp2DateStr(block.getHeader().getTime() * 1000) + ", hash-" + block.getHeader().getHash());
                    boolean b = blockService.saveBlock(chainId, block, 1, true, false, true);
                    if (!b) {
                        SmallBlockCacher.setStatus(chainId, header.getHash(), ERROR);
                    }
                    del.add(blockHash);
                    continue;
                }
                hashListMessage.setTxHashList(hashList);
                if (original != filtered) {
                    entry.getValue().forEach(e -> e.setRequest(hashListMessage));
                    Map<NulsHash, Transaction> map = cachedSmallBlock.getTxMap();
                    existTransactions.forEach(e -> map.put(e.getHash(), e));
                }
                boolean b = NetworkCall.sendToNode(chainId, hashListMessage, task.getNodeId(), GET_TXGROUP_MESSAGE);
                logger.debug("TxGroupRequestor send getTxgroupMessage to " + task.getNodeId() + ", result-" + b + ", blockHash-" + blockHash);
            }
        }
        del.forEach(delayQueueMap::remove);
    }

}
