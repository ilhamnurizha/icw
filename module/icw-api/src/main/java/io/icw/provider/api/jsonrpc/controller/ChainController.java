package io.icw.provider.api.jsonrpc.controller;


import io.icw.base.api.provider.Result;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Controller;
import io.icw.core.core.annotation.RpcMethod;
import io.icw.core.rpc.model.Key;
import io.icw.core.rpc.model.ResponseData;
import io.icw.core.rpc.model.TypeDescriptor;
import io.icw.provider.api.config.Config;
import io.icw.provider.api.config.Context;
import io.icw.provider.model.jsonrpc.RpcResult;
import io.icw.provider.rpctools.BlockTools;
import io.icw.provider.utils.ResultUtil;
import io.icw.v2.model.annotation.Api;
import io.icw.v2.model.annotation.ApiOperation;
import io.icw.v2.model.annotation.ApiType;

import java.util.List;
import java.util.Map;

@Controller
@Api(type = ApiType.JSONRPC)
public class ChainController {

    @Autowired
    private Config config;
    @Autowired
    BlockTools blockTools;

    @RpcMethod("info")
    @ApiOperation(description = "获取本链相关信息,其中共识资产为本链创建共识节点交易和创建委托共识交易时，需要用到的资产", order = 001)
    @ResponseData(name = "返回值", description = "返回本链信息", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "chainId", description = "本链的ID"),
            @Key(name = "assetId", description = "本链默认主资产的ID"),
            @Key(name = "inflationAmount", description = "本链默认主资产的初始数量"),
            @Key(name = "agentChainId", description = "本链共识资产的链ID"),
            @Key(name = "agentAssetId", description = "本链共识资产的ID"),
            @Key(name = "addressPrefix", description = "本链地址前缀"),
            @Key(name = "symbol", description = "本链主资产符号")
    }))
    public RpcResult getInfo(List<Object> params) {
        Result<Map> result = blockTools.getInfo(config.getChainId());
        if (result.isSuccess()) {
            Map map = result.getData();
            map.put("chainId", config.getChainId());
            map.put("assetId", config.getAssetsId());
            map.put("addressPrefix", config.getAddressPrefix());
            map.put("symbol", config.getSymbol());
            map.remove("awardAssetId");
            map.remove("seedNodes");
        }
        return ResultUtil.getJsonRpcResult(result);
    }


    /**
     * 获取资产信息
     *
     * @param params
     * @return
     */
    @RpcMethod("assetsInfo")
    public RpcResult getAssetsInfo(List<Object> params) {
        if (Context.isRunCrossChain) {
            return RpcResult.success(Context.assetList);
        } else {
            return RpcResult.success(Context.defaultChain.getAssets());
        }
    }

}
