package io.icw.crosschain.nuls.rpc.call;

import io.icw.core.constant.ErrorCode;
import io.icw.core.exception.NulsException;
import io.icw.core.rpc.info.Constants;
import io.icw.core.rpc.model.ModuleE;
import io.icw.core.rpc.model.message.Response;
import io.icw.core.rpc.netty.processor.ResponseMessageProcessor;
import io.icw.crosschain.nuls.model.bo.Chain;
import io.icw.crosschain.nuls.constant.NulsCrossChainErrorCode;

import java.util.HashMap;
import java.util.Map;
/**
 * 与交易模块交互类
 * Interaction class with transaction module
 * @author tag
 * 2019/4/10
 */
public class TransactionCall {
    /**
     * 将新创建的交易发送给交易管理模块
     * The newly created transaction is sent to the transaction management module
     *
     * @param chain chain info
     * @param tx transaction hex
     */
    @SuppressWarnings("unchecked")
    public static boolean sendTx(Chain chain, String tx) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
        params.put("tx", tx);
        try {
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
            if (!cmdResp.isSuccess()) {
                String errorCode = cmdResp.getResponseErrorCode();
                chain.getLogger().error("Call interface [{}] error, ErrorCode is {}, ResponseComment:{}",
                        "tx_newTx", errorCode, cmdResp.getResponseComment());
                throw new NulsException(ErrorCode.init(errorCode));
            }
            return true;
        }catch (NulsException e){
            throw e;
        }catch (Exception e) {
            throw new NulsException(NulsCrossChainErrorCode.INTERFACE_CALL_FAILED);
        }
    }
}
