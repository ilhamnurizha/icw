package io.icw.sdk.core;

import io.icw.sdk.core.contast.RpcConstant;
import io.icw.sdk.core.contast.SDKConstant;
import io.icw.sdk.core.utils.RestFulUtils;
import io.icw.sdk.core.utils.StringUtils;

/**
 * @author: Charlie
 */
public class SDKBootstrap {

    public static void init() {
        init(null, null);
    }

    public static void init(String ip, String port) {
        if (StringUtils.isBlank(ip) || StringUtils.isBlank(port)) {
            RestFulUtils.getInstance().setServerUri("http://" + RpcConstant.DEFAULT_IP + ":" + RpcConstant.DEFAULT_PORT + RpcConstant.PREFIX);
        } else {
            RestFulUtils.getInstance().setServerUri("http://" + ip + ":" + port + RpcConstant.PREFIX);
        }
    }

    public static void init(String ip, String port, int chainId) {
        init(ip, port);
        SDKConstant.DEFAULT_CHAIN_ID = (short) chainId;
    }

    public static void init(String ip, String port, int chainId, String nulscanUrl) {
        init(ip, port);
        SDKConstant.DEFAULT_CHAIN_ID = (short) chainId;
        SDKConstant.NULSCAN_URL = nulscanUrl;
    }
}
