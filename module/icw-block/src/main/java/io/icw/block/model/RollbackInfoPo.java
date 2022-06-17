package io.icw.block.model;

import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.basic.NulsOutputStreamBuffer;
import io.icw.base.data.BaseNulsData;
import io.icw.core.exception.NulsException;
import io.icw.core.parse.SerializeUtils;

import java.io.IOException;

public class RollbackInfoPo extends BaseNulsData {
    private long height;

    public RollbackInfoPo(){
    }

    public RollbackInfoPo(long height){
        this.height = height;
    }

    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(height);
    }

    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.height = byteBuffer.readVarInt();
    }

    public int size() {
        return  SerializeUtils.sizeOfVarInt(height);
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }
}
