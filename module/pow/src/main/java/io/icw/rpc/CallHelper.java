/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.icw.rpc;

import java.util.HashMap;
import java.util.Map;

import io.icw.core.constant.ErrorCode;
import io.icw.core.exception.NulsException;
import io.icw.core.log.Log;
import io.icw.core.parse.JSONUtils;
import io.icw.core.rpc.info.Constants;
import io.icw.core.rpc.model.ModuleE;
import io.icw.core.rpc.model.message.Response;
import io.icw.core.rpc.netty.processor.ResponseMessageProcessor;
import io.icw.core.rpc.util.RpcCall;

public class CallHelper {
    /**
     * 调用其他模块接口
     * Call other module interfaces
     */
    public static Object request(String moduleCode, String cmd, Map params) throws NulsException {
        try {
            params.put(Constants.VERSION_KEY_STR, "1.0");
            Response response = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params);
            Map resData = (Map) response.getResponseData();
            if (!response.isSuccess()) {
                Log.error("response error info is {}, cmd is {}, params is {}", response, cmd, JSONUtils.obj2json(params));
                String errorCode = response.getResponseErrorCode();
                Log.error("Call interface [{}] error, ErrorCode is {}, ResponseComment:{}", cmd, errorCode, response.getResponseComment());
                throw new NulsException(ErrorCode.init(errorCode));
            }
            return resData.get(cmd);
        } catch (Exception e) {
            Log.error(e);
            if(e instanceof NulsException) {
                throw (NulsException) e;
            }
            throw new NulsException(e);
        }
    }
    
    public static Map getConsensusConfig(int chainId) throws NulsException {
    	Map configMap = new HashMap();
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        try {
        	configMap = (Map) RpcCall.request(ModuleE.CS.abbr, "cs_getConsensusConfig", params);
        } catch (NulsException e) {
        	throw new NulsException(e);
        }
        return configMap;
    }
}
