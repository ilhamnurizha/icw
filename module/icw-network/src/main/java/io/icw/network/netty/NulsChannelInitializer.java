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

package io.icw.network.netty;

import io.icw.network.constant.NetworkConstant;
import io.icw.network.netty.codec.NulsMessageDecoder;
import io.icw.network.netty.codec.NulsMessageEncoder;
import io.icw.network.netty.handler.HeartbeatServerHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * NulsChannelInitializer
 *
 * @author lan
 * @date 2018/11/01
 */
public class NulsChannelInitializer<T extends ChannelInboundHandlerAdapter> extends ChannelInitializer<SocketChannel> {


    private T t;

    public NulsChannelInitializer(T t) {
        this.t = t;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline p = socketChannel.pipeline();
        p.addLast("idle", new IdleStateHandler(NetworkConstant.READ_IDEL_TIME_OUT, NetworkConstant.WRITE_IDEL_TIME_OUT, NetworkConstant.ALL_IDLE_TIME_OUT));
        p.addLast("decoder", new NulsMessageDecoder());
        p.addLast("encoder0", new NulsMessageEncoder());
        p.addLast("heartbeat", new HeartbeatServerHandler());
        p.addLast(t);
    }
}
