package io.icw.provider.task;

import io.icw.core.exception.NulsException;
import io.icw.core.rpc.model.ModuleE;
import io.icw.core.rpc.util.RpcCall;
import io.icw.provider.api.config.Context;
import io.icw.provider.api.constant.CommandConstant;
import io.icw.provider.api.model.AssetInfo;
import io.icw.provider.api.model.ChainInfo;
import io.icw.provider.utils.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryChainInfoTask implements Runnable {

    @Override
    public void run() {
        if (Context.isRunCrossChain) {
            queryRegisteredChainInfoList();
        }
    }

    public void queryRegisteredChainInfoList() {
        try {
            Map<String, Object> map = (Map) RpcCall.request(ModuleE.CC.abbr, CommandConstant.GET_REGISTERED_CHAIN, new HashMap());
            List<Map<String, Object>> resultList = (List<Map<String, Object>>) map.get("list");

            List<ChainInfo> chainInfoList = new ArrayList<>();
            List<AssetInfo> assetInfoList = new ArrayList<>();

            for (Map<String, Object> resultMap : resultList) {
                ChainInfo chainInfo = new ChainInfo();
                chainInfo.setChainId((Integer) resultMap.get("chainId"));
                chainInfo.setChainName((String) resultMap.get("chainName"));

                List<Map<String, Object>> assetList = (List<Map<String, Object>>) resultMap.get("assetInfoList");
                if (assetList != null) {
                    for (Map<String, Object> assetMap : assetList) {
                        AssetInfo assetInfo = new AssetInfo();
                        assetInfo.setChainId((Integer) resultMap.get("chainId"));
                        assetInfo.setAssetId((Integer) assetMap.get("assetId"));
                        assetInfo.setSymbol((String) assetMap.get("symbol"));
                        assetInfo.setDecimals((Integer) assetMap.get("decimalPlaces"));

                        chainInfo.getAssets().add(assetInfo);
                        assetInfoList.add(assetInfo);
                    }
                }
                chainInfoList.add(chainInfo);
            }

            Context.chainList = chainInfoList;
            Context.assetList = assetInfoList;
        } catch (NulsException e) {
            Log.error(e);
        }
    }
}
