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
import io.icw.base.data.NulsHash;
import io.icw.base.protocol.MessageProcessor;
import io.icw.block.constant.BlockForwardEnum;
import io.icw.block.constant.StatusEnum;
import io.icw.block.manager.ContextManager;
import io.icw.block.message.HashListMessage;
import io.icw.block.message.HashMessage;
import io.icw.block.model.CachedSmallBlock;
import io.icw.block.model.ChainContext;
import io.icw.block.model.TxGroupTask;
import io.icw.block.rpc.call.NetworkCall;
import io.icw.block.thread.monitor.TxGroupRequestor;
import io.icw.block.utils.SmallBlockCacher;
import io.icw.core.core.annotation.Component;
import io.icw.core.log.logback.NulsLogger;

import java.util.List;

import static io.icw.block.BlockBootstrap.blockConfig;
import static io.icw.block.constant.CommandConstant.FORWARD_SMALL_BLOCK_MESSAGE;
import static io.icw.block.constant.CommandConstant.GET_SMALL_BLOCK_MESSAGE;

/**
 * 处理收到的{@link HashMessage},用于区块的广播与转发
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component("ForwardSmallBlockHandlerV1")
public class ForwardSmallBlockHandler implements MessageProcessor {

    @Override
    public String getCmd() {
        return FORWARD_SMALL_BLOCK_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        HashMessage message = RPCUtil.getInstanceRpcStr(msgStr, HashMessage.class);
        if (message == null) {
            return;
        }
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger logger = context.getLogger();
        NulsHash blockHash = message.getRequestHash();
        Long height = context.getCachedHashHeightMap().get(blockHash);
        if (height != null) {
            NetworkCall.setHashAndHeight(chainId, blockHash, height, nodeId);
        }
        BlockForwardEnum status = SmallBlockCacher.getStatus(chainId, blockHash);
//        logger.debug("recieve " + message + " from node-" + nodeId + ", hash:" + blockHash);
        List<String> nodes = context.getOrphanBlockRelatedNodes().get(blockHash);
        if (nodes != null && !nodes.contains(nodeId)) {
            nodes.add(nodeId);
            logger.debug("add OrphanBlockRelatedNodes, blockHash-{}, nodeId-{}", blockHash, nodeId);
        }
        //1.已收到完整区块,丢弃
        if (BlockForwardEnum.COMPLETE.equals(status)) {
            return;
        }
        //2.已收到部分区块,还缺失交易信息,发送HashListMessage到源节点
        if (BlockForwardEnum.INCOMPLETE.equals(status) && !context.getStatus().equals(StatusEnum.SYNCHRONIZING)) {
            CachedSmallBlock block = SmallBlockCacher.getCachedSmallBlock(chainId, blockHash);
            if (block == null) {
                return;
            }
            HashListMessage request = new HashListMessage();
            request.setBlockHash(blockHash);
            request.setTxHashList(block.getMissingTransactions());
            TxGroupTask task = new TxGroupTask();
            task.setId(System.nanoTime());
            task.setNodeId(nodeId);
            task.setRequest(request);
            task.setExcuteTime(blockConfig.getTxGroupTaskDelay());
            TxGroupRequestor.addTask(chainId, blockHash.toString(), task);
            return;
        }
        //3.未收到区块
        if (BlockForwardEnum.EMPTY.equals(status)) {
            HashMessage request = new HashMessage();
            request.setRequestHash(blockHash);
            NetworkCall.sendToNode(chainId, request, nodeId, GET_SMALL_BLOCK_MESSAGE);
        }
    }
}
