package io.icw.base.api.provider.network;

import io.icw.base.api.provider.network.facade.NetworkInfo;
import io.icw.base.api.provider.network.facade.RemoteNodeInfo;
import io.icw.base.api.provider.Result;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-12 16:11
 * @Description: 功能描述
 */
public interface NetworkProvider {

    /**
     * get network info
     * @return
     */
    Result<NetworkInfo> getInfo();

    /**
     * get network nodes for ip
     * @return
     */
    Result<String> getNodes();


    /**
     * get network nodes for detail info
     * @return
     */
    Result<RemoteNodeInfo> getNodesInfo();

}
