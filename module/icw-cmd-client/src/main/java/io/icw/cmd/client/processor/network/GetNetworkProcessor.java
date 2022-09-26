/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.icw.cmd.client.processor.network;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.icw.base.api.provider.BaseReq;
import io.icw.base.api.provider.BaseRpcService;
import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.network.NetworkProvider;
import io.icw.base.api.provider.network.facade.RemoteNodeInfo;
import io.icw.cmd.client.CommandBuilder;
import io.icw.cmd.client.CommandResult;
import io.icw.cmd.client.processor.CommandGroup;
import io.icw.cmd.client.processor.CommandProcessor;
import io.icw.core.constant.ErrorCode;
import io.icw.core.core.annotation.Component;
import io.icw.core.log.Log;
import io.icw.core.model.StringUtils;
import io.icw.core.parse.MapUtils;
import io.icw.core.rpc.model.ModuleE;
import io.icw.core.rpc.model.message.Response;
import io.icw.core.rpc.netty.processor.ResponseMessageProcessor;

/**
 * @author: zhoulijun
 */
@Component
public class GetNetworkProcessor implements CommandProcessor {

    NetworkProvider networkProvider = ServiceManager.get(NetworkProvider.class);

    @Override
    public String getCommand() {
        return "network";
    }

    @Override
    public CommandGroup getGroup() {
        return CommandGroup.System;
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription());
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "network info --get network info \nnetwork nodes --get network nodes  \\nnetwork remove --remove network nodes";
    }

    @Override
    public boolean argsValidate(String[] args) {
//        checkArgsNumber(args,1);
        checkArgs(("info".equals(args[1]) || "nodes".equals(args[1]) || "remove".equals(args[1])),getCommandDescription());
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String cmd = args[1];
        Result<?> result;
        if("info".equals(cmd)){
            result = networkProvider.getInfo();
            if (result.isFailed()) {
                return CommandResult.getFailed(result);
            }
            return CommandResult.getSuccess(result);
        } else if("nodes".equals(cmd)){
        	result = networkProvider.getNodesInfo();
            if (result.isFailed()) {
                return CommandResult.getFailed(result);
            }
            return CommandResult.getResult(CommandResult.dataTransformList(result));
        } else{
        	Map params = new HashMap();
        	params.put("chainId", 1);
        	params.put("nodes", args[2]);
            Function<List,Result> callback = res->{
            	 List<String> list = new ArrayList<>();
            	 list.add(String.valueOf(res));
                 return success(list);
            };
            
            result = callRpc(ModuleE.NW.abbr, "nw_delNodes", params, callback);
            result.setMessage(result.getMessage() + " : " + params.toString());
            if (result.isFailed()) {
                return CommandResult.getFailed(result);
            }
            return CommandResult.getResult(CommandResult.dataTransformList(result));
        }
    }
    
 public static final ErrorCode RPC_ERROR_CODE = ErrorCode.init("10016");
    
    protected <T,R> Result<T> callRpc(String module,String method,Map params,Function<R,Result> callback) {
//        Map<String, Object> params = MapUtils.beanToLinkedMap(req);
        Log.info("call {} rpc , method : {},param : {}",module,method,params);
        Response cmdResp = null;
        try {
            cmdResp = ResponseMessageProcessor.requestAndResponse(module, method, params);
            Log.debug("result : {}",cmdResp);
        } catch (Exception e) {
            Log.warn("Calling remote interface failed. module:{} - interface:{} - message:{}", module, method, e.getMessage());
            return fail(RPC_ERROR_CODE,e.getMessage());
        }
        if (!cmdResp.isSuccess()) {
            String comment = cmdResp.getResponseComment();
            if(StringUtils.isBlank(comment)) {
                comment = "";
            }
            Log.warn("Calling remote interface failed. module:{} - interface:{} - ResponseComment:{}", module, method, cmdResp.getResponseComment());
            if(cmdResp.getResponseStatus() == Response.FAIL){
                //business error
                String errorCode = cmdResp.getResponseErrorCode();
                if(StringUtils.isBlank(errorCode)){
                    return fail(RPC_ERROR_CODE, StringUtils.isBlank(comment) ? "unknown error" : comment);
                }
                return fail(ErrorCode.init(errorCode), comment);
            }else{
                if(StringUtils.isNotBlank(comment)) {
                    return fail(RPC_ERROR_CODE, comment);
                }
                return fail(RPC_ERROR_CODE, "unknown error");
            }
        }
        return callback.apply((R) ((HashMap) cmdResp.getResponseData()).get(method));
    }
    
    protected <T> Result<T> success(T data) {
        return new Result<>(data);
    }

    protected <T> Result<T> success(List<T> list) {
        return new Result<>(list);
    }

    public static Result fail(ErrorCode errorCode, String message) {
        return new Result(errorCode.getCode(), StringUtils.isNotBlank(message) ? message : StringUtils.isBlank(errorCode.getMsg()) ?
                "fail,error code:" + errorCode.getCode() : errorCode.getMsg());
    }

    public static Result fail(String errorCode) {
        return fail(ErrorCode.init(errorCode));
    }

    public static Result fail(ErrorCode errorCode) {
        return fail(errorCode, errorCode.getMsg());
    }
    
    public static void main(String[] args) {
    	Map params = new HashMap();
    	params.put("chainId", 1);
    	params.put("nodes", 123);
    	Map<String, Object> params1 = MapUtils.beanToLinkedMap(params);
    	System.out.println(params1);
    }
}
