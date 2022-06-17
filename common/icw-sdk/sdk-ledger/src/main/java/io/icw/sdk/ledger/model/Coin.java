package io.icw.sdk.ledger.model;

import io.icw.sdk.core.exception.NulsException;
import io.icw.sdk.core.model.BaseNulsData;
import io.icw.sdk.core.utils.NulsByteBuffer;
import io.icw.sdk.core.utils.NulsOutputStreamBuffer;

import java.io.IOException;

public class Coin extends BaseNulsData {




    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        // todo auto-generated method stub

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        // todo auto-generated method stub

    }

    @Override
    public int size() {
        // todo auto-generated method stub
        return 0;
    }
}
