package io.icw.ledger.test.validater;

import io.icw.base.basic.AddressTool;
import io.icw.core.exception.NulsException;
import io.icw.core.log.Log;
import io.icw.core.rockdb.manager.RocksDBManager;
import io.icw.core.rockdb.model.Entry;
import io.icw.ledger.model.po.AccountState;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.List;

/**
 * @author Niels
 */
public class TotalAmountTest {

    @Test
    public void test() throws Exception {
        RocksDBManager.init("/Users/niels/workspace/nuls-v2/data/ledger");
        List<Entry<byte[], byte[]>> list = RocksDBManager.entryList("account_1");
        BigInteger total = list.stream().map(d -> {
            try {
                String key = new String(d.getKey(), "UTF8");
                String[] keyAry = key.split("-");
                byte[] address = AddressTool.getAddress("NULSd" + keyAry[0]);
                if (AddressTool.getChainIdByAddress(address) != 1) {
                    return BigInteger.ZERO;
                }
                if ("9".equals(keyAry[1]) && "1".equals(keyAry[2]) && !"6HgWSU1iR6BfNoQi85mAMT52JMFzpnok".equals(keyAry[0])) {
                    AccountState accountState = new AccountState();
                    accountState.parse(d.getValue(), 0);
                    return accountState.getTotalAmount();
                } else {
                    return BigInteger.ZERO;
                }
            } catch (UnsupportedEncodingException | NulsException e) {
                return BigInteger.ZERO;
            }
        }).reduce(BigInteger::add).orElse(BigInteger.ZERO);
        Log.info("total::::{}", total.toString());
    }
}
