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

package io.icw.network.manager.handler.message;

import io.icw.network.manager.MessageFactory;
import io.icw.network.manager.MessageManager;
import io.icw.network.manager.handler.base.BaseMessageHandler;
import io.icw.network.model.NetworkEventResult;
import io.icw.network.model.Node;
import io.icw.network.model.message.PingMessage;
import io.icw.network.model.message.PongMessage;
import io.icw.network.model.message.base.BaseMessage;

/**
 * get time message handler
 *
 * @author lan
 * @date 2018/10/15
 */
public class PingMessageHandler extends BaseMessageHandler {

    private static PingMessageHandler instance = new PingMessageHandler();

    private PingMessageHandler() {

    }

    public static PingMessageHandler getInstance() {
        return instance;
    }

    /**
     * recieve ping message
     *
     * @param message address message
     * @param node    peer info
     * @return
     */
    @Override
    public NetworkEventResult recieve(BaseMessage message, Node node) {
        PingMessage pingMessage = (PingMessage) message;
//        LoggerUtil.logger(node.getNodeGroup().getChainId()).debug("PingMessageHandler Recieve:magicNum={}, node={},randCode={}", pingMessage.getHeader().getMagicNumber(), node.getId(), pingMessage.getMsgBody().getRandomCode());
        /*
         *  回复Pong消息
         */
        PongMessage pongMessage = MessageFactory.getInstance().buildPongMessage(pingMessage);
        MessageManager.getInstance().sendHandlerMsg(pongMessage, node, true);
        return NetworkEventResult.getResultSuccess();
    }
}
