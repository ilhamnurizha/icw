package io.icw.rpc;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.icw.core.constant.CommonCodeConstanst;
import io.icw.core.constant.ErrorCode;
import io.icw.core.exception.NulsRuntimeException;
import io.icw.core.log.Log;
import io.icw.core.model.StringUtils;
import io.icw.core.rpc.model.message.Response;
import io.icw.core.rpc.netty.processor.ResponseMessageProcessor;

public interface CallRpc {

    default  <T,R> T callRpc(String module, String method,Map<String, Object> params, Function<R,T> callback) {
    	if (!"getBlockByHeight".equals(method)) {
    		Log.debug("call {} rpc , method : {},param : {}",module,method,params);
    	}
        Response cmdResp = null;
        try {
            cmdResp = ResponseMessageProcessor.requestAndResponse(module, method, params);
            if (!"getBlockByHeight".equals(method)) {
            	Log.debug("result : {}",cmdResp);
            }
        } catch (Exception e) {
            Log.warn("Calling remote interface failed. module:{} - interface:{} - message:{}", module, method, e.getMessage());
            throw new NulsRuntimeException(CommonCodeConstanst.FAILED);
        }
        if (!cmdResp.isSuccess()) {
            Log.warn("Calling remote interface failed. module:{} - interface:{} - ResponseComment:{}", module, method, cmdResp.getResponseComment());
            if(cmdResp.getResponseStatus() == Response.FAIL){
                //business error
                String errorCode = cmdResp.getResponseErrorCode();
                if(StringUtils.isBlank(errorCode)){
                    throw new NulsRuntimeException(CommonCodeConstanst.SYS_UNKOWN_EXCEPTION);
                }
                throw new NulsRuntimeException(ErrorCode.init(errorCode));
            }else{
                if(StringUtils.isNotBlank(cmdResp.getResponseComment())) {
                    throw new NulsRuntimeException(CommonCodeConstanst.FAILED, cmdResp.getResponseComment());
                }
                throw new NulsRuntimeException(CommonCodeConstanst.SYS_UNKOWN_EXCEPTION, "unknown error");
            }
        }
        return callback.apply((R) ((HashMap) cmdResp.getResponseData()).get(method));
    }

}
