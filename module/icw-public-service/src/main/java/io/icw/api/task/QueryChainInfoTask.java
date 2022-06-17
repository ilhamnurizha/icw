package io.icw.api.task;


import io.icw.api.analysis.WalletRpcHandler;
import io.icw.api.cache.ApiCache;
import io.icw.api.manager.CacheManager;
import io.icw.api.model.po.AssetInfo;
import io.icw.api.model.po.ChainInfo;
import io.icw.api.utils.LoggerUtil;
import io.icw.api.ApiContext;
import io.icw.core.basic.Result;

import java.util.HashMap;
import java.util.Map;

public class QueryChainInfoTask implements Runnable {

    private int chainId;


    public QueryChainInfoTask(int chainId) {
        this.chainId = chainId;
    }

    @Override
    public void run() {
        Map<Integer, ChainInfo> chainInfoMap;
        Map<String, AssetInfo> assetInfoMap;
        try {
            if (ApiContext.isRunCrossChain) {
                Result<Map<String, Object>> result = WalletRpcHandler.getRegisteredChainInfoList();
                Map<String, Object> map = result.getData();
                if (map == null || map.get("chainInfoMap") == null) {
                    return;
                }
                chainInfoMap = (Map<Integer, ChainInfo>) map.get("chainInfoMap");
                CacheManager.setChainInfoMap(chainInfoMap);

                assetInfoMap = (Map<String, AssetInfo>) map.get("assetInfoMap");
                for (AssetInfo assetInfo : assetInfoMap.values()) {
                    if (!CacheManager.getAssetInfoMap().containsKey(assetInfo.getKey())) {
                        CacheManager.getAssetInfoMap().put(assetInfo.getKey(), assetInfo);
                    }
                }
                ApiContext.isReady = true;
            } else {
                chainInfoMap = new HashMap<>();
                assetInfoMap = new HashMap<>();
                ApiCache apiCache = CacheManager.getCache(chainId);
                ChainInfo chainInfo = apiCache.getChainInfo();
                chainInfoMap.put(chainInfo.getChainId(), chainInfo);
                assetInfoMap.put(chainInfo.getDefaultAsset().getKey(), chainInfo.getDefaultAsset());

                CacheManager.setChainInfoMap(chainInfoMap);
                CacheManager.setAssetInfoMap(assetInfoMap);

                ApiContext.isReady = true;
            }
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
        }
    }
}
