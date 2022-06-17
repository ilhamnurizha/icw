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
package io.icw.network.model.message;

import io.icw.base.basic.NulsByteBuffer;
import io.icw.network.model.message.base.BaseMessage;
import io.icw.network.model.message.body.AddrMessageBody;
import io.icw.network.constant.NetworkConstant;
import io.icw.core.exception.NulsException;

/**
 * peer地址协议消息
 * addr message
 *
 * @author lan
 * @date 2018/11/01
 */
public class AddrMessage extends BaseMessage<AddrMessageBody> {

    @Override
    protected AddrMessageBody parseMessageBody(NulsByteBuffer byteBuffer) throws NulsException {
        try {
            return byteBuffer.readNulsData(new AddrMessageBody());
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    public AddrMessage() {
        super(NetworkConstant.CMD_MESSAGE_ADDR, 0);
    }

    public AddrMessage(long magicNumber, String cmd, AddrMessageBody body) {
        super(cmd, magicNumber);
        this.setMsgBody(body);
    }
}
