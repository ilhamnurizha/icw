package io.icw.crosschain.base.message;

import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.basic.NulsOutputStreamBuffer;
import io.icw.base.data.NulsHash;
import io.icw.crosschain.base.message.base.BaseMessage;
import io.icw.core.exception.NulsException;

import java.io.IOException;

/**
 * 接收跨链交易在主网节点的处理结果
 * @author tag
 * @date 2019/4/4
 */
public class GetCtxStateMessage extends BaseMessage {
    /**
     * 请求连协议跨链交易Hash
     * */
    private NulsHash requestHash;


    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(requestHash.getBytes());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.requestHash = byteBuffer.readHash();
    }

    @Override
    public int size() {
        int size = 0;
        size += NulsHash.HASH_LENGTH;
        return size;
    }

    public NulsHash getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(NulsHash requestHash) {
        this.requestHash = requestHash;
    }
}
