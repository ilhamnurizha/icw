package io.icw.crosschain.base.message;

import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.basic.NulsOutputStreamBuffer;
import io.icw.crosschain.base.message.base.BaseMessage;
import io.icw.core.exception.NulsException;
import io.icw.core.parse.SerializeUtils;

/**
 * 主网向友链获取链资产发行量
 * @author tag
 * @date 2019/4/4
 */
public class GetCirculationMessage extends BaseMessage {
    private String assetIds;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream){
        stream.writeString(assetIds);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.assetIds = byteBuffer.readString();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfString(assetIds);
        return size;
    }

    public String getAssetIds() {
        return assetIds;
    }

    public void setAssetIds(String assetIds) {
        this.assetIds = assetIds;
    }
}
