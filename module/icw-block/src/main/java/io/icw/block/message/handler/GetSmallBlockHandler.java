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
import io.icw.base.data.SmallBlock;
import io.icw.base.protocol.MessageProcessor;
import io.icw.block.manager.ContextManager;
import io.icw.block.message.HashMessage;
import io.icw.block.message.SmallBlockMessage;
import io.icw.block.rpc.call.NetworkCall;
import io.icw.block.utils.SmallBlockCacher;
import io.icw.core.core.annotation.Component;
import io.icw.core.log.logback.NulsLogger;

import static io.icw.block.constant.CommandConstant.GET_SMALL_BLOCK_MESSAGE;
import static io.icw.block.constant.CommandConstant.SMALL_BLOCK_MESSAGE;

/**
 * 处理收到的{@link HashMessage},用于区块的广播与转发
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component("GetSmallBlockHandlerV1")
public class GetSmallBlockHandler implements MessageProcessor {

    @Override
    public String getCmd() {
        return GET_SMALL_BLOCK_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        HashMessage message = RPCUtil.getInstanceRpcStr(msgStr, HashMessage.class);
        if (message == null) {
            return;
        }
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        NulsHash blockHash = message.getRequestHash();
//        logger.debug("recieve " + message + " from node-" + nodeId + ", hash:" + blockHash);
        SmallBlock smallBlock = SmallBlockCacher.getSmallBlock(chainId, blockHash);
        if (smallBlock != null) {
            SmallBlockMessage smallBlockMessage = new SmallBlockMessage();
            smallBlockMessage.setSmallBlock(smallBlock);
            NetworkCall.sendToNode(chainId, smallBlockMessage, nodeId, SMALL_BLOCK_MESSAGE);
        }
    }
}
