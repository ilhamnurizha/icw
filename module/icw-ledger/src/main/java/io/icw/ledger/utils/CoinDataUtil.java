package io.icw.ledger.utils;

import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.data.CoinData;
import io.icw.base.data.CoinFrom;
import io.icw.core.exception.NulsException;
import io.icw.core.log.Log;
import io.icw.ledger.constant.LedgerConstant;
import io.icw.ledger.model.po.TxUnconfirmed;

import java.util.Map;

/**
 * Created by ljs on 2018/12/30.
 *
 * @author lanjinsheng
 */
public class CoinDataUtil {
    /**
     * parseCoinData
     *
     * @param stream
     * @return
     */
    public static CoinData parseCoinData(byte[] stream) {
        if (null == stream) {
            return null;
        }
        CoinData coinData = new CoinData();
        try {
            coinData.parse(new NulsByteBuffer(stream));
        } catch (NulsException e) {
            Log.error("coinData parse error", e);
        }
        return coinData;
    }

    public static void calTxFromAmount(Map<String, TxUnconfirmed> map, CoinFrom coinFrom, byte[] txNonce, String accountKey, String address) {
        TxUnconfirmed txUnconfirmed;
        if (null == map.get(accountKey)) {
            txUnconfirmed = new TxUnconfirmed(address, coinFrom.getAssetsChainId(),
                    coinFrom.getAssetsId(),coinFrom.getNonce(),txNonce,coinFrom.getAmount());
            map.put(accountKey,txUnconfirmed);
        } else {
            txUnconfirmed = map.get(accountKey);
            System.arraycopy(txNonce, 0, txUnconfirmed.getNonce(), 0, LedgerConstant.NONCE_LENGHT);
            System.arraycopy(coinFrom.getNonce(), 0, txUnconfirmed.getFromNonce(), 0, LedgerConstant.NONCE_LENGHT);
            txUnconfirmed.setAmount(txUnconfirmed.getAmount().add(coinFrom.getAmount()));
        }
    }

}
