package io.icw.transaction.rpc.call;

import io.icw.base.basic.AddressTool;
import io.icw.base.data.MultiSigAccount;
import io.icw.core.exception.NulsException;
import io.icw.core.log.Log;
import io.icw.core.rpc.info.Constants;
import io.icw.core.rpc.model.ModuleE;
import io.icw.transaction.constant.TxConstant;
import io.icw.transaction.constant.TxErrorCode;
import io.icw.transaction.utils.TxUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: qinyifeng
 * @date: 2018/12/20
 */
public class AccountCall {

    /**
     * 查询多签账户
     * Query multi-sign account
     *
     * @param multiSignAddress
     * @return
     */
    public static MultiSigAccount getMultiSigAccount(byte[] multiSignAddress) throws NulsException {
        try {
            String address = AddressTool.getStringAddressByBytes(multiSignAddress);
            int chainId = AddressTool.getChainIdByAddress(address);
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.AC.abbr, "ac_getMultiSignAccount", params);
            String mAccountStr = (String) result.get("value");
            return null == mAccountStr ? null : TxUtil.getInstanceRpcStr(mAccountStr, MultiSigAccount.class);
        } catch (RuntimeException e){
            Log.error(e);
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }

}
