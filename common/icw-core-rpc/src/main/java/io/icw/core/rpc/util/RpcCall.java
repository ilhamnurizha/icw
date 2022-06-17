package io.icw.core.rpc.util;

import io.icw.core.exception.NulsException;
import io.icw.core.log.Log;
import io.icw.core.parse.JSONUtils;
import io.icw.core.rpc.model.message.Response;
import io.icw.core.rpc.info.Constants;
import io.icw.core.rpc.netty.processor.ResponseMessageProcessor;

import java.util.Map;

/**
 * RPC调用工具类
 *
 * @author captain
 * @version 1.0
 * @date 2019/8/29 10:43
 */
public class RpcCall {
    public static Object request(String moduleCode, String cmd, Map params) throws NulsException {
        return request(moduleCode, cmd, params, null);
    }

    /**
     * 调用其他模块接口
     * Call other module interfaces
     */
    public static Object request(String moduleCode, String cmd, Map params, Long timeout) throws NulsException {
        try {
            params.put(Constants.VERSION_KEY_STR, "1.0");
            Response cmdResp;
            if (null == timeout) {
                cmdResp = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params);
            } else {
                cmdResp = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params, timeout);
            }
            Map resData = (Map) cmdResp.getResponseData();
            if (!cmdResp.isSuccess()) {
                Log.error("response error info is {}", cmdResp);
                String errorMsg;
                if (null == resData) {
                    errorMsg = String.format("Remote call fail. ResponseComment: %s ", cmdResp.getResponseComment());
                } else {
                    Map map = (Map) resData.get(cmd);
                    errorMsg = String.format("Remote call fail. msg: %s - code: %s - module: %s - interface: %s \n- params: %s ",
                            map.get("msg"), map.get("code"), moduleCode, cmd, JSONUtils.obj2PrettyJson(params));
                }
                throw new Exception(errorMsg);
            }
            return resData.get(cmd);
        } catch (Exception e) {
            Log.debug("cmd: {}", cmd);
            throw new NulsException(e);
        }
    }
}
