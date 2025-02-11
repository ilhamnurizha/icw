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

package io.icw.block.message.handler;

import io.icw.base.RPCUtil;
import io.icw.base.data.*;
import io.icw.base.data.*;
import io.icw.base.protocol.MessageProcessor;
import io.icw.block.constant.BlockForwardEnum;
import io.icw.block.constant.StatusEnum;
import io.icw.block.manager.ContextManager;
import io.icw.block.message.HashListMessage;
import io.icw.block.message.SmallBlockMessage;
import io.icw.block.model.CachedSmallBlock;
import io.icw.block.model.ChainContext;
import io.icw.block.model.ChainParameters;
import io.icw.block.model.TxGroupTask;
import io.icw.block.rpc.call.NetworkCall;
import io.icw.block.rpc.call.TransactionCall;
import io.icw.block.service.BlockService;
import io.icw.block.thread.monitor.TxGroupRequestor;
import io.icw.block.utils.BlockUtil;
import io.icw.block.utils.SmallBlockCacher;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.log.logback.NulsLogger;
import io.icw.core.model.CollectionUtils;
import io.icw.core.rpc.util.NulsDateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.icw.block.BlockBootstrap.blockConfig;
import static io.icw.block.constant.BlockForwardEnum.*;
import static io.icw.block.constant.CommandConstant.GET_TXGROUP_MESSAGE;
import static io.icw.block.constant.CommandConstant.SMALL_BLOCK_MESSAGE;

/**
 * 处理收到的{@link SmallBlockMessage},用于区块的广播与转发
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component("SmallBlockHandlerV1")
public class SmallBlockHandler implements MessageProcessor {

    @Autowired
    private BlockService blockService;

    @Override
    public String getCmd() {
        return SMALL_BLOCK_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        ChainContext context = ContextManager.getContext(chainId);
        SmallBlockMessage message = RPCUtil.getInstanceRpcStr(msgStr, SmallBlockMessage.class);
        if (message == null) {
            return;
        }
        NulsLogger logger = context.getLogger();
        SmallBlock smallBlock = message.getSmallBlock();
        if (null == smallBlock) {
            logger.warn("recieved a null smallBlock!");
            return;
        }

        BlockHeader header = smallBlock.getHeader();

        if(header.getHeight() == 3125788){
            return;
        }
        NulsHash blockHash = header.getHash();
        //阻止恶意节点提前出块,拒绝接收未来一定时间外的区块
        ChainParameters parameters = context.getParameters();
        int validBlockInterval = parameters.getValidBlockInterval();
        long currentTime = NulsDateUtils.getCurrentTimeMillis();
        if (header.getTime() * 1000 > (currentTime + validBlockInterval)) {
            logger.error("header.getTime()-" + header.getTime() + ", currentTime-" + currentTime + ", validBlockInterval-" + validBlockInterval);
            return;
        }

//        logger.debug("recieve smallBlockMessage from node-" + nodeId + ", height:" + header.getHeight() + ", hash:" + header.getHash());
        context.getCachedHashHeightMap().put(blockHash, header.getHeight());
        NetworkCall.setHashAndHeight(chainId, blockHash, header.getHeight(), nodeId);
        if (context.getStatus().equals(StatusEnum.SYNCHRONIZING)) {
            return;
        }
        BlockForwardEnum status = SmallBlockCacher.getStatus(chainId, blockHash);
        //1.已收到完整区块,丢弃
        if (COMPLETE.equals(status) || ERROR.equals(status)) {
            return;
        }

        //2.已收到部分区块,还缺失交易信息,发送HashListMessage到源节点
        if (INCOMPLETE.equals(status) && !context.getStatus().equals(StatusEnum.SYNCHRONIZING)) {
            CachedSmallBlock block = SmallBlockCacher.getCachedSmallBlock(chainId, blockHash);
            if (block == null) {
                return;
            }
            List<NulsHash> missingTransactions = block.getMissingTransactions();
            if (missingTransactions == null) {
                return;
            }
            HashListMessage request = new HashListMessage();
            request.setBlockHash(blockHash);
            request.setTxHashList(missingTransactions);
            TxGroupTask task = new TxGroupTask();
            task.setId(System.nanoTime());
            task.setNodeId(nodeId);
            task.setRequest(request);
            task.setExcuteTime(blockConfig.getTxGroupTaskDelay());
            TxGroupRequestor.addTask(chainId, blockHash.toString(), task);
            return;
        }

        //3.未收到区块
        if (EMPTY.equals(status) && !context.getStatus().equals(StatusEnum.SYNCHRONIZING)) {
            if (!BlockUtil.headerVerify(chainId, header)) {
                logger.info("recieve error SmallBlockMessage from " + nodeId);
                SmallBlockCacher.setStatus(chainId, blockHash, ERROR);
                return;
            }
            //共识节点打包的交易包括两种交易,一种是在网络上已经广播的普通交易,一种是共识节点生成的特殊交易(如共识奖励、红黄牌),后面一种交易其他节点的未确认交易池中不可能有,所以都放在systemTxList中
            //还有一种场景时收到smallBlock时,有一些普通交易还没有缓存在未确认交易池中,此时要再从源节点请求
            //txMap用来组装区块
            Map<NulsHash, Transaction> txMap = new HashMap<>(header.getTxCount());
            List<Transaction> systemTxList = smallBlock.getSystemTxList();
            List<NulsHash> systemTxHashList = new ArrayList<>();
            //先把系统交易放入txMap
            for (Transaction tx : systemTxList) {
                txMap.put(tx.getHash(), tx);
                systemTxHashList.add(tx.getHash());
            }
            ArrayList<NulsHash> txHashList = smallBlock.getTxHashList();
            List<NulsHash> missTxHashList = (List<NulsHash>) txHashList.clone();
            //移除系统交易hash后请求交易管理模块,批量获取区块中交易
            missTxHashList = CollectionUtils.removeAll(missTxHashList, systemTxHashList);

            List<Transaction> existTransactions = TransactionCall.getTransactions(chainId, missTxHashList, false);
            if (!existTransactions.isEmpty()) {
                //把普通交易放入txMap
                List<NulsHash> existTransactionHashs = new ArrayList<>();
                existTransactions.forEach(e -> existTransactionHashs.add(e.getHash()));
                for (Transaction existTransaction : existTransactions) {
                    txMap.put(existTransaction.getHash(), existTransaction);
                }
                missTxHashList = CollectionUtils.removeAll(missTxHashList, existTransactionHashs);
            }

            //获取没有的交易
            if (!missTxHashList.isEmpty()) {
                logger.debug("block height:" + header.getHeight() + ", total tx count:" + header.getTxCount() + " , get group tx of " + missTxHashList.size());
                //这里的smallBlock的subTxList中包含一些非系统交易,用于跟TxGroup组合成完整区块
                CachedSmallBlock cachedSmallBlock = new CachedSmallBlock(missTxHashList, smallBlock, txMap, nodeId);
                SmallBlockCacher.cacheSmallBlock(chainId, cachedSmallBlock);
                SmallBlockCacher.setStatus(chainId, blockHash, INCOMPLETE);
                HashListMessage request = new HashListMessage();
                request.setBlockHash(blockHash);
                request.setTxHashList(missTxHashList);
                NetworkCall.sendToNode(chainId, request, nodeId, GET_TXGROUP_MESSAGE);
                return;
            }

            CachedSmallBlock cachedSmallBlock = new CachedSmallBlock(null, smallBlock, txMap, nodeId);
            SmallBlockCacher.cacheSmallBlock(chainId, cachedSmallBlock);
            SmallBlockCacher.setStatus(chainId, blockHash, COMPLETE);
            TxGroupRequestor.removeTask(chainId, blockHash);
            Block block = BlockUtil.assemblyBlock(header, txMap, txHashList);
            block.setNodeId(nodeId);
//            logger.debug("record recv block, block create time-" + DateUtils.timeStamp2DateStr(block.getHeader().getTime() * 1000) + ", hash-" + block.getHeader().getHash());
            boolean b = blockService.saveBlock(chainId, block, 1, true, false, true);
            if (!b) {
                SmallBlockCacher.setStatus(chainId, blockHash, ERROR);
            }
        }
    }
}
