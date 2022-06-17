package io.icw.rpc;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.icw.Config;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;
import io.icw.core.model.StringUtils;
import io.icw.core.parse.MapUtils;
import io.icw.core.rpc.info.Constants;
import io.icw.core.rpc.model.ModuleE;
import io.icw.rpc.vo.Account;

@Component
public class AccountTools implements CallRpc {

    @Autowired
    Config config;

    public Account getAccountByAddress(String address) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("chainId", config.getChainId());
        param.put("address", address);
        return callRpc(ModuleE.AC.name, "ac_getAccountByAddress", param, (Function<Map<String, Object>, Account>) res -> {
                    if (res == null) {
                        return null;
                    }
                    return MapUtils.mapToBean(res, new Account());
                }
        );
    }


    /**
     * 账户验证
     * account validate
     *
     * @param chainId
     * @param address
     * @param password
     * @return validate result
     */
    public boolean accountValid(int chainId, String address, String password) throws NulsException {
        Map<String, Object> callParams = new HashMap<>(4);
        callParams.put(Constants.CHAIN_ID, chainId);
        callParams.put("address", address);
        callParams.put("password", password);
        return callRpc(ModuleE.AC.abbr, "ac_getPriKeyByAddress", callParams, (Function<Map<String, Object>, Boolean>) res -> StringUtils.isNotBlank((String)res.get("priKey")));
    }


    /**
     * 获取账户私钥
     * account validate
     *
     * @param chainId
     * @param address
     * @param password
     * @return validate result
     */
    public String getAddressPriKey(int chainId, String address, String password) throws NulsException {
        Map<String, Object> callParams = new HashMap<>(4);
        callParams.put(Constants.CHAIN_ID, chainId);
        callParams.put("address", address);
        callParams.put("password", password);
        return callRpc(ModuleE.AC.abbr, "ac_getPriKeyByAddress", callParams, (Function<Map<String, Object>, String>) res -> (String) res.get("priKey"));
    }


}
