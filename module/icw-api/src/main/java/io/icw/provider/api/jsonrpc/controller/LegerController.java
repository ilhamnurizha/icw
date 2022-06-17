package io.icw.provider.api.jsonrpc.controller;

import io.icw.base.api.provider.Result;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Controller;
import io.icw.core.core.annotation.RpcMethod;
import io.icw.provider.model.jsonrpc.RpcResult;
import io.icw.provider.model.jsonrpc.RpcResultError;
import io.icw.provider.rpctools.LegderTools;
import io.icw.v2.model.annotation.Api;
import io.icw.v2.model.annotation.ApiType;

import java.util.List;

@Controller
@Api(type = ApiType.JSONRPC)
public class LegerController {


    @Autowired
    private LegderTools legderTools;

    @RpcMethod("getAllAsset")
    public RpcResult getAllAsset(List<Object> params) {
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }

        Result<List> result = legderTools.getAllAsset(chainId);
        RpcResult rpcResult = new RpcResult();
        if (result.isFailed()) {
            return rpcResult.setError(new RpcResultError(result.getStatus(), result.getMessage(), null));
        }
        return rpcResult.setResult(result.getList());
    }
}
