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

package io.icw.network.manager.handler.base;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.icw.base.data.BaseNulsData;
import io.icw.network.constant.NetworkErrorCode;
import io.icw.network.model.NetworkEventResult;
import io.icw.network.model.Node;
import io.icw.network.model.message.base.BaseMessage;
import io.icw.network.model.message.base.MessageHeader;
import io.icw.network.utils.LoggerUtil;


/**
 * base message handler
 *
 * @author lan
 * @date 2018/11/01
 */
public abstract class BaseMessageHandler implements BaseMeesageHandlerInf {
    /**
     * 实现发送消息
     * Implement sending a message
     *
     * @param message message
     * @param node    peer info
     * @param asyn    default true
     * @return NetworkEventResult
     */
    @Override
    public NetworkEventResult send(BaseMessage message, Node node, boolean asyn) {
        try {
            MessageHeader header = message.getHeader();
            header.setMagicNumber(header.getMagicNumber());
            BaseNulsData body = message.getMsgBody();
            header.setPayloadLength(body.size());
            ChannelFuture future = node.getChannel().writeAndFlush(Unpooled.wrappedBuffer(message.serialize()));
            if (!asyn) {
                future.await();
                boolean success = future.isSuccess();
                if (!success) {
                    return NetworkEventResult.getResultFail(NetworkErrorCode.NET_MESSAGE_SEND_FAIL);
                }
            }
        } catch (Exception e) {
            LoggerUtil.logger(node.getNodeGroup().getChainId()).error(e.getMessage(), e);
            return NetworkEventResult.getResultFail(NetworkErrorCode.NET_MESSAGE_SEND_EXCEPTION);
        }
        return NetworkEventResult.getResultSuccess();
    }
}
