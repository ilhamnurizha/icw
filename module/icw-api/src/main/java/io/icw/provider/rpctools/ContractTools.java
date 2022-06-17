package io.icw.provider.rpctools;

import io.icw.base.api.provider.Result;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsRuntimeException;
import io.icw.core.parse.MapUtils;
import io.icw.core.rpc.info.Constants;
import io.icw.core.rpc.model.ModuleE;
import io.icw.provider.api.constant.CommandConstant;
import io.icw.provider.model.dto.ContractTokenInfoDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author: PierreLuo
 * @date: 2019-06-30
 */
@Component
public class ContractTools implements CallRpc {

    public Result<ContractTokenInfoDto> getTokenBalance(int chainId, String contractAddress, String address) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("contractAddress", contractAddress);
        params.put("address", address);
        try {
            return  callRpc(ModuleE.SC.abbr, CommandConstant.TOKEN_BALANCE, params,(Function<Map<String,Object>, Result<ContractTokenInfoDto>>) res->{
                if(res == null){
                    return new Result();
                }
                return new Result(MapUtils.mapToBean(res, new ContractTokenInfoDto()));
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<Map> getContractInfo(int chainId, String contractAddress) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("contractAddress", contractAddress);
        try {
            return  callRpc(ModuleE.SC.abbr, CommandConstant.CONTRACT_INFO, params,(Function<Map<String,Object>, Result<Map>>) res->{
                if(res == null){
                    return new Result();
                }
                return new Result(res);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<Map> getContractResult(int chainId, String hash) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("hash", hash);
        try {
            return  callRpc(ModuleE.SC.abbr, CommandConstant.CONTRACT_RESULT, params,(Function<Map<String,Object>, Result<Map>>) res->{
                if(res == null){
                    return new Result();
                }
                return new Result(res);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<Map> getContractResultList(int chainId, List<String> hashList) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("hashList", hashList);
        try {
            return  callRpc(ModuleE.SC.abbr, CommandConstant.CONTRACT_RESULT_LIST, params,(Function<Map<String,Object>, Result<Map>>) res->{
                if(res == null){
                    return new Result();
                }
                return new Result(res);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<Map> getContractConstructor(int chainId, String contractCode) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("contractCode", contractCode);
        try {
            return  callRpc(ModuleE.SC.abbr, CommandConstant.CONSTRUCTOR, params,(Function<Map<String,Object>, Result<Map>>) res->{
                if(res == null){
                    return new Result();
                }
                return new Result(res);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<Map> validateContractCreate(int chainId, Object sender, Object gasLimit, Object price, Object contractCode, Object args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("gasLimit", gasLimit);
        params.put("price", price);
        params.put("contractCode", contractCode);
        params.put("args", args);
        Map map = new HashMap(4);
        try {
            return callRpc(ModuleE.SC.abbr, CommandConstant.VALIDATE_CREATE, params,(Function<Map<String,Object>, Result<Map>>) res->{
                map.put("success", true);
                return new Result(map);
            });
        } catch (NulsRuntimeException e) {
            map.put("success", false);
            map.put("code", e.getCode());
            map.put("msg", e.getMessage());
            return new Result(map);
        }
    }

    public Result<Map> contractCall(int chainId, Object sender, Object password, Object value, Object gasLimit, Object price,
                                            Object contractAddress, Object methodName, Object methodDesc, Object args, Object remark, Object multyAssetValues) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("password", password);
        params.put("value", value);
        params.put("gasLimit", gasLimit);
        params.put("price", price);
        params.put("contractAddress", contractAddress);
        params.put("methodName", methodName);
        params.put("methodDesc", methodDesc);
        params.put("args", args);
        params.put("remark", remark);
        params.put("multyAssetValues", multyAssetValues);
        Map map = new HashMap(4);
        try {
            return callRpc(ModuleE.SC.abbr, CommandConstant.CALL, params,(Function<Map<String,Object>, Result<Map>>) res->{
                return new Result(res);
            });
        } catch (NulsRuntimeException e) {
            map.put("success", false);
            map.put("code", e.getCode());
            map.put("msg", e.getMessage());
            return new Result(map);
        }
    }

    public Result<Map> validateContractCall(int chainId, Object sender, Object value, Object gasLimit, Object price,
                                            Object contractAddress, Object methodName, Object methodDesc, Object args, Object multyAssetValues) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("value", value);
        params.put("gasLimit", gasLimit);
        params.put("price", price);
        params.put("contractAddress", contractAddress);
        params.put("methodName", methodName);
        params.put("methodDesc", methodDesc);
        params.put("args", args);
        params.put("multyAssetValues", multyAssetValues);
        Map map = new HashMap(4);
        try {
            return callRpc(ModuleE.SC.abbr, CommandConstant.VALIDATE_CALL, params,(Function<Map<String,Object>, Result<Map>>) res->{
                map.put("success", true);
                return new Result(map);
            });
        } catch (NulsRuntimeException e) {
            map.put("success", false);
            map.put("code", e.getCode());
            map.put("msg", e.getMessage());
            return new Result(map);
        }
    }

    public Result<Map> validateContractDelete(int chainId, Object sender, Object contractAddress) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("contractAddress", contractAddress);
        Map map = new HashMap(4);
        try {
            return callRpc(ModuleE.SC.abbr, CommandConstant.VALIDATE_DELETE, params,(Function<Map<String,Object>, Result<Map>>) res->{
                map.put("success", true);
                return new Result(map);
            });
        } catch (NulsRuntimeException e) {
            map.put("success", false);
            map.put("code", e.getCode());
            map.put("msg", e.getMessage());
            return new Result(map);
        }
    }

    public Result<Map> imputedContractCreateGas(int chainId, Object sender, Object contractCode, Object args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("contractCode", contractCode);
        params.put("args", args);
        try {
            return callRpc(ModuleE.SC.abbr, CommandConstant.IMPUTED_CREATE_GAS, params,(Function<Map<String,Object>, Result<Map>>) res->{
                if(res == null){
                    return null;
                }
                return new Result(res);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<Map> imputedContractCallGas(int chainId, Object sender, Object value,
                                                 Object contractAddress, Object methodName, Object methodDesc, Object args, Object multyAssetValues) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("value", value);
        params.put("contractAddress", contractAddress);
        params.put("methodName", methodName);
        params.put("methodDesc", methodDesc);
        params.put("args", args);
        params.put("multyAssetValues", multyAssetValues);
        try {
            return callRpc(ModuleE.SC.abbr, CommandConstant.IMPUTED_CALL_GAS, params,(Function<Map<String,Object>, Result<Map>>) res->{
                if(res == null){
                    return null;
                }
                return new Result(res);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<Map> invokeView(int chainId, Object contractAddress, Object methodName, Object methodDesc, Object args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("contractAddress", contractAddress);
        params.put("methodName", methodName);
        params.put("methodDesc", methodDesc);
        params.put("args", args);
        try {
            return callRpc(ModuleE.SC.abbr, CommandConstant.INVOKE_VIEW, params,(Function<Map<String,Object>, Result<Map>>) res->{
                if(res == null){
                    return null;
                }
                return new Result(res);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

}
