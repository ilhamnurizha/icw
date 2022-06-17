package io.icw.block.message;

import io.icw.base.data.NulsHash;
import io.icw.core.crypto.HexUtil;
import org.junit.Test;

import java.io.IOException;

public class BlockMessageTest {

    @Test
    public void name() throws IOException {
        BlockMessage message = new BlockMessage();
        message.setBlock(null);
        message.setSyn(false);
        message.setRequestHash(NulsHash.calcHash("123".getBytes()));

        System.out.println(HexUtil.encode(message.serialize()));
    }
}